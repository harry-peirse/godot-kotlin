package godot

import godot.internal.godot_rect2
import kotlinx.cinterop.*
import kotlin.math.max
import kotlin.math.min

class Rect2(var position: Vector2 = Vector2(), var size: Vector2 = Vector2()) {
    internal constructor(raw: CPointer<godot_rect2>) : this(
            memScoped { Vector2(api.godot_rect2_get_position!!(raw).ptr) },
            memScoped { Vector2(api.godot_rect2_get_size!!(raw).ptr) }
    )

    internal fun _raw(scope: AutofreeScope): CPointer<godot_rect2> {
        val raw = scope.alloc<godot_rect2>()
        api.godot_rect2_new_with_position_and_size!!(raw.ptr, position._raw(scope), size._raw(scope))
        return raw.ptr
    }

    fun copy() = Rect2(position.copy(), size.copy())

    fun intersects(rect: Rect2): Boolean {
        if (position.x >= (rect.position.x + rect.size.width))
            return false
        if ((position.x + size.width) <= rect.position.x)
            return false
        if (position.y >= (rect.position.y + rect.size.height))
            return false
        if ((position.y + size.height) <= rect.position.y)
            return false

        return true
    }

    fun encloses(rect: Rect2) = (rect.position.x >= position.x) && (rect.position.y >= position.y) &&
            ((rect.position.x + rect.size.x) < (position.x + size.x)) &&
            ((rect.position.y + rect.size.y) < (position.y + size.y))

    fun hasNoArea() = size.x <= 0f || size.y <= 0f

    fun hasPoint(point: Point2): Boolean {
        if (point.x < position.x)
            return false
        if (point.y < position.y)
            return false

        if (point.x >= (position.x + size.x))
            return false
        if (point.y >= (position.y + size.y))
            return false

        return true
    }

    fun noArea() = size.width <= 0f || size.height <= 0f

    override fun equals(other: Any?) = other is Rect2 && position == other.position && size == other.size

    override fun hashCode() = arrayOf(position, size).hashCode()

    fun grow(by: Float): Rect2 {
        val g = copy()
        g.position.x -= by
        g.position.y -= by
        g.size.width += by * 2
        g.size.height += by * 2
        return g
    }

    fun expand(vector: Vector2): Rect2 {
        val r = copy()
        r.expandTo(vector)
        return r
    }

    fun expandTo(vector: Vector2) {
        val begin = position
        val end = position + size

        if (vector.x < begin.x)
            begin.x = vector.x
        if (vector.y < begin.y)
            begin.y = vector.y

        if (vector.x > end.x)
            end.x = vector.x
        if (vector.y > end.y)
            end.y = vector.y

        position = begin
        size = end - begin
    }

    fun distanceTo(point: Vector2): Float {
        var dist = 1e20f

        if (point.x < position.x) {
            dist = min(dist, position.x - point.x)
        }
        if (point.y < position.y) {
            dist = min(dist, position.y - point.y)
        }
        if (point.x >= (position.x + size.x)) {
            dist = min(point.x - (position.x + size.x), dist)
        }
        if (point.y >= (position.y + size.y)) {
            dist = min(point.y - (position.y + size.y), dist)
        }

        return if (dist == 1e20f) 0f else dist
    }

    fun clip(rect: Rect2): Rect2 {

        val newRect = rect.copy()

        if (!intersects(newRect))
            return Rect2()

        newRect.position.x = max(rect.position.x, position.x)
        newRect.position.y = max(rect.position.y, position.y)

        val rectEnd = rect.position + rect.size
        val end = position + size

        newRect.size.x = min(rectEnd.x, end.x) - newRect.position.x
        newRect.size.y = min(rectEnd.y, end.y) - newRect.position.y

        return newRect
    }

    fun merge(rect: Rect2): Rect2 {
        val newRect = Rect2()

        newRect.position.x = min(rect.position.x, position.x)
        newRect.position.y = min(rect.position.y, position.y)

        newRect.size.x = max(rect.position.x + rect.size.x, position.x + size.x)
        newRect.size.y = max(rect.position.y + rect.size.y, position.y + size.y)

        newRect.size = newRect.size - newRect.position

        return newRect
    }

    override fun toString() = "$position, $size"

