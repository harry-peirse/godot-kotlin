package godot.generator

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

data class GEnum(
        val name: String,
        val values: Map<String, Int>
) {

    fun self(): ClassName = ClassName("", name)

    fun parse(): TypeSpec = TypeSpec.enumBuilder(ClassName(PACKAGE, name))
            .addType(TypeSpec.companionObjectBuilder()
                    .addProperty(PropertySpec.builder("values", HashMap.parameterizedBy(UInt, self()), KModifier.PRIVATE)
                            .initializer("%T()", HashMap)
                            .build())
                    .addFunction(FunSpec.builder("byValue")
                            .addParameter("value", UInt)
                            .returns(self())
                            .addStatement("return values[value]!!")
                            .build())
                    .build())
            .addAnnotation(AnnotationSpec.builder(UseExperimental)
                    .addMember("ExperimentalUnsignedTypes::class")
                    .build())
            .primaryConstructor(FunSpec.constructorBuilder()
                    .addParameter("value", UInt)
                    .addStatement("$name.values[value] = this")
                    .build())
            .addProperty(PropertySpec.builder("value", UInt, KModifier.PUBLIC)
                    .initializer("value")
                    .build())
            .apply {
                values.entries.sortedBy { it.value }.forEach {
                    addEnumConstant(it.key, TypeSpec.anonymousClassBuilder()
                            .addSuperclassConstructorParameter("%Lu", it.value)
                            .build())
                }
            }
            .build()
}