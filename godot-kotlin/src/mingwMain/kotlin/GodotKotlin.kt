import godotapi.*
import kotlinx.cinterop.*

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
        if(extension[0].type == GDNATIVE_API_TYPES.GDNATIVE_EXT_NATIVESCRIPT.value) {
            nativeScript = NativeScript(extension.reinterpret<godot_gdnative_ext_nativescript_api_struct>()[0])
            break
        }
    }

    gdNative?.printAPIVersion()
    nativeScript?.printAPIVersion()
}