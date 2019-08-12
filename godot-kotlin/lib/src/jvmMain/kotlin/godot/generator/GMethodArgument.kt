package godot.generator

import com.squareup.kotlinpoet.ParameterSpec

data class GMethodArgument(
        var name: String,
        var type: String,
        val hasDefaultValue: Boolean,
        val defaultValue: String
) {
    fun sanitisedName() = name.sanitisedName()
    fun sanitisedType() = type.sanitisedType()

    fun isEnum(): Boolean = type.startsWith("enum.")

    fun parse(): ParameterSpec {
        return ParameterSpec.builder(sanitisedName(), sanitisedType().toClassName())
                .build()
    }
}