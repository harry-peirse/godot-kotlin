package godot.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

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
    fun parse(): FileSpec {
        val builder = if (singleton) {
            TypeSpec.objectBuilder(ClassName(PACKAGE, name))
                    .addProperties(constants.map { (key, value) ->
                        PropertySpec.builder(key, Int::class)
                                .addModifiers(KModifier.PUBLIC, KModifier.CONST)
                                .initializer(value.toString())
                                .build()
                    })
                    .addType(TypeSpec.classBuilder("__method_bindings")
                            .addProperties(methods.map { it.parseBinding() })
                            .build())
                    .addProperty(PropertySpec.builder("__mb", ClassName(PACKAGE, name, "__method_bindings"))
                            .initializer("__method_bindings()")
                            .build())
                    .addFunction(FunSpec.builder("__init_method_bindings")
                            .addCode(CodeBlock.builder()
                                    .beginControlFlow("memScoped")
                                    .apply {
                                        methods.forEach {
                                            addStatement("__mb.__${it.name} = api.c.godot_method_bind_get_method!!(\"$name\".cstr.ptr, \"${it.name}\".cstr.ptr)")
                                        }
                                    }
                                    .endControlFlow()
                                    .build())
                            .build())
        } else {
            TypeSpec.classBuilder(ClassName(PACKAGE, name))
                    .addType(TypeSpec.companionObjectBuilder()
                            .addProperties(constants.map { (key, value) ->
                                PropertySpec.builder(key, Int::class)
                                        .addModifiers(KModifier.PUBLIC, KModifier.CONST)
                                        .initializer(value.toString())
                                        .build()
                            })
                            .addType(TypeSpec.classBuilder("__method_bindings")
                                    .addProperties(methods.map { it.parseBinding() })
                                    .build())
                            .addProperty(PropertySpec.builder("__mb", TypeVariableName("__method_bindings"))
                                    .initializer("__method_bindings()")
                                    .build())
                            .addFunction(FunSpec.builder("__init_method_bindings")
                                    .addCode(CodeBlock.builder()
                                            .beginControlFlow("memScoped")
                                            .apply {
                                                methods.forEach {
                                                    addStatement("__mb.__${it.name} = api.c.godot_method_bind_get_method!!(\"$name\".cstr.ptr, \"${it.name}\".cstr.ptr)")
                                                }
                                            }
                                            .endControlFlow()
                                            .build())
                                    .build())
                            .build())
        }
        return FileSpec.builder(PACKAGE, name)
                .addImport("kotlinx.cinterop", "invoke", "cstr", "memScoped", "alloc", "allocArray", "pointed", "set", "value", "reinterpret")
                .addType(builder
                        .addProperties(properties.mapNotNull { property -> property.parse(this) })
                        .addTypes(enums.map { enum -> enum.parse() })
                        .addFunctions(methods.map { method -> method.parse() })
                        .apply {
                            if (baseClass.isEmpty()) {
                                superclass(ClassName(PACKAGE, "_Wrapped"))
                            } else {
                                superclass(ClassName(PACKAGE, baseClass))
                            }

                            if (!singleton) {
                                addModifiers(KModifier.OPEN)
                                if (!instanciable) {
                                    addModifiers(KModifier.ABSTRACT)
                                }
                            }
                        }
                        .build()
                ).build()
    }
}

