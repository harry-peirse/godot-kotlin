package godot

import godot.internal.godot_transform
import kotlinx.cinterop.*

class Transform(var basis: Basis = Basis(), var origin: Vector3 = Vector3()) {
    internal constructor(raw: CPointer<godot_transform>) : this(
            memScoped { Basis(api.godot_transform_get_basis!!(raw).ptr) },
            memScoped { Vector3(api.godot_transform_get_origin!!(raw).ptr) }
    )

    internal fun _raw(scope: AutofreeScope): CPointer<godot_transform> {
        val raw = scope.alloc<godot_transform>()
        api.godot_transform_new!!(raw.ptr, basis._raw(scope), origin._raw(scope))
        return raw.ptr
    }
}