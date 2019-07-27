package game

import godot.*
import kotlinx.cinterop.invoke
import kotlinx.cinterop.pointed
import kotlinx.cinterop.reinterpret
import kotlin.math.cos
import kotlin.math.sin

@CName(GDNATIVE_INIT)
fun gdNativeInit(options: GDNativeInitOptions) {
    Godot.gdNativeInit(options)
}

@CName(GDNATIVE_TERMINATE)
fun gdNativeTerminate(options: GDNativeTerminateOptions) {
    Godot.gdNativeTerminate(options)
}

@CName(NATIVESCRIPT_INIT)
fun nativeScriptInit(handle: NativescriptHandle) {
    Godot.nativeScriptInit(handle)

    Godot.registerClass<Sample, Sprite>(Sample.Companion::registerMethods)
}

class Sample : Sprite() {

    var timePassed: Float = 0f

    override fun _init() {
        Godot.print("Init the Sample!!")
    }

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