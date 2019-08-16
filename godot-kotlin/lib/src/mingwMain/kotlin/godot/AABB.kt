package godot

import godot.internal.godot_aabb
import kotlinx.cinterop.*

class AABB(var position: Vector3 = Vector3(), var size: Vector3 = Vector3()) {
    internal constructor(raw: CPointer<godot_aabb>) : this(
            memScoped { Vector3(api.godot_aabb_get_position!!(raw).ptr) },
            memScoped { Vector3(api.godot_aabb_get_size!!(raw).ptr) }
    )

    internal fun _raw(scope: AutofreeScope): CPointer<godot_aabb> {
        val raw = scope.alloc<godot_aabb>()
        api.godot_aabb_new!!(raw.ptr, position._raw(scope), size._raw(scope))
        return raw.ptr
    }

    fun hasNoArea() = size.x <= CMP_EPSILON || size.y <= CMP_EPSILON || size.z <= CMP_EPSILON

    fun hasNoSurface() = size.x <= CMP_EPSILON && size.y <= CMP_EPSILON && size.z <= CMP_EPSILON

    fun intersects(aabb: AABB): Boolean {
        if (position.x >= (aabb.position.x + aabb.size.x))
            return false
        if ((position.x + size.x) <= aabb.position.x)
            return false
        if (position.y >= (aabb.position.y + aabb.size.y))
            return false
        if ((position.y + size.y) <= aabb.position.y)
            return false
        if (position.z >= (aabb.position.z + aabb.size.z))
            return false
        if ((position.z + size.z) <= aabb.position.z)
            return false

        return true
    }

    fun intersectsInclusive(aabb: AABB): Boolean {
        if (position.x > (aabb.position.x + aabb.size.x))
            return false
        if ((position.x + size.x) < aabb.position.x)
            return false
        if (position.y > (aabb.position.y + aabb.size.y))
            return false
        if ((position.y + size.y) < aabb.position.y)
            return false
        if (position.z > (aabb.position.z + aabb.size.z))
            return false
        if ((position.z + size.z) < aabb.position.z)
            return false

        return true
    }

    fun encloses(aabb: AABB): Boolean {
        val src_min = position
        val src_max = position + size
        val dst_min = aabb.position
        val dst_max = aabb.position + aabb.size

        return ((src_min.x <= dst_min.x) &&
                (src_max.x > dst_max.x) &&
                (src_min.y <= dst_min.y) &&
                (src_max.y > dst_max.y) &&
                (src_min.z <= dst_min.z) &&
                (src_max.z > dst_max.z))
    }

    fun getSupport(normal: Vector3): Vector3 {

        val halfExtents = size * 0.5f
        val ofs = position + halfExtents

        return Vector3(
                if (normal.x > 0) -halfExtents.x else halfExtents.x,
                if (normal.y > 0) -halfExtents.y else halfExtents.y,
                if (normal.z > 0) -halfExtents.z else halfExtents.z) +
                ofs
    }

    fun getEndpoint(point: Int) = when (point) {
        0 -> Vector3(position.x, position.y, position.z)
        1 -> Vector3(position.x, position.y, position.z + size.z)
        2 -> Vector3(position.x, position.y + size.y, position.z)
        3 -> Vector3(position.x, position.y + size.y, position.z + size.z)
        4 -> Vector3(position.x + size.x, position.y, position.z)
        5 -> Vector3(position.x + size.x, position.y, position.z + size.z)
        6 -> Vector3(position.x + size.x, position.y + size.y, position.z)
        7 -> Vector3(position.x + size.x, position.y + size.y, position.z + size.z)
        else -> throw IndexOutOfBoundsException("Tried to get endpoint $point from AABB")
    }

    fun intersectsConvexShape(planes: List<Plane>): Boolean {
        val halfExtents = size * 0.5f
        val ofs = position + halfExtents

        planes.forEach {
            var point = Vector3(
                    if (it.normal.x > 0) -halfExtents.x else halfExtents.x,
                    if (it.normal.y > 0) -halfExtents.y else halfExtents.y,
                    if (it.normal.z > 0) -halfExtents.z else halfExtents.z)
            point += ofs
            if (it.isPointOver(point)) return false
        }

        return true
    }

