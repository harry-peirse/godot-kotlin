package godot

import godot.internal.godot_pool_int_array
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class PoolIntArray : CoreType<godot_pool_int_array> {
    override val _wrapped: CPointer<godot_pool_int_array>

    internal constructor (_wrapped: CPointer<godot_pool_int_array>) {
        this._wrapped = _wrapped
    }

    internal constructor(value: CValue<godot_pool_int_array>) {
        val _wrapped: CPointer<godot_pool_int_array> = godot.alloc(godot_pool_int_array.size)
        this._wrapped = value.place(_wrapped)
    }

    internal constructor() : this(godot.alloc(godot_pool_int_array.size))
}