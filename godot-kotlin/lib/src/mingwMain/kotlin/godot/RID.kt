package godot

import godot.internal.godot_rid
import kotlinx.cinterop.*

class RID() {
    internal constructor(raw: CPointer<godot_rid>) : this()

    internal fun _raw(scope: AutofreeScope): CPointer<godot_rid> {
        val raw = scope.alloc<godot_rid>()
        api.godot_rid_new!!(raw.ptr)
        return raw.ptr
    }
}