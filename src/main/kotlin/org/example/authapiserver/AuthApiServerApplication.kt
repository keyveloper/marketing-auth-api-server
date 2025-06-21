package org.example.authapiserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AuthApiServerApplication

fun main(args: Array<String>) {
    runApplication<AuthApiServerApplication>(*args)
}
