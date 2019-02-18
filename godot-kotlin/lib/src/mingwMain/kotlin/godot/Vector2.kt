package godot

import godotapi.godot_variant
import godotapi.godot_vector2
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.invoke
import kotlinx.cinterop.reinterpret
import platform.posix.FLT_EPSILON
import kotlin.math.*

operator fun Float.times(vector: Vector2): Vector2 = vector * this

fun stepify(value: Float, step: Float): Float = if (step == 0f) value else floor(value / step + 0.5f)

typealias Point2 = Vector2
typealias Size2 = Vector2

class Vector2(x: Float = 0f, y: Float = 0f) : Comparable<Vector2> {
    var x: Float
        get() = api.c.godot_vector2_get_x!!(native)
        set(value) = api.c.godot_vector2_set_x!!(native, value)

    var y: Float
        get() = api.c.godot_vector2_get_y!!(native)
        set(value) = api.c.godot_vector2_set_y!!(native, value)

    var width
        get() = x
        set(v) {
            x = v
        }

    var height
        get() = y
        set(v) {
            y = v
        }

    private val native: CPointer<godot_vector2> =
        api.c.godot_alloc!!(godot_vector2.size.toInt())!!.reinterpret()

    init {
        val nativeVariant: CPointer<godot_variant> =
            api.c.godot_alloc!!(godot_variant.size.toInt())!!.reinterpret()
        api.c.godot_variant_new_vector2!!(nativeVariant, native)

        this.x = x
        this.y = y
    }

    operator fun plus(vector: Vector2): Vector2 = Vector2(x + vector.x, y + vector.y)

    operator fun plusAssign(vector: Vector2) {
        x += vector.x
        y += vector.y
    }

    operator fun minus(vector: Vector2): Vector2 = Vector2(x - vector.x, y - vector.y)

    operator fun minusAssign(vector: Vector2) {
        x -= vector.x
        y -= vector.y
    }

    operator fun times(scalar: Float): Vector2 = Vector2(x * scalar, y * scalar)

    operator fun times(vector: Vector2): Vector2 = Vector2(x * vector.x, y * vector.y)

    operator fun timesAssign(vector: Vector2) {
        x *= vector.x
        y *= vector.y
    }

    operator fun timesAssign(scalar: Float) {
        x *= scalar
        y *= scalar
    }

    operator fun div(scalar: Float): Vector2 = Vector2(x / scalar, y / scalar)

    operator fun div(vector: Vector2): Vector2 = Vector2(x / vector.x, y / vector.y)

    operator fun divAssign(vector: Vector2) {
        x /= vector.x
        y /= vector.y
    }

    operator fun divAssign(scalar: Float) {
        x /= scalar
        y /= scalar
    }

    operator fun unaryMinus(): Vector2 = this * -1f

    operator fun get(index: Int): Float? = if (index == 0) x else if (index == 1) y else null

    operator fun set(index: Int, value: Float) {
        if (index == 0) x = value
        else if (index == 1) y = value
    }

    override operator fun compareTo(other: Vector2): Int = lengthSquared().compareTo(other.lengthSquared())

    override fun toString(): String = "$x, $y"

    override fun hashCode(): Int = (x * 17 + y * 17).toInt()

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Vector2) return false
        return other.x == x && other.y == y
    }

    fun clone(func: Vector2.() -> Unit = {}): Vector2 {
        val vector = Vector2(x, y)
        vector.func()
        return vector
    }

    fun abs(): Vector2 = Vector2(kotlin.math.abs(x), kotlin.math.abs(y))

    fun angle(): Float = atan2(y, x)

    fun angleTo(vector: Vector2): Float = atan2(cross(vector), dot(vector))

    fun angleToPoint(vector: Vector2): Float = atan2(y - vector.y, x - vector.x)

    fun aspect(): Float = x / y

    fun bounce(vector: Vector2): Vector2 = -reflect(vector)

    fun planeProject(value: Float, vector: Vector2): Vector2 = vector - this * (dot(vector) - value)

    fun clamp(value: Float) {
        val length = length()
        val v = this
        if (length > 0 && value < length) {
            v /= length
            v *= value
        }
    }

    fun clamped(value: Float): Vector2 = clone { clamp(value) }

    fun cubicInterpolate(vector1: Vector2, vector2: Vector2, vector3: Vector2, value: Float): Vector2 =
        0.5f * ((this * 2f)
                + (-vector1 + vector2) * value
                + (2f * vector1 - 5f * this + 4f * vector2 - vector3) * (value * value)
                + (-vector1 + 3f * this - 3f * vector2 + vector3) * (value * value * value))

    fun distanceSquaredTo(vector: Vector2): Float = (x - vector.x) * (x - vector.x) + (y - vector.y) * (y - vector.y)

    fun distanceTo(vector: Vector2): Float = sqrt(distanceSquaredTo(vector))

    fun dot(vector: Vector2): Float = x * vector.x + y * vector.y

    fun cross(vector: Vector2): Float = x * vector.y - y * vector.x

    fun floor(): Vector2 = Vector2(floor(x), floor(y))

    fun ceil(): Vector2 = Vector2(ceil(x), ceil(y))

    fun rount(): Vector2 = Vector2(round(x), round(y))

    fun isNormalized(): Boolean = abs(lengthSquared() - 1f) < FLT_EPSILON

    fun length(): Float = sqrt(lengthSquared())

    fun lengthSquared(): Float = x * x + y * y

    fun linearInterpolate(vector: Vector2, value: Float): Vector2 = clone { this += value * (vector - this) }

    fun linearInterpolate(vector1: Vector2, vector2: Vector2, value: Float): Vector2 =
        clone { this += (value * (vector2 - vector1)) }

    fun slerp(vector: Vector2, value: Float): Vector2 = rotated(angleTo(vector) * value)

    fun normalize() {
        val lengthSquared = lengthSquared()
        if (lengthSquared != 0f) {
            val length = length()
            x /= length
            y /= length
        }
    }

    fun normalized(): Vector2 = clone { normalize() }

    fun tangent(): Vector2 = Vector2(y, -x)

    fun reflect(normal: Vector2): Vector2 = 2f * normal * dot(normal) - this

    fun project(vector: Vector2): Vector2 = vector * (dot(vector) / vector.lengthSquared())

    fun rotated(radians: Float): Vector2 = clone { rotate(radians) }

    fun rotate(radians: Float) {
        val length = length()
        val angle = angle()
        x = cos(angle + radians)
        y = sin(angle + radians)
        this *= length
    }

    fun slide(normal: Vector2): Vector2 = this - normal * dot(normal)

    fun snap(vector: Vector2) {
        x = stepify(x, vector.x)
        y = stepify(y, vector.y)
    }

    fun snapped(vector: Vector2): Vector2 = clone { snap(vector) }
}