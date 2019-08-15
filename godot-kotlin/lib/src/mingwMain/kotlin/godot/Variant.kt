package godot

import godot.internal.godot_variant
import kotlinx.cinterop.*
import kotlin.reflect.KClass

@UseExperimental(ExperimentalUnsignedTypes::class)
class Variant internal constructor(val _raw: CPointer<godot_variant>) : Comparable<Variant> {
    enum class Type(val type: KClass<out Any>) {

        NIL(Any::class),

        // atomic types
        BOOL(Boolean::class),
        INT(Int::class),
        FLOAT(Float::class),
        STRING(String::class),

        // math types

        VECTOR2(Vector2::class), // 5
        RECT2(Rect2::class),
        VECTOR3(Vector3::class),
        TRANSFORM2D(Transform2D::class),
        PLANE(Plane::class),
        QUAT(Quat::class), // 10
        AABB(AABB::class),
        BASIS(Basis::class),
        TRANSFORM(Transform::class),

        // misc types
        COLOR(Color::class),
        NODE_PATH(NodePath::class), // 15
        _RID(RID::class),
        OBJECT(Object::class),
        DICTIONARY(MutableMap::class),
        ARRAY(Array<Variant>::class),

        // arrays
        POOL_BYTE_ARRAY(PoolByteArray::class), // 20
        POOL_INT_ARRAY(PoolIntArray::class),
        POOL_FLOAT_ARRAY(PoolFloatArray::class),
        POOL_STRING_ARRAY(PoolStringArray::class),
        POOL_VECTOR2_ARRAY(PoolVector2Array::class),
        POOL_VECTOR3_ARRAY(PoolVector3Array::class), // 25
        POOL_COLOR_ARRAY(PoolColorArray::class),

        VARIANT_MAX(Nothing::class);

        val value = ordinal.toUInt()

        companion object {
            fun byValue(value: UInt) = values()[value.toInt()]
        }
    }

    enum class Operator {

        //comparation
        OP_EQUAL,
        OP_NOT_EQUAL,
        OP_LESS,
        OP_LESS_EQUAL,
        OP_GREATER,
        OP_GREATER_EQUAL,

        //mathematic
        OP_ADD,
        OP_SUBSTRACT,
        OP_MULTIPLY,
        OP_DIVIDE,
        OP_NEGATE,
        OP_POSITIVE,
        OP_MODULE,
        OP_STRING_CONCAT,

        //bitwise
        OP_SHIFT_LEFT,
        OP_SHIFT_RIGHT,
        OP_BIT_AND,
        OP_BIT_OR,
        OP_BIT_XOR,
        OP_BIT_NEGATE,

        //logic
        OP_AND,
        OP_OR,
        OP_XOR,
        OP_NOT,

        //containment
        OP_IN,
        OP_MAX;

        val value = ordinal.toUInt()

        companion object {
            fun byValue(value: UInt) = values()[value.toInt()]
        }
    }

    internal constructor(_raw: CValue<godot_variant>) : this(_raw.place(godotAlloc()))

    constructor() : this(godotAlloc()) {
        api.godot_variant_new_nil!!(_raw)
    }

    constructor(value: Variant) : this(godotAlloc()) {
        api.godot_variant_new_copy!!(_raw, value._raw)
    }

    constructor(value: Boolean) : this(godotAlloc()) {
        api.godot_variant_new_bool!!(_raw, value)
    }

    constructor(value: Long) : this(godotAlloc()) {
        api.godot_variant_new_int!!(_raw, value)
    }

    constructor(value: Int) : this(value.toLong())

    constructor(value: Short) : this(value.toLong())

    constructor(value: Char) : this(value.toLong())

    constructor(value: Double) : this(godotAlloc()) {
        api.godot_variant_new_real!!(_raw, value)
    }

    constructor(value: Float) : this(value.toDouble())

    constructor(value: ULong) : this(godotAlloc()) {
        api.godot_variant_new_uint!!(_raw, value)
    }

    constructor(value: UInt) : this(value.toULong())

    constructor(value: UShort) : this(value.toULong())

