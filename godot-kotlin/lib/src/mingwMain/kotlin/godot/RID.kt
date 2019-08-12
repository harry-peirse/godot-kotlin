package godot

import godot.internal.godot_rid
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class RID : CoreType<godot_rid> {
    override val _wrapped: CPointer<godot_rid>

    internal constructor (_wrapped: CPointer<godot_rid>) {
        this._wrapped = _wrapped
    }

    internal constructor(value: CValue<godot_rid>) {
        val _wrapped: CPointer<godot_rid> = godot.alloc(godot_rid.size)
        this._wrapped = value.place(_wrapped)
    }

    internal constructor() : this(godot.alloc(godot_rid.size))
}