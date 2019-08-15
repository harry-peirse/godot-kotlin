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
            .addParameter("_raw", COpaquePointer)
            .returns(returnType.parameterized())
            .apply {
                arguments.forEachIndexed { index, it -> addParameter("arg$index", it.parameterized()) }
                if (varargs) addParameter("varargs", _Any, KModifier.VARARG)
            }
            .addCode(CodeBlock.builder()
                    .beginControlFlow("memScoped")
                    .apply {
                        if (varargs) {
                            add(argumentDeclarations(arguments, varargs))
                            if (returnType == Object) {
                                addStatement("return %T(godot.api.godot_method_bind_call!!(methodBinding, _raw, args, ${arguments.size} + varargs.size, null)).toObject()", Variant)
                            } else {
                                addStatement("return %T(godot.api.godot_method_bind_call!!(methodBinding, _raw, args, ${arguments.size} + varargs.size, null))", Variant)
                            }
                        } else {
                            if (!returnType.isUnit()) add(returnTypeDeclaration(returnType))
                            add(argumentDeclarations(arguments, varargs))
                            addStatement("godot.api.godot_method_bind_ptrcall!!(methodBinding, _raw, args, ${returnOutParameter(returnType)})")
                            if (!returnType.isUnit()) add(returnStatement(returnType))
                        }
                    }
                    .endControlFlow()
                    .build())
            .build()

    fun methodName() = "_icall__${returnType.simpleName}${if (arguments.isNotEmpty()) "__" else ""}${arguments.joinToString("_") { it.simpleName }}"

    override fun compareTo(other: Signature) = methodName().compareTo(other.methodName())

    private fun argumentDeclarations(arguments: List<ClassName>, hasVarargs: Boolean) = CodeBlock.builder().apply {
        if (hasVarargs) {
            addStatement("val args: %T = allocArray((${arguments.size} + varargs.size) * %T.size)", CPointer_CPointerVar_GodotVariant, GodotVariant)
            arguments.forEachIndexed { index, _ ->
                when {
                    else -> addStatement("args[$index] = Variant(arg$index)._raw")
                }
            }
            beginControlFlow("varargs.forEachIndexed")
            addStatement("index, it -> args[index + ${arguments.size}] = Variant.of(it)._raw")
            endControlFlow()
        } else {
            val sb = StringBuilder()
            if(arguments.isEmpty()) {
                addStatement("val args: %T = allocArray(0)", CPointer_COpaquePointerVar)
            } else {
                sb.append("val args: %T = allocArrayOf(\n")
                sb.append(arguments.mapIndexed { index, it ->
                    when {
                        it.isPrimitiveType() -> "alloc<${it.toVarType().simpleName}>{ this.value = arg$index }.ptr"
                        it == String -> "arg$index.toGString(this)"
                        it == Array -> "arg$index.toGArray(this)"
                        it == MutableMap -> "arg$index.toGDictionary(this)"
                        it.simpleName == "Vector2" -> "arg$index._raw(this)"
                        else -> "arg$index._raw"
                    }
                }.joinToString(", \n"))
                sb.append("\n)")
                addStatement(sb.toString(), CPointer_COpaquePointerVar)
            }
        }
    }.build()

    private fun returnTypeDeclaration(type: ClassName) = CodeBlock.builder().apply {
        when {
            type.isPrimitiveType() -> addStatement("val ret = alloc<%T>()", type.toVarType())
            type.simpleName == "Vector2" -> addStatement("val ret = alloc<%T>()", ClassName(INTERNAL_PACKAGE, "godot_vector2"))
            type.isCoreType() -> addStatement("val ret = %T()", type)
            else -> addStatement("val ret = Variant()")
        }
    }.build()

    private fun returnOutParameter(type: ClassName) = when {
        type.isUnit() -> "null"
        type.simpleName == "Vector2" -> "ret.ptr"
        type.isPrimitiveType() -> "ret.ptr"
        else -> "ret._raw"
    }

    private fun returnStatement(type: ClassName) = CodeBlock.builder().apply {
        when {
            type == Variant -> addStatement("return ret")
            type == String -> addStatement("return ret.toString()")
            type == Array -> addStatement("return ret.toArray()")
            type == MutableMap -> addStatement("return ret.toMutableMap()")
            type.simpleName == "Vector2" -> addStatement("return Vector2(ret.ptr)")
            type.isEnumType() -> addStatement("return %T.byValue(ret.value)", type)
            type.isPrimitiveType() -> addStatement("return ret.value")
            type.isCoreType() -> addStatement("return ret")
            else -> addStatement("return %T.getFromVariant(ret._raw)", type)
        }
    }.build()
}

class SignatureCollector {

    private var mostArgs = 0
    private val list = HashSet<Signature>()

    fun collect(method: GMethod): Signature {
        if (method.arguments.size + (if (method.hasVarargs) 1 else 0) > mostArgs) {
            mostArgs = method.arguments.size
        }

        val signature = Signature(
                when {
                    method.returnTypeIsEnum -> _UInt
                    method.sanitisedReturnType.isClassType() -> Object
                    method.sanitisedReturnType == "Unit" -> _Unit
                    else -> method.sanitisedReturnType.toClassName()
                },
                method.arguments.map {
                    when {
                        it.isEnum -> _UInt
                        it.sanitisedType.isClassType() -> Object
                        else -> it.sanitisedType.toClassName()
                    }
                },
                method.hasVarargs)

        synchronized(list) {
            list.add(signature)
            when {
                signature.varargs -> list.remove(signature.copy(varargs = false))
                list.contains(signature.copy(varargs = true)) -> list.remove(signature)
                else -> Unit
            }
        }

        return signature
    }

    fun parse(): FileSpec = FileSpec.builder(PACKAGE, "__ICalls")
            .addImport("kotlinx.cinterop", "allocArrayOf", "invoke", "cstr", "memScoped", "alloc", "cValue", "allocArray", "pointed", "set", "value", "ptr", "reinterpret", "COpaquePointer")
            .apply {
                println("Most args: $mostArgs")
                println("Size: ${list.size}")
                list.sorted().forEach {
                    addFunction(it.parse())
                }
            }
            .build()
}
