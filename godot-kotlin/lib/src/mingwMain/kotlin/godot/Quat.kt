package godot

import godot.internal.godot_quat
import kotlinx.cinterop.*
import kotlin.math.*

class Quat(var x: Float = 0f, var y: Float = 0f, var z: Float = 0f, var w: Float = 0f) {
    internal constructor(raw: CPointer<godot_quat>) : this(
            api.godot_quat_get_x!!(raw),
            api.godot_quat_get_y!!(raw),
            api.godot_quat_get_z!!(raw),
            api.godot_quat_get_w!!(raw)
    )

    internal fun _raw(scope: AutofreeScope): CPointer<godot_quat> {
        val raw = scope.alloc<godot_quat>()
        api.godot_quat_new!!(raw.ptr, x, y, z, w)
        return raw.ptr
    }

    var euler: Vector3
        get() = getEulerYXZ()
        set(v) = setEulerYXZ(v)

    constructor() : this(0f, 0f, 0f, 1f)

    constructor(v0: Vector3, v1: Vector3) : this() {// shortest arc
        val c = v0.cross(v1)
        val d = v0.dot(v1)

        if (d < -1f + CMP_EPSILON) {
            x = 0f
            y = 1f
            z = 0f
            w = 0f
        } else {
            val s = sqrt((1f + d) * 2f)
            val rs = 1f / s

            x = c.x * rs
            y = c.y * rs
            z = c.z * rs
            w = s * 0.5f
        }
    }

    constructor(axis: Vector3, angle: Float) : this() {
        val d = axis.length()
        if (d == 0f)
            set(0f, 0f, 0f, 0f)
        else {
            val sinAngle = sin(angle * 0.5f)
            val cosAngle = cos(angle * 0.5f)
            val s = sinAngle / d
            set(axis.x * s, axis.y * s, axis.z * s,
                    cosAngle)
        }
    }

    fun set(quat: Quat) {
        this.x = quat.x
        this.y = quat.y
        this.z = quat.z
        this.w = quat.w
    }

    fun set(x: Float, y: Float, z: Float, w: Float) {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
    }

    // set_euler_xyz expects a vector containing the Euler angles in the format
// (ax,ay,az), where ax is the angle of rotation around x axis,
// and similar for other axes.
// This implementation uses XYZ convention (Z is the first rotation).
    fun setEulerXYZ(euler: Vector3) {
        val halfA1 = euler.x * 0.5f
        val halfA2 = euler.y * 0.5f
        val halfA3 = euler.z * 0.5f

        // R = X(a1).Y(a2).Z(a3) convention for Euler angles.
        // Conversion to quaternion as listed in https://ntrs.nasa.gov/archive/nasa/casi.ntrs.nasa.gov/19770024290.pdf (page A-2)
        // a3 is the angle of the first rotation, following the notation in this reference.

        val cosA1 = cos(halfA1)
        val sinA1 = sin(halfA1)
        val cosA2 = cos(halfA2)
        val sinA2 = sin(halfA2)
        val cosA3 = cos(halfA3)
        val sinA3 = sin(halfA3)

        set(sinA1 * cosA2 * cosA3 + sinA2 * sinA3 * cosA1,
                -sinA1 * sinA3 * cosA2 + sinA2 * cosA1 * cosA3,
                sinA1 * sinA2 * cosA3 + sinA3 * cosA1 * cosA2,
                -sinA1 * sinA2 * sinA3 + cosA1 * cosA2 * cosA3)
    }

    // get_euler_xyz returns a vector containing the Euler angles in the format
// (ax,ay,az), where ax is the angle of rotation around x axis,
// and similar for other axes.
// This implementation uses XYZ convention (Z is the first rotation).
    fun getEulerXYZ() = Basis(this).getEulerXYZ()

    // set_euler_yxz expects a vector containing the Euler angles in the format
// (ax,ay,az), where ax is the angle of rotation around x axis,
// and similar for other axes.
// This implementation uses YXZ convention (Z is the first rotation).
    fun setEulerYXZ(euler: Vector3) {
        val halfA1 = euler.y * 0.5f
        val halfA2 = euler.x * 0.5f
        val halfA3 = euler.z * 0.5f

        // R = Y(a1).X(a2).Z(a3) convention for Euler angles.
        // Conversion to quaternion as listed in https://ntrs.nasa.gov/archive/nasa/casi.ntrs.nasa.gov/19770024290.pdf (page A-6)
        // a3 is the angle of the first rotation, following the notation in this reference.

        val cosA1 = cos(halfA1)
        val sinA1 = sin(halfA1)
        val cosA2 = cos(halfA2)
        val sinA2 = sin(halfA2)
        val cosA3 = cos(halfA3)
        val sinA3 = sin(halfA3)

        set(sinA1 * cosA2 * sinA3 + cosA1 * sinA2 * cosA3,
                sinA1 * cosA2 * cosA3 - cosA1 * sinA2 * sinA3,
                -sinA1 * sinA2 * cosA3 + cosA1 * sinA2 * sinA3,
                sinA1 * sinA2 * sinA3 + cosA1 * cosA2 * cosA3)
    }

