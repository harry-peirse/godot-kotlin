package godot.generator

import com.squareup.kotlinpoet.*

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
                    .addType(TypeSpec.classBuilder("__method_bindings")
                            .addProperties(methods.map { it.parseBinding() })
                            .build())
                    .addProperty(PropertySpec.builder("__mb", ClassName(PACKAGE, name, "__method_bindings"))
                            .initializer("__method_bindings()")
                            .build())
                    .addFunction(FunSpec.builder("__init_method_bindings")
                            .apply {
                                methods.forEach {
                                    addStatement("__mb.__${it.name} = api.c.godot_method_bind_get_method!!(\"$name\", \"${it.name}\"")
                                }
                            }
                            .build())
        } else {
            TypeSpec.classBuilder(ClassName(PACKAGE, name))
                    .addType(TypeSpec.companionObjectBuilder()
                            .addType(TypeSpec.classBuilder("__method_bindings")
                                    .addProperties(methods.map { it.parseBinding() })
                                    .build())
                            .addProperty(PropertySpec.builder("__mb", TypeVariableName("__method_bindings"))
                                    .initializer("__method_bindings()")
                                    .build())
                            .addFunction(FunSpec.builder("__init_method_bindings")
                                    .apply {
                                        methods.forEach {
                                            addStatement("__mb.__${it.name} = api.c.godot_method_bind_get_method!!(\"$name\", \"${it.name}\")")
                                        }
                                    }
                                    .build())
                            .build())
        }
        return FileSpec.builder(PACKAGE, name)
                .addImport("kotlinx.cinterop", "invoke")
                .addType(builder
                        .addProperties(constants.map { (key, value) ->
                            PropertySpec.builder(key, Int::class)
                                    .addModifiers(KModifier.PUBLIC, KModifier.CONST)
                                    .initializer(value.toString())
                                    .build()

                        })
                        .addProperties(properties.mapNotNull { property -> property.parse(this) })
                        .addTypes(enums.map { enum -> enum.parse() })
                        .addFunctions(methods.map { method -> method.parse() })
                        .apply {
                            if (!baseClass.isEmpty()) {
                                superclass(ClassName(PACKAGE, baseClass))
                            }

                            /* if object
                        source.append(class_name + "::" + class_name + "() {")
                        source.append("\t_owner = godot::api->godot_global_get_singleton((char *) \"" + strip_name(c["name"]) + "\");")
                        source.append("}")
                        */

                            if (instanciable) {
                                addModifiers(KModifier.OPEN)
                                /*
                                source.append(class_name + " *" + strip_name(c["name"]) + "::_new()")
                                source.append("{")
                                source.append("\treturn (" + class_name + " *) godot::nativescript_1_1_api->godot_nativescript_get_instance_binding_data(godot::_RegisterState::language_index, godot::api->godot_get_class_constructor((char *)\"" + c["name"] + "\")());")
                                source.append("}")
                            */
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
        val returnType: String,
        val isEditor: Boolean,
        val isNoscript: Boolean,
        val isConst: Boolean,
        val isReverse: Boolean,
        val isVirtual: Boolean,
        val hasvalargs: Boolean,
        val isFromScript: Boolean,
        val arguments: List<GMethodArgument>
) {
    fun parseGetter(): FunSpec = FunSpec.builder("get()").build()
    fun parseSetter(): FunSpec = FunSpec.builder("set()").build()

    fun parse(): FunSpec = FunSpec.builder(name)
            .addParameters(arguments.map { it.parse() })
            .addStatement("return %S", "foo")
            .returns(typeOf(returnType))
            .build()

    fun parseBinding(): PropertySpec = PropertySpec.builder("__$name", ClassName("godotapi", "godot_method_bind"))
            .mutable()
            .addModifiers(KModifier.LATEINIT)
            .build()
}

data class GMethodArgument(
        val name: String,
        val type: String,
        val hasDefaultValue: Boolean,
        val defaultValue: String
) {
    fun parse(): ParameterSpec = ParameterSpec.builder(name, typeOf(type))
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

fun typeOf(type: String): ClassName {
    return when (type) {
        "void" -> Unit::class.asClassName()
        "float", "real" -> Float::class.asClassName()
        "int" -> Int::class.asClassName()
        "bool" -> Boolean::class.asClassName()
        else -> ClassName(PACKAGE, type)
    }
}