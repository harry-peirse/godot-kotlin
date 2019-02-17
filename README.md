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


# Current Focus
### 17-Feb-2019
 - Having encountered the restriction of not being able to pass structs by value in callbacks when attempting to use the Nativescript API, I am going to check if the same problem might be encountered using the GDNative API anywhere.
 - Start considering the design of a v0.1 Kotlin API to wrap the native calls, to make it easier to use. 
 

# Status Report

### 17-Feb-2019
 - I have successfully called the GDNative API from Kotlin
 - I have attempted and failed to register Nativescript callbacks
 - Nativescript callbacks can't be supported as the API requires callbacks which pass structs as values, which Kotlin Native does not currently support. The Kotlin documentation implies this is may be temporary (I think the words were 'currently unavailable' but I don't see anything further on it from Googling.
 
### 15-Feb-2019
 - Godot project starts up!
 - Kotlin project compiles!
 - Godot project successfully imports binary from Kotlin project (on Windows)
 - Kotlin project println() appears in the Godot console
