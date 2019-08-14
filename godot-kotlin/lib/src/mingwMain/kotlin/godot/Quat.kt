package godot

import godot.internal.godot_quat
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class Quat internal constructor(val _raw: CPointer<godot_quat>) {
    internal constructor(_raw: CValue<godot_quat>) : this(_raw.place(godot.alloc(godot_quat.size)))

    constructor() : this(godot.alloc())
}