package game

import godot.GodotClass
import godot.Sprite
import godot.Vector2
import kotlin.math.cos
import kotlin.math.sin

class SimpleTest : Sprite() {

    var timePassed: Float = 0f

    override fun _init() {
        timePassed = 0f
    }

    override fun _process(delta: Float) {
        timePassed += delta

        val x = 10 + 10 * sin(timePassed * 2)
        val y = 10 + 10 * cos(timePassed * 1.5f)
        val newPosition = Vector2(x, y)

        setPosition(newPosition)
    }

    fun sayHello() {
        godot.print("Hello world!")
    }

    fun whatsMyName(): String {
        return "Bobby Brown"
    }

    companion object : GodotClass {
        override val type = SimpleTest::class
        override val baseType = Sprite::class
        override fun new() = SimpleTest()
        override fun registerMethods() {
            godot.registerMethod("_process", SimpleTest::_process)
            godot.registerMethod("say_hello", SimpleTest::sayHello)
            godot.registerMethod("whats_my_name", SimpleTest::whatsMyName)
        }
    }
}