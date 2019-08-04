package game

import godot.*
import godotapi.GODOT_METHOD_RPC_MODE_DISABLED
import godotapi.godot_string
import godotapi.godot_variant
import kotlinx.cinterop.*
import platform.posix.strcpy

@CName(GDNATIVE_INIT)
fun gdNativeInit(options: GDNativeInitOptions) {
    try {
        godot.gdNativeInit(options)
        godot.print("gdNativeInit(${options.in_editor})")
    } catch (e: Exception) {
        println(e.message)
        e.printStackTrace()
    }
}

@CName(GDNATIVE_TERMINATE)
fun gdNativeTerminate(options: GDNativeTerminateOptions) {
    try {
        godot.gdNativeTerminate(options)
        godot.print("gdNativeTerminate(${options.in_editor})")
    } catch (e: Exception) {
        println(e.message)
        e.printStackTrace()
    }
}

@CName(NATIVESCRIPT_INIT)
fun nativescriptInit(handle: NativescriptHandle) {
    try {
        godot.nativeScriptInit(handle)
        godot.print("nativescriptInit")

        godot.registerClass(SimpleTest._GODOT_CLASS)
    } catch (e: Exception) {
        println(e.message)
        e.printStackTrace()
    }
}

class SimpleTest : Node() {
    companion object _GODOT_CLASS : GODOT_CLASS<SimpleTest, Node> {
        @CName("godot_get_data")
        fun getData(godot_object: COpaquePointer?,
                    method_data: COpaquePointer?,
                    user_data: COpaquePointer?,
                    num_args: Int,
                    args: CPointer<CPointerVar<godot_variant>>?
        ): CValue<godot_variant> {
            val data: CPointer<godot_string> = godot.api.godot_alloc!!(godot_string.size.toInt())!!.reinterpret()
            val userData: CPointer<ByteVar> = user_data!!.reinterpret()
            godot.api.godot_string_new!!(data)
            godot.api.godot_string_parse_utf8!!(data, userData)

            val ret: CPointer<godot_variant> = godot.api.godot_alloc!!(godot_variant.size.toInt())!!.reinterpret()
            godot.api.godot_variant_new_string!!(ret, data)
            godot.api.godot_string_destroy!!(data)

            return ret.pointed.readValue()
        }

        fun simple_constructor(instance: COpaquePointer?, method_data: COpaquePointer?): COpaquePointer? {
            val user_data: COpaquePointer? = godot.api.godot_alloc!!("World from GDNative!".cstr.size)
            val p: CPointer<ByteVar> = user_data!!.reinterpret()
            strcpy(p, "World from GDNative!")

            return user_data
        }

        override val type = SimpleTest::class
        override val baseType = Node::class
        override fun _new() = SimpleTest()
        override fun registerMethods() {
            registerMethod(::getData)
        }
    }
}