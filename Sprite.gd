extends Sprite

const SimpleTest = preload("res://sample.gdns")

onready var simpleTest: SimpleTest = $Sprite as SimpleTest

func _process(delta: float) -> void:
	simpleTest.speed *= 1.01
