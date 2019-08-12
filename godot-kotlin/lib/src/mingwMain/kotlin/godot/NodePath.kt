package godot

import godot.internal.godot_node_path
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class NodePath : CoreType<godot_node_path> {
    internal constructor (_wrapped: CPointer<godot_node_path>) : super(_wrapped)
    internal constructor(value: CValue<godot_node_path>) : super(value.place(godot.alloc(godot_node_path.size)))
    internal constructor() : this(godot.alloc(godot_node_path.size))
}