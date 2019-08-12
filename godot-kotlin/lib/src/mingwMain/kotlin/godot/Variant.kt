package godot

import godot.internal.godot_variant
import godot.internal.godot_variant_type
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.invoke
import kotlin.reflect.KClass

@UseExperimental(ExperimentalUnsignedTypes::class)
class Variant : CoreType<godot_variant> {
    internal constructor (_variant: CPointer<godot_variant>) : super(_variant)
    internal constructor(value: CValue<godot_variant>) : super(value.place(godot.alloc(godot_variant.size)))
    internal constructor() : this(godot.alloc(godot_variant.size))

    constructor(value: Float) : this() {
        godot.api.godot_variant_new_real!!(_variant, value.toDouble())
    }

    constructor(value: String) : this() {
        godot.api.godot_variant_new_string!!(_variant, value.toGodotString())
    }

    constructor(value: GodotString) : this() {
        godot.api.godot_variant_new_string!!(_variant, value._variant)
    }

    constructor(value: Vector2) : this() {
        godot.api.godot_variant_new_vector2!!(_variant, value._variant)
    }

    constructor(value: Int) : this() {
        godot.api.godot_variant_new_int!!(_variant, value.toLong())
    }

    constructor(value: UInt) : this() {
        godot.api.godot_variant_new_uint!!(_variant, value.toULong())
    }

    fun asFloat(): Float {
        return godot.api.godot_variant_as_real!!(_variant).toFloat()
    }

    fun asString(): String {
        return godot.api.godot_variant_as_string!!(_variant).toKotlinString()
    }

    fun asVector2(): Vector2 {
        return Vector2(godot.api.godot_variant_as_vector2!!(_variant))
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

    internal fun getType(): godot_variant_type {
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
        fun from(value: Any?): Variant? {
            return when (value) {
                is Float -> Variant(value)
                is Double -> Variant(value.toFloat())
                is Int -> Variant(value)
                is Long -> Variant(value.toInt())
                is UInt -> Variant(value)
                is ULong -> Variant(value.toUInt())
                is String -> Variant(value)
                is Vector2 -> Variant(value)
                is Unit -> null
                else -> nil()
            }
        }

        fun nil(): Variant {
            val variant = Variant()
            godot.api.godot_variant_new_nil!!(variant._variant)
            return variant
        }
    }
}