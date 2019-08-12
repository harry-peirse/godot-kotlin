package godot

import godot.internal.godot_aabb
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class AABB : CoreType<godot_aabb> {
    internal constructor (_wrapped: CPointer<godot_aabb>) : super(_wrapped)
    internal constructor(value: CValue<godot_aabb>) : super(value.place(godot.alloc(godot_aabb.size)))
    internal constructor() : this(godot.alloc(godot_aabb.size))
}