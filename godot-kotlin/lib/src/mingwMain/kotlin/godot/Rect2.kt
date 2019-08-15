package godot

import godot.internal.godot_rect2
import kotlinx.cinterop.*

class Rect2(var position: Vector2 = Vector2(), var size: Vector2 = Vector2()) {
    internal constructor(raw: CPointer<godot_rect2>) : this(
            memScoped { Vector2(api.godot_rect2_get_position!!(raw).ptr) },
            memScoped { Vector2(api.godot_rect2_get_size!!(raw).ptr) }
    )

    internal fun _raw(scope: AutofreeScope): CPointer<godot_rect2> {
        val raw = scope.alloc<godot_rect2>()
        api.godot_rect2_new_with_position_and_size!!(raw.ptr, position._raw(scope), size._raw(scope))
        return raw.ptr
    }
}