package godot

import godot.internal.godot_basis
import kotlinx.cinterop.*
import kotlin.math.*

class Basis(x: Vector3 = Vector3(1f, 0f, 0f),
            y: Vector3 = Vector3(0f, 1f, 0f),
            z: Vector3 = Vector3(0f, 0f, 1f)) {

    val elements = arrayOf(x, y, z)

    var x: Vector3
        get() = elements[0]
        set(v) {
            elements[0] = v
        }
    var y: Vector3
        get() = elements[1]
        set(v) {
            elements[1] = v
        }
    var z: Vector3
        get() = elements[2]
        set(v) {
            elements[2] = v
        }

    var euler: Vector3
        get() = getEulerYXZ()
        set(value) = setEulerXYZ(value)

    internal constructor(raw: CPointer<godot_basis>) : this(
            memScoped { Vector3(api.godot_basis_get_axis!!(raw, 0).ptr) },
            memScoped { Vector3(api.godot_basis_get_axis!!(raw, 1).ptr) },
            memScoped { Vector3(api.godot_basis_get_axis!!(raw, 2).ptr) }
    )

    constructor(axis: Vector3, phi: Float) : this() {
        val axisSquared = Vector3(axis.x * axis.x, axis.y * axis.y, axis.z * axis.z)

        val cosine = cos(phi)
        val sine = sin(phi)

        x.x = axisSquared.x + cosine * (1f - axisSquared.x)
        x.y = axis.x * axis.y * (1f - cosine) - axis.z * sine
        x.z = axis.z * axis.x * (1f - cosine) + axis.y * sine

        y.x = axis.x * axis.y * (1f - cosine) + axis.z * sine
        y.y = axisSquared.y + cosine * (1f - axisSquared.y)
        y.z = axis.y * axis.z * (1f - cosine) - axis.x * sine

        z.x = axis.z * axis.x * (1f - cosine) - axis.y * sine
        z.y = axis.y * axis.z * (1f - cosine) + axis.x * sine
        z.z = axisSquared.z + cosine * (1f - axisSquared.z)
    }

    constructor(xx: Float, xy: Float, xz: Float, yx: Float, yy: Float, yz: Float, zx: Float, zy: Float, zz: Float) : this(
            Vector3(xx, xy, xz),
            Vector3(yx, yy, yz),
            Vector3(zx, zy, zz)
    )

    constructor() : this(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f)

    internal fun _raw(scope: AutofreeScope): CPointer<godot_basis> {
        val raw = scope.alloc<godot_basis>()
        api.godot_basis_new_with_rows!!(raw.ptr, x._raw(scope), y._raw(scope), z._raw(scope))
        return raw.ptr
    }

    operator fun get(axis: Int) = elements[axis]

    fun set(basis: Basis) = set(basis[0][0], basis[0][1], basis[0][2], basis[1][0], basis[1][1], basis[1][2], basis[2][0], basis[2][1], basis[2][2])

    fun set(xx: Float, xy: Float, xz: Float, yx: Float, yy: Float, yz: Float, zx: Float, zy: Float, zz: Float) {
        elements[0][0] = xx
        elements[0][1] = xy
        elements[0][2] = xz
        elements[1][0] = yx
        elements[1][1] = yy
        elements[1][2] = yz
        elements[2][0] = zx
        elements[2][1] = zy
        elements[2][2] = zz
    }

    private fun cofac(row1: Int, col1: Int, row2: Int, col2: Int) = (elements[row1][col1] * elements[row2][col2] - elements[row1][col2] * elements[row2][col1])

    fun invert() {
        val co = arrayOf(
                cofac(1, 1, 2, 2),
                cofac(1, 2, 2, 0),
                cofac(1, 0, 2, 1)
        )
        val det = elements[0][0] * co[0] +
                elements[0][1] * co[1] +
                elements[0][2] * co[2]

        if (det == 0f) {
            printError("det == 0f", "invert", "Basis")
            throw IllegalStateException("det == 0")
        }

        val s = 1f / det

        set(co[0] * s, cofac(0, 2, 2, 1) * s, cofac(0, 1, 1, 2) * s,
                co[1] * s, cofac(0, 0, 2, 2) * s, cofac(0, 2, 1, 0) * s,
                co[2] * s, cofac(0, 1, 2, 0) * s, cofac(0, 0, 1, 1) * s)
    }

    fun isEqualApprox(a: Basis, b: Basis): Boolean {
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (abs(a.elements[i][j] - b.elements[i][j]) >= CMP_EPSILON) return false
            }
        }

        return true
    }

    fun copy() = Basis(x.copy(), y.copy(), z.copy())

    fun isOrthogonal(): Boolean {
        val id = Basis()
        val m = copy() * transposed()

        return isEqualApprox(id, m)
    }

    fun isRotation() = abs(determinant() - 1) < CMP_EPSILON && isOrthogonal()

    fun transpose() {
        var tmp = elements[0][1]
        elements[0][1] = elements[1][0]
        elements[1][0] = tmp

        tmp = elements[0][2]
        elements[0][2] = elements[2][0]
        elements[2][0] = tmp

        tmp = elements[1][2]
        elements[1][2] = elements[2][1]
        elements[2][1] = tmp
    }

    fun inverse(): Basis {
        val b = copy()
        b.invert()
        return b
    }

    fun transposed(): Basis {
        val b = copy()
        b.transpose()
        return b
    }

    fun determinant() = elements[0][0] * (elements[1][1] * elements[2][2] - elements[2][1] * elements[1][2]) -
            elements[1][0] * (elements[0][1] * elements[2][2] - elements[2][1] * elements[0][2]) +
            elements[2][0] * (elements[0][1] * elements[1][2] - elements[1][1] * elements[0][2])

    fun getAxis(axis: Int) = Vector3(elements[0][axis], elements[1][axis], elements[2][axis])

    fun setAxis(axis: Int, value: Vector3) {
        // get actual basis axis (elements is transposed for performance)
        elements[0][axis] = value.x
        elements[1][axis] = value.y
        elements[2][axis] = value.z
    }

    fun rotate(axis: Vector3, phi: Float) {
        set(rotated(axis, phi))
    }

    fun rotated(axis: Vector3, phi: Float): Basis {
        return Basis(axis, phi) * this
    }

    fun scale(scale: Vector3) {
        elements[0][0] *= scale.x
        elements[0][1] *= scale.x
        elements[0][2] *= scale.x
        elements[1][0] *= scale.y
        elements[1][1] *= scale.y
        elements[1][2] *= scale.y
        elements[2][0] *= scale.z
        elements[2][1] *= scale.z
        elements[2][2] *= scale.z
    }

    fun scaled(scale: Vector3): Basis {
        val b = copy()
        b.scale(scale)
        return b
    }

    fun getScale(): Vector3 {
        // We are assuming M = R.S, and performing a polar decomposition to extract R and S.
        // FIXME: We eventually need a proper polar decomposition.
        // As a cheap workaround until then, to ensure that R is a proper rotation matrix with determinant +1
        // (such that it can be represented by a Quat or Euler angles), we absorb the sign flip into the scaling matrix.
        // As such, it works in conjuction with get_rotation().
        val detSign = if (determinant() > 0f) 1f else -1f
        return Vector3(
                Vector3(elements[0][0], elements[1][0], elements[2][0]).length(),
                Vector3(elements[0][1], elements[1][1], elements[2][1]).length(),
                Vector3(elements[0][2], elements[1][2], elements[2][2]).length()) * detSign
    }

    // get_euler_xyz returns a vector containing the Euler angles in the format
