package game

import godot.Vector2
import godot.api
import godotapi.godot_gdnative_init_options
import godotapi.godot_gdnative_terminate_options
import kotlinx.cinterop.COpaquePointer

@CName("godot_gdnative_init")
fun gdNativeInit(options: godot_gdnative_init_options) {
    godot.gdNativeInit(options)
}

@CName("godot_gdnative_terminate")
fun gdNativeTerminate(options: godot_gdnative_terminate_options) {
    godot.gdNativeTerminate(options)
}

@ExperimentalUnsignedTypes
@CName("godot_nativescript_init")
fun nativeScriptInit(handle: COpaquePointer) {
    godot.nativeScriptInit(handle)

    val v = Vector2(3f,1f)
    api.print(v)
}