package godot

import godot.internal.godot_vector2
import kotlinx.cinterop.*

class Vector2 internal constructor(val _raw: CPointer<godot_vector2>) {
    internal constructor(_raw: CValue<godot_vector2>) : this(_raw.place(godotAlloc()))

    constructor(x: Float = 0f, y: Float = 0f) : this(godotAlloc()) {
        godot.api.godot_vector2_new!!(_raw, x, y)
    }

    var x: Float
        get() = godot.api.godot_vector2_get_x!!(_raw)
        set(value) = godot.api.godot_vector2_set_x!!(_raw, value)
    var y: Float
        get() = godot.api.godot_vector2_get_y!!(_raw)
        set(value) = godot.api.godot_vector2_set_y!!(_raw, value)

    override fun hashCode(): Int {
        return _raw.pointed.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Vector2 -> godot.api.godot_vector2_operator_equal!!(_raw, other._raw)
            else -> false
        }
    }

    override fun toString(): String = memScoped {
        godot.api.godot_vector2_as_string!!(_raw).ptr.toKString()
    }

    fun dispose() {
        godotFree(_raw)
    }
}