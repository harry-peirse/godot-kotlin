package godot

import godot.internal.godot_plane
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class Plane : CoreType<godot_plane> {
    internal constructor (_wrapped: CPointer<godot_plane>) : super(_wrapped)
    internal constructor(value: CValue<godot_plane>) : super(value.place(godot.alloc(godot_plane.size)))
    internal constructor() : this(godot.alloc(godot_plane.size))
}