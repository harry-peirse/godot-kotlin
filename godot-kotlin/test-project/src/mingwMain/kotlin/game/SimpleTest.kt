package game

import godot.GodotString
import godot.Input

class SimpleTest : godot.Sprite() {

    var speed = 120f

    override fun _process(delta: Float) {
        val frameSpeed = delta * speed
        val position = position.apply {
            when {
                Input().isActionPressed(GodotString("move_left")) -> x -= frameSpeed
                Input().isActionPressed(GodotString("move_right")) -> x += frameSpeed
                Input().isActionPressed(GodotString("move_up")) -> y -= frameSpeed
                Input().isActionPressed(GodotString("move_down")) -> y += frameSpeed
            }
        }
        setPosition(position)
    }

    fun sayHello() {
        godot.print("Hello world!")
    }

    fun whatsMyName(): String {
        return "Bobby Brown"
    }

    companion object : godot.GodotClass {
        override val type = SimpleTest::class
        override val baseType = godot.Sprite::class
        override fun new() = SimpleTest()
        override fun registerMethods() {
            godot.registerMethod("_process", SimpleTest::_process)
            godot.registerMethod("say_hello", SimpleTest::sayHello)
            godot.registerMethod("whats_my_name", SimpleTest::whatsMyName)
            godot.registerProperty("speed", SimpleTest::speed, 120f)
        }
    }
}