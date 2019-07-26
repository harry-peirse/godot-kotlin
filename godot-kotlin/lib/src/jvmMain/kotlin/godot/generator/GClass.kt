package godot.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

private val cCPointer = ClassName("kotlinx.cinterop", "CPointer")
private val mCPointer = MemberName("kotlinx.cinterop", "CPointer")
private val mCOpaquePointerVar = MemberName("kotlinx.cinterop", "COpaquePointerVar")
private val mStableRef = MemberName("kotlinx.cinterop", "StableRef")
private val cGodotMethodBind = ClassName("godotapi", "godot_method_bind")

data class GClass(
        val name: String,
        val baseClass: String,
        val singleton: Boolean,
        val instanciable: Boolean,
        val isReference: Boolean,
        val constants: Map<String, Int>,
        val properties: List<GProperty>,
        val signals: List<GSignal>,
        val methods: MutableList<GMethod>,
        val enums: List<GEnum>
) {
    var isObject = singleton
    fun parse(content: List<GClass>): FileSpec {
        isObject = singleton && !hasSubClass(content)
        return FileSpec.builder(PACKAGE, name)
                .addImport("kotlinx.cinterop", "invoke", "cstr", "memScoped", "alloc", "allocArray", "pointed", "set", "value", "ptr")
                .addType((if (isObject) buildCore(TypeSpec.objectBuilder(ClassName(PACKAGE, name))) else TypeSpec.classBuilder(ClassName(PACKAGE, name)).addType(buildCore(TypeSpec.companionObjectBuilder()).build()))
                        .addProperties(properties.filter { !it.name.contains("/") }.mapNotNull { property -> property.parse(this) })
                        .addTypes(enums.map { enum -> enum.parse() })
                        .addFunctions(methods.map { method -> method.parse(this, content) })
                        .apply {
                            if (baseClass.isEmpty()) {
                                superclass(ClassName(PACKAGE, "_Wrapped"))
                            } else {
                                superclass(ClassName(PACKAGE, baseClass))
                            }
                            if (!isObject) addModifiers(KModifier.OPEN)
                        }
                        .build()
                ).build()
    }

    fun hasSubClass(content: List<GClass>) = content.find { it.baseClass == name } != null

    fun buildCore(builder: TypeSpec.Builder) = builder
            .addProperties(constants.map { (key, value) ->
                PropertySpec.builder(key, Int::class)
                        .addModifiers(KModifier.PUBLIC, KModifier.CONST)
                        .initializer(value.toString())
                        .build()
            })
            .addType(TypeSpec.classBuilder("MethodBindings")
                    .addProperties(methods.map { it.parseBinding() })
                    .build())
            .addProperty(PropertySpec.builder("mb", if (isObject) ClassName(PACKAGE, name, "MethodBindings") else TypeVariableName("MethodBindings"))
                    .initializer("MethodBindings()")
                    .build())
            .addFunction(FunSpec.builder("initMethodBindings")
                    .addCode(CodeBlock.builder()
                            .beginControlFlow("memScoped")
                            .apply {
                                methods.forEach {
                                    addStatement("mb.${underscoreToCamelCase(it.name)} = api.c.godot_method_bind_get_method!!(\"$name\".cstr.ptr, \"${underscoreToCamelCase(it.name)}\".cstr.ptr)")
                                }
                            }
                            .endControlFlow()
                            .build())
                    .build())
}

data class GProperty(
        val name: String,
        val type: String,
        val getter: String,
        val setter: String
) {
    fun parse(clazz: GClass): PropertySpec? {
        val getter = clazz.methods.find { it.name == getter }
        return if (getter?.arguments.isNullOrEmpty()) {
            val spec = PropertySpec.builder(underscoreToCamelCase(name), typeOf(getter?.returnType?.substringAfter("enum.")
                    ?: type)).apply {
                if (getter != null) {
                    getter(getter.also { clazz.methods.remove(it) }.parseGetter())

                    val setter = clazz.methods.find { it.name == setter }
                    if (setter != null) {
                        setter(setter.also { clazz.methods.remove(it) }.parseSetter())
                        mutable()
                    }
                }
            }.build()

            if (spec.getter != null) spec else null
        } else null
    }
}

data class GSignal(
        val name: String,
        val arguments: List<GSignalArgument>
)

