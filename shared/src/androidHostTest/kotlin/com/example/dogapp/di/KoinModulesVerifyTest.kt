package com.example.dogapp.di

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify
import kotlin.test.Test

class KoinModulesVerifyTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun appModulesDeclareEveryDependency() {
        module { includes(appModules) }.verify()
    }
}
