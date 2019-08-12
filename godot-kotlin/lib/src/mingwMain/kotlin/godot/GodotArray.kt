package godot

import godot.internal.godot_array
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class GodotArray : CoreType<godot_array> {
    override val _wrapped: CPointer<godot_array>

    internal constructor (_wrapped: CPointer<godot_array>) {
        this._wrapped = _wrapped
    }

    internal constructor(value: CValue<godot_array>) {
        val _wrapped: CPointer<godot_array> = godot.alloc(godot_array.size)
        this._wrapped = value.place(_wrapped)
    }

    internal constructor() : this(godot.alloc(godot_array.size))
}