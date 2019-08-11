extends Sprite

const SimpleTest = preload("res://sample.gdns")

onready var simpleTest: SimpleTest = $Sprite as SimpleTest

func _ready():
	print(simpleTest)
	print(get_parent())
	print(simpleTest.get_parent())
	print(simpleTest.say_hello())
	print(simpleTest.whats_my_name())
	print("Hello!")
