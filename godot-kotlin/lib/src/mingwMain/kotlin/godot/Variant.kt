package godot

import godot.internal.godot_variant
import godot.internal.godot_variant_type
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlin.reflect.KClass

@UseExperimental(ExperimentalUnsignedTypes::class)
open class Variant internal constructor() {

    internal lateinit var _variant: CPointer<godot_variant>

    val _type: UInt
        get() = godot_variant_type.GODOT_VARIANT_TYPE_OBJECT.value

    fun asFloat(): Float {
        return godot.api.godot_variant_as_real!!(_variant).toFloat()
    }

    fun asString(): String {
        memScoped {
            return GodotString(godot.api.godot_variant_as_string!!(_variant).ptr).string
        }
    }

    fun asVector2(): Vector2 {
        memScoped {
            return Vector2(godot.api.godot_variant_as_vector2!!(_variant).ptr)
        }
    }

    fun asInt(): Int {
        return godot.api.godot_variant_as_int!!(_variant).toInt()
    }

    fun asUInt(): UInt {
        return godot.api.godot_variant_as_uint!!(_variant).toUInt()
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> cast(type: KClass<T>): T {
        val result: Any = when (type) {
            Float::class -> this.asFloat()
            String::class -> this.asString()
            Vector2::class -> this.asVector2()
            Int::class -> this.asInt()
            UInt::class -> this.asUInt()
            else -> throw UnsupportedOperationException("Could not cast Variant to $type")
        }
        return result as T
    }

    internal fun _getType(): godot_variant_type {
        return godot.api.godot_variant_get_type!!(_variant)
    }

    private var stableRef: StableRef<Variant>? = null

    internal fun asStableRef(): StableRef<Variant>? {
        if (stableRef == null) {
            stableRef = StableRef.create(this)
        }
        return stableRef
    }

    internal fun dispose() {
        stableRef?.dispose()
    }

    companion object {

        fun <T : Any?> from(value: T): Variant? {
            TODO()
        }

        inline fun <reified T : Variant> create(_variant: CPointer<godot_variant>): T {
            TODO()
        }

        fun <T : S, S : Object> create(_variant: CPointer<godot_variant>, boundClass: BoundClass<T, S>): T {
            TODO()
        }
    }
}