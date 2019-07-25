package godot.generator

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asClassName
import java.io.File
import kotlin.reflect.KClass

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
            .fromJson(File("godot_headers/api.json").readText(), (object : TypeToken<List<GClass>>(){}).type)
    File(genSrcDir, "raw.txt").writeText(content.toString())

    File(genSrcDir, "output.kt").writeText(content.map(GClass::parse).toString())

    val typeNames: Map<String, TypeName> = extractTypeNames(content)
    File(genSrcDir, "type_names.txt").writeText(typeNames.toString())

    println("Complete.")
}

fun extractTypeNames(content: List<GClass>): Map<String, TypeName> {
    val typeNames: HashMap<String, TypeName> = HashMap()

    content.forEach {clazz ->
        if(!clazz.baseClass.isBlank()) typeNames[clazz.baseClass] = ClassName(PACKAGE, clazz.baseClass)
        if(!clazz.name.isBlank()) typeNames[clazz.name] = ClassName(PACKAGE, clazz.name)
        clazz.properties.forEach { property ->
            if(!property.type.isBlank()) typeNames[property.type] = ClassName(PACKAGE, property.type)
        }
        clazz.signals.forEach { signal ->
            signal.arguments.forEach { argument ->
                if(!argument.type.isBlank()) typeNames[argument.type] = ClassName(PACKAGE, argument.type)
            }
        }
        clazz.methods.forEach { method ->
            if(!method.returnType.isBlank()) typeNames[method.returnType] = ClassName(PACKAGE, method.returnType)
            method.arguments.forEach { argument ->
                if(!argument.type.isBlank()) typeNames[argument.type] = ClassName(PACKAGE, argument.type)
            }
        }
    }

    return typeNames.mapValues {(key, value) ->
        when (key) {
            "void" -> Unit::class.asClassName()
            "float" -> Float::class.asClassName()
            "int" -> Int::class.asClassName()
            "bool" -> Boolean::class.asClassName()
            else -> value
        }
    }
}