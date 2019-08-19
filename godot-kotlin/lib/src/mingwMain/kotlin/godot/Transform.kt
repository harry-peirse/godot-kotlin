package godot

import godot.internal.godot_transform
import kotlinx.cinterop.*

class Transform(var basis: Basis = Basis(), var origin: Vector3 = Vector3()) {

    internal constructor(raw: CPointer<godot_transform>) : this(
            memScoped { Basis(api.godot_transform_get_basis!!(raw).ptr) },
            memScoped { Vector3(api.godot_transform_get_origin!!(raw).ptr) }
    )

    internal fun _raw(scope: AutofreeScope): CPointer<godot_transform> {
        val raw = scope.alloc<godot_transform>()
        api.godot_transform_new!!(raw.ptr, basis._raw(scope), origin._raw(scope))
        return raw.ptr
    }

    fun inverseXform(t: Transform): Transform {
        val v = t.origin - origin
        return Transform(basis.transposeXform(t.basis), basis.xform(v))
    }

    fun set(transform: Transform) {
        this.basis = transform.basis
        this.origin = transform.origin
    }

    fun set(basis: Basis, origin: Vector3) {
        this.basis = basis
        this.origin = origin
    }

    fun set(xx: Float, xy: Float, xz: Float, yx: Float, yy: Float, yz: Float, zx: Float, zy: Float, zz: Float, tx: Float, ty: Float, tz: Float) {
        basis.elements[0][0] = xx
        basis.elements[0][1] = xy
        basis.elements[0][2] = xz
        basis.elements[1][0] = yx
        basis.elements[1][1] = yy
        basis.elements[1][2] = yz
        basis.elements[2][0] = zx
        basis.elements[2][1] = zy
        basis.elements[2][2] = zz
        origin.x = tx
        origin.y = ty
        origin.z = tz
    }

    fun xform(vector: Vector3) = Vector3(
            basis[0].dot(vector) + origin.x,
            basis[1].dot(vector) + origin.y,
            basis[2].dot(vector) + origin.z)

    fun xformInv(vector: Vector3): Vector3 {
        val v = vector - origin
        return Vector3(
                (basis.elements[0][0] * v.x) + (basis.elements[1][0] * v.y) + (basis.elements[2][0] * v.z),
                (basis.elements[0][1] * v.x) + (basis.elements[1][1] * v.y) + (basis.elements[2][1] * v.z),
                (basis.elements[0][2] * v.x) + (basis.elements[1][2] * v.y) + (basis.elements[2][2] * v.z))
    }

    fun xform(plane: Plane): Plane {
        var point = plane.normal * plane.distance
        var pointDir = point + plane.normal
        point = xform(point)
        pointDir = xform(pointDir)

        val normal = pointDir - point
        normal.normalize()
        val d = normal.dot(point)

        return Plane(normal, d)
    }

    fun xformInv(plane: Plane): Plane {
        var point = plane.normal * plane.distance
        var pointDir = point + plane.normal
        point = xformInv(point)
        pointDir = xformInv(pointDir)

        val normal = pointDir - point
        normal.normalize()
        val d = normal.dot(point)

        return Plane(normal, d)
    }

    fun xform(aabb: AABB): AABB {
        /* define vertices */
        val x = basis.getAxis(0) * aabb.size.x
        val y = basis.getAxis(1) * aabb.size.y
        val z = basis.getAxis(2) * aabb.size.z
        val pos = xform(aabb.position)
        //could be even further optimized
        val newAabb = AABB()
        newAabb.position = pos
        newAabb.expandTo(pos + x)
        newAabb.expandTo(pos + y)
        newAabb.expandTo(pos + z)
        newAabb.expandTo(pos + x + y)
        newAabb.expandTo(pos + x + z)
        newAabb.expandTo(pos + y + z)
        newAabb.expandTo(pos + x + y + z)
        return newAabb
    }