// (a1,a2,a3), where a3 is the angle of the first rotation, and a1 is the last
// (following the convention they are commonly defined in the literature).
//
// The current implementation uses XYZ convention (Z is the first rotation),
// so euler.z is the angle of the (first) rotation around Z axis and so on,
//
// And thus, assuming the matrix is a rotation matrix, this function returns
// the angles in the decomposition R = X(a1).Y(a2).Z(a3) where Z(a) rotates
// around the z-axis by a and so on.
    fun getEulerXYZ(): Vector3 {
        // Euler angles in XYZ convention.
        // See https://en.wikipedia.org/wiki/Euler_angles#Rotation_matrix
        //
        // rot =  cy*cz          -cy*sz           sy
        //        cz*sx*sy+cx*sz  cx*cz-sx*sy*sz -cy*sx
        //       -cx*cz*sy+sx*sz  cz*sx+cx*sy*sz  cx*cy

        val euler = Vector3()

        if (!isRotation()) {
            printWarning("is not rotation", "getEulerXYZ", "Basis")
            throw IllegalStateException("is not rotation")
        }

        val sy = elements[0][2]
        if (sy < 1f) {
            if (sy > -1f) {
                // is this a pure Y rotation?
                if (elements[1][0] == 0f && elements[0][1] == 0f && elements[1][2] == 0f && elements[2][1] == 0f && elements[1][1] == 1f) {
                    // return the simplest form (human friendlier in editor and scripts)
                    euler.x = 0f
                    euler.y = atan2(elements[0][2], elements[0][0])
                    euler.z = 0f
                } else {
                    euler.x = atan2(-elements[1][2], elements[2][2])
                    euler.y = asin(sy)
                    euler.z = atan2(-elements[0][1], elements[0][0])
                }
            } else {
                euler.x = -atan2(elements[0][1], elements[1][1])
                euler.y = -Math_PI / 2f
                euler.z = 0f
            }
        } else {
            euler.x = atan2(elements[0][1], elements[1][1])
            euler.y = Math_PI / 2f
            euler.z = 0f
        }
        return euler
    }

    // set_euler_xyz expects a vector containing the Euler angles in the format
