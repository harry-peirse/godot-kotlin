package godot

import godot.internal.godot_color
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class Color : CoreType<godot_color> {
    internal constructor (_wrapped: CPointer<godot_color>) : super(_wrapped)
    internal constructor(value: CValue<godot_color>) : super(value.place(godot.alloc(godot_color.size)))
    internal constructor() : this(godot.alloc(godot_color.size))
}