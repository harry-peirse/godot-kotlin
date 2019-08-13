package godot.generator

import com.squareup.kotlinpoet.ParameterSpec

data class GMethodArgument(
        var name: String,
        var type: String,
        val hasDefaultValue: Boolean,
        val defaultValue: String
) {
    val sanitisedName: String get() = name.sanitisedName()
    val sanitisedType: String get() = type.sanitisedType()
    val isEnum: Boolean get() = type.contains(".")

    fun parse(): ParameterSpec {
        return ParameterSpec.builder(sanitisedName, sanitisedType.toClassName().parameterized())
                .build()
    }
}