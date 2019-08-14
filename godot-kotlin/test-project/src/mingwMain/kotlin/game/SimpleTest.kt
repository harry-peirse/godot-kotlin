package game

import godot.Input_
import godot.Sprite
import godot.Variant

class SimpleTest : Sprite() {

    var speed = 120f
    var lastDirection = 0

    override fun _process(delta: Float) {
        val frameSpeed = delta * speed
        val newDirection: Int
        val position = position.apply {
            when {
                Input_.isActionPressed("move_left") -> {
                    newDirection = 1
                    x -= frameSpeed
                }
                Input_.isActionPressed("move_right") -> {
                    newDirection = 2
                    x += frameSpeed
                }
                Input_.isActionPressed("move_up") -> {
                    newDirection = 3
                    y -= frameSpeed
                }
                Input_.isActionPressed("move_down") -> {
                    newDirection = 4
                    y += frameSpeed
                }
                else -> newDirection = 0
            }
        }

        setPosition(position)

        if (newDirection != lastDirection) {
            emitSignal("direction_changed", Variant(position))
            lastDirection = newDirection
        }
    }

    fun sayHello() {
        godot.print("Hello world!")
    }

    fun whatsMyName(): String {
        return "Bobby Brown"
    }
}