data class GSignalArgument(
        val name: String,
        val type: String,
        val defaultValue: String
)

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
                add(returnTypeDeclaration(returnType))
                add(argumentDeclarations(arguments))
                addStatement("api.c.godot_method_bind_ptrcall!!(mb.${underscoreToCamelCase(name)}, _owner, args, ${returnOutParameter(returnType)})")
                add(argumentCleanup(arguments))
                add(returnStatement(returnType))
            }
            .endControlFlow()
            .build()

    fun parseGetter(): FunSpec = FunSpec.builder("get()")
            .addCode(functionBody())
            .build()

    fun parseSetter(): FunSpec = FunSpec.builder("set()")
            .addParameter(arguments.map { it.parse() }.first())
            .addCode(functionBody())
            .build()

    fun parse(clazz: GClass, content: List<GClass>): FunSpec = FunSpec.builder(underscoreToCamelCase(name))
            .addParameters(arguments.map { it.parse() })
            .addAnnotation(AnnotationSpec.builder(ClassName("kotlin", "ExperimentalUnsignedTypes")).build())
            .apply {
                if (isVirtual) {
                    addModifiers(KModifier.OPEN)
                }
                var c = clazz
                loop@ while (c.baseClass.isNotBlank()) {
                    val bc = content.find { it.name == c.baseClass }!!
                    val m = bc.methods.find { it.name == name }
                    if (m != null) {
                        addModifiers(KModifier.OVERRIDE)
                        break@loop
                    }
                    c = bc
                }
            }
            .addCode(functionBody())
            .returns(typeOf(returnType.removePrefix("enum.")))
            .build()

    fun parseBinding(): PropertySpec = PropertySpec.builder(underscoreToCamelCase(name), cCPointer.parameterizedBy(cGodotMethodBind).copy(nullable = true))
            .mutable()
            .initializer("null")
            .build()
}

data class GMethodArgument(
        private val name: String,
        val type: String,
        val hasDefaultValue: Boolean,
        val defaultValue: String
) {
    fun safeName() = sanitised(name)

    fun parse(): ParameterSpec = ParameterSpec.builder(safeName(), typeOf(type))
            .build()
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
    else ->
        if (type.contains("::")) ClassName(PACKAGE, type.substringBefore("::"), type.substringAfter("::"))
        else ClassName(PACKAGE, type)
}

fun sanitised(value: String) = underscoreToCamelCase(when (value) {
    "class" -> "_class"
    "object" -> "_object"
    "api" -> "_api"
    "interface" -> "_interface"
    "event" -> "_event"
    "in" -> "_in"
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
    "Vector3" -> true
    else -> false
}

fun returnTypeDeclaration(type: String) = CodeBlock.builder().apply {
    if (type != "void") when {
        isPrimitive(type) || isEnum(type) || isCoreType(type) -> addStatement("val ret = alloc<%M>()", toVar(type))
        else -> addStatement("val ret = %M()", toVar(type))
    }
}.build()

fun argumentDeclarations(arguments: List<GMethodArgument>) = CodeBlock.builder().apply {
    addStatement("val args: %M<%M> = allocArray(${arguments.size})", mCPointer, mCOpaquePointerVar)
    arguments.forEachIndexed { index, it ->
        addStatement("val ${it.safeName()}StableRef = %M.create(${it.safeName()})", mStableRef)
        addStatement("args[$index] = ${it.safeName()}StableRef.asCPointer()")
    }
}.build()

fun returnOutParameter(type: String) = when {
    type == "void" -> "null"
    isEnum(type) || isCoreType(type) -> "ret.ptr"
    !isPrimitive(type) -> "ret._owner"
    else -> "ret.ptr"
}

fun argumentCleanup(arguments: List<GMethodArgument>) = CodeBlock.builder().apply {
    arguments.forEach {
        addStatement("${it.safeName()}StableRef.dispose()")
    }
}.build()

fun isEnum(type: String) = type.startsWith("enum.")

fun cleanEnum(name: String) = name.substringAfter("enum.")

fun returnStatement(type: String) = CodeBlock.builder().apply {
    if (type != "void") when {
        isEnum(type) && isCoreType(type) -> addStatement("return ${cleanEnum(type)}.byValue(ret.value)")
        isEnum(type) -> addStatement("return ${cleanEnum(type.replace("::", "."))}.values()[ret.value.toInt()]")
        isPointer(type) -> addStatement("return ret")
        isPrimitive(type) -> addStatement("return ret.value")
        else -> addStatement("return ret.pointed.value")
    }
}.build()

fun underscoreToCamelCase(name: String) = (if (name.startsWith("_")) "_" else "") + name.split("_").joinToString("") { it.capitalize() }.decapitalize()