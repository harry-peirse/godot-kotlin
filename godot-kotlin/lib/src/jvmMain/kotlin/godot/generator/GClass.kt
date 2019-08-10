package godot.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

private val cCPointer = ClassName("kotlinx.cinterop", "CPointer")
private val mCPointer = MemberName("kotlinx.cinterop", "CPointer")
private val mCOpaquePointerVar = MemberName("kotlinx.cinterop", "COpaquePointerVar")
private val mStableRef = MemberName("kotlinx.cinterop", "StableRef")
private val cGodotMethodBind = ClassName("godot", "godot_method_bind")

data class GClass(
        val name: String,
        val baseClass: String,
        val singleton: Boolean,
        val instanciable: Boolean,
        val isReference: Boolean,
        val constants: Map<String, Int>,
//        val signals: List<GSignal>,
        val methods: MutableList<GMethod>,
        val enums: List<GEnum>
) {
    fun parseRegisterCall() = CodeBlock.builder()
            .addStatement("godot.tagDB.registerGlobalType(\"$name\", $name::class.hashCode().toUInt(), ${if (baseClass.isBlank()) "0u" else "$baseClass::class.hashCode().toUInt()"})")
            .build()

    fun parseBindingCall() = CodeBlock.builder()
            .addStatement("$name.initMethodBindings()")
            .build()

    fun parse(content: List<GClass>): FileSpec {
        return FileSpec.builder(PACKAGE, name)
                .addImport("kotlinx.cinterop", "invoke", "cstr", "memScoped", "alloc", "cValue", "allocArray", "pointed", "set", "value", "ptr", "reinterpret", "CFunction", "COpaquePointer")
                .addType((TypeSpec.classBuilder(ClassName(PACKAGE, name)).addType(buildCore(TypeSpec.companionObjectBuilder()).build()))
                        .addTypes(enums.map { enum -> enum.parse() })
                        .addFunctions(methods
                                .map { it.parse(this, content) })
                        .apply {
                            if (baseClass.isEmpty()) {
                                superclass(ClassName(PACKAGE, "Wrapped"))
                            } else {
                                superclass(ClassName(PACKAGE, baseClass))
                            }
                            addModifiers(KModifier.OPEN)
                        }
                        .build()
                ).build()
    }

    fun buildCore(builder: TypeSpec.Builder) = builder
            .addAnnotation(AnnotationSpec.builder(ClassName("kotlin.native", "ThreadLocal")).build())
            .addProperties(constants.map { (key, value) ->
                PropertySpec.builder(key, Int::class)
                        .addModifiers(KModifier.PUBLIC, KModifier.CONST)
                        .initializer(value.toString())
                        .build()
            })
            .addType(TypeSpec.classBuilder("MethodBindings")
                    .addProperties(methods.map { it.parseBinding() })
                    .build())
            .addProperty(PropertySpec.builder("mb", /*if (isObject) ClassName(PACKAGE, name, "MethodBindings") else*/ TypeVariableName("MethodBindings"))
                    .initializer("MethodBindings()")
                    .build())
            .addFunction(FunSpec.builder("initMethodBindings")
                    .addAnnotation(AnnotationSpec.builder(ClassName("kotlin", "UseExperimental"))
                            .addMember("ExperimentalUnsignedTypes::class")
                            .build())
                    .addCode(CodeBlock.builder()
                            .beginControlFlow("memScoped")
                            .apply {
                                methods.forEach {
                                    addStatement("mb.${underscoreToCamelCase(it.name)} = godot.api.godot_method_bind_get_method!!(\"$name\".cstr.ptr, \"${it.name}\".cstr.ptr)")
                                }
                            }
                            .endControlFlow()
                            .build())
                    .build())
            .addFunction(FunSpec.builder("getFromVariant")
                    .returns(ClassName(PACKAGE, name))
                    .addParameter("variant", cCPointer.parameterizedBy(ClassName(PACKAGE, "Variant")))
                    .addCode(CodeBlock.builder()
                            .addStatement("val instance = $name()")
                            .addStatement("instance._wrapped = godot.nativescript11Api.godot_nativescript_get_instance_binding_data!!(godot.languageIndex, variant.reinterpret())!!.reinterpret()")
                            .addStatement("return instance")
                            .build())
                    .build())
            .apply {
                if (singleton) {
                    addProperty(PropertySpec.builder("singleton", ClassName(PACKAGE, name))
                            .initializer("$name()")
                            .addModifiers(KModifier.PRIVATE)
                            .build())
                    addFunction(FunSpec.builder("singleton")
                            .returns(ClassName(PACKAGE, name))
                            .addAnnotation(AnnotationSpec.builder(ClassName("kotlin", "UseExperimental"))
                                    .addMember("ExperimentalUnsignedTypes::class")
                                    .build())
                            .addCode(CodeBlock.builder()
                                    .beginControlFlow("if(singleton._wrapped == null)")
                                    .beginControlFlow("memScoped")
                                    .addStatement("singleton._wrapped = godot.api.godot_alloc!!(_Wrapped.size.toInt())?.reinterpret()")
                                    .addStatement("singleton._wrapped?.pointed?._owner = godot.api.godot_global_get_singleton!!(\"$name\".cstr.ptr)")
                                    .addStatement("singleton._wrapped?.pointed?._typeTag = $name::class.simpleName.hashCode().toUInt()")
                                    .endControlFlow()
                                    .endControlFlow()
                                    .addStatement("return singleton")
                                    .build())
                            .build())
                }
                if (instanciable) addFunction(FunSpec.builder("new")
                        .returns(ClassName(PACKAGE, name))
                        .addCode(CodeBlock.builder()
                                .beginControlFlow("memScoped")
                                .addStatement("val instance = $name()")
                                .addStatement("instance._wrapped = " +
                                        "godot.nativescript11Api.godot_nativescript_get_instance_binding_data!!(godot.languageIndex, " +
                                        "godot.api.godot_get_class_constructor!!(\"$name\".cstr.ptr)?.reinterpret<CFunction<() -> COpaquePointer?>>()!!())?.reinterpret()")
                                .addStatement("return instance")
                                .endControlFlow()
                                .build())
                        .build())
            }
}

