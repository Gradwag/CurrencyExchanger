package com.paysera.currencyexchanger.view.common.composable

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun BalanceComposable(currency: String, balance: String) {
    Text(
        text = "$balance $currency",
        textAlign = TextAlign.Center,
        fontSize = 18.sp,
    )
}