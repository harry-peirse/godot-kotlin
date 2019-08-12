package godot

import godot.internal.godot_node_path
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

class NodePath : CoreType<godot_node_path> {
    override val _wrapped: CPointer<godot_node_path>

    internal constructor (_wrapped: CPointer<godot_node_path>) {
        this._wrapped = _wrapped
    }

    internal constructor(value: CValue<godot_node_path>) {
        val _wrapped: CPointer<godot_node_path> = godot.alloc(godot_node_path.size)
        this._wrapped = value.place(_wrapped)
    }

    internal constructor() : this(godot.alloc(godot_node_path.size))
}