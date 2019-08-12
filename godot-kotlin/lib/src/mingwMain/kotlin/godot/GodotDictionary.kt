package godot

import godot.internal.godot_dictionary
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class GodotDictionary : CoreType<godot_dictionary> {
    override val _wrapped: CPointer<godot_dictionary>

    internal constructor (_wrapped: CPointer<godot_dictionary>) {
        this._wrapped = _wrapped
    }

    internal constructor(value: CValue<godot_dictionary>) {
        val _wrapped: CPointer<godot_dictionary> = godot.alloc(godot_dictionary.size)
        this._wrapped = value.place(_wrapped)
    }

    internal constructor() : this(godot.alloc(godot_dictionary.size))
}