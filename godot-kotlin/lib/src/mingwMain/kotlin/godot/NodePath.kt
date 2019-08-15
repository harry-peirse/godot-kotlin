package godot

import godot.internal.godot_node_path
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.reinterpret

fun String.toNodePath(): NodePath = NodePath(this)

class NodePath internal constructor(internal val raw: CPointer<godot_node_path>) {

    constructor(from: String = "") : this(godotAlloc()) {
        memScoped {
            godot.api.godot_node_path_new!!(raw, from.toGString(this))
        }
    }

    constructor(other: NodePath) : this(other.toString())

    fun getName(index: Int): String = memScoped {
        godot.api.godot_node_path_get_name!!(raw, index).ptr.toKString()
    }

    fun getNameCount(): Int = godot.api.godot_node_path_get_name_count!!(raw)

    fun getSubname(index: Int): String = memScoped {
        godot.api.godot_node_path_get_subname!!(raw, index).ptr.toKString()
    }

    fun getSubnameCount(): Int = godot.api.godot_node_path_get_subname_count!!(raw)

    fun isAbsolute(): Boolean = godot.api.godot_node_path_is_absolute!!(raw)

    fun isEmpty(): Boolean = godot.api.godot_node_path_is_empty!!(raw)

    override fun toString(): String = memScoped {
        godot.api.godot_node_path_as_string!!(raw).ptr.toKString()
    }

    override fun hashCode(): Int = toString().hashCode()

    override fun equals(other: Any?) = other is NodePath && godot.api.godot_node_path_operator_equal!!(raw, other.raw)

    fun dispose() {
        api.godot_node_path_destroy!!(raw)
    }

    companion object {
        private fun godotAlloc(): CPointer<godot_node_path> {
            return godot.api.godot_alloc!!(godot_node_path.size.toInt())!!.reinterpret()
        }
    }
}