package godot

import godot.internal.godot_vector3
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue

@UseExperimental(ExperimentalUnsignedTypes::class)
class Vector3 internal constructor(val _raw: CPointer<godot_vector3>) {
    internal constructor(_raw: CValue<godot_vector3>) : this(_raw.place(godot.alloc(godot_vector3.size)))

    enum class Axis {
        X,
        Y,
        Z;

        val value = ordinal.toUInt()

        companion object {
            fun byValue(value: UInt) = values()[value.toInt()]
        }
    }
}