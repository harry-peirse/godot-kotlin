package godot

import godot.internal.godot_string
import godot.internal.godot_variant
import kotlinx.cinterop.*

@UseExperimental(ExperimentalUnsignedTypes::class)
class Variant : Comparable<Variant>, Core<godot_variant> {
    enum class Type {

        NIL,

        // atomic types
        BOOL,
        INT,
        REAL,
        STRING,

        // math types

        VECTOR2, // 5
        RECT2,
        VECTOR3,
        TRANSFORM2D,
        PLANE,
        QUAT, // 10
        RECT3, //sorry naming convention fail :( not like it's used often
        BASIS,
        TRANSFORM,

        // misc types
        COLOR,
        NODE_PATH, // 15
        _RID,
        OBJECT,
        DICTIONARY,
        ARRAY,

        // arrays
        POOL_BYTE_ARRAY, // 20
        POOL_INT_ARRAY,
        POOL_REAL_ARRAY,
        POOL_STRING_ARRAY,
        POOL_VECTOR2_ARRAY,
        POOL_VECTOR3_ARRAY, // 25
        POOL_COLOR_ARRAY,

        VARIANT_MAX

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
        OP_MAX

    }

    override val _raw: CPointer<godot_variant> = godot.alloc(godot_variant.size)

    constructor() {
        godot.api.godot_variant_new_nil!!(_raw)
    }

    constructor(value: Variant) {
        godot.api.godot_variant_new_copy!!(_raw, value._raw)
    }

    constructor(value: Boolean) {
        godot.api.godot_variant_new_bool!!(_raw, value)
    }

    constructor(value: Long) {
        godot.api.godot_variant_new_int!!(_raw, value)
    }

    constructor(value: Int) : this(value.toLong())
    constructor(value: Short) : this(value.toLong())
    constructor(value: Char) : this(value.toLong())

    constructor(value: Double) {
        godot.api.godot_variant_new_real!!(_raw, value)
    }

    constructor(value: Float) : this(value.toDouble())

    constructor(value: ULong) {
        godot.api.godot_variant_new_uint!!(_raw, value)
    }

    constructor(value: UInt) : this(value.toULong())
    constructor(value: UShort) : this(value.toULong())

    constructor(value: String) {
        memScoped {
            val _string = alloc<godot_string>()
            godot.api.godot_string_new!!(_string.ptr)
            godot.api.godot_string_parse_utf8!!(_string.ptr, value.cstr.ptr)
            godot.api.godot_variant_new_string!!(_raw, _string.ptr)
        }
    }

    constructor(value: Vector2) {
        godot.api.godot_variant_new_vector2!!(_raw, value._raw)
    }

    constructor(value: Rect2) {
        godot.api.godot_variant_new_rect2!!(_raw, value._raw)
    }

    constructor(value: Vector3) {
        godot.api.godot_variant_new_vector3!!(_raw, value._raw)
    }

    constructor(value: Plane) {
        godot.api.godot_variant_new_plane!!(_raw, value._raw)
    }

    constructor(value: AABB) {
        godot.api.godot_variant_new_aabb!!(_raw, value._raw)
    }

    constructor(value: Quat) {
        godot.api.godot_variant_new_quat!!(_raw, value._raw)
    }

    constructor(value: Basis) {
        godot.api.godot_variant_new_basis!!(_raw, value._raw)
    }

    constructor(value: Transform2D) {
        godot.api.godot_variant_new_transform2d!!(_raw, value._raw)
    }

    constructor(value: Transform) {
        godot.api.godot_variant_new_transform!!(_raw, value._raw)
    }

    constructor(value: Color) {
        godot.api.godot_variant_new_color!!(_raw, value._raw)
    }

    constructor(value: NodePath) {
        godot.api.godot_variant_new_node_path!!(_raw, value._raw)
    }

    constructor(value: RID) {
        godot.api.godot_variant_new_rid!!(_raw, value._raw)
    }

    constructor(value: Object) {
        godot.api.godot_variant_new_object!!(_raw, value._raw)
    }

    constructor(value: GodotDictionary) {
        godot.api.godot_variant_new_dictionary!!(_raw, value._raw)
    }

    constructor(value: GodotArray) {
        godot.api.godot_variant_new_array!!(_raw, value._raw)
    }

    constructor(value: PoolByteArray) {
        godot.api.godot_variant_new_pool_byte_array!!(_raw, value._raw)
    }

    constructor(value: PoolIntArray) {
        godot.api.godot_variant_new_pool_int_array!!(_raw, value._raw)
    }

    constructor(value: PoolFloatArray) {
        godot.api.godot_variant_new_pool_real_array!!(_raw, value._raw)
    }

    constructor(value: PoolStringArray) {
        godot.api.godot_variant_new_pool_string_array!!(_raw, value._raw)
    }

