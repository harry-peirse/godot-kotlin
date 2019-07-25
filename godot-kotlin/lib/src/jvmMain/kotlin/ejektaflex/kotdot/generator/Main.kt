package ejektaflex.kotdot.generator

import ejektaflex.kotdot.generator.json.core.CoreClass
import ejektaflex.kotdot.generator.json.reg.CoreClassRegistry
import ejektaflex.kotdot.generator.json.reg.GodotClassRegistry
import ejektaflex.kotdot.generator.json.structure.GodotClass
import java.io.File

val genSrcDir = System.getenv("GEN_SRC_DIR") + "/"

fun main() {
    createDir("godot")

    GodotClassRegistry
            .forEach(::writeGodotClass)
//    CoreClassRegistry
//            .forEach(::writeCoreClass)
}

fun createDir(fileName: String) {
    val dir = File(genSrcDir + fileName)
    if (dir.exists()) {
        dir.delete()
    }
    dir.mkdirs()
}

fun writeGodotClass(entry: Map.Entry<String, GodotClass>) {
    val file = File(genSrcDir + "godot/" + entry.key + ".kt")
    if (file.exists()) {
        file.delete()
    }
    file.createNewFile()
    file.writeText(entry.value.generate())
}

fun writeCoreClass(entry: Map.Entry<String, CoreClass>) {
    val file = File(genSrcDir + "godot/" + entry.key + ".kt")
    if (file.exists()) {
        file.delete()
    }
    file.createNewFile()
    file.writeText(entry.value.generate())
}