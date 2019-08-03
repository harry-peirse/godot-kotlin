package godot

import kotlinx.cinterop.*

@UseExperimental(ExperimentalUnsignedTypes::class)
abstract class Wrapped {
    var mbOwner: COpaquePointer = nativeHeap.alloc<COpaquePointerVar>().ptr.reinterpret()
    var typeTag: Int = 0
    fun destroy() {
        nativeHeap.free(mbOwner)
    }
}