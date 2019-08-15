package godot

import godot.internal.godot_aabb
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class AABB internal constructor(val _raw: CPointer<godot_aabb>) {
    internal constructor(_raw: CValue<godot_aabb>) : this(_raw.place(godotAlloc()))

    constructor() : this(godotAlloc())
}