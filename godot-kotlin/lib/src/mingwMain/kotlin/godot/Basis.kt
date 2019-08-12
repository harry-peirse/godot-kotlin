package godot

import godot.internal.godot_basis
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class Basis : CoreType<godot_basis> {
    override val _wrapped: CPointer<godot_basis>

    internal constructor (_wrapped: CPointer<godot_basis>) {
        this._wrapped = _wrapped
    }

    internal constructor(value: CValue<godot_basis>) {
        val _wrapped: CPointer<godot_basis> = godot.alloc(godot_basis.size)
        this._wrapped = value.place(_wrapped)
    }

    internal constructor() : this(godot.alloc(godot_basis.size))
}