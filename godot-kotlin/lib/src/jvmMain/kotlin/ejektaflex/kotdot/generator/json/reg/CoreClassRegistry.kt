package ejektaflex.kotdot.generator.json.reg

import com.google.gson.Gson
import ejektaflex.kotdot.generator.json.core.CoreClass
import ejektaflex.kotdot.generator.json.core.CoreGlobal
import ejektaflex.kotdot.generator.json.core.GDNativeAPIObject
import java.io.File

private val apifile = File("godot_headers/gdnative_api.json")

private val content = Gson().fromJson(apifile.readText(), GDNativeAPIObject::class.java)

object CoreClassRegistry : SimpleRegistry<String, CoreClass>() {
    init {

        // Grab all core functions that create a new class
        val regex = Regex("(godot_[a-z_0-9]+)_new.*")
        val newFuncs = content.allMethods.groupBy { regex.matchEntire(it.name)?.groupValues?.get(1) }

        // Grab the names of said class for each grouping
        val clNames = newFuncs.keys.filterNotNull()

        // Group functions by class
        val groupedFuncs = content.allMethods.groupBy {method ->
            clNames.filter { it in method.name }.maxBy { it.length }
        }

        // Fill registry with CoreClasses, add methods to each core class
        for (funcGroup in groupedFuncs) {
            if (funcGroup.key == null) {
                CoreGlobal.methods.addAll(funcGroup.value)
            } else {
                insert(funcGroup.key!!, CoreClass(funcGroup.key!!).apply {
                    methods.addAll(funcGroup.value)
                    methods.forEach { it.parentClass = this }
                })
            }
        }


    }
}