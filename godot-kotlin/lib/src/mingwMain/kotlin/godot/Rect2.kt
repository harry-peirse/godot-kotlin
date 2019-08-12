package godot

import godot.internal.godot_rect2
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class Rect2 : CoreType<godot_rect2> {
    override val _wrapped: CPointer<godot_rect2>

    internal constructor (_wrapped: CPointer<godot_rect2>) {
        this._wrapped = _wrapped
    }

    internal constructor(value: CValue<godot_rect2>) {
        val _wrapped: CPointer<godot_rect2> = godot.alloc(godot_rect2.size)
        this._wrapped = value.place(_wrapped)
    }

    internal constructor() : this(godot.alloc(godot_rect2.size))
}