    fun intersectsSegment(from: Vector2, to: Vector2, /* out */ position: Vector2? = null, /* out */ normal: Vector2? = null): Boolean {
        var min = 0f
        var max = 1f
        var axis = 0
        var sign = 0f

        for (i in 0 until 2) {
            val segFrom = from[i]
            val segTo = to[i]
            val boxBegin = this.position[i]
            val boxEnd = boxBegin + size[i]
            var cmin: Float
            var cmax: Float
            var csign: Float

            if (segFrom < segTo) {
                if (segFrom > boxEnd || segTo < boxBegin) return false
                val length = segTo - segFrom
                cmin = if (segFrom < boxBegin) ((boxBegin - segFrom) / length) else 0f
                cmax = if (segTo > boxEnd) ((boxEnd - segFrom) / length) else 1f
                csign = -1f
            } else {
                if (segTo > boxEnd || segFrom < boxBegin) return false
                val length = segTo - segFrom
                cmin = if (segFrom > boxEnd) (boxEnd - segFrom) / length else 0f
                cmax = if (segTo < boxBegin) (boxBegin - segFrom) / length else 1f
                csign = 1f
            }

            if (cmin > min) {
                min = cmin
                axis = i
                sign = csign
            }
            if (cmax < max)
                max = cmax
            if (max < min)
                return false
        }

        val rel = to - from

        if (normal != null) {
            normal[axis] = sign
        }

        position?.set(from + rel * min)

        return true
    }

    fun intersectsTransformed(xform: Transform2D, rect: Rect2): Boolean {
        val xf_points = arrayOf(
                xform.xform(rect.position),
                xform.xform(Vector2(rect.position.x + rect.size.x, rect.position.y)),
                xform.xform(Vector2(rect.position.x, rect.position.y + rect.size.y)),
                xform.xform(Vector2(rect.position.x + rect.size.x, rect.position.y + rect.size.y))
        )

        var low_limit: Float

        fun next4(): Boolean {
            val xf_points2 = arrayOf(
                    position,
                    Vector2(position.x + size.x, position.y),
                    Vector2(position.x, position.y + size.y),
                    Vector2(position.x + size.x, position.y + size.y)
            )

            var maxa = xform.elements[0].dot(xf_points2[0])
            var mina = maxa

            var dp = xform.elements[0].dot(xf_points2[1])
            maxa = max(dp, maxa)
            mina = min(dp, mina)

            dp = xform.elements[0].dot(xf_points2[2])
            maxa = max(dp, maxa)
            mina = min(dp, mina)

            dp = xform.elements[0].dot(xf_points2[3])
            maxa = max(dp, maxa)
            mina = min(dp, mina)

            var maxb = xform.elements[0].dot(xf_points[0])
            var minb = maxb

            dp = xform.elements[0].dot(xf_points[1])
            maxb = max(dp, maxb)
            minb = min(dp, minb)

            dp = xform.elements[0].dot(xf_points[2])
            maxb = max(dp, maxb)
            minb = min(dp, minb)

            dp = xform.elements[0].dot(xf_points[3])
            maxb = max(dp, maxb)
            minb = min(dp, minb)

            if (mina > maxb)
                return false
            if (minb > maxa)
                return false

            maxa = xform.elements[1].dot(xf_points2[0])
            mina = maxa

            dp = xform.elements[1].dot(xf_points2[1])
            maxa = max(dp, maxa)
            mina = min(dp, mina)

            dp = xform.elements[1].dot(xf_points2[2])
            maxa = max(dp, maxa)
            mina = min(dp, mina)

            dp = xform.elements[1].dot(xf_points2[3])
            maxa = max(dp, maxa)
            mina = min(dp, mina)

            maxb = xform.elements[1].dot(xf_points[0])
            minb = maxb

            dp = xform.elements[1].dot(xf_points[1])
            maxb = max(dp, maxb)
            minb = min(dp, minb)

            dp = xform.elements[1].dot(xf_points[2])
            maxb = max(dp, maxb)
            minb = min(dp, minb)

            dp = xform.elements[1].dot(xf_points[3])
            maxb = max(dp, maxb)
            minb = min(dp, minb)

            if (mina > maxb)
                return false
            if (minb > maxa)
                return false

            return true
        }

        fun next3(): Boolean {
            low_limit = position.x + size.x

            if (xf_points[0].x < low_limit)
                return next4()
            if (xf_points[1].x < low_limit)
                return next4()
            if (xf_points[2].x < low_limit)
                return next4()
            if (xf_points[3].x < low_limit)
                return next4()

            return false
        }

        fun next2(): Boolean {
            if (xf_points[0].x > position.x)
                return next3()
            if (xf_points[1].x > position.x)
                return next3()
            if (xf_points[2].x > position.x)
                return next3()
            if (xf_points[3].x > position.x)
                return next3()

            return false
        }

        fun next1(): Boolean {
            low_limit = position.y + size.y

            if (xf_points[0].y < low_limit)
                return next2()
            if (xf_points[1].y < low_limit)
                return next2()
            if (xf_points[2].y < low_limit)
                return next2()
            if (xf_points[3].y < low_limit)
                return next2()

            return false
        }

        if (xf_points[0].y > position.y)
            return next1()
        if (xf_points[1].y > position.y)
            return next1()
        if (xf_points[2].y > position.y)
            return next1()
        if (xf_points[3].y > position.y)
            return next1()

        return false
    }
}