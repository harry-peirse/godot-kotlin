package godot

import kotlinx.cinterop.CPointer

abstract class Wrapped {
    internal var _wrapped: CPointer<_Wrapped>? = null
    open fun _init() {}
}