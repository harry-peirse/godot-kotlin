package godot

import godot.internal.godot_transform
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class Transform internal constructor(val _raw: CPointer<godot_transform>) {
    internal constructor(_raw: CValue<godot_transform>) : this(_raw.place(godot.alloc(godot_transform.size)))
}