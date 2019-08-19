package godot

import godot.internal.godot_vector2
import kotlinx.cinterop.*
import kotlin.math.*

class Vector2(var x: Float = 0f, var y: Float = 0f) : Comparable<Vector2> {

    var width: Float
        get() = x
        set(v) {
            x = v
        }

    var height: Float
        get() = y
        set(v) {
            y = v
        }

    internal constructor(raw: CPointer<godot_vector2>) : this(
            api.godot_vector2_get_x!!(raw),
            api.godot_vector2_get_y!!(raw)
    )

    internal fun _raw(scope: AutofreeScope): CPointer<godot_vector2> {
        val raw = scope.alloc<godot_vector2>()
        api.godot_vector2_new!!(raw.ptr, x, y)
        return raw.ptr
    }

    operator fun get(index: Int) = when (index) {
        0 -> x
        1 -> y
        else -> throw IndexOutOfBoundsException("Tried to get index $index from Vector2")
    }

    operator fun get(axis: Axis) = when (axis) {
        Axis.X -> x
        Axis.Y -> y
    }

    operator fun set(index: Int, value: Float) = when (index) {
        0 -> x = value
        1 -> y = value
        else -> throw IndexOutOfBoundsException("Tried to set index $index from Vector2")
    }

    operator fun set(axis: Axis, value: Float) = when (axis) {
        Axis.X -> x = value
        Axis.Y -> y = value
    }

    operator fun plus(v: Vector2) = Vector2(x + v.x, y + v.y)

    operator fun minus(v: Vector2) = Vector2(x - v.x, y - v.y)

    operator fun times(v: Vector2) = Vector2(x * v.x, y * v.y)

    operator fun times(value: Float) = Vector2(x * value, y * value)

    operator fun div(v: Vector2) = Vector2(x / v.x, y / v.y)

    operator fun div(value: Float) = Vector2(x / value, y / value)

    operator fun unaryMinus() = Vector2(-x, -y)

    fun copy() = Vector2(x, y)

    fun set(x: Float = 0f, y: Float = 0f) {
        this.x = x
        this.y = y
    }

    fun set(v: Vector2) {
        this.x = v.x
        this.y = v.y
    }

    override fun compareTo(other: Vector2): Int = if (this == other) 0 else {
        if (x == other.x) {
            if (y < other.y) -1 else 1
        } else {
            if (x < other.x) -1 else 1
        }
    }

    fun normalize() {
        if (x != y) {
            set(this / length())
        }
    }

    fun normalized(): Vector2 {
        val v = copy()
        v.normalize()
        return v
    }

    fun length() = sqrt(x * x + y * y)

    fun lengthSquared() = x * x + y * y

    fun distanceTo(v: Vector2) = sqrt((x - v.x) * (x - v.x) + (y - v.y) * (y - v.y))

    fun distanceSquaredTo(v: Vector2) = (x - v.x) * (x - v.x) + (y - v.y) * (y - v.y)

    fun angleTo(v: Vector2) = atan2(cross(v), dot(v))

    fun angleToPoint(v: Vector2) = atan2(y - v.y, x - v.x)

    fun dot(v: Vector2) = x * v.x + y * v.y

    fun cross(v: Vector2) = x * v.y - y * v.x

    fun cross(value: Float) = Vector2(value * y, -value * x)

    fun project(v: Vector2) = v * (this.dot(v) / v.dot(v))

    fun planeProject(f: Float, v: Vector2) = v - this * (dot(v) - f)

    fun clamped(f: Float): Vector2 {
        val l = length()
        var v = copy()
        if (l > 0 && f < l) {
            v /= l
            v *= f
        }
        return v
    }

    fun linearInterpolate(v: Vector2, f: Float): Vector2 {
        val res = copy()
        res.x += (f * (v.x - x))
        res.y += (f * (v.y - y))
        return res
    }

    fun cubicInterpolate(p_b: Vector2, p_pre_a: Vector2, p_post_b: Vector2, p_t: Float): Vector2 {
        val p0 = p_pre_a
        val p1 = this.copy()
        val p2 = p_b
        val p3 = p_post_b

        val t = p_t
        val t2 = t * t
        val t3 = t2 * t

        return ((p1 * 2f) +
                (-p0 + p2) * t +
                (p0 * 2f - p1 * 5f + p2 * 4f - p3) * t2 +
                (-p0 + p1 * 3f - p2 * 3f + p3) * t3) *
                0.5f
    }

    fun slide(v: Vector2) = v - this * this.dot(v)

    fun reflect(v: Vector2) = v - this * this.dot(v) * 2f

    fun angle() = atan2(y, x)

    fun setRotation(radians: Float) {
        x = cos(radians)
        y = sin(radians)
    }

    fun abs() = Vector2(abs(x), abs(y))

    fun rotated(by: Float): Vector2 {
        var v = Vector2()
        v.setRotation(angle() + by)
        v *= length()
        return v
    }

    fun tangent() = Vector2(y, -x)

    fun floor() = Vector2(floor(x), floor(y))

    fun snapped(by: Vector2) = Vector2(
            if (by.x != 0f) floor(x / by.x + 0.5f) * by.x else x,
            if (by.y != 0f) floor(y / by.y + 0.5f) * by.y else y)

    override fun equals(other: Any?) = other is Vector2 && x == other.x && y == other.y

    override fun hashCode() = arrayOf(x, y).hashCode()

    override fun toString() = "$x, $y"

    enum class Axis { X, Y }

    companion object {
        fun linearInterpolate(v1: Vector2, v2: Vector2, f: Float): Vector2 {
            val res = v1.copy()
            res.x += (f * (v2.x - v1.x))
            res.y += (f * (v2.y - v1.y))
            return res
        }
    }
}