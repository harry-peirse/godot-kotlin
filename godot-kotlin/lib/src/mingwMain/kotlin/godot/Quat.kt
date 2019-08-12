package godot

import godot.internal.godot_quat
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class Quat : CoreType<godot_quat> {
    override val _wrapped: CPointer<godot_quat>

    internal constructor (_wrapped: CPointer<godot_quat>) {
        this._wrapped = _wrapped
    }

    internal constructor(value: CValue<godot_quat>) {
        val _wrapped: CPointer<godot_quat> = godot.alloc(godot_quat.size)
        this._wrapped = value.place(_wrapped)
    }

    internal constructor() : this(godot.alloc(godot_quat.size))
}