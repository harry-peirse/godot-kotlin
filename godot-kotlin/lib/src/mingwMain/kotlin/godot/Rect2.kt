package godot

import godot.internal.godot_rect2
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class Rect2 : CoreType<godot_rect2> {
    internal constructor (_wrapped: CPointer<godot_rect2>) : super(_wrapped)
    internal constructor(value: CValue<godot_rect2>) : super(value.place(godot.alloc(godot_rect2.size)))
    internal constructor() : this(godot.alloc(godot_rect2.size))
}