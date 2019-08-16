package godot

import godot.internal.godot_vector3
import godot.internal.godot_vector3_axis
import kotlinx.cinterop.*
import kotlin.math.*

@UseExperimental(ExperimentalUnsignedTypes::class)
class Vector3(var x: Float = 0f, var y: Float = 0f, var z: Float = 0f) : Comparable<Vector3> {
    internal constructor(raw: CPointer<godot_vector3>) : this(
            api.godot_vector3_get_axis!!(raw, godot_vector3_axis.GODOT_VECTOR3_AXIS_X),
            api.godot_vector3_get_axis!!(raw, godot_vector3_axis.GODOT_VECTOR3_AXIS_Y),
            api.godot_vector3_get_axis!!(raw, godot_vector3_axis.GODOT_VECTOR3_AXIS_Z)
    )

    internal fun _raw(scope: AutofreeScope): CPointer<godot_vector3> {
        val raw = scope.alloc<godot_vector3>()
        api.godot_vector3_new!!(raw.ptr, x, y, z)
        return raw.ptr
    }

    operator fun get(index: Int) = when (index) {
        0 -> x
        1 -> y
        2 -> z
        else -> throw IndexOutOfBoundsException("Tried to get index $index from Vector3")
    }

    operator fun get(axis: Axis) = when (axis) {
        Axis.X -> x
        Axis.Y -> y
        Axis.Z -> z
    }

    operator fun set(index: Int, value: Float) = when (index) {
        0 -> x = value
        1 -> y = value
        2 -> z = value
        else -> throw IndexOutOfBoundsException("Tried to set index $index from Vector3")
    }

    operator fun set(axis: Axis, value: Float) = when (axis) {
        Axis.X -> x = value
        Axis.Y -> y = value
        Axis.Z -> z = value
    }

    operator fun plus(v: Vector3) = Vector3(x + v.x, y + v.y, z + v.z)

    operator fun minus(v: Vector3) = Vector3(x - v.x, y - v.y, z - v.z)

    operator fun times(v: Vector3) = Vector3(x * v.x, y * v.y, z * v.z)

    operator fun times(value: Float) = Vector3(x * value, y * value, z * value)

    operator fun div(v: Vector3) = Vector3(x / v.x, y / v.y, z / v.z)

    operator fun div(value: Float) = Vector3(x / value, y / value, z / value)

    operator fun unaryMinus() = Vector3(-x, -y, -z)

    fun copy() = Vector3(x, y, z)

    fun set(v: Vector3) {
        x = v.x
        y = v.y
        z = v.z
    }

    fun set(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
    }

    override fun compareTo(other: Vector3): Int = if (this == other) 0 else {
        if (x == other.x) {
            if (y == other.y)
                if (z < other.z) -1 else 1
            else
                if (y < other.y) -1 else 1
        } else {
            if (x < other.x) -1 else 1
        }
    }

    fun abs() = Vector3(abs(x), abs(y), abs(z))

    fun ceil() = Vector3(ceil(x), ceil(y), ceil(z))

    fun cross(b: Vector3) = Vector3(
            (y * b.z) - (z * b.y),
            (z * b.x) - (x * b.z),
            (x * b.y) - (y * b.x))

    fun linearInterpolate(b: Vector3, t: Float) = Vector3(
            x + (t * (b.x - x)),
            y + (t * (b.y - y)),
            z + (t * (b.z - z)))

    fun cubicInterpolate(b: Vector3, pre_a: Vector3, post_b: Vector3, t: Float): Vector3 {
        val p0 = pre_a
        val p1 = this.copy()
        val p2 = b
        val p3 = post_b

        val t2 = t * t
        val t3 = t2 * t

        return ((p1 * 2f) +
                (-p0 + p2) * t +
                (p0 * 2f - p1 * 5f + p2 * 4f - p3) * t2 +
                (-p0 + p1 * 3f - p2 * 3f + p3) * t3) *
                0.5f
    }

    fun bounce(normal: Vector3) = -reflect(normal)

    fun length() = sqrt(x * x + y * y + z * z)

    fun lengthSquared() = x * x + y * y + z * z

    fun distanceSquaredTo(b: Vector3) = (b - this).lengthSquared()

    fun distanceTo(b: Vector3) = (b - this).length()

    fun dot(b: Vector3) = x * b.x + y * b.y + z * b.z

    fun angleTo(b: Vector3) = atan2(cross(b).length(), dot(b))

    fun floor() = Vector3(floor(x), floor(y), floor(z))

    fun inverse() = Vector3(1f / x, 1f / y, 1f / z)

    fun isNormalized() = abs(lengthSquared() - 1f) < 0.00001f

    fun outer(b: Vector3): Basis {
        val row0 = Vector3(x * b.x, x * b.y, x * b.z)
        val row1 = Vector3(y * b.x, y * b.y, y * b.z)
        val row2 = Vector3(z * b.x, z * b.y, z * b.z)
        return Basis(row0, row1, row2)
    }

    fun maxAxis() =
            if (x < y)
                if (y < z) 2
                else 1
            else
                if (x < z) 2
                else 0

    fun minAxis() =
            if (x < y)
                if (x < z) 0
                else 2
            else
                if (y < z) 1
                else 2

    fun normalize() {
        val l = length()
        if (l == 0f) {
            x = 0f
            y = 0f
            z = 0f
        } else {
            x /= l
            y /= l
            z /= l
        }
    }

    fun normalized(): Vector3 {
        val v = copy()
        v.normalize()
        return v
    }

    fun reflect(by: Vector3) = by - this * this.dot(by) * 2f

    fun rotated(axis: Vector3, phi: Float): Vector3 {
        val v = copy()
        v.rotate(axis, phi)
        return v
    }

    fun rotate(axis: Vector3, phi: Float) {
        val v = Basis(axis, phi).xform(this)
        x = v.x
        y = v.y
        z = v.z
    }

    fun slide(by: Vector3) = by - this * this.dot(by)

    private fun uglyStepify(value: Float, step: Float) =
            if (step != 0f) floor(value / step + 0.5f) * step
            else value

    fun snap(step: Float) {
        x = uglyStepify(x, step)
        y = uglyStepify(y, step)
        z = uglyStepify(z, step)
    }

    fun snapped(by: Float): Vector3 {
        val v = copy()
        v.snap(by)
        return v
    }

    override fun equals(other: Any?) = other is Vector3 && x == other.x && y == other.y && z == other.z

    override fun hashCode() = arrayOf(x, y, z).hashCode()

    override fun toString() = "$x, $y, $z"

    enum class Axis {
        X,
        Y,
        Z;

        internal val value = ordinal.toUInt()

        companion object {
            internal fun byValue(value: UInt) = values()[value.toInt()]
        }
    }
}