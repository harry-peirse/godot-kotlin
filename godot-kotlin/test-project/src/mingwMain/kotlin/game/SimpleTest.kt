package game

import godot.Input_
import godot.Sprite

class SimpleTest : Sprite() {

    var speed = 120f
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

        godot.print("")
        godot.print("1  $position")

        position.apply {
            when (newDirection) {
                1 -> x -= frameSpeed
                2 -> x += frameSpeed
                3 -> y -= frameSpeed
                4 -> y += frameSpeed
            }
        }
        godot.print("2  $position")

        setPosition(position)
        godot.print("3  $position")

        if (newDirection != lastDirection) {
            emitSignal("direction_changed", newDirection, position)
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