package godot

import godot.internal.godot_vector2
import kotlinx.cinterop.*

class Vector2(var x: Float = 0f, var y: Float = 0f) {
    internal constructor(raw: CPointer<godot_vector2>) : this(godot.api.godot_vector2_get_x!!(raw), godot.api.godot_vector2_get_y!!(raw))

    internal fun _raw(scope: AutofreeScope): CPointer<godot_vector2> {
        val raw = scope.alloc<godot_vector2>()
        api.godot_vector2_new!!(raw.ptr, x, y)
        return raw.ptr
    }
}