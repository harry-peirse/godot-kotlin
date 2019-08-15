package godot

import godot.internal.godot_plane
import kotlinx.cinterop.*
import kotlin.math.abs

private const val PLANE_EQ_DOT_EPSILON = 0.999f
private const val PLANE_EQ_D_EPSILON = 0.0001f
private const val CMP_EPSILON = 0.00001f
private const val CMP_EPSILON2 = (CMP_EPSILON * CMP_EPSILON)
private const val Math_PI = 3.14159265358979323846f

class Plane(var normal: Vector3, var distance: Float) {

    constructor(a: Float, b: Float, c: Float, d: Float) : this(Vector3(a, b, c), d)

    constructor(point: Vector3, normal: Vector3) : this(normal, normal.dot(point))

    constructor(point1: Vector3, point2: Vector3, point3: Vector3, direction: ClockDirection = ClockDirection.CLOCKWISE) : this(point1,
            when (direction) {
                ClockDirection.CLOCKWISE -> (point1 - point3).cross(point1 - point2)
                ClockDirection.COUNTERCLOCKWISE -> ((point1 - point2).cross(point1 - point3)).normalized()
            })

    internal constructor(raw: CPointer<godot_plane>) : this(
            memScoped { Vector3(api.godot_plane_get_normal!!(raw).ptr) },
            api.godot_plane_get_d!!(raw)
    )

    internal fun _raw(scope: AutofreeScope): CPointer<godot_plane> {
        val raw = scope.alloc<godot_plane>()
        api.godot_plane_new_with_normal!!(raw.ptr, normal._raw(scope), distance)
        return raw.ptr
    }

    fun copy() = Plane(normal.copy(), distance)

    fun setNormal(normal: Vector3) {
        this.normal = normal
    }

    fun project(point: Vector3) = point - normal * distanceTo(point)

    fun normalize() {
        val l = normal.length()
        if (l == 0f) {
            normal = Vector3()
            distance = 0f
            return
        }
        normal = normal / l
        distance /= l
    }

    fun normalized(): Plane {
        val p = copy()
        p.normalize()
        return p
    }

    fun getAnyPoint() = normal * distance

    fun getAnyPerpendicularNormal(): Vector3 {
        val p1 = Vector3(1f, 0f, 0f)
        val p2 = Vector3(0f, 1f, 0f)

        val p = if (abs(normal.dot(p1)) > 0.99f) // if too similar to p1
            p2 // use p2
        else
            p1 // use p1

        p -= normal * normal.dot(p)
        p.normalize()

        return p
    }

    fun intersect3(plane1: Plane, plane2: Plane, /* out */ intersection: Vector3? = null): Boolean {
        val plane0 = copy()
        val normal0 = plane0.normal
        val normal1 = plane1.normal
        val normal2 = plane2.normal

        val denom = (normal0.cross(normal1)).dot(normal2)

        if (abs(denom) <= CMP_EPSILON) return false

        if (intersection != null) {
            val result = ((normal1.cross(normal2)) * plane0.distance) +
                    ((normal2.cross(normal0) * plane1.distance) +
                            ((normal0.cross(normal1)) * plane2.distance)) /
                    denom
            intersection.x = result.x
            intersection.x = result.y
            intersection.y = result.z
        }

        return true
    }

    fun intersectsRay(from: Vector3, dir: Vector3, /* out */ intersection: Vector3? = null): Boolean {
        val segment = dir
        val den = normal.dot(segment)

        if (abs(den) <= CMP_EPSILON) return false

        val dist = (normal.dot(from) - distance) / den

        if (dist > CMP_EPSILON) //this is a ray, before the emiting pos (p_from) doesnt exist
            return false

        if (intersection != null) {
            val result = from + segment * -dist
            intersection.x = result.x
            intersection.y = result.y
            intersection.z = result.z
        }

        return true
    }

    fun intersectsSegment(begin: Vector3, end: Vector3, /* out */ intersection: Vector3? = null): Boolean {
        val segment = begin - end
        val den = normal.dot(segment)

        if (abs(den) <= CMP_EPSILON) return false

        val dist = (normal.dot(begin) - distance) / den

        if (dist < -CMP_EPSILON || dist > (1.0 + CMP_EPSILON)) return false

        if (intersection != null) {
            val result = begin + segment * -dist
            intersection.x = result.x
            intersection.y = result.y
            intersection.z = result.z
        }

        return true
    }

    fun isAlmostLike(plane: Plane) = normal.dot(plane.normal) > PLANE_EQ_DOT_EPSILON && abs(distance - plane.distance) < PLANE_EQ_D_EPSILON

    fun isPointOver(point: Vector3) = normal.dot(point) > distance

    fun distanceTo(point: Vector3) = normal.dot(point) - distance

    fun hasPoint(point: Vector3, epsilon: Float) = abs(normal.dot(point) - distance) <= epsilon

    override fun equals(other: Any?) = other is Plane && normal == other.normal && distance == other.distance

    override fun hashCode() = arrayOf(normal, distance).hashCode()

    override fun toString() = "$normal; $distance"

    enum class ClockDirection {
        CLOCKWISE,
        COUNTERCLOCKWISE
    }
}