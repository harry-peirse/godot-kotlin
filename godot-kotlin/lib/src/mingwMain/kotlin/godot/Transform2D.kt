package godot

import godot.internal.godot_transform2d
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class Transform2D : CoreType<godot_transform2d> {
    internal constructor (_wrapped: CPointer<godot_transform2d>) : super(_wrapped)
    internal constructor(value: CValue<godot_transform2d>) : super(value.place(godot.alloc(godot_transform2d.size)))
    internal constructor() : this(godot.alloc(godot_transform2d.size))
}