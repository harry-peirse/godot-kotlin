package game

import godot.*
import kotlinx.cinterop.invoke
import kotlinx.cinterop.pointed
import kotlinx.cinterop.reinterpret
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

        test()

//        Godot.registerClass<Sample, Sprite>(Sample.Companion::registerMethods)
    } catch (e: Exception) {
        println(e.message)
        e.printStackTrace()
    }
}

class Sample : Sprite() {

    var timePassed: Float = 0f

    @CName("godot_init")
    override fun _init() {
        godot.print("Init the Sample!!")
    }

    @CName("godot_process")
    override fun _process(delta: Float) {
        godot.print("Process with delta $delta from the Sample!!")

        timePassed += delta

        val newPosition = godot.api.godot_alloc!!(Vector2.size.toInt())!!.reinterpret<Vector2>()
        godot.api.godot_vector2_new!!(newPosition, 10f + 10f * sin(timePassed * 2f), 10f + 10f * cos(timePassed * 1.5f))
        setPosition(newPosition.pointed)
    }

    companion object {
        fun registerMethods() {
            godot.registerMethod(Sample::_process)
        }
    }
}