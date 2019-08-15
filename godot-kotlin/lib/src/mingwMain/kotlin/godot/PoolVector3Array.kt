package godot

import godot.internal.godot_pool_vector3_array
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class PoolVector3Array internal constructor(val _raw: CPointer<godot_pool_vector3_array>) {
    internal constructor(_raw: CValue<godot_pool_vector3_array>) : this(_raw.place(godotAlloc()))

    constructor() : this(godotAlloc())
}