package godot.generator

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

const val PACKAGE = "godot"

fun createDir(dirName: String) {
    val dir = File(dirName)
    if (dir.exists()) {
        dir.deleteRecursively()
    }
    dir.mkdirs()
}

fun main() {
    println("Generating Godot native class bindings...")
    val genSrcDir = System.getenv("GEN_SRC_DIR") + "/$PACKAGE"
    createDir(genSrcDir)

    val content: List<GClass> = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()
            .fromJson(File("godot_headers/api.json").readText(), (object : TypeToken<List<GClass>>() {}).type)

    content.forEach {
        File(genSrcDir, "${it.name}.kt").writeText(it.parse().toString())
    }

    println("Complete.")
}