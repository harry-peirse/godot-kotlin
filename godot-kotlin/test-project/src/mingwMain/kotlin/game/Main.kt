package game

import godot.*
import kotlinx.cinterop.invoke
import kotlinx.cinterop.pointed
import kotlinx.cinterop.reinterpret
import kotlin.math.cos
import kotlin.math.sin

@CName(GDNATIVE_INIT)
fun gdNativeInit(options: GDNativeInitOptions) {
    Godot.print("gdNativeInit(${options.in_editor})")
    Godot.gdNativeInit(options)
}

@CName(GDNATIVE_TERMINATE)
fun gdNativeTerminate(options: GDNativeTerminateOptions) {
    Godot.print("gdNativeTerminate(${options.in_editor})")
    Godot.gdNativeTerminate(options)
}

@CName(NATIVESCRIPT_INIT)
fun nativescriptInit(handle: NativescriptHandle) {
    Godot.print("nativescriptInit")
    Godot.nativeScriptInit(handle)

    Godot.registerClass<Sample, Sprite>(Sample.Companion::registerMethods)
}

class Sample : Sprite() {

    var timePassed: Float = 0f

    @CName("godot_init")
    override fun _init() {
        Godot.print("Init the Sample!!")
    }

    @CName("godot_process")
    override fun _process(delta: Float) {
        Godot.print("Process with delta $delta from the Sample!!")

        timePassed += delta

        val newPosition = Godot.api.godot_alloc!!(Vector2.size.toInt())!!.reinterpret<Vector2>()
        Godot.api.godot_vector2_new!!(newPosition, 10f + 10f * sin(timePassed * 2f), 10f + 10f * cos(timePassed * 1.5f))
        setPosition(newPosition.pointed)
    }

    companion object {
        fun registerMethods() {
            Godot.registerMethod(Sample::_process)
        }
    }
}