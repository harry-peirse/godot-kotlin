package godot

import godot.internal.godot_aabb
import kotlinx.cinterop.*

class AABB(var position: Vector3, var size: Vector3) {
    internal constructor(raw: CPointer<godot_aabb>) : this(
            memScoped { Vector3(api.godot_aabb_get_position!!(raw).ptr) },
            memScoped { Vector3(api.godot_aabb_get_size!!(raw).ptr) }
    )

    internal fun _raw(scope: AutofreeScope): CPointer<godot_aabb> {
        val raw = scope.alloc<godot_aabb>()
        api.godot_aabb_new!!(raw.ptr, position._raw(scope), size._raw(scope))
        return raw.ptr
    }
}