package godot.generator

import com.squareup.kotlinpoet.*

data class Signature(
        val returnType: String,
        val arguments: List<String>,
        val varargs: Boolean
) {
    fun parse(): FunSpec = FunSpec.builder("_icall__${returnType}__${arguments.joinToString("_")}")
            .addModifiers(KModifier.INTERNAL)
            .addAnnotation(AnnotationSpec.builder(ClassName("kotlin", "UseExperimental"))
                    .addMember("ExperimentalUnsignedTypes::class")
                    .build())
            .apply {
                if (returnType != "void") returns(typeOf(returnType.removePrefix("enum.")))
                addParameters(arguments.mapIndexed { index, it ->
                    ParameterSpec.builder("arg$index", typeOf(it.removePrefix("enum."))).build()
                })
                if (varargs) addParameter(ParameterSpec.builder("varargs", ClassName(PACKAGE, "Variant"))
                        .addModifiers(KModifier.VARARG)
                        .build())
            }
            .build()
}

class SignatureCollector {

    val list = HashSet<Signature>()

    fun collect(method: GMethod) {
        list.add(Signature(method.returnType, method.arguments.map { it.type }, method.hasVarargs))
    }

    fun parse(): FileSpec = FileSpec.builder(PACKAGE, "__ICalls")
            .apply {
                list.forEach {
                    addFunction(it.parse())
                }
            }
            .build()
}