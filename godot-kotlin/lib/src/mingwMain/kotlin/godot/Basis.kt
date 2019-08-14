package godot

import godot.internal.godot_basis
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class Basis internal constructor(val _raw: CPointer<godot_basis>) {
    internal constructor(_raw: CValue<godot_basis>) : this(_raw.place(godot.alloc(godot_basis.size)))

    constructor() : this(godot.alloc())
}