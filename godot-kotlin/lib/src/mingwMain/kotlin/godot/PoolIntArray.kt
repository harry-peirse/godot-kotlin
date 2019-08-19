package godot

import godot.internal.godot_pool_int_array
import kotlinx.cinterop.AutofreeScope
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.invoke

class PoolIntArray internal constructor(val _raw: CPointer<godot_pool_int_array>) {
    internal constructor(_raw: CValue<godot_pool_int_array>) : this(_raw.place(godot.api.godot_alloc!!(godot_pool_int_array.size.toInt())!! as CPointer<godot_pool_int_array>))

    constructor() : this(godot.api.godot_alloc!!(godot_pool_int_array.size.toInt())!! as CPointer<godot_pool_int_array>)

    internal fun _raw(scope: AutofreeScope): CPointer<godot_pool_int_array> {
        return _raw
    }
}