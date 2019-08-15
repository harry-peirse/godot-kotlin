package godot

import godot.internal.godot_transform2d
import kotlinx.cinterop.*

class Transform2D(var rotation: Float = 0f, var position: Vector2 = Vector2()) {
    internal constructor(raw: CPointer<godot_transform2d>) : this(
            api.godot_transform2d_get_rotation!!(raw),
            memScoped { Vector2(api.godot_transform2d_get_origin!!(raw).ptr) }
    )

    internal fun _raw(scope: AutofreeScope): CPointer<godot_transform2d> {
        val raw = scope.alloc<godot_transform2d>()
        api.godot_transform2d_new!!(raw.ptr, rotation, position._raw(scope))
        return raw.ptr
    }
}