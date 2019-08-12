package godot

import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer

abstract class CoreType<T : CPointed>(internal val _wrapped: CPointer<T>)