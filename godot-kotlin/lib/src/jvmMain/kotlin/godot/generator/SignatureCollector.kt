package godot.generator

import com.squareup.kotlinpoet.*

data class Signature(
        val returnType: String,
        val arguments: List<String>,
        val varargs: Boolean
) : Comparable<Signature> {
    fun parse(): FunSpec = FunSpec.builder(methodName())
            .addModifiers(KModifier.INTERNAL)
            .addAnnotation(AnnotationSpec.builder(UseExperimental)
                    .addMember("ExperimentalUnsignedTypes::class")
                    .build())
            .addParameter("methodBinding", CPointer_GodotMethodBind)
            .addParameter("owner", COpaquePointer)
            .apply {
                if (returnType != "void") returns(typeOf(returnType))
                arguments.forEachIndexed { index, it -> addParameter("arg$index", typeOf(it)) }
                if (varargs) addParameter("varargs", ClassName(PACKAGE, "Variant"), KModifier.VARARG)
            }
            .addCode(CodeBlock.builder()
                    .beginControlFlow("memScoped")
                    .apply {
                        add(returnTypeDeclaration(returnType))
                        add(argumentDeclarations(arguments, varargs))
                        addStatement("godot.api.godot_method_bind_ptrcall!!(methodBinding, owner, args, ${returnOutParameter(returnType)})")
                        add(returnStatement(returnType))
                    }
                    .endControlFlow()
                    .build())
            .build()

    fun methodName() = "_icall__${returnType}__${arguments.joinToString("_")}"

    override fun compareTo(other: Signature) = methodName().compareTo(other.methodName())
}

fun argumentDeclarations(arguments: List<String>, hasVarargs: Boolean) = CodeBlock.builder().apply {
    val argumentsSize = if (hasVarargs) "${arguments.size} + varargs.size" else "${arguments.size}"
    addStatement("val args: %T = allocArray($argumentsSize)", CPointer_COpaquePointerVar)
    arguments.forEachIndexed { index, it ->
        when {
            it == "COpaquePointer" -> addStatement("args[$index] = arg$index")
            isCoreType(it) -> addStatement("args[$index] = arg$index._wrapped")
            isPrimitive(it) || isEnum(it) -> addStatement("args[$index] = alloc<%M> { this.value = arg$index }.ptr", toVar(it))
            else -> addStatement("args[$index] = arg$index._wrapped")
        }
    }
    if (hasVarargs) {
        beginControlFlow("varargs.forEachIndexed")
        addStatement("index, it -> args[index] = it._wrapped")
        endControlFlow()
    }
}.build()

class SignatureCollector {

    var mostArgs = 0
    val list = HashSet<Signature>()

    fun collect(method: GMethod): Signature {
        if (method.arguments.size + (if (method.hasVarargs) 1 else 0) > mostArgs) {
            mostArgs = method.arguments.size
        }

        val signature = Signature(
                if (method.returnType.startsWith("enum.")) "UInt" else method.returnType,
                method.arguments.map {
                    when {
                        it.type.startsWith("enum.") -> "UInt"
                        isGeneratedClassType(it.type) -> "Wrapped"
                        isCoreType(it.type) -> "CoreType"
                        else -> it.type
                    }
                },
                method.hasVarargs)
        list.add(signature)
        return signature
    }

    fun parse(): FileSpec = FileSpec.builder(PACKAGE, "__ICalls")
            .addImport("kotlinx.cinterop", "invoke", "cstr", "memScoped", "alloc", "cValue", "allocArray", "pointed", "set", "value", "ptr", "reinterpret", "CFunction", "COpaquePointer")
            .apply {
                println("Most args: $mostArgs")
                println("Size: ${list.size}")
                list.sorted().forEach {
                    addFunction(it.parse())
                }
            }
            .build()

    private fun isGeneratedClassType(type: String): Boolean {
        return !isCoreType(type) && !isEnum(type) && !isPrimitive(type)
    }
}
