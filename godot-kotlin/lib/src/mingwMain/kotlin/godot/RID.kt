package godot

import godot.internal.godot_rid
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class RID internal constructor(val _raw: CPointer<godot_rid>) {
    internal constructor(_raw: CValue<godot_rid>) : this(_raw.place(godotAlloc()))

    constructor() : this(godotAlloc())
}