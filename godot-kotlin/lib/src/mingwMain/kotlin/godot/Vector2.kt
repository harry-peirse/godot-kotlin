package godot

import godot.internal.godot_variant
import godot.internal.godot_vector2
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.invoke

class Vector2 : Variant {

    internal val _vector2: CPointer<godot_vector2>

    constructor(x: Float, y: Float) : super() {
        _vector2 = godot.alloc(godot_vector2.size)
        godot.api.godot_vector2_new!!(_vector2, x, y)
        _variant = godot.alloc(godot_variant.size)
        godot.api.godot_variant_new_vector2!!(_variant, _vector2)
    }

    internal constructor(_vector2: CPointer<godot_vector2>) : super() {
        this._vector2 = _vector2
        _variant = godot.alloc(godot_variant.size)
        godot.api.godot_variant_new_vector2!!(_variant, _vector2)
    }

    var x: Float
        get() = godot.api.godot_vector2_get_x!!(_vector2)
        set(value) = godot.api.godot_vector2_set_x!!(_vector2, value)
    var y: Float
        get() = godot.api.godot_vector2_get_y!!(_vector2)
        set(value) = godot.api.godot_vector2_set_y!!(_vector2, value)

}