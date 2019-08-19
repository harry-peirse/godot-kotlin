# godot-kotlin

## What is this

Still very experimental Godot bindings in Kotlin, so you can write kotlin native code and have it run in your Godot game

## Yeah, but what is this

This repository contains:
 - A godot project in the root for testing
 - A gradle/kotlin multimodule project under `/godot-kotlin`
   - `/godot-kotlin/lib` is ultimately what the goal is to publish. A set of bindings for Godot in Kotlin.
   - `/godot-kotlin/test-project` is the test bed and where examples can be found of usage. This is what we want a user of this library to have their code look like. It should be as similar to GDScript and the Godot documentation as possible.
 - A git submodule to the GDNative headers repository under `/godot-kotlin/godot_headers` <- where the C bindings are generated from. There is also some code generation that happens using kotlinpoet to help out with Nodes and Objects and things.

It uses (at the time of writing):
 - Godot 3.1
 - Kotlin 1.3.41
 - Gradle 5.5.1

The godot project is setup to import the binaries built by gradle.
See the [Kotlin project readme](/godot-kotlin/README.md) for details on how to build the binaries.

Note that the godot-kotlin subdirectory contains a `.gdignore` file so that Godot will not try to import the project. 
The `gdns` and `gdnlib` resource files (that are used to import the binary to godot) are therefore sitting in the root directory
currently and should not be placed in the subdirectory or they will not be visible to godot.

## Current Focus
### 17-Aug-2019
 - Writing out the logic for core classes
 - Need to look at compilation time. It's obscene (~7 minutes!!!) and not useful to anyone. Not sure what can be done as it's probably all konan related... maybe a fundamental flaw in the project as it makes the quick iteration of change and test that GDScript allows basically impossible. Time to get philosophical? What is the best use of Kotlin here?
 
## Status Report

### 17-Aug-2019
 - Lots of little bug fixes
 - I fixed all the memory leaks Godot reported! All other allocs are memscoped so I think we're good (risk: passing memscoped things elsewhere where they are remembered)
 - Lib is much much easier to use. Arrays, Dictionaries, Strings etc all use the native Kotlin version (Array, MutableMap, String)
 - Variants are much better done. Goal is a user mostly doesn't need to care about them (similar to GDScript)
 - Signals working!

### 10-Aug-2019
 - Varargs generated in Godot classes 
 - Kotlin classes can be used - kind of - instead of stateless static functions everywhere!

### 09-Aug-2019
 - More or less restarted from scratch but much better progress now
 - Class generation working pretty well thanks to some inspiration from @ejektaflex!
 - Have got a successful child of Node working

### 06-Jul-2019
 - After a long absence and an update to Kotlin Native I now have overcome the earlier problems with NativeScript and have a working call!
 - A somewhat tested Vector2 class has been created in Kotlin as a start on some Kotlin-coder friendly bindings.

### 17-Feb-2019
 - I have successfully called the GDNative API from Kotlin
 - I have attempted and failed to register Nativescript callbacks
 - Nativescript callbacks can't be supported as the API requires callbacks which pass structs as values, which Kotlin Native does not currently support. The Kotlin documentation implies this is may be temporary (I think the words were 'currently unavailable' but I don't see anything further on it from Googling.
 
### 15-Feb-2019
 - Godot project starts up!
 - Kotlin project compiles!
 - Godot project successfully imports binary from Kotlin project (on Windows)
 - Kotlin project println() appears in the Godot console
