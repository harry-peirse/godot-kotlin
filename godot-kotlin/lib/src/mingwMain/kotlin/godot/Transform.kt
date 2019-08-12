package godot

import godot.internal.godot_transform
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class Transform : CoreType<godot_transform> {
    internal constructor (_wrapped: CPointer<godot_transform>) : super(_wrapped)
    internal constructor(value: CValue<godot_transform>) : super(value.place(godot.alloc(godot_transform.size)))
    internal constructor() : this(godot.alloc(godot_transform.size))
}