data class GProperty(
        val name: String,
        val type: String,
        val getter: String,
        val setter: String,
        val index: Int
) {
    fun parse(clazz: GClass): PropertySpec? {
        val spec = PropertySpec.builder(name, typeOf(type)).apply {
            val getter = clazz.methods.find { it.name == getter }
            if (getter != null) {
                getter(getter.also { clazz.methods.remove(it) }.parseGetter())

                val setter = clazz.methods.find { it.name == setter }
                if (setter != null) {
                    setter(setter.also { clazz.methods.remove(it) }.parseSetter())
                    mutable()
                }
            }
        }.build()

        return if (spec.getter != null) spec else null
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
        private val returnType: String,
        val isEditor: Boolean,
        val isNoscript: Boolean,
        val isConst: Boolean,
        val isReverse: Boolean,
        val isVirtual: Boolean,
        val hasvalargs: Boolean,
        val isFromScript: Boolean,
        val arguments: List<GMethodArgument>
) {
    fun safeReturnType() = if (returnType.startsWith("enum.")) returnType.substring(5) else returnType

    fun parseGetter(): FunSpec = FunSpec.builder("get()")
            .addCode(CodeBlock.builder()
                    .beginControlFlow("memScoped")
                    .apply {
                        if (safeReturnType() != "void") {
                            if (isCoreType(safeReturnType())) {
                                addStatement("val ret = alloc<%M>()", toVar(safeReturnType()))
                            } else if (isPrimitive(safeReturnType())) {
                                addStatement("val ret: %M<%M> = alloc()", MemberName("kotlinx.cinterop", "CPointer"), toVar(safeReturnType()))
                            } else {
                                addStatement("val ret = %M()", toVar(safeReturnType()))
                                addStatement("ret._owner = alloc()")
                            }
                        }
                        addStatement("val args: %M<%M> = allocArray(${arguments.size})", MemberName("kotlinx.cinterop", "CPointer"), MemberName("kotlinx.cinterop", "COpaquePointerVar"))
                        arguments.forEachIndexed { index, it ->
                            addStatement("val ${it.safeName()}StableRef = %M.create(${it.safeName()})", MemberName("kotlinx.cinterop", "StableRef"))
                            addStatement("args[$index] = ${it.safeName()}StableRef.asCPointer()")
                        }
                        addStatement("api.c.godot_method_bind_ptrcall!!(__mb.__$name, _owner, args, ${if (safeReturnType() != "void") if (isCoreType(safeReturnType())) "ret.reinterpret()" else if (!isPrimitive(safeReturnType())) "ret._owner" else "ret" else "null"})")
                        arguments.forEach {
                            addStatement("${it.safeName()}StableRef.dispose()")
                        }
                        if (safeReturnType() != "void") {
                            if (isPointer(safeReturnType())) {
                                if (!isPrimitive(safeReturnType())) {
                                    addStatement("return ret")
                                } else {
                                    addStatement("return ret.pointed")
                                }
                            } else {
                                addStatement("return ret.pointed.value")
                            }
                        }
                    }
                    .endControlFlow()
                    .build())
            .build()

    fun parseSetter(): FunSpec = FunSpec.builder("set()")
            .addParameter(arguments.map { it.parse() }.first())
            .addCode(CodeBlock.builder()
                    .beginControlFlow("memScoped")
                    .apply {
                        if (safeReturnType() != "void") {
                            if (isCoreType(safeReturnType())) {
                                addStatement("val ret = alloc<%M>()", toVar(safeReturnType()))
                            } else if (isPrimitive(safeReturnType())) {
                                addStatement("val ret: %M<%M> = alloc()", MemberName("kotlinx.cinterop", "CPointer"), toVar(safeReturnType()))
                            } else {
                                addStatement("val ret = %M()", toVar(safeReturnType()))
                                addStatement("ret._owner = alloc()")
                            }
                        }
                        addStatement("val args: %M<%M> = allocArray(${arguments.size})", MemberName("kotlinx.cinterop", "CPointer"), MemberName("kotlinx.cinterop", "COpaquePointerVar"))
                        arguments.forEachIndexed { index, it ->
                            addStatement("val ${it.safeName()}StableRef = %M.create(${it.safeName()})", MemberName("kotlinx.cinterop", "StableRef"))
                            addStatement("args[$index] = ${it.safeName()}StableRef.asCPointer()")
                        }
                        addStatement("api.c.godot_method_bind_ptrcall!!(__mb.__$name, _owner, args, ${if (safeReturnType() != "void") if (isCoreType(safeReturnType())) "ret.reinterpret()" else if (!isPrimitive(safeReturnType())) "ret._owner" else "ret" else "null"})")
                        arguments.forEach {
                            addStatement("${it.safeName()}StableRef.dispose()")
                        }
                        if (safeReturnType() != "void") {
                            if (isPointer(safeReturnType())) {
                                if (!isPrimitive(safeReturnType())) {
                                    addStatement("return ret")
                                } else {
                                    addStatement("return ret.pointed")
                                }
                            } else {
                                addStatement("return ret.pointed.value")
                            }
                        }
                    }
                    .endControlFlow()
                    .build())
            .build()

    fun parse(): FunSpec = FunSpec.builder(name)
            .addParameters(arguments.map { it.parse() })
            .addCode(CodeBlock.builder()
                    .beginControlFlow("memScoped")
                    .apply {
                        if (safeReturnType() != "void") {
                            if (isCoreType(safeReturnType())) {
                                addStatement("val ret = alloc<%M>()", toVar(safeReturnType()))
                            } else if (isPrimitive(safeReturnType())) {
                                addStatement("val ret: %M<%M> = alloc()", MemberName("kotlinx.cinterop", "CPointer"), toVar(safeReturnType()))
                            } else {
                                addStatement("val ret = %M()", toVar(safeReturnType()))
                                addStatement("ret._owner = alloc()")
                            }
                        }
                        addStatement("val args: %M<%M> = allocArray(${arguments.size})", MemberName("kotlinx.cinterop", "CPointer"), MemberName("kotlinx.cinterop", "COpaquePointerVar"))
                        arguments.forEachIndexed { index, it ->
                            addStatement("val ${it.safeName()}StableRef = %M.create(${it.safeName()})", MemberName("kotlinx.cinterop", "StableRef"))
                            addStatement("args[$index] = ${it.safeName()}StableRef.asCPointer()")
                        }
                        addStatement("api.c.godot_method_bind_ptrcall!!(__mb.__$name, _owner, args, ${if (safeReturnType() != "void") if (isCoreType(safeReturnType())) "ret.reinterpret()" else if (!isPrimitive(safeReturnType())) "ret._owner" else "ret" else "null"})")
                        arguments.forEach {
                            addStatement("${it.safeName()}StableRef.dispose()")
                        }
                        if (safeReturnType() != "void") {
                            if (isPointer(safeReturnType())) {
                                if (!isPrimitive(safeReturnType())) {
                                    addStatement("return ret")
                                } else {
                                    addStatement("return ret.pointed")
                                }
                            } else {
                                addStatement("return ret.pointed.value")
                            }
                        }
                    }
                    .endControlFlow()
                    .build())
            .returns(typeOf(safeReturnType()))
            .build()

    fun parseBinding(): PropertySpec = PropertySpec.builder("__$name", ClassName("kotlinx.cinterop", "CPointer")
            .parameterizedBy(ClassName("godotapi", "godot_method_bind")).copy(nullable = true))
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
    else -> ClassName(PACKAGE, type)
}

fun sanitised(value: String) = when (value) {
    "class" -> "_class"
    "object" -> "_object"
    "api" -> "_api"
    else -> value
}

fun isPointer(value: String) = when (value) {
    "int", "bool", "float", "real" -> false
    else -> true
}

fun isPrimitive(value: String) = when (value) {
    "int", "bool", "float", "real" -> true
    else -> false
}

fun toVar(value: String) = when (value) {
    "int" -> MemberName("kotlinx.cinterop", "IntVar")
    "bool" -> MemberName("kotlinx.cinterop", "BooleanVar")
    "float", "real" -> MemberName("kotlinx.cinterop", "FloatVar")
    else -> MemberName(PACKAGE, value)
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