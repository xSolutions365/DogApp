package com.example.dogapp.di

import com.example.dogapp.Greeting
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val sharedModule = module {
    singleOf(::Greeting)
}
