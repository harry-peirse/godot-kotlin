package godot

import godot.internal.godot_color
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class Color : CoreType<godot_color> {
    override val _wrapped: CPointer<godot_color>

    internal constructor (_wrapped: CPointer<godot_color>) {
        this._wrapped = _wrapped
    }

    internal constructor(value: CValue<godot_color>) {
        val _wrapped: CPointer<godot_color> = godot.alloc(godot_color.size)
        this._wrapped = value.place(_wrapped)
    }

    internal constructor() : this(godot.alloc(godot_color.size))
}