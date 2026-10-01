package com.example.dogapp.core.data.breed

private const val KILOGRAMS_PER_POUND = 0.45359237
private const val CENTIMETRES_PER_INCH = 2.54

fun poundsToKilograms(pounds: Double): Double = pounds * KILOGRAMS_PER_POUND

fun inchesToCentimetres(inches: Double): Double = inches * CENTIMETRES_PER_INCH
