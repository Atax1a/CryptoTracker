package com.diplom.petproject.Model

data class CurrencyModel(
    val currencyName: String,
    val currencyCode: String,
    val currencyPrice: Double,
    val percentChangeDay: Double,
    val percentChangeHour: Double,
    val percentChangeWeek: Double,
    val lastUpdated: String,
    val id: String,
    var imageUrl: String = ""
)

