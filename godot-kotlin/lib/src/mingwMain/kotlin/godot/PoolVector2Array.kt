package godot

import godot.internal.godot_pool_vector2_array
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class PoolVector2Array internal constructor(val _raw: CPointer<godot_pool_vector2_array>) {
    internal constructor(_raw: CValue<godot_pool_vector2_array>) : this(_raw.place(godot.alloc(godot_pool_vector2_array.size)))
}