package godot

import godot.internal.godot_plane
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class Plane : CoreType<godot_plane> {
    override val _wrapped: CPointer<godot_plane>

    internal constructor (_wrapped: CPointer<godot_plane>) {
        this._wrapped = _wrapped
    }

    internal constructor(value: CValue<godot_plane>) {
        val _wrapped: CPointer<godot_plane> = godot.alloc(godot_plane.size)
        this._wrapped = value.place(_wrapped)
    }

    internal constructor() : this(godot.alloc(godot_plane.size))
}