    fun hasPoint(point: Vector3): Boolean {
        if (point.x < position.x)
            return false
        if (point.y < position.y)
            return false
        if (point.z < position.z)
            return false
        if (point.x > position.x + size.x)
            return false
        if (point.y > position.y + size.y)
            return false
        if (point.z > position.z + size.z)
            return false

        return true
    }

    fun expandTo(vector: Vector3) {

        val begin = position
        val end = position + size

        if (vector.x < begin.x)
            begin.x = vector.x
        if (vector.y < begin.y)
            begin.y = vector.y
        if (vector.z < begin.z)
            begin.z = vector.z

        if (vector.x > end.x)
            end.x = vector.x
        if (vector.y > end.y)
            end.y = vector.y
        if (vector.z > end.z)
            end.z = vector.z

        position = begin
        size = end - begin
    }

    fun projectRangeInPlane(plane: Plane): Pair<Float, Float> {
        val halfExtents = Vector3(size.x * 0.5f, size.y * 0.5f, size.z * 0.5f)
        val center = Vector3(position.x + halfExtents.x, position.y + halfExtents.y, position.z + halfExtents.z)

        val length = plane.normal.abs().dot(halfExtents)
        val distance = plane.distanceTo(center)
        return (distance - length) to (distance + length)
    }

    fun getLongestAxisSize(): Float {
        var maxSize = size.x
        if (size.y > maxSize) {
            maxSize = size.y
        }
        if (size.z > maxSize) {
            maxSize = size.z
        }
        return maxSize
    }

    fun getShortestAxisSize(): Float {
        var minSize = size.x
        if (size.y < minSize) {
            minSize = size.y
        }
        if (size.z < minSize) {
            minSize = size.z
        }
        return minSize
    }

    fun smitsIntersectRay(from: Vector3, dir: Vector3, t0: Float, t1: Float): Boolean {
        val divx = 1f / dir.x
        val divy = 1f / dir.y
        val divz = 1f / dir.z

        val upbound = position + size
        var tmin: Float
        var tmax: Float
        val tymin: Float
        val tymax: Float
        val tzmin: Float
        val tzmax: Float
        if (dir.x >= 0f) {
            tmin = (position.x - from.x) * divx
            tmax = (upbound.x - from.x) * divx
        } else {
            tmin = (upbound.x - from.x) * divx
            tmax = (position.x - from.x) * divx
        }
        if (dir.y >= 0f) {
            tymin = (position.y - from.y) * divy
            tymax = (upbound.y - from.y) * divy
        } else {
            tymin = (upbound.y - from.y) * divy
            tymax = (position.y - from.y) * divy
        }
        if ((tmin > tymax) || (tymin > tmax))
            return false
        if (tymin > tmin)
            tmin = tymin
        if (tymax < tmax)
            tmax = tymax
        if (dir.z >= 0f) {
            tzmin = (position.z - from.z) * divz
            tzmax = (upbound.z - from.z) * divz
        } else {
            tzmin = (upbound.z - from.z) * divz
            tzmax = (position.z - from.z) * divz
        }
        if ((tmin > tzmax) || (tzmin > tmax))
            return false
        if (tzmin > tmin)
            tmin = tzmin
        if (tzmax < tmax)
            tmax = tzmax
        return ((tmin < t1) && (tmax > t0))
    }

    fun growBy(amount: Float) {
        position.x -= amount
        position.y -= amount
        position.z -= amount
        size.x += 2f * amount
        size.y += 2f * amount
        size.z += 2f * amount
    }

    fun getArea() = size.x * size.y * size.z

    fun mergeWith(aabb: AABB) {
        val beg1 = position
        val beg2 = aabb.position
        val end1 = Vector3(size.x, size.y, size.z) + beg1
        val end2 = Vector3(aabb.size.x, aabb.size.y, aabb.size.z) + beg2

        val min = Vector3(if (beg1.x < beg2.x) beg1.x else beg2.x,
                if (beg1.y < beg2.y) beg1.y else beg2.y,
                if (beg1.z < beg2.z) beg1.z else beg2.z)

        val max = Vector3(if (end1.x > end2.x) end1.x else end2.x,
                if (end1.y > end2.y) end1.y else end2.y,
                if (end1.z > end2.z) end1.z else end2.z)

        position = min
        size = max - min
    }

