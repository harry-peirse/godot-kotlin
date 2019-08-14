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
                    .addProperty(PropertySpec.builder("values", HashMap.parameterizedBy(_UInt, self()), KModifier.PRIVATE)
                            .initializer("%T()", HashMap)
                            .build())
                    .addFunction(FunSpec.builder("byValue")
                            .addParameter("value", _UInt)
                            .returns(self())
                            .addStatement("return values[value]!!")
                            .build())
                    .build())
            .addAnnotation(UseExperimentalUnsignedTypes)
            .primaryConstructor(FunSpec.constructorBuilder()
                    .addParameter("value", _UInt)
                    .addStatement("$name.values[value] = this")
                    .build())
            .addProperty(PropertySpec.builder("value", _UInt, KModifier.PUBLIC)
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