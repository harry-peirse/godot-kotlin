package godot

import godot.internal.godot_vector2
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.invoke

class Vector2(x: Float, y: Float) : Core<godot_vector2> {

    override val _raw: CPointer<godot_vector2> = godot.alloc(godot_vector2.size)

    var x: Float
        get() = godot.api.godot_vector2_get_x!!(_raw)
        set(value) = godot.api.godot_vector2_set_x!!(_raw, value)
    var y: Float
        get() = godot.api.godot_vector2_get_y!!(_raw)
        set(value) = godot.api.godot_vector2_set_y!!(_raw, value)

    init {
        godot.api.godot_vector2_new!!(_raw, x, y)
    }
}