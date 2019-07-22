package ejektaflex.kotdot.generator.json.reg

import com.google.gson.Gson
import ejektaflex.kotdot.generator.json.structure.GodotClass
import java.io.File

private val apifile = File("godot_headers/api.json")

private val content = Gson().fromJson(apifile.readText(), Array<GodotClass>::class.java)

object GodotClassRegistry : SimpleRegistry<String, GodotClass>(content.associateBy { it.name }.toMutableMap()) {
    init {

        // On creation of class registry, link class/method/property hierarchy
        for ((name, clazz) in delegate) {

            // Link class heirarchy
            if (clazz.baseClass in delegate) {
                clazz.baseClassGodot = delegate[clazz.baseClass]
                //println("$name->${clazz.baseClassGodot?.name}")
            }

            // Link methods to classes
            for (method in clazz.methods) {
                method.parentClass = clazz
            }

            // Link properties to classes
            for (property in clazz.properties) {
                property.parentClass = clazz
            }

        }

    }
}