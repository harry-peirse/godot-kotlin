package godot

import godot.internal.godot_vector3
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class Vector3 internal constructor(val _raw: CPointer<godot_vector3>) {
    internal constructor(_raw: CValue<godot_vector3>) : this(_raw.place(godot.alloc(godot_vector3.size)))
}