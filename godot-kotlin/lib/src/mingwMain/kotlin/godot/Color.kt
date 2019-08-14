package godot

import godot.internal.godot_color
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class Color internal constructor(val _raw: CPointer<godot_color>) {
    internal constructor(_raw: CValue<godot_color>) : this(_raw.place(godot.alloc(godot_color.size)))

    constructor() : this(godot.alloc())
}