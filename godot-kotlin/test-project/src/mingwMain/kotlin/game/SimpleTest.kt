package game

import godot.Input_
import godot.Sprite

class SimpleTest : Sprite() {

    var speed = 200f
    var lastDirection = 0

    override fun _process(delta: Float) {
        val newDirection = when {
            Input_.isActionPressed("move_left") -> 1
            Input_.isActionPressed("move_right") -> 2
            Input_.isActionPressed("move_up") -> 3
            Input_.isActionPressed("move_down") -> 4
            else -> 0
        }

        val frameSpeed = delta * speed
        val position = getPosition()
        when (newDirection) {
            1 -> position.x -= frameSpeed
            2 -> position.x += frameSpeed
            3 -> position.y -= frameSpeed
            4 -> position.y += frameSpeed
        }
        setPosition(position)

        if (newDirection != lastDirection) {
            emitSignal("direction_changed", newDirection, position)
            lastDirection = newDirection
        }

        position.dispose()
    }

    fun sayHello() {
        godot.print("Hello world!")
    }

    fun whatsMyName(): String {
        return "Bobby Brown"
    }
}