    fun intersection(aabb: AABB): AABB {
        val srcMin = position
        val srcMax = position + size
        val dstMin = aabb.position
        val dstMax = aabb.position + aabb.size

        val min = Vector3()
        val max = Vector3()

        if (srcMin.x > dstMax.x || srcMax.x < dstMin.x)
            return AABB()
        else {

            min.x = if (srcMin.x > dstMin.x) srcMin.x else dstMin.x
            max.x = if (srcMax.x < dstMax.x) srcMax.x else dstMax.x
        }

        if (srcMin.y > dstMax.y || srcMax.y < dstMin.y)
            return AABB()
        else {

            min.y = if (srcMin.y > dstMin.y) srcMin.y else dstMin.y
            max.y = if (srcMax.y < dstMax.y) srcMax.y else dstMax.y
        }

        if (srcMin.z > dstMax.z || srcMax.z < dstMin.z)
            return AABB()
        else {

            min.z = if (srcMin.z > dstMin.z) srcMin.z else dstMin.z
            max.z = if (srcMax.z < dstMax.z) srcMax.z else dstMax.z
        }

        return AABB(min, max - min)
    }

    fun intersectsRay(from: Vector3, dir: Vector3, /* out */ clip: Vector3? = null, /* out */ normal: Vector3? = null): Boolean {
        var c1 = Vector3()
        var c2 = Vector3()
        val end = position + size
        var near = -1e20f
        var far = 1e20f
        var axis = 0

        for (i in 0 until 3) {
            if (dir[i] == 0f) {
                if ((from[i] < position[i]) || (from[i] > end[i])) {
                    return false
                }
            } else { // ray not parallel to planes in this direction
                c1[i] = (position[i] - from[i]) / dir[i]
                c2[i] = (end[i] - from[i]) / dir[i]

                if (c1[i] > c2[i]) {
                    val tmp = c2
                    c2 = c1
                    c1 = tmp
                }
                if (c1[i] > near) {
                    near = c1[i]
                    axis = i
                }
                if (c2[i] < far) {
                    far = c2[i]
                }
                if ((near > far) || (far < 0f)) {
                    return false
                }
            }
        }

        if (clip != null)
            clip.set(c1)
        if (normal != null) {
            normal[axis] = if (dir[axis] != 0f) -1f else 1f
        }

        return true
    }

    fun intersectsSegment(from: Vector3, to: Vector3, /* out */ clip: Vector3? = null, /* out */ normal: Vector3? = null): Boolean {
        var min = 0f
        var max = 1f
        var axis = 0
        var sign = 0f

        for (i in 0 until 3) {
            val segFrom = from[i]
            val segTo = to[i]
            val boxBegin = position[i]
            val boxEnd = boxBegin + size[i]
            val cMin: Float
            val cMax: Float
            val cSign: Float

            if (segFrom < segTo) {

                if (segFrom > boxEnd || segTo < boxBegin)
                    return false
                val length = segTo - segFrom
                cMin = if (segFrom < boxBegin) ((boxBegin - segFrom) / length) else 0f
                cMax = if (segTo > boxEnd) ((boxEnd - segFrom) / length) else 1f
                cSign = -1f

            } else {

                if (segTo > boxEnd || segFrom < boxBegin)
                    return false
                val length = segTo - segFrom
                cMin = if (segFrom > boxEnd) (boxEnd - segFrom) / length else 0f
                cMax = if (segTo < boxBegin) (boxBegin - segFrom) / length else 1f
                cSign = 1f
            }

            if (cMin > min) {
                min = cMin
                axis = i
                sign = cSign
            }
            if (cMax < max)
                max = cMax
            if (max < min)
                return false
        }

        val rel = to - from

        if (normal != null) {
            normal[axis] = sign
        }

        clip?.set(from + rel * min)

        return true
    }