    // get_euler_yxz returns a vector containing the Euler angles in the format
// (ax,ay,az), where ax is the angle of rotation around x axis,
// and similar for other axes.
// This implementation uses YXZ convention (Z is the first rotation).
    fun getEulerYXZ() = Basis(this).getEulerYXZ()

    fun length() = sqrt(lengthSquared())

    fun normalize() {
        set(this / length())
    }

    fun normalized() = copy().apply { normalize() }

    fun inverse() = Quat(-x, -y, -z, w)

    fun slerp(q: Quat, t: Float): Quat {
        val to1 = Quat()
        val omega: Float
        var cosom: Float
        val sinom: Float
        val scale0: Float
        val scale1: Float

        // calc cosine
        cosom = dot(q)

        // adjust signs (if necessary)
        if (cosom < 0f) {
            cosom = -cosom
            to1.x = -q.x
            to1.y = -q.y
            to1.z = -q.z
            to1.w = -q.w
        } else {
            to1.x = q.x
            to1.y = q.y
            to1.z = q.z
            to1.w = q.w
        }

        // calculate coefficients

        if ((1f - cosom) > CMP_EPSILON) {
            // standard case (slerp)
            omega = acos(cosom)
            sinom = sin(omega)
            scale0 = sin((1f - t) * omega) / sinom
            scale1 = sin(t * omega) / sinom
        } else {
            // "from" and "to" quaternions are very close
            //  ... so we can do a linear interpolation
            scale0 = 1f - t
            scale1 = t
        }
        // calculate final values
        return Quat(
                scale0 * x + scale1 * to1.x,
                scale0 * y + scale1 * to1.y,
                scale0 * z + scale1 * to1.z,
                scale0 * w + scale1 * to1.w)
    }

    fun slerpni(q: Quat, t: Float): Quat {
        val from = copy()

        val dot = from.dot(q)

        if (abs(dot) > 0.9999f) return from

        val theta = acos(dot)
        val sinT = 1f / sin(theta)
        val newFactor = sin(t * theta) * sinT
        val invFactor = sin((1f - t) * theta) * sinT

        return Quat(invFactor * from.x + newFactor * q.x,
                invFactor * from.y + newFactor * q.y,
                invFactor * from.z + newFactor * q.z,
                invFactor * from.w + newFactor * q.w)
    }

    fun cubicSlerp(q: Quat, prep: Quat, postq: Quat, t: Float): Quat {
        //the only way to do slerp :|
        val t2 = (1f - t) * t * 2f
        val sp = slerp(q, t)
        val sq = prep.slerpni(postq, t)
        return sp.slerpni(sq, t2)
    }

    fun getAxisAndAngle(): Pair<Vector3, Float> {
        val angle = acos(w) * 2
        val axis = Vector3(
                x / sqrt(1f - w * w),
                y / sqrt(1f - w * w),
                z / sqrt(1f - w * w)
        )
        return axis to angle
    }

    operator fun times(v: Vector3) = Quat(w * v.x + y * v.z - z * v.y,
            w * v.y + z * v.x - x * v.z,
            w * v.z + x * v.y - y * v.x,
            -x * v.x - y * v.y - z * v.z)

    fun xform(v: Vector3) = copy()
            .apply { inverse() }
            .let { Vector3(x, y, z) }

    fun dot(q: Quat) = x * q.x + y * q.y + z * q.z + w * q.w

    fun lengthSquared() = dot(this)

    fun copy() = Quat(x, y, z, w)

    operator fun plus(q: Quat): Quat {
        val copy = copy()
        copy.x += q.x
        copy.y += q.y
        copy.z += q.z
        copy.w += q.w
        return copy
    }

    operator fun minus(q: Quat): Quat {
        val copy = copy()
        copy.x -= q.x
        copy.y -= q.y
        copy.z -= q.z
        copy.w -= q.w
        return copy
    }

    operator fun times(q: Quat): Quat {
        val copy = copy()
        copy.x *= q.x
        copy.y *= q.y
        copy.z *= q.z
        copy.w *= q.w
        return copy
    }

    operator fun times(f: Float): Quat {
        val copy = copy()
        copy.x *= f
        copy.y *= f
        copy.z *= f
        copy.w *= f
        return copy
    }

    operator fun div(f: Float) = this * (1f / f)

    override fun equals(other: Any?) = other is Quat && other.x == x && other.y == y && other.z == z && other.w == w

    override fun hashCode() = arrayOf(x, y, z, w).hashCode()

    override fun toString() = "$x, $y, $z ,$w"
}