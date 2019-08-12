extends Sprite

const SimpleTest = preload("res://sample.gdns")

onready var simpleTest: SimpleTest = $Sprite as SimpleTest

func _process(delta: float) -> void:
	simpleTest.speed *= 1.00001


func _on_Sprite_direction_changed(position):
	print("Changed direction at " + str(position))
