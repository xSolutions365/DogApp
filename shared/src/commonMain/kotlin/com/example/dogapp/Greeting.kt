package com.example.dogapp

class Greeting(private val platform: Platform) {

    fun greet(): String {
        return sayHello(platform.name)
    }
}
