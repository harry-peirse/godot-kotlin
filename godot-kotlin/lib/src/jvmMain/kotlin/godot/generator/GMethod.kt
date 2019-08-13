package godot.generator

import com.squareup.kotlinpoet.*

data class GMethod(
        val name: String,
        var returnType: String,
        val isEditor: Boolean,
        val isNoscript: Boolean,
        val isConst: Boolean,
        val isReverse: Boolean,
        val isVirtual: Boolean,
        val hasVarargs: Boolean,
        val isFromScript: Boolean,
        val arguments: List<GMethodArgument>
) {
    lateinit var clazz: GClass

    fun returnTypeIsEnum(): Boolean = returnType.startsWith("enum.")
    fun sanitisedName() = name.sanitisedName()
    fun sanitisedReturnType() = returnType.sanitisedType()

    fun functionBody(signature: Signature) = CodeBlock.builder()
            .apply {
                if (name == "free") {
                    addStatement("godot.api.godot_object_destroy!!(_raw)")
                } else {
                    addStatement("${if (returnType != "void") "return " else ""}" +
                            "${if (returnTypeIsEnum()) "${sanitisedReturnType()}.byValue(" else ""}" +
                            "${signature.methodName()}(mb.${sanitisedName()}!!, " +
                            "_raw${if (arguments.isNotEmpty()) ", " else ""}" +
                            "${arguments.joinToString(", ") {
                                if (it.isEnum()) "${it.sanitisedName()}.value"
                                else it.sanitisedName()
                            }}" +
                            "${if (hasVarargs) ", *varargs" else ""})" +
                            "${if (returnTypeIsEnum()) ")" else ""}")
                }
            }
            .build()

    fun parse(clazz: GClass, content: List<GClass>, signature: Signature): FunSpec {
        this.clazz = clazz
        return FunSpec.builder(sanitisedName())
                .addModifiers(KModifier.OPEN)
                .apply {
                    var c = clazz
                    var override = false
                    var parent: GMethod? = null
                    loop@ while (c.baseClass.isNotEmpty() && c.baseClass != "Variant") {
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
                    addParameters(arguments.mapIndexed { index, it ->
                        if (override && it.name.startsWith("arg")) {
                            it.name = parent!!.arguments[index].name
                            it.parse()
                        } else it.parse()
                    })
                    if (hasVarargs) {
                        addParameter(ParameterSpec.builder("varargs", ClassName(PACKAGE, "Variant"))
                                .addModifiers(KModifier.VARARG)
                                .build())
                    }
                }
                .addAnnotation(AnnotationSpec.builder(ClassName("kotlin", "UseExperimental"))
                        .addMember("ExperimentalUnsignedTypes::class")
                        .build())
                .addCode(functionBody(signature))
                .returns(sanitisedReturnType().toClassName())
                .build()
    }

    fun parseBinding(): PropertySpec = PropertySpec.builder(sanitisedName(), CPointer_GodotMethodBind.copy(nullable = true))
            .mutable()
            .initializer("null")
            .build()
}