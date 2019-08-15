package godot

import godot.internal.godot_transform2d
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class Transform2D internal constructor(val _raw: CPointer<godot_transform2d>) {
    internal constructor(_raw: CValue<godot_transform2d>) : this(_raw.place(godotAlloc()))

    constructor() : this(godotAlloc())
}