package godot

import godot.internal.godot_array
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class GodotArray : CoreType<godot_array> {
    internal constructor (_wrapped: CPointer<godot_array>) : super(_wrapped)
    internal constructor(value: CValue<godot_array>) : super(value.place(godot.alloc(godot_array.size)))
    internal constructor() : this(godot.alloc(godot_array.size))
}