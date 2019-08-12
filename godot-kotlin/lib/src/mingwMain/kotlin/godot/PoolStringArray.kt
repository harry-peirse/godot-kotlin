package godot

import godot.internal.godot_pool_string_array
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class PoolStringArray : CoreType<godot_pool_string_array> {
    override val _wrapped: CPointer<godot_pool_string_array>

    internal constructor (_wrapped: CPointer<godot_pool_string_array>) {
        this._wrapped = _wrapped
    }

    internal constructor(value: CValue<godot_pool_string_array>) {
        val _wrapped: CPointer<godot_pool_string_array> = godot.alloc(godot_pool_string_array.size)
        this._wrapped = value.place(_wrapped)
    }

    internal constructor() : this(godot.alloc(godot_pool_string_array.size))
}