package godot

import godot.internal.godot_string
import godot.internal.godot_variant
import kotlinx.cinterop.*

class GodotString : Variant {
    internal val _string: CPointer<godot_string>
    val string: String

    constructor(string: String) : super() {
        this.string = string
        _string = godot.alloc(godot_string.size)
        _variant = godot.alloc(godot_variant.size)
        godot.api.godot_string_new!!(_string)
        memScoped {
            godot.api.godot_string_parse_utf8!!(_string, string.cstr.ptr)
        }
        godot.api.godot_variant_new_string!!(_variant, _string)
    }

    internal constructor(_string: CPointer<godot_string>) : super() {
        this._string = _string
        _variant = godot.alloc(godot_variant.size)
        godot.api.godot_variant_new_string!!(_variant, _string)
        string = memScoped {
            godot.api.godot_char_string_get_data!!(godot.api.godot_string_utf8!!(_string).ptr)!!.toKStringFromUtf8()
        }
    }
}