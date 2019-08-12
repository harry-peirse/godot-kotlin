package godot

import godot.internal.godot_quat
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class Quat : CoreType<godot_quat> {
    internal constructor (_wrapped: CPointer<godot_quat>) : super(_wrapped)
    internal constructor(value: CValue<godot_quat>) : super(value.place(godot.alloc(godot_quat.size)))
    internal constructor() : this(godot.alloc(godot_quat.size))
}