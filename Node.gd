extends Node

onready var Simple = preload("res://SIMPLE.gdns")

func _ready():
	var data = Simple.new()
	var result = data.get_data()
	print(result)
