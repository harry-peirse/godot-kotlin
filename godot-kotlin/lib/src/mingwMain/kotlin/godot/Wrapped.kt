package godot

import godot.internal._Wrapped
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.pointed

abstract class Wrapped {
    internal var _wrapped: CPointer<_Wrapped>? = null
    internal val _owner
        get() = _wrapped?.pointed?._owner
    internal val _typeTag
        get() = _wrapped?.pointed?._typeTag

    open fun _init() {}
}