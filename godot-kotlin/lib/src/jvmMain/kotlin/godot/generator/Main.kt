package godot.generator

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.squareup.kotlinpoet.*
import java.io.File

const val PACKAGE = "godot"
const val INTERNAL_PACKAGE = "godot.internal"

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

    val collector = SignatureCollector()

    val built = content.parallelStream().map { it.parse(content, collector) }

    built.forEach {
        File(genSrcDir, "${it.name}.kt").writeText(it.toString())
    }

    File(genSrcDir, "__ICalls.kt").writeText(collector.parse().toString())

    File(genSrcDir, "__Bindings.kt").writeText(FileSpec.builder(PACKAGE, "__Bindings")
            .addImport("kotlinx.cinterop", "invoke")
            .addFunction(FunSpec.builder("_registerTypes")
                    .addModifiers(KModifier.INTERNAL)
                    .addAnnotation(AnnotationSpec.builder(ClassName("kotlin", "UseExperimental"))
                            .addMember("ExperimentalUnsignedTypes::class")
                            .build())
                    .addCode(CodeBlock.builder()
                            .apply {
                                content.map {
                                    add(it.parseRegisterCall())
                                }
                            }
                            .build())
                    .build())
            .addFunction(FunSpec.builder("_initMethodBindings")
                    .addModifiers(KModifier.INTERNAL)
                    .addAnnotation(AnnotationSpec.builder(ClassName("kotlin", "UseExperimental"))
                            .addMember("ExperimentalUnsignedTypes::class")
                            .build())
                    .addCode(CodeBlock.builder()
                            .apply {
                                content.map {
                                    add(it.parseBindingCall())
                                }
                            }
                            .build())
                    .build())
            .build()
            .toString())

    println("Complete.")
}