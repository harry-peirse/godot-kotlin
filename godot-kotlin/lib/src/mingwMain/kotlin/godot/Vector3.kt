package godot

import godot.internal.godot_vector3
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class Vector3 : CoreType<godot_vector3> {
    override val _wrapped: CPointer<godot_vector3>

    internal constructor (_wrapped: CPointer<godot_vector3>) {
        this._wrapped = _wrapped
    }

    internal constructor(value: CValue<godot_vector3>) {
        val _wrapped: CPointer<godot_vector3> = godot.alloc(godot_vector3.size)
        this._wrapped = value.place(_wrapped)
    }

    internal constructor() : this(godot.alloc(godot_vector3.size))
}