// (ax,ay,az), where ax is the angle of rotation around x axis,
// and similar for other axes.
// The current implementation uses XYZ convention (Z is the first rotation).
    fun setEulerXYZ(euler: Vector3) {
        var c: Float
        var s: Float

        c = cos(euler.x)
        s = sin(euler.x)
        val xmat = Basis(1f, 0f, 0f, 0f, c, -s, 0f, s, c)

        c = cos(euler.y)
        s = sin(euler.y)
        val ymat = Basis(c, 0f, s, 0f, 1f, 0f, -s, 0f, c)

        c = cos(euler.z)
        s = sin(euler.z)
        val zmat = Basis(c, -s, 0f, s, c, 0f, 0f, 0f, 1f)

        //optimizer will optimize away all this anyway
        set(xmat * (ymat * zmat))
    }

    // get_euler_yxz returns a vector containing the Euler angles in the YXZ convention,
// as in first-Z, then-X, last-Y. The angles for X, Y, and Z rotations are returned
// as the x, y, and z components of a Vector3 respectively.
    fun getEulerYXZ(): Vector3 {
        // Euler angles in YXZ convention.
        // See https://en.wikipedia.org/wiki/Euler_angles#Rotation_matrix
        //
        // rot =  cy*cz+sy*sx*sz    cz*sy*sx-cy*sz        cx*sy
        //        cx*sz             cx*cz                 -sx
        //        cy*sx*sz-cz*sy    cy*cz*sx+sy*sz        cy*cx

        val euler = Vector3()

        if (!isRotation()) {
            printWarning("is not rotation", "getEulerYXZ", "Basis")
            throw IllegalStateException("is not rotation")
        }

        val m12 = elements[1][2]

        if (m12 < 1) {
            if (m12 > -1) {
                // is this a pure X rotation?
                if (elements[1][0] == 0f && elements[0][1] == 0f && elements[0][2] == 0f && elements[2][0] == 0f && elements[0][0] == 1f) {
                    // return the simplest form (human friendlier in editor and scripts)
                    euler.x = atan2(-m12, elements[1][1])
                    euler.y = 0f
                    euler.z = 0f
                } else {
                    euler.x = asin(-m12)
                    euler.y = atan2(elements[0][2], elements[2][2])
                    euler.z = atan2(elements[1][0], elements[1][1])
                }
            } else { // m12 == -1
                euler.x = Math_PI * 0.5f
                euler.y = -atan2(-elements[0][1], elements[0][0])
                euler.z = 0f
            }
        } else { // m12 == 1
            euler.x = -Math_PI * 0.5f
            euler.y = -atan2(-elements[0][1], elements[0][0])
            euler.z = 0f
        }

        return euler
    }

    // set_euler_yxz expects a vector containing the Euler angles in the format
