package game

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

        godot.registerClass(SimpleTest)
    } catch (e: Exception) {
        println(e.message)
        e.printStackTrace()
    }
}