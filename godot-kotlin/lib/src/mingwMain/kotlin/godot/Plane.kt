package godot

import godot.internal.godot_plane
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class Plane internal constructor(val _raw: CPointer<godot_plane>) {
    internal constructor(_raw: CValue<godot_plane>) : this(_raw.place(godotAlloc()))

    constructor() : this(godotAlloc())
}