/*
data class GSignal(
        val name: String,
        val arguments: List<GSignalArgument>
)

data class GSignalArgument(
        val name: String,
        val type: String,
        val defaultValue: String
)
*/
data class GMethod(
        val name: String,
        val returnType: String,
        val isEditor: Boolean,
        val isNoscript: Boolean,
        val isConst: Boolean,
        val isReverse: Boolean,
        val isVirtual: Boolean,
        val hasVarargs: Boolean,
        val isFromScript: Boolean,
        val arguments: List<GMethodArgument>
) {

    fun functionBody() = CodeBlock.builder()
            .beginControlFlow("memScoped")
            .apply {
                if (name == "free") {
                    addStatement("godot.api.godot_object_destroy!!(_wrapped?.pointed?._owner)")
                } else {
                    add(returnTypeDeclaration(returnType))
                    add(argumentDeclarations(arguments, hasVarargs))
                    addStatement("godot.api.godot_method_bind_ptrcall!!(mb.${underscoreToCamelCase(name)}, _wrapped?.pointed?._owner, args, ${returnOutParameter(returnType)})")
                    add(returnStatement(returnType))
                }
            }
            .endControlFlow()
            .build()

    fun safeName(name: String) = if (name == "to_string") "to_g_string" else name
    fun parse(clazz: GClass, content: List<GClass>): FunSpec = FunSpec.builder(underscoreToCamelCase(safeName(name)))
            .addModifiers(KModifier.OPEN)
            .apply {
                if (name == "_init") {
                    addModifiers(KModifier.OVERRIDE)
                }

                var c = clazz
                var override = false
                var parent: GMethod? = null
                loop@ while (c.baseClass.isNotBlank()) {
                    val bc = content.find { it.name == c.baseClass }!!
                    val m = bc.methods.find { it.name == name && it.arguments.map { it.type } == arguments.map { it.type } }
                    if (m != null) {
                        modifiers.remove(KModifier.OPEN)
                        addModifiers(KModifier.OVERRIDE)
                        override = true
                        parent = m
                        break@loop
                    }
                    c = bc
                }
                addParameters(arguments.mapIndexed { index, it -> if (override && it.name.startsWith("arg")) it.parse(parent!!.arguments[index].name) else it.parse() })
                if (hasVarargs) {
                    addParameter(ParameterSpec.builder("variants", ClassName(PACKAGE, "Variant"))
                            .addModifiers(KModifier.VARARG)
                            .build())
                }
            }
            .addAnnotation(AnnotationSpec.builder(ClassName("kotlin", "UseExperimental"))
                    .addMember("ExperimentalUnsignedTypes::class")
                    .build())
            .addCode(functionBody())
            .returns(typeOf(returnType.removePrefix("enum.")))
            .build()

    fun parseBinding(): PropertySpec = PropertySpec.builder(underscoreToCamelCase(name), cCPointer.parameterizedBy(cGodotMethodBind).copy(nullable = true))
            .mutable()
            .initializer("null")
            .build()
}

data class GMethodArgument(
        val name: String,
        val type: String,
        val hasDefaultValue: Boolean,
        val defaultValue: String
) {
    fun safeName() = sanitised(override)
    private var override: String = name

    fun parse(overrideName: String = name): ParameterSpec {
        override = overrideName
        return ParameterSpec.builder(safeName(), typeOf(type))
                .build()
    }
}

data class GEnum(
        val name: String,
        val values: Map<String, Int>
) {
    fun parse(): TypeSpec = TypeSpec.enumBuilder(ClassName(PACKAGE, name))
            .primaryConstructor(FunSpec.constructorBuilder()
                    .addParameter("value", Int::class)
                    .build())
            .addProperty(PropertySpec.builder("value", Int::class, KModifier.PUBLIC)
                    .initializer("value")
                    .build())
            .apply {
                values.entries.sortedBy { it.value }.forEach {
                    addEnumConstant(it.key, TypeSpec.anonymousClassBuilder()
                            .addSuperclassConstructorParameter("%L", it.value)
                            .build())
                }
            }
            .build()
}

