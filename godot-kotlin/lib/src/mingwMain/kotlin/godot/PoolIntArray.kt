package godot

import godot.internal.godot_pool_int_array
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class PoolIntArray internal constructor(val _raw: CPointer<godot_pool_int_array>) {
    internal constructor(_raw: CValue<godot_pool_int_array>) : this(_raw.place(godot.alloc(godot_pool_int_array.size)))

    constructor() : this(godot.alloc())
}