package godot

import godot.internal.godot_basis
import kotlinx.cinterop.*
import kotlin.math.cos
import kotlin.math.sin

class Basis(var x: Vector3 = Vector3(1f, 0f, 0f),
            var y: Vector3 = Vector3(0f, 1f, 0f),
            var z: Vector3 = Vector3(0f, 0f, 1f)) {

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

    internal fun _raw(scope: AutofreeScope): CPointer<godot_basis> {
        val raw = scope.alloc<godot_basis>()
        api.godot_basis_new_with_rows!!(raw.ptr, x._raw(scope), y._raw(scope), z._raw(scope))
        return raw.ptr
    }

    fun xform(vector: Vector3) = Vector3(x.dot(vector), y.dot(vector), z.dot(vector))
}