package godot

import godot.internal.godot_pool_vector3_array
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class PoolVector3Array : CoreType<godot_pool_vector3_array> {
    override val _wrapped: CPointer<godot_pool_vector3_array>

    internal constructor (_wrapped: CPointer<godot_pool_vector3_array>) {
        this._wrapped = _wrapped
    }

    internal constructor(value: CValue<godot_pool_vector3_array>) {
        val _wrapped: CPointer<godot_pool_vector3_array> = godot.alloc(godot_pool_vector3_array.size)
        this._wrapped = value.place(_wrapped)
    }

    internal constructor() : this(godot.alloc(godot_pool_vector3_array.size))
}