import godotapi.*
import kotlinx.cinterop.*

class GodotAPI(val api: godot_gdnative_core_api_struct) {
    fun print(value: String) {
        memScoped {
            val stringPointer = alloc<godot_string>().ptr
            api.godot_string_new!!(stringPointer)
            api.godot_string_parse_utf8_with_len!!(stringPointer, value.cstr.ptr, value.length)
            api.godot_print!!(stringPointer)
            api.godot_string_destroy!!(stringPointer)
        }
    }
}

var godot: GodotAPI? = null

@ExperimentalUnsignedTypes
@CName("godot_gdnative_init")
fun godot_gdnative_init(options: godot_gdnative_init_options) {
    godot = GodotAPI(options.api_struct!![0])
    godot?.print("Initializing Kotlin library.")
}

@CName("godot_gdnative_terminate")
fun godot_gdnative_terminate(options: godot_gdnative_terminate_options) {
    godot?.print("De-initializing Kotlin library.")
    godot = null
}

@CName("godot_nativescript_init")
fun godot_nativescript_init(p_handle: COpaquePointer) {
    godot?.print("Initializing Kotlin-Godot nativescript.")
    godot?.print("Now what should we do?")
}