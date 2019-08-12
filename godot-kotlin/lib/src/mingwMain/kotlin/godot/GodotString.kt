package godot

import godot.internal.godot_string
import kotlinx.cinterop.*

internal fun String.toGodotString(): CPointer<godot_string> = memScoped {
    val godotString: CPointer<godot_string> = godot.alloc(godot_string.size)
    godot.api.godot_string_new!!(godotString)
    godot.api.godot_string_parse_utf8!!(godotString, this@toGodotString.cstr.ptr)
    return godotString
}

internal fun CValue<godot_string>.toKotlinString(): String = memScoped {
    val godotCharString = godot.api.godot_string_utf8!!(this@toKotlinString.ptr)
    godot.api.godot_char_string_get_data!!(godotCharString.ptr)!!.toKStringFromUtf8()
}

class GodotString : CoreType<godot_string> {
    internal constructor (_wrapped: CPointer<godot_string>) : super(_wrapped)
    internal constructor(value: CValue<godot_string>) : super(value.place(godot.alloc(godot_string.size)))
    internal constructor() : this(godot.alloc(godot_string.size))
    constructor(string: String) : this(memScoped { godot.api.godot_string_chars_to_utf8!!(string.cstr.ptr) })

    companion object {
        operator fun <R> invoke(string: String, function: GodotString.() -> R): R {
            val godotString = GodotString(string)
            val result = godotString.function()
            godotString.destroy()
            return result
        }
    }
}