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


fun _SimpleTest_process(godotObject: COpaquePointer?,
                        methodData: COpaquePointer?,
                        userData: COpaquePointer?,
                        numArgs: Int,
                        args: CPointer<CPointerVar<Variant>>?
): CValue<Variant> = memScoped {
    val delta = godot.api.godot_variant_as_real!!(args?.pointed?.value).toFloat()
    userData!!.asStableRef<SimpleTest>().get()._process(delta)
    return cValue()
}

fun _SimpleTest_new(instance: COpaquePointer?, method_data: COpaquePointer?): COpaquePointer? {
    val wrapped = godot.api.godot_alloc!!(_Wrapped.size.toInt())!!.reinterpret<_Wrapped>().pointed
    wrapped._owner = instance
    wrapped._typeTag = SimpleTest.getTypeTag()

    val simpleTest = SimpleTest._new()
    simpleTest._wrapped = wrapped.ptr

    return StableRef.create(simpleTest).asCPointer()
}

class SimpleTest : Sprite() {

    var timePassed: Float = 0f

    override fun _process(delta: Float) {
        timePassed += delta
        val newPosition: CPointer<Vector2> = godot.api.godot_alloc!!(Vector2.size.toInt())!!.reinterpret()
        val x: Float = 10f + 10f * sin(timePassed * 2f)
        val y: Float = 10f + 10f * cos(timePassed * 1.5f)
        godot.api.godot_vector2_new!!(newPosition, x, y)

        setPosition(newPosition.pointed)
    }

    companion object _GODOT_CLASS : GODOT_CLASS<SimpleTest, Sprite> {
        override val type = SimpleTest::class
        override val baseType = Sprite::class
        override fun _new() = SimpleTest()
        override fun registerMethods() {
            registerMethod("_process", staticCFunction(::_SimpleTest_process))
        }
    }
}