package game

import godot.BoundClass
import godot.Sprite
import godot.internal.godot_variant_type

@CName(godot.GDNATIVE_INIT)
fun gdNativeInit(options: godot.GDNativeInitOptions) {
    try {
        godot.gdNativeInit(options)
        godot.print("gdNativeInit(${options.in_editor})")
    } catch (e: Exception) {
        println(e.message)
        e.printStackTrace()
    }
}

@CName(godot.GDNATIVE_TERMINATE)
fun gdNativeTerminate(options: godot.GDNativeTerminateOptions) {
    try {
        godot.gdNativeTerminate(options)
        godot.print("gdNativeTerminate(${options.in_editor})")
    } catch (e: Exception) {
        println(e.message)
        e.printStackTrace()
    }
}

@CName(godot.NATIVESCRIPT_INIT)
fun nativescriptInit(handle: godot.NativescriptHandle) {
    try {
        godot.nativeScriptInit(handle)
        godot.print("nativescriptInit")

        godot.registerClass(BoundClass(SimpleTest::class, Sprite::class, { -> SimpleTest()}) {
            godot.registerMethod("_process", SimpleTest::_process)
            godot.registerMethod("say_hello", SimpleTest::sayHello)
            godot.registerMethod("whats_my_name", SimpleTest::whatsMyName)
            godot.registerProperty("speed", SimpleTest::speed, 120f)
            godot.registerSignal<SimpleTest>("direction_changed", "position" to godot_variant_type.GODOT_VARIANT_TYPE_VECTOR2)
        })
    } catch (e: Exception) {
        println(e.message)
        e.printStackTrace()
    }
}