    constructor(value: String) : this(godotAlloc()) {
        memScoped {
            api.godot_variant_new_string!!(_raw, value.toGString(this))
        }
    }

    constructor(value: Vector2) : this(godotAlloc()) {
        memScoped {
            api.godot_variant_new_vector2!!(_raw, value._raw(this))
        }
    }

    constructor(value: Rect2) : this(godotAlloc()) {
        memScoped {
            api.godot_variant_new_rect2!!(_raw, value._raw(this))
        }
    }

    constructor(value: Vector3) : this(godotAlloc()) {
        memScoped {
            api.godot_variant_new_vector3!!(_raw, value._raw(this))
        }
    }

    constructor(value: Plane) : this(godotAlloc()) {
        memScoped {
            api.godot_variant_new_plane!!(_raw, value._raw(this))
        }
    }

    constructor(value: AABB) : this(godotAlloc()) {
        memScoped {
            api.godot_variant_new_aabb!!(_raw, value._raw(this))
        }
    }

    constructor(value: Quat) : this(godotAlloc()) {
        memScoped {
            api.godot_variant_new_quat!!(_raw, value._raw(this))
        }
    }

    constructor(value: Basis) : this(godotAlloc()) {
        memScoped {
            api.godot_variant_new_basis!!(_raw, value._raw(this))
        }
    }

    constructor(value: Transform2D) : this(godotAlloc()) {
        memScoped {
            api.godot_variant_new_transform2d!!(_raw, value._raw(this))
        }
    }

    constructor(value: Transform) : this(godotAlloc()) {
        memScoped {
            api.godot_variant_new_transform!!(_raw, value._raw(this))
        }
    }

    constructor(value: Color) : this(godotAlloc()) {
        memScoped {
            api.godot_variant_new_color!!(_raw, value._raw(this))
        }
    }

    constructor(value: NodePath) : this(godotAlloc()) {
        api.godot_variant_new_node_path!!(_raw, value.raw)
    }

    constructor(value: RID) : this(godotAlloc()) {
        memScoped {
            api.godot_variant_new_rid!!(_raw, value._raw(this))
        }
    }

    constructor(value: Object) : this(godotAlloc()) {
        memScoped {
            api.godot_variant_new_object!!(_raw, value._raw)
        }
    }

    constructor(value: MutableMap<Variant, Variant>) : this(godotAlloc()) {
        memScoped {
            api.godot_variant_new_dictionary!!(_raw, value.toGDictionary(this))
        }
    }

    constructor(value: Array<Variant>) : this(godotAlloc()) {
        memScoped {
            api.godot_variant_new_array!!(_raw, value.toGArray(this))
        }
    }

    constructor(value: PoolByteArray) : this(godotAlloc()) {
        api.godot_variant_new_pool_byte_array!!(_raw, value._raw)
    }

    constructor(value: PoolIntArray) : this(godotAlloc()) {
        api.godot_variant_new_pool_int_array!!(_raw, value._raw)
    }

    constructor(value: PoolFloatArray) : this(godotAlloc()) {
        api.godot_variant_new_pool_real_array!!(_raw, value._raw)
    }

    constructor(value: PoolStringArray) : this(godotAlloc()) {
        api.godot_variant_new_pool_string_array!!(_raw, value._raw)
    }

    constructor(value: PoolVector2Array) : this(godotAlloc()) {
        api.godot_variant_new_pool_vector2_array!!(_raw, value._raw)
    }

    constructor(value: PoolVector3Array) : this(godotAlloc()) {
        api.godot_variant_new_pool_vector3_array!!(_raw, value._raw)
    }

    constructor(value: PoolColorArray) : this(godotAlloc()) {
        api.godot_variant_new_pool_color_array!!(_raw, value._raw)
    }

    fun toBoolean(): Boolean {
        return api.godot_variant_booleanize!!(_raw)
    }

    fun toLong(): Long {
        return api.godot_variant_as_int!!(_raw)
    }

    fun toInt(): Int = toLong().toInt()

    fun toShort(): Short = toLong().toShort()

    fun toChar(): Char = toLong().toChar()

