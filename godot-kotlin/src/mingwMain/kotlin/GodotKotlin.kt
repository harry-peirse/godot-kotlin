import godotapi.*
import kotlinx.cinterop.*
//import platform.posix.strcpy

typealias p<T> = CPointer<T>
typealias v<T> = CValue<T>
typealias GDNativeAPI = godot_gdnative_core_api_struct

 class Vector2() {
     private val native: p<godot_vector2> = gdNative?.api?.godot_alloc!!(godot_vector2.size.toInt())!!.reinterpret()

     init {
         val nativeVariant: CPointer<godot_variant> = gdNative?.api?.godot_alloc!!(godot_variant.size.toInt())!!.reinterpret()
         gdNative?.api?.godot_variant_new_vector2!!(nativeVariant, native)
     }

    var x: Float
        get() = gdNative?.api?.godot_vector2_get_x!!(native)
        set(value) = gdNative?.api?.godot_vector2_set_x!!(native, value)

    var y: Float
        get() = gdNative?.api?.godot_vector2_get_y!!(native)
        set(value) = gdNative?.api?.godot_vector2_set_y!!(native, value)

    constructor(x: Float = 0f, y: Float = 0f) : this() {
        this.x = x
        this.y = y
    }

    constructor(nativeVector2: v<godot_vector2>) : this() {
        nativeVector2.useContents {
            this@Vector2.x = x
            this@Vector2.y = y
        }
    }


    fun abs(): Vector2 = Vector2(kotlin.math.abs(x), kotlin.math.abs(y))

    fun angle(): Float = gdNative?.api?.godot_vector2_angle!!(native)

    fun angleTo(vector: Vector2): Float = gdNative?.api?.godot_vector2_angle_to!!(native, vector.native)

    fun angleToPoint(vector: Vector2): Float = gdNative?.api?.godot_vector2_angle_to_point!!(native, vector.native)

    override fun toString() = "[$x, $y]"

    fun aspect(): Float = gdNative?.api?.godot_vector2_aspect!!(native)

     // XXX: Turns out you can't return structs by value from native methods yet.

//    fun bounce(vector: Vector2): Vector2 = Vector2(gdNative?.api?.godot_vector2_bounce!!(native, vector.native))

//    fun clamped(value: Float): Vector2 = Vector2(gdNative?.api?.godot_vector2_clamped!!(native, value))

//    fun cubicInterpolate(vector1: Vector2, vector2: Vector2, vector3: Vector2, value: Float): Vector2 =
//        Vector2(gdNative?.api?.godot_vector2_cubic_interpolate!!(native, vector1.native, vector2.native, vector3.native, value))

    fun distanceSquaredTo(vector: Vector2): Float = gdNative?.api?.godot_vector2_distance_squared_to!!(native, vector.native)

    fun distanceTo(vector: Vector2): Float = gdNative?.api?.godot_vector2_distance_to!!(native, vector.native)

    fun dot(vector: Vector2): Float = gdNative?.api?.godot_vector2_dot!!(native, vector.native)

//    fun floor(): Vector2 = Vector2(gdNative?.api?.godot_vector2_floor!!(native))

    fun isNormalized(): Boolean = gdNative?.api?.godot_vector2_is_normalized!!(native)

    fun length(): Float = gdNative?.api?.godot_vector2_length!!(native)

    fun lengthSquared(): Float = gdNative?.api?.godot_vector2_length_squared!!(native)

//    fun linearInterpolate(vector: Vector2, value: Float): Vector2 = Vector2(gdNative?.api?.godot_vector2_linear_interpolate!!(native, vector.native, value))

//    fun normalized(): Vector2 = Vector2(gdNative?.api?.godot_vector2_normalized!!(native))

    operator fun plus(vector: Vector2) {}

    operator fun div(scalar: Float) {}

    operator fun div(vector: Vector2) {}

    override fun equals(other: Any?) = false

    operator fun compareTo(other: Vector2) = 0

    operator fun times(scalar: Float) {}

    operator fun times(vector: Vector2) {}

    operator fun unaryMinus() {}

    operator fun minus(vector: Vector2) {}

    fun reflect() {}

    fun rotated() {}

    fun slide() {}

    fun snapped() {}

    fun tangent() {}

}

class GDNative(val api: GDNativeAPI) {
    fun print(value: Any?) {
        memScoped {
            val string = (value?.toString() ?: "null")
            val stringPointer = alloc<godot_string>().ptr
            api.godot_string_new!!(stringPointer)
            api.godot_string_parse_utf8_with_len!!(stringPointer, string.cstr.ptr, string.length)
            api.godot_print!!(stringPointer)
            api.godot_string_destroy!!(stringPointer)
        }
    }

