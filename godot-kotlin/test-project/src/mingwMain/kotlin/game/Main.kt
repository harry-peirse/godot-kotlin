package game

import godot.test
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

    test()
}