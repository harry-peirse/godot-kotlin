package godot

import godot.internal.godot_string
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class GodotString : CoreType<godot_string> {
    internal constructor (_wrapped: CPointer<godot_string>) : super(_wrapped)
    internal constructor(value: CValue<godot_string>) : super(value.place(godot.alloc(godot_string.size)))
    internal constructor() : this(godot.alloc(godot_string.size))
}