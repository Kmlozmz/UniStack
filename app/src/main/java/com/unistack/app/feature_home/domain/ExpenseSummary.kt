package com.unistack.app.feature_home.domain

data class ExpenseSummary(
    val transport: Int,
    val food: Int,
    val chartValues: List<Int>
)
