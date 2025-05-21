package com.example.myapplication

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object bonus_card {
    private val _balance = MutableStateFlow(0)
    val balance: StateFlow<Int> = _balance

    fun addPoints(points: Int) {
        _balance.value += points
    }

    fun resetBalance() {
        _balance.value = 0
    }
}