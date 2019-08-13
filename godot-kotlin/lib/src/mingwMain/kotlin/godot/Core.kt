package godot

import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer

interface Core<T : CPointed> {
    val _raw: CPointer<T>
}