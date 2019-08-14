package godot

import godot.internal.godot_node_path
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class NodePath internal constructor(val _raw: CPointer<godot_node_path>) {
    internal constructor(_raw: CValue<godot_node_path>) : this(_raw.place(godot.alloc(godot_node_path.size)))

    constructor() : this(godot.alloc())
}