    fun printAPIVersion() {
        print("GDNative API version: " + api.version.major + "." + api.version.minor)
    }
}

class NativeScript(val api: godot_gdnative_ext_nativescript_api_struct) {
    fun printAPIVersion() {
        gdNative?.print("NativeScript API version: " + api.version.major + "." + api.version.minor)
    }
}

var gdNative: GDNative? = null
var nativeScript: NativeScript? = null

@CName("godot_gdnative_init")
fun godot_gdnative_init(options: godot_gdnative_init_options) {
    gdNative = GDNative(options.api_struct!![0])
    gdNative?.print("Initializing Kotlin library.")
}

@CName("godot_gdnative_terminate")
fun godot_gdnative_terminate(options: godot_gdnative_terminate_options) {
    gdNative?.print("De-initializing Kotlin library.")
    gdNative = null
    nativeScript = null
}

@ExperimentalUnsignedTypes
@CName("godot_nativescript_init")
fun godot_nativescript_init(p_handle: COpaquePointer) {
    gdNative?.print("Initializing Kotlin-Godot nativescript.")

    for (i in 0..gdNative!!.api.num_extensions.toInt()) {
        val extension = gdNative!!.api.extensions!![i]!!
        if (extension[0].type == GDNATIVE_API_TYPES.GDNATIVE_EXT_NATIVESCRIPT.value) {
            nativeScript = NativeScript(extension.reinterpret<godot_gdnative_ext_nativescript_api_struct>()[0])
            break
        }
    }

    gdNative?.printAPIVersion()
    nativeScript?.printAPIVersion()


    val v1 = Vector2()

    gdNative?.print("v1 = " + v1)

    v1.x = 9f
    v1.y = -3f

    gdNative?.print("v1.x = 9f, v1.y = -3f")
    gdNative?.print("v1 = " + v1)

    val v2 = v1.abs()
    gdNative?.print("v2 = v1.abs()")
    gdNative?.print("v1 = " + v1)
    gdNative?.print("v2 = " + v2)

    v2.x = 1f

    gdNative?.print("v2.x = 1f")
    gdNative?.print("v1.length() = " + v1.length())
    gdNative?.print("v2.length() = " + v2.length())

    /*
    NATIVESCRIPT attempt 1

    XXX: This doesn't work yet, cannot pass struct by value in callbacks, therefore registering types with NativeScript won't work

    memScoped {

        fun simple_constructor(instance: COpaquePointer?, method_data: COpaquePointer?): COpaquePointer? {
            val user_data: COpaquePointer? = gdNative!!.api.godot_alloc!!("World from GDNative!".cstr.size)
            val p: CPointer<ByteVar> = user_data!!.reinterpret()
            strcpy(p, "World from GDNative!")

            return user_data
        }

        fun create(): CValue<godot_instance_create_func> {
            return cValue {
                create_func = staticCFunction(::simple_constructor)
            }
        }

        fun simple_destructor(instance: COpaquePointer?, method_data: COpaquePointer?, user_data: COpaquePointer?) {
            gdNative!!.api.godot_free!!(user_data)
        }

        fun destroy(): CValue<godot_instance_destroy_func> {
            return cValue {
                destroy_func = staticCFunction(::simple_destructor)
            }
        }

        fun getData(godot_object: COpaquePointer?,
                    method_data: COpaquePointer?,
                    user_data: COpaquePointer?,
                    num_args: Int,
                    args: CPointer<CPointerVar<godot_variant>>?
        ): CValue<godot_variant> {
            memScoped {
                val data: CPointer<godot_string> = alloc<godot_string>().ptr
                val ret: CValue<godot_variant> = cValue()

                val userData: CPointer<ByteVar> = user_data!!.reinterpret()

                gdNative!!.api.godot_string_new!!(data)
                gdNative!!.api.godot_string_parse_utf8!!(data, userData)
                gdNative!!.api.godot_variant_new_string!!(ret.ptr, data)
                gdNative!!.api.godot_string_destroy!!(data)

                return ret
            }
        }

        nativeScript!!.api.godot_nativescript_register_class!!(p_handle, "SIMPLE".cstr.ptr, "Reference".cstr.ptr, create(), destroy())

        val get_data: CValue<godot_instance_method> = cValue {
            method = staticCFunction(::getData)

        }

        val attributes: CValue<godot_method_attributes> = cValue {
            rpc_type = GODOT_METHOD_RPC_MODE_DISABLED
        }

        nativeScript!!.api.godot_nativescript_register_method!!(p_handle, "SIMPLE".cstr.ptr, "get_data".cstr.ptr, attributes, get_data)
    }
    */
}