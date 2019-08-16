package godot

import godot.internal.godot_transform2d
import kotlinx.cinterop.*
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class Transform2D(xx: Float, xy: Float, yx: Float, yy: Float, ox: Float, oy: Float) {

    val elements: Array<Vector2> = arrayOf(
            Vector2(xx, xy),
            Vector2(yx, yy),
            Vector2(ox, oy)
    )

    val scale: Size2
        get() {
            val detSign = if (basisDeterminant() > 0) 1f else -1f
            return Size2(elements[0].length(), elements[1].length()) * detSign
        }

    var origin: Vector2
        get() = elements[2]
        set(value) {
            elements[2] = value
        }

    var rotation: Float
        get() {
            val det = basisDeterminant()
            val m = orthonormalized()
            if (det < 0) {
                m.scaleBasis(Size2(-1f, -1f))
            }
            return atan2(m[0].y, m[0].x)
        }
        set(value) {
            val cr = cos(value)
            val sr = sin(value)
            elements[0][0] = cr
            elements[0][1] = sr
            elements[1][0] = -sr
            elements[1][1] = cr
        }

    constructor(rotation: Float = 0f, position: Vector2 = Vector2()) : this(cos(rotation), sin(rotation), -sin(rotation), cos(rotation), position.x, position.y)

    internal constructor(raw: CPointer<godot_transform2d>) : this(
            api.godot_transform2d_get_rotation!!(raw),
            memScoped { Vector2(api.godot_transform2d_get_origin!!(raw).ptr) }
    )

    internal fun _raw(scope: AutofreeScope): CPointer<godot_transform2d> {
        val raw = scope.alloc<godot_transform2d>()
        api.godot_transform2d_new!!(raw.ptr, rotation, origin._raw(scope))
        return raw.ptr
    }

    fun tdotx(v: Vector2) = elements[0][0] * v.x + elements[1][0] * v.y

    fun tdoty(v: Vector2) = elements[0][1] * v.x + elements[1][1] * v.y

    operator fun get(index: Int) = elements[index]

    operator fun set(index: Int, v: Vector2) {
        elements[index] = v
    }

    fun getAxis(axis: Int) = get(axis)

    fun setAxis(axis: Int, v: Vector2) {
        set(axis, v)
    }

    fun basisXform(v: Vector2) = Vector2(tdotx(v), tdoty(v))

    fun basisXformInv(v: Vector2) = Vector2(elements[0].dot(v), elements[1].dot(v))

    fun xform(v: Vector2) = Vector2(tdotx(v), tdoty(v)) + elements[2]

    fun xformInv(vec: Vector2): Vector2 {
        val v = vec - elements[2]
        return Vector2(elements[0].dot(v), elements[1].dot(v))
    }

    fun xform(rect: Rect2): Rect2 {
        val x = elements[0] * rect.size.x
        val y = elements[1] * rect.size.y
        val position = xform(rect.position)

        val newRect = Rect2()
        newRect.position = position
        newRect.expandTo(position + x)
        newRect.expandTo(position + y)
        newRect.expandTo(position + x + y)
        return newRect
    }

    fun setRotationAndScale(rotation: Float, scale: Size2) {
        elements[0][0] = cos(rotation) * scale.x
        elements[1][1] = cos(rotation) * scale.y
        elements[1][0] = -sin(rotation) * scale.y
        elements[0][1] = sin(rotation) * scale.x
    }

    fun xformInv(rect: Rect2): Rect2 {
        val ends = arrayOf(
                xformInv(rect.position),
                xformInv(Vector2(rect.position.x, rect.position.y + rect.size.y)),
                xformInv(Vector2(rect.position.x + rect.size.x, rect.position.y + rect.size.y)),
                xformInv(Vector2(rect.position.x + rect.size.x, rect.position.y))
        )

        val newRect = Rect2()
        newRect.position = ends[0]
        newRect.expandTo(ends[1])
        newRect.expandTo(ends[2])
        newRect.expandTo(ends[3])

        return newRect
    }

    fun invert() {
        // FIXME: this function assumes the basis is a rotation matrix, with no scaling.
        // Transform2D::affine_inverse can handle matrices with scaling, so GDScript should eventually use that.
        val tmp = elements[1][0]
        elements[1][0] = elements[0][1]
        elements[0][1] = tmp
        elements[2] = basisXform(-elements[2])
    }

    fun copy() = Transform2D(elements[0][0], elements[0][1], elements[1][0], elements[1][1], elements[2][0], elements[2][1])

    fun inverse(): Transform2D {
        val inv = copy()
        inv.invert()
        return inv
    }

    fun affineInvert() {
        val det = basisDeterminant()
        val idet = 1f / det

        val tmp = elements[1][1]
        elements[1][1] = elements[0][0]
        elements[0][0] = tmp

        elements[0] *= Vector2(idet, -idet)
        elements[1] *= Vector2(-idet, idet)

        elements[2] = basisXform(-elements[2])
    }

    fun affineInverse(): Transform2D {
        val inv = copy()
        inv.affineInvert()
        return inv
    }

    fun set(transform: Transform2D) {
        elements[0] = transform.elements[0].copy()
        elements[1] = transform.elements[1].copy()
        elements[2] = transform.elements[2].copy()
    }

    fun rotate(phi: Float) {
        set(Transform2D(phi, Vector2()) * this)
    }

    fun scale(scale: Size2) {
        scaleBasis(scale)
        elements[2] *= scale
    }

    fun scaleBasis(scale: Size2) {
        elements[0][0] *= scale.x
        elements[0][1] *= scale.y
        elements[1][0] *= scale.x
        elements[1][1] *= scale.y
    }

    fun translate(x: Float, y: Float) {
        translate(Vector2(x, y))
    }

    fun translate(translation: Vector2) {
        elements[2] += basisXform(translation)
    }

    fun orthonormalize() {
        val x = elements[0]
        var y = elements[1]

        x.normalize()
        y = (y - x * (x.dot(y)))
        y.normalize()

        elements[0] = x
        elements[1] = y
    }

    fun orthonormalized(): Transform2D {
        val on = copy()
        on.orthonormalize()
        return on
    }

    override fun equals(other: Any?): Boolean {
        if (other is Transform2D) {
            for (i in 0..2) {
                if (elements[i] != other.elements[i]) return false
            }
            return true
        } else return false
    }

    override fun hashCode() = elements.hashCode()

    operator fun times(transform: Transform2D): Transform2D {
        val new = copy()
        new.elements[2] = xform(transform.elements[2])

        val x0 = tdotx(transform.elements[0])
        val x1 = tdoty(transform.elements[0])
        val y0 = tdotx(transform.elements[1])
        val y1 = tdoty(transform.elements[1])

        new.elements[0][0] = x0
        new.elements[0][1] = x1
        new.elements[1][0] = y0
        new.elements[1][1] = y1

        return new
    }

    fun scaled(scale: Size2): Transform2D {
        val copy = copy()
        copy.scale(scale)
        return copy
    }

    fun basisScaled(scale: Size2): Transform2D {
        val copy = copy()
        copy.scaleBasis(scale)
        return copy
    }

    fun untranslated(): Transform2D {
        val copy = copy()
        copy.elements[2] = Vector2()
        return copy
    }

    fun translated(offset: Vector2): Transform2D {
        val copy = copy()
        copy.translate(offset)
        return copy
    }

    fun rotated(phi: Float): Transform2D {
        val copy = copy()
        copy.rotate(phi)
        return copy
    }

    fun basisDeterminant() = elements[0].x * elements[1].y - elements[0].y * elements[1].x

    fun interpolateWith(transform: Transform2D, c: Float): Transform2D {
        //extract parameters
        val p1 = origin
        val p2 = transform.origin

        val r1 = rotation
        val r2 = transform.rotation

        val s1 = scale
        val s2 = transform.scale

        //slerp rotation
        val v1 = Vector2(cos(r1), sin(r1))
        val v2 = Vector2(cos(r2), sin(r2))

        var dot = v1.dot(v2)

        dot = if (dot < -1f) -1f else (if (dot > 1f) 1f else dot)

        val v: Vector2

        if (dot > 0.9995f) {
            v = Vector2.linearInterpolate(v1, v2, c).normalized() //linearly interpolate to avoid numerical precision issues
        } else {
            val angle = c * acos(dot)
            val v3 = (v2 - v1 * dot).normalized()
            v = v1 * cos(angle) + v3 * sin(angle)
        }

        //construct matrix
        val res = Transform2D(atan2(v.y, v.x), Vector2.linearInterpolate(p1, p2, c))
        res.scaleBasis(Vector2.linearInterpolate(s1, s2, c))
        return res
    }

    override fun toString() = "${elements[0]}, ${elements[1]}, ${elements[2]}"
}