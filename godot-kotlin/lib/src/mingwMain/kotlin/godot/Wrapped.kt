package godot

import kotlinx.cinterop.*

@UseExperimental(ExperimentalUnsignedTypes::class)
abstract class Wrapped {
    var _instanceBindingData: COpaquePointer? = null
    var _userData: COpaquePointer? = null
    var _owner: COpaquePointer? = null
    var _typeTag: Int = 0
    open fun _init() {}
}