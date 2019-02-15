# godot-kotlin
Very experimental Godot bindings in Kotlin

This repository contains:
 - A godot project in the root
 - A gradle/kotlin project (I used IntelliJ IDEA) under `/godot-kotlin`
 - A git submodule to the GDNative headers repository under `/godot-kotlin/godot_headers`

It uses (at the time of writing):
 - Godot 3.1
 - Kotlin 1.3
 - Gradle 5.2.1

The godot project is setup to import the binaries built by gradle.
See the [Kotlin project readme](/godot-kotlin/README.md) for details on how to build the binaries.

Note that the godot-kotlin subdirectory contains a `.gdignore` file so that Godot will not try to import the project. 
The `gdns` and `gdnlib` resource files (that are used to import the binary to godot) are therefore sitting in the root directory
currently and should not be placed in the subdirectory or they will not be visible to godot.
