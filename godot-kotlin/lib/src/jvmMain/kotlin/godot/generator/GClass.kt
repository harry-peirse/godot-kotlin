package godot.generator

import com.squareup.kotlinpoet.*

data class GClass(
        val name: String,
        val baseClass: String,
        val singleton: Boolean,
        val instanciable: Boolean,
        val isReference: Boolean,
        val constants: Map<String, Int>,
        val methods: MutableList<GMethod>,
        val properties: MutableList<GProperty>,
        val enums: List<GEnum>
) {

    var methodMap: MutableMap<String, GMethod> = HashMap()

    fun parseRegisterCall() = CodeBlock.builder()
            .addStatement("godot.tagDB.registerGlobalType(\"$name\", $name::class.hashCode().toUInt(), ${if (baseClass.isBlank()) "0u" else "$baseClass::class.hashCode().toUInt()"})")
            .build()

    fun parseBindingCall() = CodeBlock.builder()
            .addStatement("$name.initMethodBindings()")
            .build()

    fun parse(content: List<GClass>, collector: SignatureCollector): FileSpec {
        methodMap = HashMap()
        methods.map {
            methodMap[it.name] = it
        }

        return FileSpec.builder(PACKAGE, name)
                .addImport("kotlinx.cinterop", "invoke", "cstr", "memScoped", "alloc", "cValue", "allocArray", "pointed", "set", "value", "ptr", "reinterpret", "CFunction", "COpaquePointer")
                .addType((TypeSpec.classBuilder(ClassName(PACKAGE, name)).addType(buildCore(TypeSpec.companionObjectBuilder()).build()))
                        .addTypes(enums.map { enum -> enum.parse() })
                        .addProperties(properties.mapNotNull {
                            it.parse(this, content)
                        })
                        .addFunctions(methods
                                .map {
                                    it.parse(this, content, collector.collect(it))
                                })
                        .apply {
                            if (singleton) {
                                primaryConstructor(FunSpec.constructorBuilder()
                                        .addModifiers(KModifier.INTERNAL)
                                        .build())
                            }
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
                PropertySpec.builder(key, Int)
                        .addModifiers(KModifier.PUBLIC, KModifier.CONST)
                        .initializer(value.toString())
                        .build()
            })
            .addType(TypeSpec.classBuilder("MethodBindings")
                    .addProperties(methods.map { it.parseBinding() })
                    .build())
            .addProperty(PropertySpec.builder("mb", TypeVariableName("MethodBindings"))
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
                                    addStatement("mb.${it.sanitisedName()} = godot.api.godot_method_bind_get_method!!(\"$name\".cstr.ptr, \"${it.name}\".cstr.ptr)")
                                }
                            }
                            .endControlFlow()
                            .build())
                    .build())
            .addFunction(FunSpec.builder("getFromVariant")
                    .returns(ClassName(PACKAGE, name))
                    .addParameter("variant", ClassName(PACKAGE, "Variant"))
                    .addCode(CodeBlock.builder()
                            .addStatement("val instance = $name()")
                            .addStatement("instance._wrapped = godot.nativescript11Api.godot_nativescript_get_instance_binding_data!!(godot.languageIndex, variant._wrapped)!!.reinterpret()")
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
                                    .addStatement("singleton._wrapped = godot.api.godot_alloc!!(%T.size.toInt())?.reinterpret()", _Wrapped)
                                    .addStatement("singleton._wrapped?.pointed?._owner = godot.api.godot_global_get_singleton!!(\"$name\".cstr.ptr)")
                                    .addStatement("singleton._wrapped?.pointed?._typeTag = $name::class.simpleName.hashCode().toUInt()")
                                    .endControlFlow()
                                    .endControlFlow()
                                    .addStatement("return singleton")
                                    .build())
                            .build())
                    addFunction(FunSpec.builder("invoke")
                            .returns(ClassName(PACKAGE, name))
                            .addModifiers(KModifier.OPERATOR)
                            .addStatement("return singleton()")
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