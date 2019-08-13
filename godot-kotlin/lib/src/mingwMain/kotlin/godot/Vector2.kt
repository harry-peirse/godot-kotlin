package godot

import godot.internal.godot_vector2
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.invoke

class Vector2 internal constructor(val _raw: CPointer<godot_vector2>) {
    internal constructor(_raw: CValue<godot_vector2>) : this(_raw.place(godot.alloc(godot_vector2.size)))

    constructor(x: Float = 0f, y: Float = 0f) : this(godot.alloc(godot_vector2.size)) {
        godot.api.godot_vector2_new!!(_raw, x, y)
    }

    var x: Float
        get() = godot.api.godot_vector2_get_x!!(_raw)
        set(value) = godot.api.godot_vector2_set_x!!(_raw, value)
    var y: Float
        get() = godot.api.godot_vector2_get_y!!(_raw)
        set(value) = godot.api.godot_vector2_set_y!!(_raw, value)
}