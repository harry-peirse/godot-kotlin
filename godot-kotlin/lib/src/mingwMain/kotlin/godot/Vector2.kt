package godot

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.invoke

class Vector2 {
    internal val _wrapped: CPointer<godot_vector2>

    internal constructor (_wrapped: CPointer<godot_vector2>) {
        this._wrapped = _wrapped
    }

    internal constructor(value: CValue<godot_vector2>) {
        val _wrapped: CPointer<godot_vector2> = godot.alloc(godot_vector2.size)
        this._wrapped = value.place(_wrapped)
    }

    internal constructor() : this(godot.alloc(godot_vector2.size))

    constructor(x: Float, y: Float) : this() {
        godot.api.godot_vector2_new!!(_wrapped, x, y)
    }

    var x: Float
        get() = godot.api.godot_vector2_get_x!!(_wrapped)
        set(value) = godot.api.godot_vector2_set_x!!(_wrapped, value)
    var y: Float
        get() = godot.api.godot_vector2_get_y!!(_wrapped)
        set(value) = godot.api.godot_vector2_set_y!!(_wrapped, value)

}