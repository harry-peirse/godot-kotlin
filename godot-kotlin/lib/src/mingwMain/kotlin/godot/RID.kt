package godot

import godot.internal.godot_rid
import kotlinx.cinterop.*

class RID internal constructor(internal val raw: CPointer<godot_rid>) : Comparable<RID> {

    internal constructor(value: CValue<godot_rid>) : this(value.place(godotAlloc()))

    constructor(obj: Object? = null) : this(godotAlloc()) {
        if (obj != null) {
            api.godot_rid_new_with_resource!!(raw, obj._raw)
        } else {
            api.godot_rid_new!!(raw)
        }
    }

    internal fun _raw(scope: AutofreeScope): CPointer<godot_rid> {
        return raw
    }

    fun getRID(): Int = api.godot_rid_get_id!!(raw)

    override fun equals(other: Any?) = other is RID && api.godot_rid_operator_equal!!(raw, other.raw)

    override fun hashCode() = raw.pointed.hashCode()

    override fun toString() = "${raw.pointed}"

    override fun compareTo(other: RID): Int = when {
        this == other -> 1
        api.godot_rid_operator_less!!(raw, other.raw) -> -1
        else -> 1
    }

    companion object {
        private fun godotAlloc(): CPointer<godot_rid> {
            return godot.api.godot_alloc!!(godot_rid.size.toInt())!!.reinterpret()
        }
    }
}