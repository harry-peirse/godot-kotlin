package godot

import kotlinx.cinterop.*

abstract class _Wrapped {
    val mbOwner: COpaquePointer = nativeHeap.alloc<COpaquePointerVar>().ptr.reinterpret()
    fun destory() {
        nativeHeap.free(mbOwner)
    }
}