package godot

import godot.internal.godot_pool_vector3_array
import kotlinx.cinterop.AutofreeScope
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.invoke

class PoolVector3Array internal constructor(val _raw: CPointer<godot_pool_vector3_array>) {
    internal constructor(_raw: CValue<godot_pool_vector3_array>) : this(_raw.place(godot.api.godot_alloc!!(godot_pool_vector3_array.size.toInt())!! as CPointer<godot_pool_vector3_array>))

    constructor() : this(godot.api.godot_alloc!!(godot_pool_vector3_array.size.toInt())!! as CPointer<godot_pool_vector3_array>)

    internal fun _raw(scope: AutofreeScope): CPointer<godot_pool_vector3_array> {
        return _raw
    }
}