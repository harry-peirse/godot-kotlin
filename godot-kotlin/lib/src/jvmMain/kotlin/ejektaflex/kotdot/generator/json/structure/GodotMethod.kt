package ejektaflex.kotdot.generator.json.structure

import com.google.gson.annotations.SerializedName
import com.squareup.kotlinpoet.*
import ejektaflex.kotdot.generator.json.NativeCommon
import ejektaflex.kotdot.generator.json.reg.TypeRegistry
import ejektaflex.kotdot.generator.simpleName
import ejektaflex.kotdot.generator.toCamelCase

data class GodotMethod(
    val name: String = "UNDEF_NAME",

    @SerializedName("return_type")
    val returnType: String = "void",

    @SerializedName("is_virtual")
    val isVirtual: Boolean = false,

    val arguments: List<GodotArgument>

)  {

    lateinit var parentClass: GodotClass

    val bindingName: String
        get() = "${parentClass.name}::$name"

    val returnTypeName: TypeName
        get() {
            return if (returnType.startsWith("enum")) {
                TypeRegistry.lookup("void")
            } else {
                TypeRegistry.lookup(returnType)
            }
        }


    fun returnArg(context: FunSpec.Builder, i: Int, argument: GodotArgument, argName: String) {
        context.apply {
            addStatement("args[$i] = " + when (argument.type) {
                "bool" -> "cValuesOf($argName.toByte()).getPointer(memScope)"
                "float", "long", "int" -> "cValuesOf($argName).getPointer(memScope)"
                else -> "$argName.getMemory(memScope)"
            })
        }
    }

    fun generate(document: Boolean = true): FunSpec.Builder {

        return FunSpec.builder(name.toCamelCase()).apply {
            returns(returnTypeName)

            if (document) {
                addKdoc("@see·" + NativeCommon.methodUrl(this@GodotMethod.parentClass, this@GodotMethod))
            }

            // Add arguments to constructor

            for (arg in arguments) {
                addParameter(
                        ParameterSpec.builder(arg.name, TypeRegistry.lookup(arg.type)).build()
                )
            }

            genPtrCall(this)

        }

    }

    fun genPtrCall(builder: FunSpec.Builder, singleVarName: String? = null) {

        builder.apply {

            val returnsVar = returnType != "void"

            // Return block
            if (returnsVar) {
                addCode(
                        CodeBlock.of("%L", PropertySpec.builder(
                                "toReturn",
                                returnTypeName
                        ).apply {
                            addModifiers(KModifier.LATEINIT)
                            mutable()
                        }.build())
                ).build()
            }


            beginControlFlow("memScoped")

            if (returnsVar) {
                addStatement(when(val tName = returnTypeName.simpleName)  {
                    "Double", "Boolean", "Long" -> "val cDestVar = alloc<${tName}Var>()"
                    else -> "val cDestVar = allocArray<ByteVar>(8)"
                })
            }


            addStatement("val args = allocArray<${NativeCommon.copaquevar.simpleName}>(${arguments.size + 1})")

            if (singleVarName != null && arguments.size == 1) {
                returnArg(this, 0, arguments.first(), singleVarName)
            } else {
                arguments.forEachIndexed { i, argument ->
                    returnArg(this, i, argument, argument.name)
                }
            }

            addStatement("args[${arguments.size}] = null")

            val lastPtrValue = if (returnsVar) "cDestVar" else "null"

            addStatement("godot_method_bind_ptrcall(bind_$name, getMemory(), args, $lastPtrValue)")

            if (returnsVar) {
                addStatement("toReturn = " + when (returnTypeName.simpleName)  {
                    "Double", "Boolean", "Long" -> "cDestVar.value"
                    "String" -> "cDestVar.value.toString()"
                    else -> "${returnTypeName.simpleName}(cDestVar)"
                })
            }

            endControlFlow()

            if (returnsVar) {
                addStatement("return toReturn")
            }

        }
    }




}