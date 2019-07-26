package godot

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.alloc
import kotlinx.cinterop.free
import kotlinx.cinterop.nativeHeap

abstract class _Wrapped {
    val _owner: COpaquePointer = nativeHeap.alloc()
    fun destory() {
        nativeHeap.free(_owner)
    }
}