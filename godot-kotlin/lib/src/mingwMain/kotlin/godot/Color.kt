package godot

import godot.internal.godot_color
import kotlinx.cinterop.*

class Color(var r: Float = 0f, var g: Float = 0f, var b: Float = 0f, var a: Float = 1f) {
    internal constructor(raw: CPointer<godot_color>) : this(
            api.godot_color_get_r!!(raw),
            api.godot_color_get_g!!(raw),
            api.godot_color_get_b!!(raw),
            api.godot_color_get_a!!(raw)
    )

    internal fun _raw(scope: AutofreeScope): CPointer<godot_color> {
        val raw = scope.alloc<godot_color>()
        api.godot_color_new_rgba!!(raw.ptr, r, g, b, a)
        return raw.ptr
    }

    operator fun get(index: Int) = when (index) {
        0 -> r
        1 -> g
        2 -> b
        3 -> a
        else -> throw IndexOutOfBoundsException("Tried to get index $index from Color")
    }

    operator fun set(index: Int, value: Float) = when (index) {
        0 -> r = value
        1 -> g = value
        2 -> b = value
        3 -> a = value
        else -> throw IndexOutOfBoundsException("Tried to set index $index from Color")
    }

    operator fun get(bit: Bit) = when (bit) {
        Bit.R -> r
        Bit.G -> g
        Bit.B -> b
        Bit.A -> a
    }

    operator fun set(bit: Bit, value: Float) = when (bit) {
        Bit.R -> r = value
        Bit.G -> g = value
        Bit.B -> b = value
        Bit.A -> a = value
    }

    override fun equals(other: Any?) = other is Color && r == other.r && g == other.g && b == other.b && a == other.a

    override fun hashCode() = arrayOf(r, g, b, a).hashCode()

    override fun toString() = "$r, $g, $b, $a"

    enum class Bit { R, G, B, A }
}