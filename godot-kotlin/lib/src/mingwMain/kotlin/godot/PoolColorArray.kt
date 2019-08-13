package godot

import godot.internal.godot_pool_color_array
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class PoolColorArray internal constructor(val _raw: CPointer<godot_pool_color_array>) {
    internal constructor(_raw: CValue<godot_pool_color_array>) : this(_raw.place(godot.alloc(godot_pool_color_array.size)))
}