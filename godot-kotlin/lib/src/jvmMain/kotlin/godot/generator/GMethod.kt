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

    val returnTypeIsEnum: Boolean get() = returnType.startsWith("enum.")
    val sanitisedName: String get() = name.sanitisedName()
    val sanitisedReturnType: String get() = returnType.sanitisedType().removePrefix("$sanitisedName.")

    fun functionBody(signature: Signature) = CodeBlock.builder()
            .apply {
                if (name == "free") {
                    addStatement("godot.api.godot_object_destroy!!(_raw)")
                } else {
                    val sb = StringBuilder()
                    if (returnType != "void") sb.append("return ")
                    if (returnTypeIsEnum) sb.append("$sanitisedReturnType.byValue(")
                    sb.append("${signature.methodName()}(mb.$sanitisedName!!, _raw")
                    if (arguments.isNotEmpty()) sb.append(", ")
                    sb.append(arguments.joinToString(", ") { it.sanitisedName + if (it.sanitisedType.toClassName().isEnumType()) ".value" else "" })
                    if (hasVarargs) sb.append(", *varargs")
                    sb.append(")")
                    if (returnTypeIsEnum) sb.append(")")
                    if (returnType.sanitisedType().isClassType() && !returnTypeIsEnum && returnType != "Object") sb.append(" as %T")
                    val statement = sb.toString()
                    if (returnType.sanitisedType().isClassType() && !returnTypeIsEnum && returnType != "Object") addStatement(statement, sanitisedReturnType.toClassName().parameterized())
                    else addStatement(statement)
                }
            }
            .build()

    fun parse(clazz: GClass, content: List<GClass>, signature: Signature): FunSpec {
        this.clazz = clazz
        return FunSpec.builder(sanitisedName)
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
                .addCode(functionBody(signature))
                .returns(sanitisedReturnType.toClassName().parameterized())
                .build()
    }

    fun parseBinding(): PropertySpec = PropertySpec.builder(sanitisedName, CPointer_GodotMethodBind.copy(nullable = true))
            .mutable()
            .initializer("null")
            .build()
}