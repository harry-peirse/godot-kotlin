package godot

import godot.internal.godot_pool_vector2_array
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class PoolVector2Array : CoreType<godot_pool_vector2_array> {
    internal constructor (_wrapped: CPointer<godot_pool_vector2_array>) : super(_wrapped)
    internal constructor(value: CValue<godot_pool_vector2_array>) : super(value.place(godot.alloc(godot_pool_vector2_array.size)))
    internal constructor() : this(godot.alloc(godot_pool_vector2_array.size))
}