    fun xformInv(aabb: AABB): AABB {
        /* define vertices */
        val vertices = arrayOf(
                Vector3(aabb.position.x + aabb.size.x, aabb.position.y + aabb.size.y, aabb.position.z + aabb.size.z),
                Vector3(aabb.position.x + aabb.size.x, aabb.position.y + aabb.size.y, aabb.position.z),
                Vector3(aabb.position.x + aabb.size.x, aabb.position.y, aabb.position.z + aabb.size.z),
                Vector3(aabb.position.x + aabb.size.x, aabb.position.y, aabb.position.z),
                Vector3(aabb.position.x, aabb.position.y + aabb.size.y, aabb.position.z + aabb.size.z),
                Vector3(aabb.position.x, aabb.position.y + aabb.size.y, aabb.position.z),
                Vector3(aabb.position.x, aabb.position.y, aabb.position.z + aabb.size.z),
                Vector3(aabb.position.x, aabb.position.y, aabb.position.z)
        )

        val ret = AABB()

        ret.position = xformInv(vertices[0])

        for (i in 1 until 8) ret.expandTo(xformInv(vertices[i]))

        return ret
    }

    fun affineInvert() {
        basis.invert()
        origin = basis.xform(-origin)
    }

    fun copy(): Transform = Transform(basis.copy(), origin.copy())

    fun affineInverse(): Transform {
        val ret = copy()
        ret.affineInvert()
        return ret
    }

    fun invert() {
        basis.transpose()
        origin = basis.xform(-origin)
    }

    fun inverse(): Transform {
        // FIXME: this function assumes the basis is a rotation matrix, with no scaling.
        // Transform::affineInverse can handle matrices with scaling, so GDScript should eventually use that.
        val ret = copy()
        ret.invert()
        return ret
    }

    fun rotate(axis: Vector3, phi: Float) {
        set(rotated(axis, phi))
    }

    fun rotated(axis: Vector3, phi: Float) = Transform(Basis(axis, phi), Vector3()) * this

    fun rotateBasis(axis: Vector3, phi: Float) {
        basis.rotate(axis, phi)
    }

    fun lookingAt(target: Vector3, up: Vector3): Transform {
        val t = copy()
        t.setLookAt(origin, target, up)
        return t
    }

    fun setLookAt(eye: Vector3, target: Vector3, up: Vector3) {
        // Reference: MESA source code
        val x: Vector3
        var y: Vector3
        val z: Vector3

        /* Make rotation matrix */

        /* Z vector */
        z = eye - target

        z.normalize()

        y = up

        x = y.cross(z)

        /* Recompute Y = Z cross X */
        y = z.cross(x)

        x.normalize()
        y.normalize()

        basis.setAxis(0, x)
        basis.setAxis(1, y)
        basis.setAxis(2, z)
        origin = eye
    }

    fun interpolateWith(transform: Transform, c: Float): Transform {
        /* not sure if very "efficient" but good enough? */
        val srcScale = basis.getScale()
        val srcRot = basis
        val srcLoc = origin

        val dstScale = transform.basis.getScale()
        val dstRot = transform.basis.toQuat()
        val dstLoc = transform.origin

        val dst = Transform()
        dst.basis = Basis(srcRot.toQuat().slerp(dstRot, c))
        dst.basis.scale(srcScale.linearInterpolate(dstScale, c))
        dst.origin = srcLoc.linearInterpolate(dstLoc, c)

        return dst
    }

    fun scale(scale: Vector3) {
        basis.scale(scale)
        origin *= scale
    }

    fun scaled(scale: Vector3): Transform {
        val t = copy()
        t.scale(scale)
        return t
    }

    fun scaleBasis(scale: Vector3) {
        basis.scale(scale)
    }

    fun translate(x: Float, y: Float, z: Float) {
        translate(Vector3(x, y, z))
    }

    fun translate(translation: Vector3) {
        for (i in 0 until 3) {
            origin[i] += basis[i].dot(translation)
        }
    }

    fun translated(translation: Vector3): Transform {
        val t = copy()
        t.translate(translation)
        return t
    }

    fun orthonormalize() {
        basis.orthonormalize()
    }

    fun orthonormalized(): Transform {
        val copy = copy()
        copy.orthonormalize()
        return copy
    }

    override fun equals(other: Any?) = other is Transform && basis == other.basis && origin == other.origin

    override fun hashCode() = arrayOf(basis, origin).hashCode()

    override fun toString() = "$basis - $origin"

    operator fun times(transform: Transform): Transform {
        val t = copy()
        t.origin = t.xform(transform.origin)
        t.basis *= transform.basis
        return t
    }
}