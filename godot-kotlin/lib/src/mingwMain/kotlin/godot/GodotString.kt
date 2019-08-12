package godot

import godot.internal.godot_string
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class GodotString : CoreType<godot_string> {
    override val _wrapped: CPointer<godot_string>

    internal constructor (_wrapped: CPointer<godot_string>) {
        this._wrapped = _wrapped
    }

    internal constructor(value: CValue<godot_string>) {
        val _wrapped: CPointer<godot_string> = godot.alloc(godot_string.size)
        this._wrapped = value.place(_wrapped)
    }

    internal constructor() : this(godot.alloc(godot_string.size))
}