package godot

import godot.internal.godot_vector3
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class Vector3 : CoreType<godot_vector3> {
    internal constructor (_wrapped: CPointer<godot_vector3>) : super(_wrapped)
    internal constructor(value: CValue<godot_vector3>) : super(value.place(godot.alloc(godot_vector3.size)))
    internal constructor() : this(godot.alloc(godot_vector3.size))
}