    constructor(value: PoolVector2Array) {
        godot.api.godot_variant_new_pool_vector2_array!!(_raw, value._raw)
    }

    constructor(value: PoolVector3Array) {
        godot.api.godot_variant_new_pool_vector3_array!!(_raw, value._raw)
    }

    constructor(value: PoolColorArray) {
        godot.api.godot_variant_new_pool_color_array!!(_raw, value._raw)
    }

    override fun equals(other: Any?): Boolean = TODO()

    fun toBoolean(): Boolean = TODO()
    fun toInt(): Int = TODO()
    fun toFloat(): Float = TODO()
    override fun toString(): String = TODO()
    fun toVector2(): Vector2 = TODO()
    fun toRect2(): Rect2 = TODO()
    fun toVector3(): Vector3 = TODO()
    fun toPlane(): Plane = TODO()
    fun toAABB(): AABB = TODO()
    fun toQuat(): Quat = TODO()
    fun toBasis(): Basis = TODO()
    fun toTransform(): Transform = TODO()
    fun toTransform2D(): Transform2D = TODO()
    fun toColor(): Color = TODO()
    fun toNodePath(): NodePath = TODO()
    fun toRID(): RID = TODO()
    inline fun <reified T : Object> toObject(): T {
//        return T::getFromVariant(this)
        TODO()
    }

    fun toDictionary(): MutableMap<Variant, Any?> = TODO()
    fun toArray(): Array<Variant> = TODO()

    fun toPoolByteArray(): PoolByteArray = TODO()
    fun toPoolIntArray(): PoolIntArray = TODO()
    fun toPoolFloatArray(): PoolFloatArray = TODO()
    fun toPoolStringArray(): PoolStringArray = TODO()
    fun toPoolVector2Array(): PoolVector2Array = TODO()
    fun toPoolVector3Array(): PoolVector3Array = TODO()
    fun toPoolColorArray(): PoolColorArray = TODO()

    fun getType(): Type = TODO()

    fun call(method: String, args: Variant, argCount: Int): Variant = TODO()

    fun hasMethod(method: String): Boolean = TODO()

    override fun hashCode(): Int {
        TODO()
    }

    override fun compareTo(other: Variant): Int {
        TODO()
    }

    fun booleanize(): Boolean = TODO()
}


//package godot
//
//import godot.internal.godot_variant
//import godot.internal.godot_variant_type
//import kotlinx.cinterop.CPointer
//import kotlinx.cinterop.StableRef
//import kotlinx.cinterop.invoke
//import kotlinx.cinterop.memScoped
//import kotlin.reflect.KClass
//
//@UseExperimental(ExperimentalUnsignedTypes::class)
//open class Variant internal constructor() {
//
//    internal lateinit var _variant: CPointer<godot_variant>
//
//    val _type: UInt
//        get() = godot_variant_type.GODOT_VARIANT_TYPE_OBJECT.value
//
//    fun asFloat(): Float {
//        return godot.api.godot_variant_as_real!!(_raw).toFloat()
//    }
//
//    fun asString(): String {
//        memScoped {
//            return GodotString(godot.api.godot_variant_as_string!!(_raw).ptr).string
//        }
//    }
//
//    fun asVector2(): Vector2 {
//        memScoped {
//            return Vector2(godot.api.godot_variant_as_vector2!!(_raw).ptr)
//        }
//    }
//
//    fun asInt(): Int {
//        return godot.api.godot_variant_as_int!!(_raw).toInt()
//    }
//
//    fun asUInt(): UInt {
//        return godot.api.godot_variant_as_uint!!(_raw).toUInt()
//    }
//
//    @Suppress("UNCHECKED_CAST")
//    fun <T : Any> cast(type: KClass<T>): T {
//        val result: Any = when (type) {
//            Float::class -> this.asFloat()
//            String::class -> this.asString()
//            Vector2::class -> this.asVector2()
//            Int::class -> this.asInt()
//            UInt::class -> this.asUInt()
//            else -> throw UnsupportedOperationException("Could not cast Variant to $type")
//        }
//        return result as T
//    }
//
//    internal fun _getType(): godot_variant_type {
//        return godot.api.godot_variant_get_type!!(_raw)
//    }
//
//    private var stableRef: StableRef<Variant>? = null
//
//    internal fun asStableRef(): StableRef<Variant>? {
//        if (stableRef == null) {
//            stableRef = StableRef.create(this)
//        }
//        return stableRef
//    }
//
//    internal fun dispose() {
//        stableRef?.dispose()
//    }
//
//    companion object {
//
//        fun <T : Any?> from(value: T): Variant? {
//            TODO()
//        }
//
//        inline fun <reified T : Variant> create(_raw: CPointer<godot_variant>): T {
//            TODO()
//        }
//
//        fun <T : S, S : Object> create(_raw: CPointer<godot_variant>, boundClass: BoundClass<T, S>): T {
//            TODO()
//        }
//    }
//}