    fun toULong(): ULong {
        return api.godot_variant_as_uint!!(_raw)
    }

    fun toUInt(): UInt = toULong().toUInt()

    fun toUShort(): UShort = toULong().toUShort()

    fun toDouble(): Double {
        return api.godot_variant_as_real!!(_raw)
    }

    fun toFloat(): Float = toDouble().toFloat()

    override fun toString(): String = memScoped {
        return api.godot_variant_as_string!!(_raw).ptr.toKString()
    }

    fun toVector2(): Vector2 = memScoped {
        Vector2(api.godot_variant_as_vector2!!(_raw).ptr)
    }

    fun toRect2(): Rect2 = memScoped {
        Rect2(api.godot_variant_as_rect2!!(_raw).ptr)
    }

    fun toVector3(): Vector3 = memScoped {
        Vector3(api.godot_variant_as_vector3!!(_raw).ptr)
    }

    fun toPlane(): Plane = memScoped {
        Plane(api.godot_variant_as_plane!!(_raw).ptr)
    }

    fun toAABB(): AABB = memScoped {
        AABB(api.godot_variant_as_aabb!!(_raw).ptr)
    }

    fun toQuat(): Quat = memScoped {
        Quat(api.godot_variant_as_quat!!(_raw).ptr)
    }

    fun toBasis(): Basis = memScoped {
        Basis(api.godot_variant_as_basis!!(_raw).ptr)
    }

    fun toTransform(): Transform = memScoped {
        Transform(api.godot_variant_as_transform!!(_raw).ptr)
    }

    fun toTransform2D(): Transform2D = memScoped {
        Transform2D(api.godot_variant_as_transform2d!!(_raw).ptr)
    }

    fun toColor(): Color = memScoped {
        Color(api.godot_variant_as_color!!(_raw).ptr)
    }

    fun toNodePath(): NodePath = memScoped {
        NodePath(api.godot_variant_as_node_path!!(_raw).ptr)
    }

    fun toRID(): RID = memScoped {
        RID(api.godot_variant_as_rid!!(_raw).ptr)
    }

    fun toObject(): Object {
        return Object.getFromVariant(_raw)
    }

    fun toMutableMap(): MutableMap<Variant, Variant> = memScoped {
        return api.godot_variant_as_dictionary!!(_raw).ptr.toKMutableMap()
    }

    fun toArray(): Array<Variant> = memScoped {
        return api.godot_variant_as_array!!(_raw).ptr.toKArray()
    }

    fun toPoolByteArray(): PoolByteArray {
        return PoolByteArray(api.godot_variant_as_pool_byte_array!!(_raw))
    }

    fun toPoolIntArray(): PoolIntArray {
        return PoolIntArray(api.godot_variant_as_pool_int_array!!(_raw))
    }

    fun toPoolFloatArray(): PoolFloatArray {
        return PoolFloatArray(api.godot_variant_as_pool_real_array!!(_raw))
    }

    fun toPoolStringArray(): PoolStringArray {
        return PoolStringArray(api.godot_variant_as_pool_string_array!!(_raw))
    }

    fun toPoolVector2Array(): PoolVector2Array {
        return PoolVector2Array(api.godot_variant_as_pool_vector2_array!!(_raw))
    }

    fun toPoolVector3Array(): PoolVector3Array {
        return PoolVector3Array(api.godot_variant_as_pool_vector3_array!!(_raw))
    }

    fun toPoolColorArray(): PoolColorArray {
        return PoolColorArray(api.godot_variant_as_pool_color_array!!(_raw))
    }

    fun getType(): Type {
        return Type.values()[godot.api.godot_variant_get_type!!(_raw).ordinal]
    }

    fun call(method: String, args: Variant, argCount: Int): Variant = memScoped {
        Variant(api.godot_variant_call!!(_raw, method.toGString(this), args._raw.reinterpret(), argCount, null))
    }

    fun hasMethod(method: String): Boolean = memScoped {
        api.godot_variant_has_method!!(_raw, method.toGString(this))
    }

