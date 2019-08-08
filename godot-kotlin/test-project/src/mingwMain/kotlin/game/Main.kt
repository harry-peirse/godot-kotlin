package game

import godot.*
import kotlinx.cinterop.*
import kotlin.math.cos
import kotlin.math.sin

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

        godot.registerClass(SimpleTest._GODOT_CLASS, staticCFunction(::_SimpleTest_new))
    } catch (e: Exception) {
        println(e.message)
        e.printStackTrace()
    }
}

var timePassed: Float = 0f

@CName("godot_process")
fun _SimpleTest_process(godotObject: COpaquePointer?,
                        methodData: COpaquePointer?,
                        userData: COpaquePointer?,
                        numArgs: Int,
                        args: CPointer<CPointerVar<godot_variant>>?
): CValue<godot_variant> = memScoped {
    try {
        godot.print("enter: SimpleTest_process")

        val delta = godot.api.godot_variant_as_real!!(args?.pointed?.value).toFloat()
        timePassed += delta

        godot.print("SimpleTest_process: delta is $delta, timePassed is $timePassed")

        val newPosition = godot.api.godot_alloc!!(Vector2.size.toInt())?.reinterpret<Vector2>()
        godot.api.godot_vector2_new!!(newPosition, 10f + 10f * sin(timePassed * 2f), 10f + 10f * cos(timePassed * 1.5f))

        godot.print("SimpleTest_process: newPosition is $newPosition")

        setPosition(newPosition!!.pointed, userData!!.reinterpret())

        godot.print("SimpleTest_process: sprite position is updated!")

        godot.print("exit: SimpleTest_process")
        return cValue()
    } catch (e: Exception) {
        println(e.message)
        e.printStackTrace()
        throw e
    }
}

@UseExperimental(ExperimentalUnsignedTypes::class)
fun setPosition(position: Vector2, _wrapped: CPointer<_Wrapped>?) {
    memScoped {
        godot.print("setPosition: $_wrapped")
        godot.print("setPosition: ${_wrapped?.pointed}")
        godot.print("setPosition: ${_wrapped?.pointed?._owner}")
        val args: CPointer<COpaquePointerVar> = allocArray(1)
        godot.print("set args: $args")
        val positionStableRef = StableRef.create(position)
        godot.print("stableref: $positionStableRef")
        args[0] = positionStableRef.asCPointer()
        godot.print("set stableref: ${args.get(0)}")
        godot.api.godot_method_bind_ptrcall!!(Node2D.Companion.mb.setPosition, _wrapped?.pointed?._owner, args, null)
        godot.print("made call!")
        positionStableRef.dispose()
        godot.print("dispose")
    }
}

fun _SimpleTest_new(instance: COpaquePointer?, method_data: COpaquePointer?): COpaquePointer? {
    return godot.api.godot_alloc!!(_Wrapped.size.toInt())
}

class SimpleTest : Sprite() {
    companion object _GODOT_CLASS : GODOT_CLASS<SimpleTest, Sprite> {
        override val type = SimpleTest::class
        override val baseType = Sprite::class
        override fun _new() = SimpleTest()
        override fun registerMethods() {
            registerMethod("_process", staticCFunction(::_SimpleTest_process))
        }
    }
}