// (ax,ay,az), where ax is the angle of rotation around x axis,
// and similar for other axes.
// The current implementation uses YXZ convention (Z is the first rotation).
    fun setEulerYXZ(euler: Vector3) {
        var c: Float
        var s: Float

        c = cos(euler.x)
        s = sin(euler.x)
        val xmat = Basis(1f, 0f, 0f, 0f, c, -s, 0f, s, c)

        c = cos(euler.y)
        s = sin(euler.y)
        val ymat = Basis(c, 0f, s, 0f, 1f, 0f, -s, 0f, c)

        c = cos(euler.z)
        s = sin(euler.z)
        val zmat = Basis(c, -s, 0f, s, c, 0f, 0f, 0f, 1f)

        //optimizer will optimize away all this anyway
        set(ymat * xmat * zmat)
    }

    // transposed dot products
    fun tdotx(v: Vector3) = elements[0][0] * v[0] + elements[1][0] * v[1] + elements[2][0] * v[2]

    fun tdoty(v: Vector3) = elements[0][1] * v[0] + elements[1][1] * v[1] + elements[2][1] * v[2]

    fun tdotz(v: Vector3) = elements[0][2] * v[0] + elements[1][2] * v[1] + elements[2][2] * v[2]

    override fun equals(other: Any?) = other is Basis && x == other.x && y == other.y && z == other.z

    fun xform(vector: Vector3) = Vector3(
            x.dot(vector),
            y.dot(vector),
            x.dot(vector))

    fun xformInv(vector: Vector3) = Vector3(
            (elements[0][0] * vector.x) + (elements[1][0] * vector.y) + (elements[2][0] * vector.z),
            (elements[0][1] * vector.x) + (elements[1][1] * vector.y) + (elements[2][1] * vector.z),
            (elements[0][2] * vector.x) + (elements[1][2] * vector.y) + (elements[2][2] * vector.z))

    operator fun times(matrix: Basis) = Basis(matrix.tdotx(elements[0]), matrix.tdoty(elements[0]), matrix.tdotz(elements[0]),
            matrix.tdotx(elements[1]), matrix.tdoty(elements[1]), matrix.tdotz(elements[1]),
            matrix.tdotx(elements[2]), matrix.tdoty(elements[2]), matrix.tdotz(elements[2]))

    operator fun plus(matrix: Basis) = Basis(
            elements[0] + matrix.elements[0],
            elements[1] + matrix.elements[1],
            elements[2] + matrix.elements[2])

    operator fun minus(matrix: Basis) = Basis(
            elements[0] - matrix.elements[0],
            elements[1] - matrix.elements[1],
            elements[2] - matrix.elements[2])

    operator fun times(value: Float) = Basis(
            elements[0] * value,
            elements[1] * value,
            elements[2] * value)

    override fun hashCode() = elements.hashCode()

    override fun toString() = "$x, $y, $z"

    fun getColumn(i: Int) = Vector3(elements[0][i], elements[1][i], elements[2][i])

    fun getRow(i: Int) = Vector3(elements[i][0], elements[i][1], elements[i][2])

    fun getMainDiagonal() = Vector3(elements[0][0], elements[1][1], elements[2][2])

    fun setRow(i: Int, row: Vector3) {
        elements[i][0] = row.x
        elements[i][1] = row.y
        elements[i][2] = row.z
    }

    fun transposeXform(m: Basis) = Basis(
            elements[0].x * m[0].x + elements[1].x * m[1].x + elements[2].x * m[2].x,
            elements[0].x * m[0].y + elements[1].x * m[1].y + elements[2].x * m[2].y,
            elements[0].x * m[0].z + elements[1].x * m[1].z + elements[2].x * m[2].z,
            elements[0].y * m[0].x + elements[1].y * m[1].x + elements[2].y * m[2].x,
            elements[0].y * m[0].y + elements[1].y * m[1].y + elements[2].y * m[2].y,
            elements[0].y * m[0].z + elements[1].y * m[1].z + elements[2].y * m[2].z,
            elements[0].z * m[0].x + elements[1].z * m[1].x + elements[2].z * m[2].x,
            elements[0].z * m[0].y + elements[1].z * m[1].y + elements[2].z * m[2].y,
            elements[0].z * m[0].z + elements[1].z * m[1].z + elements[2].z * m[2].z)

    fun orthonormalize() {
        if (determinant() == 0f) {
            printError("determinant == 0f", "orthonormalize", "Basis")
            throw IllegalStateException("determinant == 0f")
        }

        // Gram-Schmidt Process
        var x = getAxis(0)
        var y = getAxis(1)
        var z = getAxis(2)

        x.normalize()
        y = (y - x * (x.dot(y)))
        y.normalize()
        z = (z - x * (x.dot(z)) - y * (y.dot(z)))
        z.normalize()

        setAxis(0, x)
        setAxis(1, y)
        setAxis(2, z)
    }

    fun orthonormalized() = copy().apply { orthonormalize() }

    fun isSymmetric(): Boolean {
        if (abs(elements[0][1] - elements[1][0]) > CMP_EPSILON)
            return false
        if (abs(elements[0][2] - elements[2][0]) > CMP_EPSILON)
            return false
        if (abs(elements[1][2] - elements[2][1]) > CMP_EPSILON)
            return false

        return true
    }

    fun diagonalize(): Basis {
        if (!isSymmetric()) return Basis()

        val iteMax = 1024

        var offMatrixNorm2 = elements[0][1] * elements[0][1] + elements[0][2] * elements[0][2] + elements[1][2] * elements[1][2]

        var ite = 0
        var accRot = Basis()
        while (offMatrixNorm2 > CMP_EPSILON2 && ite++ < iteMax) {
            val el012 = elements[0][1] * elements[0][1]
            val el022 = elements[0][2] * elements[0][2]
            val el122 = elements[1][2] * elements[1][2]
            // Find the pivot element
            var i: Int
            var j: Int
            if (el012 > el022) {
                if (el122 > el012) {
                    i = 1
                    j = 2
                } else {
                    i = 0
                    j = 1
                }
            } else {
                if (el122 > el022) {
                    i = 1
                    j = 2
                } else {
                    i = 0
                    j = 2
                }
            }

            // Compute the rotation angle
            val angle = if (abs(elements[j][j] - elements[i][i]) < CMP_EPSILON) {
                Math_PI / 4
            } else {
                0.5f * atan(2f * elements[i][j] / (elements[j][j] - elements[i][i]))
            }

            // Compute the rotation matrix
            val rot = Basis()
            rot.elements[j][j] = cos(angle)
            rot.elements[i][i] = rot.elements[j][j]
            rot.elements[j][i] = sin(angle)
            rot.elements[i][j] = -rot.elements[j][i]

            // Update the off matrix norm
            offMatrixNorm2 -= elements[i][j] * elements[i][j]

            // Apply the rotation
            set(this * rot.transposed())
            accRot = rot * accRot
        }

        return accRot
    }

    fun getOrthogonalIndex(): Int {
        //could be sped up if i come up with a way
        val orth = copy()
        for (i in 0 until 3) {
            for (j in 0 until 3) {

                var v = orth[i][j]
                if (v > 0.5f)
                    v = 1f
                else if (v < -0.5f)
                    v = -1f
                else
                    v = 0f

                orth[i][j] = v
            }
        }

        for (i in 0 until 24) {
            if (orthoBases[i] == orth) return i
        }

        return 0
    }

    fun setOrthogonalIndex(index: Int) {
        //there only exist 24 orthogonal bases in r3
        if (index >= 24) {
            printError("index >= 24", "setOrthongonalIndex", "Basis")
            throw IllegalStateException("index >= 24")
        }

        set(orthoBases[index])
    }

    constructor(euler: Vector3) : this() {
        this.euler = euler
    }

    constructor(quat: Quat) : this() {
        val d = quat.lengthSquared()
        val s = 2f / d
        val xs = quat.x * s
        val ys = quat.y * s
        val zs = quat.z * s
        val wx = quat.w * xs
        val wy = quat.w * ys
        val wz = quat.w * zs
        val xx = quat.x * xs
        val xy = quat.x * ys
        val xz = quat.x * zs
        val yy = quat.y * ys
        val yz = quat.y * zs
        val zz = quat.z * zs
        set(1f - (yy + zz), xy - wz, xz + wy,
                xy + wz, 1f - (xx + zz), yz - wx,
                xz - wy, yz + wx, 1f - (xx + yy))
    }

    fun toQuat(): Quat {
        //commenting this check because precision issues cause it to fail when it shouldn't
        //ERR_FAIL_COND_V(is_rotation() == false, Quat());

        val trace = elements[0][0] + elements[1][1] + elements[2][2]
        val temp = arrayOf(0f, 0f, 0f, 0f)

        if (trace > 0f) {
            var s = sqrt(trace + 1f)
            temp[3] = (s * 0.5f)
            s = 0.5f / s

            temp[0] = ((elements[2][1] - elements[1][2]) * s)
            temp[1] = ((elements[0][2] - elements[2][0]) * s)
            temp[2] = ((elements[1][0] - elements[0][1]) * s)
        } else {
            val i = if (elements[0][0] < elements[1][1])
                (if (elements[1][1] < elements[2][2]) 2 else 1) else
                (if (elements[0][0] < elements[2][2]) 2 else 0)
            val j = (i + 1) % 3
            val k = (i + 2) % 3

            var s = sqrt(elements[i][i] - elements[j][j] - elements[k][k] + 1f)
            temp[i] = s * 0.5f
            s = 0.5f / s

            temp[3] = (elements[k][j] - elements[j][k]) * s
            temp[j] = (elements[j][i] + elements[i][j]) * s
            temp[k] = (elements[k][i] + elements[i][k]) * s
        }

        return Quat(temp[0], temp[1], temp[2], temp[3])
    }

    companion object {
        val orthoBases: Array<Basis> = arrayOf(
                Basis(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f),
                Basis(0f, -1f, 0f, 1f, 0f, 0f, 0f, 0f, 1f),
                Basis(-1f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, 1f),
                Basis(0f, 1f, 0f, -1f, 0f, 0f, 0f, 0f, 1f),
                Basis(1f, 0f, 0f, 0f, 0f, -1f, 0f, 1f, 0f),
                Basis(0f, 0f, 1f, 1f, 0f, 0f, 0f, 1f, 0f),
                Basis(-1f, 0f, 0f, 0f, 0f, 1f, 0f, 1f, 0f),
                Basis(0f, 0f, -1f, -1f, 0f, 0f, 0f, 1f, 0f),
                Basis(1f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, -1f),
                Basis(0f, 1f, 0f, 1f, 0f, 0f, 0f, 0f, -1f),
                Basis(-1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, -1f),
                Basis(0f, -1f, 0f, -1f, 0f, 0f, 0f, 0f, -1f),
                Basis(1f, 0f, 0f, 0f, 0f, 1f, 0f, -1f, 0f),
                Basis(0f, 0f, -1f, 1f, 0f, 0f, 0f, -1f, 0f),
                Basis(-1f, 0f, 0f, 0f, 0f, -1f, 0f, -1f, 0f),
                Basis(0f, 0f, 1f, -1f, 0f, 0f, 0f, -1f, 0f),
                Basis(0f, 0f, 1f, 0f, 1f, 0f, -1f, 0f, 0f),
                Basis(0f, -1f, 0f, 0f, 0f, 1f, -1f, 0f, 0f),
                Basis(0f, 0f, -1f, 0f, -1f, 0f, -1f, 0f, 0f),
                Basis(0f, 1f, 0f, 0f, 0f, -1f, -1f, 0f, 0f),
                Basis(0f, 0f, 1f, 0f, -1f, 0f, 1f, 0f, 0f),
                Basis(0f, 1f, 0f, 0f, 0f, 1f, 1f, 0f, 0f),
                Basis(0f, 0f, -1f, 0f, 1f, 0f, 1f, 0f, 0f),
                Basis(0f, -1f, 0f, 0f, 0f, -1f, 1f, 0f, 0f)
        )
    }
}