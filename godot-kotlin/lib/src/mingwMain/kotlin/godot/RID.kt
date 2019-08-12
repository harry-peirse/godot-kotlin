package godot

import godot.internal.godot_rid
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class RID : CoreType<godot_rid> {
    internal constructor (_wrapped: CPointer<godot_rid>) : super(_wrapped)
    internal constructor(value: CValue<godot_rid>) : super(value.place(godot.alloc(godot_rid.size)))
    internal constructor() : this(godot.alloc(godot_rid.size))
}