    fun intersectsPlane(plane: Plane): Boolean {
        val points = arrayOf(
                Vector3(position.x, position.y, position.z),
                Vector3(position.x, position.y, position.z + size.z),
                Vector3(position.x, position.y + size.y, position.z),
                Vector3(position.x, position.y + size.y, position.z + size.z),
                Vector3(position.x + size.x, position.y, position.z),
                Vector3(position.x + size.x, position.y, position.z + size.z),
                Vector3(position.x + size.x, position.y + size.y, position.z),
                Vector3(position.x + size.x, position.y + size.y, position.z + size.z)
        )

        var over = false
        var under = false

        for (i in 0 until 8) {

            if (plane.distanceTo(points[i]) > 0)
                over = true
            else
                under = true
        }

        return under && over
    }

    fun getLongestAxis(): Vector3 {
        var axis = Vector3(1f, 0f, 0f)
        var maxSize = size.x

        if (size.y > maxSize) {
            axis = Vector3(0f, 1f, 0f)
            maxSize = size.y
        }

        if (size.z > maxSize) {
            axis = Vector3(0f, 0f, 1f)
        }

        return axis
    }

    fun getLongestAxisIndex(): Int {
        var axis = 0
        var maxSize = size.x

        if (size.y > maxSize) {
            axis = 1
            maxSize = size.y
        }

        if (size.z > maxSize) {
            axis = 2
        }

        return axis
    }

    fun getShortestAxis(): Vector3 {
        var axis = Vector3(1f, 0f, 0f)
        var minSize = size.x

        if (size.y < minSize) {
            axis = Vector3(0f, 1f, 0f)
            minSize = size.y
        }

        if (size.z < minSize) {
            axis = Vector3(0f, 0f, 1f)
        }

        return axis
    }

    fun getShortestAxisIndex(): Int {
        var axis = 0
        var minSize = size.x

        if (size.y < minSize) {
            axis = 1
            minSize = size.y
        }

        if (size.z < minSize) {
            axis = 2
        }

        return axis
    }

    fun merge(with: AABB): AABB {
        val aabb = copy()
        aabb.mergeWith(with)
        return aabb
    }

    fun expand(vector: Vector3): AABB {
        val aabb = copy()
        aabb.expandTo(vector)
        return aabb
    }

    fun grow(by: Float): AABB {
        val aabb = copy()
        aabb.growBy(by)
        return aabb
    }

    fun getEdge(edge: Int): Pair<Vector3, Vector3> = when (edge) {
        0 -> Vector3(position.x + size.x, position.y, position.z) to Vector3(position.x, position.y, position.z)
        1 -> Vector3(position.x + size.x, position.y, position.z + size.z) to Vector3(position.x + size.x, position.y, position.z)
        2 -> Vector3(position.x, position.y, position.z + size.z) to Vector3(position.x + size.x, position.y, position.z + size.z)
        3 -> Vector3(position.x, position.y, position.z) to Vector3(position.x, position.y, position.z + size.z)
        4 -> Vector3(position.x, position.y + size.y, position.z) to Vector3(position.x + size.x, position.y + size.y, position.z)
        5 -> Vector3(position.x + size.x, position.y + size.y, position.z) to Vector3(position.x + size.x, position.y + size.y, position.z + size.z)
        6 -> Vector3(position.x + size.x, position.y + size.y, position.z + size.z) to Vector3(position.x, position.y + size.y, position.z + size.z)
        7 -> Vector3(position.x, position.y + size.y, position.z + size.z) to Vector3(position.x, position.y + size.y, position.z)
        8 -> Vector3(position.x, position.y, position.z + size.z) to Vector3(position.x, position.y + size.y, position.z + size.z)
        9 -> Vector3(position.x, position.y, position.z) to Vector3(position.x, position.y + size.y, position.z)
        10 -> Vector3(position.x + size.x, position.y, position.z) to Vector3(position.x + size.x, position.y + size.y, position.z)
        11 -> Vector3(position.x + size.x, position.y, position.z + size.z) to Vector3(position.x + size.x, position.y + size.y, position.z + size.z)
        else -> throw IndexOutOfBoundsException("Tried to get edge $edge from AABB")
    }

    fun copy() = AABB(position.copy(), size.copy())

    override fun equals(other: Any?) = other is AABB && position == other.position && size == other.size

    override fun hashCode() = arrayOf(position, size).hashCode()

    override fun toString() = "$position - $size"
}