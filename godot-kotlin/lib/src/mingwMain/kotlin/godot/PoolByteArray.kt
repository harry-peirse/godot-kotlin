package godot

import godot.internal.godot_pool_byte_array
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class PoolByteArray internal constructor(val _raw: CPointer<godot_pool_byte_array>) {
    internal constructor(_raw: CValue<godot_pool_byte_array>) : this(_raw.place(godot.alloc(godot_pool_byte_array.size)))

    constructor() : this(godot.alloc())
}