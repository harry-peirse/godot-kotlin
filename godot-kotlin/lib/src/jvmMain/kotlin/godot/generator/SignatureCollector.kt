package godot.generator

import com.squareup.kotlinpoet.*

data class Signature(
        val returnType: ClassName,
        val arguments: List<ClassName>,
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
                returns(returnType)
                arguments.forEachIndexed { index, it -> addParameter("arg$index", it.manageCoreType()) }
                if (varargs) addParameter("varargs", ClassName(PACKAGE, "Variant"), KModifier.VARARG)
            }
            .addCode(CodeBlock.builder()
                    .beginControlFlow("memScoped")
                    .apply {
                        if (varargs) {
                            add(argumentDeclarations(arguments, varargs))
                            addStatement("return %T(godot.api.godot_method_bind_call!!(methodBinding, owner, args, varargs.size + ${arguments.size}, null))", Variant)
                        } else {
                            if (!returnType.isUnit() && !varargs) add(returnTypeDeclaration(returnType))
                            add(argumentDeclarations(arguments, varargs))
                            addStatement("godot.api.godot_method_bind_ptrcall!!(methodBinding, owner, args, ${returnOutParameter(returnType)})")
                            if (!returnType.isUnit()) add(returnStatement(returnType))
                        }
                    }
                    .endControlFlow()
                    .build())
            .build()

    fun methodName() = "_icall__${returnType.simpleName}__${arguments.joinToString("_") { it.simpleName }}"

    override fun compareTo(other: Signature) = methodName().compareTo(other.methodName())

    private fun argumentDeclarations(arguments: List<ClassName>, hasVarargs: Boolean) = CodeBlock.builder().apply {
        val argumentsSize = if (hasVarargs) "${arguments.size} + varargs.size" else "${arguments.size}"
        addStatement("val args: %T = allocArray($argumentsSize * %T.size)", if (hasVarargs) CPointer_CPointerVar_GodotVariant else CPointer_COpaquePointerVar, GodotVariant)
        arguments.forEachIndexed { index, it ->
            when {
                it.isPrimitiveType() -> addStatement("args[$index] = alloc<%T> { this.value = arg$index }.ptr.reinterpret()", it.toVarType())
                else -> addStatement("args[$index] = arg$index._wrapped.reinterpret()")
            }
        }
        if (hasVarargs) {
            beginControlFlow("varargs.forEachIndexed")
            addStatement("index, it -> args[index] = it._wrapped")
            endControlFlow()
        }
    }.build()

    private fun returnTypeDeclaration(type: ClassName) = CodeBlock.builder().apply {
        when {
            type.simpleName.isCoreType() -> addStatement("val ret = %T()", type)
            type.simpleName.isPrimitiveType() -> addStatement("val ret = alloc<%T>()", type.toVarType())
            else -> addStatement("val ret = alloc<%T>()", Variant)
        }
    }.build()

    private fun returnOutParameter(type: ClassName) = when {
        type.isUnit() -> "null"
        type.isCoreType() -> "ret"
        else -> "ret.ptr"
    }

    private fun returnStatement(type: ClassName) = CodeBlock.builder().apply {
        when {
            type.isCoreEnumType() -> addStatement("return %T.byValue(ret.value)", type)
            type.isEnumType() -> addStatement("return %T.values()[ret.value.toInt()]", type)
            type.isCoreType() -> addStatement("return ret")
            !type.isPrimitiveType() -> {
                addStatement("val result = %T()", type)
                addStatement("result = godot.nativescript11Api.godot_nativescript_get_instance_binding_data!!(godot.languageIndex, ret._variant)?.reinterpret()")
                addStatement("return result")
            }
            type.isPrimitiveType() -> addStatement("return ret.value")
            else -> addStatement("return ret.pointed.value")
        }
    }.build()
}

class SignatureCollector {

    var mostArgs = 0
    val list = HashSet<Signature>()

    fun collect(method: GMethod): Signature {
        if (method.arguments.size + (if (method.hasVarargs) 1 else 0) > mostArgs) {
            mostArgs = method.arguments.size
        }

        val signature = Signature(
                (if (method.returnTypeIsEnum()) UInt else method.sanitisedReturnType().toClassName()),
                method.arguments.map {
                    when {
                        it.isEnum() -> UInt
                        it.sanitisedType().isGeneratedClassType() -> Variant
                        it.sanitisedType().isCoreType() -> CoreType.rawType
                        else -> it.sanitisedType().toClassName()
                    }
                },
                method.hasVarargs)

        synchronized(list) {
            list.add(signature)
            if (signature.varargs) list.remove(signature.copy(varargs = false))
            else if (list.contains(signature.copy(varargs = true))) list.remove(signature)
            else Unit
        }

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
}
