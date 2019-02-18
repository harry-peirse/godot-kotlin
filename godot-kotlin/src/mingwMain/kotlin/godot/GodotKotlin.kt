package godot

import godotapi.*
import kotlinx.cinterop.*

class GDNative(val api: godot_gdnative_core_api_struct,
               val options: godot_gdnative_init_options) {
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

class NativeScript(val api: godot_gdnative_ext_nativescript_api_struct,
                   val handle: COpaquePointer) {
    fun printAPIVersion() {
        gdNative?.print("NativeScript API version: " + api.version.major + "." + api.version.minor)
    }
}

var gdNative: GDNative? = null
var nativeScript: NativeScript? = null

@CName("godot_gdnative_init")
fun godot_gdnative_init(options: godot_gdnative_init_options) {
    gdNative = GDNative(options.api_struct!![0], options)
    gdNative?.print("Initializing Kotlin library. In editor: ${options.in_editor}")
}

@CName("godot_gdnative_terminate")
fun godot_gdnative_terminate(options: godot_gdnative_terminate_options) {
    gdNative?.print("De-initializing Kotlin library. In editor: ${options.in_editor}")
    gdNative = null
    nativeScript = null
}

@ExperimentalUnsignedTypes
@CName("godot_nativescript_init")
fun godot_nativescript_init(handle: COpaquePointer) {
    gdNative?.print("Initializing Kotlin-Godot nativescript.")

    for (i in 0..gdNative!!.api.num_extensions.toInt()) {
        val extension = gdNative!!.api.extensions!![i]!!
        if (extension[0].type == GDNATIVE_API_TYPES.GDNATIVE_EXT_NATIVESCRIPT.value) {
            nativeScript =
                NativeScript(extension.reinterpret<godot_gdnative_ext_nativescript_api_struct>()[0], handle)
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
}