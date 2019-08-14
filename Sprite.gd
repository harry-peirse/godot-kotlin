extends Sprite

const SimpleTest = preload("res://sample.gdns")

onready var simpleTest: SimpleTest = $Sprite as SimpleTest

func _process(delta: float) -> void:
	simpleTest.speed *= 1.00001


func _on_Sprite_direction_changed(direction, position):
	var dir: String
	match direction:
		1: dir = "WEST"
		2: dir = "EAST"
		3: dir = "NORTH"
		4: dir = "SOUTH"
		_: dir = "NONE"
	print("Changed direction to " + dir + " at " + str(position))
