package godot

import godot.internal.godot_quat
import kotlinx.cinterop.*

class Quat(var x: Float = 0f, var y: Float = 0f, var z: Float = 0f, var w: Float = 0f) {
    internal constructor(raw: CPointer<godot_quat>) : this(
            api.godot_quat_get_x!!(raw),
            api.godot_quat_get_y!!(raw),
            api.godot_quat_get_z!!(raw),
            api.godot_quat_get_w!!(raw)
    )

    internal fun _raw(scope: AutofreeScope): CPointer<godot_quat> {
        val raw = scope.alloc<godot_quat>()
        api.godot_quat_new!!(raw.ptr, x, y, z, w)
        return raw.ptr
    }
}