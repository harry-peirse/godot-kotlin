# godot-kotlin
Very experimental Godot bindings in Kotlin

This repository contains:
 - A godot project in the root
 - A gradle/kotlin multimodule project (I used IntelliJ IDEA) under `/godot-kotlin`
   - `/godot-kotlin/lib` is ultimately what the goal is to publish. A generic set of bindings for Godot.
   - `/godot-kotlin/test-project` is the test bed and where examples can be found of usage. 
 - A git submodule to the GDNative headers repository under `/godot-kotlin/godot_headers`

It uses (at the time of writing):
 - Godot 3.2.master.stern-flowers.6e032365.730-2019-06-28T13:48:13+00:00
 - Kotlin 1.3.21
 - Gradle 5.4.1

The godot project is setup to import the binaries built by gradle.
See the [Kotlin project readme](/godot-kotlin/README.md) for details on how to build the binaries.

Note that the godot-kotlin subdirectory contains a `.gdignore` file so that Godot will not try to import the project. 
The `gdns` and `gdnlib` resource files (that are used to import the binary to godot) are therefore sitting in the root directory
currently and should not be placed in the subdirectory or they will not be visible to godot.

# Current Focus
### 10-Aug-2019
 - Rewriting Core classes to be more usable
 - Look at macro-like functionality to replace companion class 
 
# Status Report

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