fun typeOf(type: String) = when (type) {
    "void" -> Unit::class.asClassName()
    "float", "real" -> Float::class.asClassName()
    "int" -> Int::class.asClassName()
    "bool" -> Boolean::class.asClassName()
    "String" -> ClassName(PACKAGE, "GString")
    "Vector3::Axis" -> ClassName(PACKAGE, "Vector3Axis")
    "Variant::Operator" -> ClassName(PACKAGE, "VariantOperator")
    "Variant::Type" -> ClassName(PACKAGE, "VariantType")
    else ->
        if (type.contains("::")) ClassName(PACKAGE, type.substringBefore("::"), type.substringAfter("::"))
        else ClassName(PACKAGE, type)
}

fun sanitised(value: String) = underscoreToCamelCase(when (value) {
    "class" -> "_class"
    "object" -> "_object"
    "api" -> "_api"
    "interface" -> "_interface"
    "in" -> "_in"
    "var" -> "_var"
    "args" -> "arguments"
    else -> value
})

fun isPointer(value: String) = when (value) {
    "int", "bool", "float", "real" -> false
    else -> true
}

fun isPrimitive(value: String) = when (value) {
    "int", "bool", "float", "real" -> true
    else -> false
}

fun toVar(value: String) =
        if (isEnum(value)) MemberName("kotlinx.cinterop", "UIntVar") else when (value) {
            "int" -> MemberName("kotlinx.cinterop", "IntVar")
            "bool" -> MemberName("kotlinx.cinterop", "BooleanVar")
            "float", "real" -> MemberName("kotlinx.cinterop", "FloatVar")
            "String" -> MemberName(PACKAGE, "GString")
            else ->
                if (value.contains("::")) MemberName(PACKAGE, value.replace("::", "."))
                else MemberName(PACKAGE, value)
        }


fun isCoreType(value: String) = when (value) {
    "Array",
    "Basis",
    "Color",
    "Dictionary",
    "Error",
    "NodePath",
    "Plane",
    "PoolByteArray",
    "PoolIntArray",
    "PoolRealArray",
    "PoolStringArray",
    "PoolVector2Array",
    "PoolVector3Array",
    "PoolColorArray",
    "Quat",
    "Rect2",
    "AABB",
    "RID",
    "String",
    "Transform",
    "Transform2D",
    "Variant",
    "Vector2",
    "Vector3",
    "Vector3::Axis",
    "Variant::Operator",
    "Variant::Type",
    "enum.Vector3::Axis",
    "enum.Variant::Operator",
    "enum.Variant::Type" -> true
    else -> false
}

fun returnTypeDeclaration(type: String) = CodeBlock.builder().apply {
    if (type != "void") when {
        isPrimitive(type) || isEnum(type) || isCoreType(type) -> addStatement("val ret = alloc<%M>()", toVar(type))
        else -> addStatement("val ret = alloc<_Wrapped>()")
    }
}.build()

fun argumentDeclarations(arguments: List<GMethodArgument>, hasVarargs: Boolean) = CodeBlock.builder().apply {
    val argumentsSize = if (hasVarargs) "${arguments.size} + variants.size" else "${arguments.size}"
    addStatement("val args: %M<%M> = allocArray(${argumentsSize})", mCPointer, mCOpaquePointerVar)
    arguments.forEachIndexed { index, it ->
        if (isCoreType(it.type)) {
            addStatement("args[$index] = ${it.safeName()}.ptr")
        } else if (isPrimitive(it.type) || isEnum(it.type)) {
            addStatement("args[$index] = alloc<%M> { this.value = ${it.safeName()} }.ptr", toVar(it.type))
        } else {
            addStatement("args[$index] = ${it.safeName()}._wrapped")
        }
    }
    if (hasVarargs) {
        beginControlFlow("variants.forEachIndexed")
        addStatement("index, it -> args[index] = it.ptr")
        endControlFlow()
    }
}.build()

fun returnOutParameter(type: String) = when (type) {
    "void" -> "null"
    else -> "ret.ptr"
}

fun isEnum(type: String) = type.startsWith("enum.")

fun cleanEnum(name: String) = name.substringAfter("enum.")

fun returnStatement(type: String) = CodeBlock.builder().apply {
    if (type != "void") when {
        isEnum(type) && isCoreType(type) -> addStatement("return ${cleanEnum(type).replace(".", "").replace("::", "")}.byValue(ret.value)")
        isEnum(type) -> addStatement("return ${cleanEnum(type.replace("::", "."))}.values()[ret.value.toInt()]")
        isCoreType(type) -> addStatement("return ret")
        isPointer(type) -> {
            addStatement("val result = $type()")
            addStatement("result._wrapped = godot.nativescript11Api.godot_nativescript_get_instance_binding_data!!(godot.languageIndex, ret._owner)?.reinterpret()")
            addStatement("return result")
        }
        isPrimitive(type) -> addStatement("return ret.value")
        else -> addStatement("return ret.pointed.value")
    }
}.build()

fun underscoreToCamelCase(name: String) = (if (name.startsWith("_")) "_" else "") + name.split("_").joinToString("") { it.capitalize() }.decapitalize()