package godot

import godot.internal.godot_rect2
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class Rect2 internal constructor(val _raw: CPointer<godot_rect2>) {
    internal constructor(_raw: CValue<godot_rect2>) : this(_raw.place(godot.alloc(godot_rect2.size)))

    constructor() : this(godot.alloc())
}