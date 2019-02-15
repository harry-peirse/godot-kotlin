import godotapi.*
import kotlinx.cinterop.*

private var api: CPointer<godot_gdnative_core_api_struct>? = null

@ExperimentalUnsignedTypes
@CName("godot_gdnative_init")
fun godot_gdnative_init(options: godot_gdnative_init_options) {
    println("Initializing Kotlin library.")
    api = options.api_struct
}

@CName("godot_gdnative_terminate")
fun godot_gdnative_terminate(options: godot_gdnative_terminate_options) {
    println("De-initializing Kotlin library.")
    api = null
}

@CName("godot_nativescript_init")
fun godot_nativescript_init(p_handle: COpaquePointer) {
    println("Initializing Kotlin-Godot nativescript.")
}