package ejektaflex.kotdot.generator.json.structure

import com.google.gson.annotations.SerializedName

data class GodotArgument(
        var name: String,
        var type: String = "UNKNOWN_TYPE",
        @SerializedName("has_default_value")
        val hasDefaultValue: Boolean = false,
        @SerializedName("default_value")
        var defaultValue: String = "UNKNOWN_DEFAULT_VALUE"
)