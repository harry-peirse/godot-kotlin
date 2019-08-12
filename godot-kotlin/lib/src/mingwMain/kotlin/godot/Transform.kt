package godot

import godot.internal.godot_transform
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class Transform : CoreType<godot_transform> {
    override val _wrapped: CPointer<godot_transform>

    internal constructor (_wrapped: CPointer<godot_transform>) {
        this._wrapped = _wrapped
    }

    internal constructor(value: CValue<godot_transform>) {
        val _wrapped: CPointer<godot_transform> = godot.alloc(godot_transform.size)
        this._wrapped = value.place(_wrapped)
    }

    internal constructor() : this(godot.alloc(godot_transform.size))
}