    override fun hashCode(): Int {
        return _raw.pointed.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Variant -> api.godot_variant_operator_equal!!(_raw, other._raw)
            else -> false
        }
    }

    override operator fun compareTo(other: Variant): Int {
        return when {
            equals(other) -> 0
            api.godot_variant_operator_less!!(_raw, other._raw) -> -1
            else -> 1
        }
    }

    fun dispose() {
        api.godot_variant_destroy!!(_raw)
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <T : Any> to(type: KClass<T>): T {
        return when (type) {
            Variant::class -> this as T
            Boolean::class -> toBoolean() as T
            Long::class -> toLong() as T
            Int::class -> toInt() as T
            Short::class -> toShort() as T
            Char::class -> toChar() as T
            Double::class -> toDouble() as T
            Float::class -> toFloat() as T
            ULong::class -> toULong() as T
            UInt::class -> toUInt() as T
            UShort::class -> toUShort() as T
            String::class -> toString() as T
            Vector2::class -> toVector2() as T
            Rect2::class -> toRect2() as T
            Vector3::class -> toVector3() as T
            Plane::class -> toPlane() as T
            AABB::class -> toAABB() as T
            Quat::class -> toQuat() as T
            Basis::class -> toBasis() as T
            Transform2D::class -> toTransform2D() as T
            Transform::class -> toTransform() as T
            Color::class -> toColor() as T
            NodePath::class -> toNodePath() as T
            RID::class -> toRID() as T
            Object::class -> toObject() as T
            MutableMap::class -> toMutableMap() as T
            Array<Variant>::class -> toArray() as T
            PoolByteArray::class -> toPoolByteArray() as T
            PoolIntArray::class -> toPoolIntArray() as T
            PoolFloatArray::class -> toPoolFloatArray() as T
            PoolStringArray::class -> toPoolStringArray() as T
            PoolVector2Array::class -> toPoolVector2Array() as T
            PoolVector3Array::class -> toPoolVector3Array() as T
            PoolColorArray::class -> toPoolColorArray() as T
            else -> {
                godot.printError("Cannot cast Variant to $type")
                throw UnsupportedOperationException("Cannot cast Variant to $type")
            }
        }
    }

    @UseExperimental(ExperimentalUnsignedTypes::class)
    companion object {

        private fun godotAlloc(): CPointer<godot_variant> {
            return godot.api.godot_alloc!!(godot_variant.size.toInt())!!.reinterpret()
        }

        @Suppress("UNCHECKED_CAST")
        internal fun <T : Any> of(value: T): Variant {
            return when (value) {
                is Variant -> Variant(value)
                is Boolean -> Variant(value)
                is Short -> Variant(value)
                is Int -> Variant(value)
                is Long -> Variant(value)
                is Char -> Variant(value)
                is Float -> Variant(value)
                is Double -> Variant(value)
                is UShort -> Variant(value)
                is UInt -> Variant(value)
                is ULong -> Variant(value)
                is String -> Variant(value)
                is Vector2 -> Variant(value)
                is Rect2 -> Variant(value)
                is Vector3 -> Variant(value)
                is Plane -> Variant(value)
                is AABB -> Variant(value)
                is Quat -> Variant(value)
                is Basis -> Variant(value)
                is Transform2D -> Variant(value)
                is Transform -> Variant(value)
                is Color -> Variant(value)
                is NodePath -> Variant(value)
                is RID -> Variant(value)
                is Object -> Variant(value)
                is MutableMap<*, *> -> Variant(value as MutableMap<Variant, Variant>)
                is Array<*> -> Variant(value as Array<Variant>)
                is PoolByteArray -> Variant(value)
                is PoolIntArray -> Variant(value)
                is PoolFloatArray -> Variant(value)
                is PoolStringArray -> Variant(value)
                is PoolVector2Array -> Variant(value)
                is PoolVector3Array -> Variant(value)
                is PoolColorArray -> Variant(value)
                else -> {
                    godot.printError("Cannot create Variant of type ${value::class.simpleName}")
                    throw UnsupportedOperationException("Cannot create Variant of type ${value::class.simpleName}")
                }
            }
        }
    }
}
