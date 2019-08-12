package godot

import godot.internal.godot_aabb
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class AABB : CoreType<godot_aabb> {
    override val _wrapped: CPointer<godot_aabb>

    internal constructor (_wrapped: CPointer<godot_aabb>) {
        this._wrapped = _wrapped
    }

    internal constructor(value: CValue<godot_aabb>) {
        val _wrapped: CPointer<godot_aabb> = godot.alloc(godot_aabb.size)
        this._wrapped = value.place(_wrapped)
    }

    internal constructor() : this(godot.alloc(godot_aabb.size))
}