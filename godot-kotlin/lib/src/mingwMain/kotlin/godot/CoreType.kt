package godot

import godot.internal.godot_variant
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.invoke

abstract class CoreType<T : CPointed>(internal val _variant: CPointer<godot_variant>) {

}