package godot

import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.invoke

abstract class CoreType<T : CPointed>(internal val _wrapped: CPointer<T>) {
    var _destroyed: Boolean = false
        private set

    fun destroy() {
        _destroyed = true
        godot.api.godot_free!!(_wrapped)
    }
}