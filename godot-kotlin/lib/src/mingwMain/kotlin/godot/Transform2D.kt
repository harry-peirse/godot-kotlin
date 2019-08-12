package godot

import godot.internal.godot_transform2d
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class Transform2D : CoreType<godot_transform2d> {
    override val _wrapped: CPointer<godot_transform2d>

    internal constructor (_wrapped: CPointer<godot_transform2d>) {
        this._wrapped = _wrapped
    }

    internal constructor(value: CValue<godot_transform2d>) {
        val _wrapped: CPointer<godot_transform2d> = godot.alloc(godot_transform2d.size)
        this._wrapped = value.place(_wrapped)
    }

    internal constructor() : this(godot.alloc(godot_transform2d.size))
}