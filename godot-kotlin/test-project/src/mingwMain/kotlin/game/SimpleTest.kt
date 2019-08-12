package game

import godot.GodotString
import godot.Input
import godot.Variant
import godot.internal.godot_variant_type

class SimpleTest : godot.Sprite() {

    var speed = 120f
    var lastDirection = 0

    override fun _process(delta: Float) {
        val frameSpeed = delta * speed
        val newDirection: Int
        val position = position.apply {
            when {
                Input().isActionPressed(GodotString("move_left")) -> {
                    newDirection = 1
                    x -= frameSpeed
                }
                Input().isActionPressed(GodotString("move_right")) -> {
                    newDirection = 2
                    x += frameSpeed
                }
                Input().isActionPressed(GodotString("move_up")) -> {
                    newDirection = 3
                    y -= frameSpeed
                }
                Input().isActionPressed(GodotString("move_down")) -> {
                    newDirection = 4
                    y += frameSpeed
                }
                else -> newDirection = 0
            }
        }

        setPosition(position)

        if (newDirection != lastDirection) {
            emitSignal(GodotString("direction_changed"), Variant(position))
            lastDirection = newDirection
        }
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
            godot.registerSignal<SimpleTest>("direction_changed", "position" to godot_variant_type.GODOT_VARIANT_TYPE_VECTOR2)
        }
    }
}