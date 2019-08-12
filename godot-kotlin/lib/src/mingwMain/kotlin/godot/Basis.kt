package godot

import godot.internal.godot_basis
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class Basis : CoreType<godot_basis> {
    internal constructor (_wrapped: CPointer<godot_basis>) : super(_wrapped)
    internal constructor(value: CValue<godot_basis>) : super(value.place(godot.alloc(godot_basis.size)))
    internal constructor() : this(godot.alloc(godot_basis.size))
}