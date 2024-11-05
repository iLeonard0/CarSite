package com.example.myapitest.model


data class Car(
    val id: String,
    val imageUrl: String,
    val year: String,
    val name: String,
    val licence: String,
    val place: Place)


data class CarDto(
    val id: String,
    val value: Car )

data class Place(
    val lat: Double,
    val long: Double
)