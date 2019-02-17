import godotapi.*
import kotlinx.cinterop.*
//import platform.posix.strcpy

class GDNative(val api: godot_gdnative_core_api_struct) {
    fun print(value: String) {
        memScoped {
            val stringPointer = alloc<godot_string>().ptr
            api.godot_string_new!!(stringPointer)
            api.godot_string_parse_utf8_with_len!!(stringPointer, value.cstr.ptr, value.length)
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