package com.paysera.currencyexchanger.view.converter

import AlertDialogComposable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.paysera.currencyexchanger.R
import com.paysera.currencyexchanger.model.db.entity.Balance
import com.paysera.currencyexchanger.model.db.entity.Currency
import com.paysera.currencyexchanger.ui.theme.CurrencyExchangerTheme
import com.paysera.currencyexchanger.view.common.composable.BalanceComposable
import com.paysera.currencyexchanger.viewmodel.converter.ConverterViewModel
import com.paysera.currencyexchanger.viewmodel.converter.SubmitMessage
import java.util.*

class ConverterActivity : ComponentActivity() {
    private lateinit var converterViewModel: ConverterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            converterViewModel = ViewModelProvider(this)[ConverterViewModel::class.java]

            val currenciesToReceive by converterViewModel.currenciesToReceive.observeAsState(initial = emptyList())
            val currenciesToSell by converterViewModel.currenciesToSell.observeAsState(initial = emptyList())

            val submitMessage = converterViewModel.submitMessage.value
            val openDialog = remember { mutableStateOf(false)  }

            if (submitMessage.title.isNotEmpty() && submitMessage.message.isNotEmpty()) {
                openDialog.value = true

                AlertDialogComposable(
                    openDialog,
                    submitMessage.title,
                    submitMessage.message
                )
            }

            CurrencyExchangerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 15.dp,
                                end = 15.dp,
                                top = 25.dp,
                                bottom = 25.dp
                            )
                    ) {
                        Balances(currenciesToSell)
                        Spacer(modifier = Modifier.height(30.dp))
                        CurrencyExchange(
                            currenciesListToReceive = currenciesToReceive,
                            currenciesToSell = currenciesToSell,
                            converterViewModel = converterViewModel
                        )
                        Spacer(modifier = Modifier.height(30.dp))
                        Submit(converterViewModel, submitMessage)
                    }
                }
            }
        }
    }
}

@Composable
fun Balances(currenciesToSell: List<Balance>) {
    Text(
        text = stringResource(R.string.converter_section_balances_title).uppercase(Locale.getDefault()),
        color = Color.Gray,
        fontSize = 12.sp
    )

    Spacer(modifier = Modifier.height(30.dp))

    Row(modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState())
    ) {
        for (i in currenciesToSell.indices) {
            val balance = currenciesToSell[i].balance!!

            BalanceComposable(
                currency = currenciesToSell[i].currency!!,
                balance = String.format(
                    "%.2f",
                    balance
                )
            )

            Spacer(modifier = Modifier.width(20.dp))
        }
    }
}

@Composable
fun CurrencyExchange(
    currenciesListToReceive: List<Currency>,
    currenciesToSell: List<Balance>,
    converterViewModel: ConverterViewModel
) {
    Text(
        text = stringResource(R.string.converter_section_currency_exchange).uppercase(Locale.getDefault()),
        color = Color.Gray,
        fontSize = 12.sp
    )

    Spacer(Modifier.height(12.dp))

    // Sell
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
     ) {
        Image(
            painter = painterResource(id = R.drawable.ic_sell),
            contentDescription = stringResource(R.string.image_content_description_sell)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = stringResource(R.string.field_sell_title),
            fontSize = 17.sp
        )

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        TextField(
            modifier = Modifier.width(80.dp),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
            ),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Start),
            value = converterViewModel.enteredAmount.collectAsState().value,
            onValueChange = {
                converterViewModel.updateEnteredValue(it)
            },
        )

        Spacer(modifier = Modifier.width(20.dp))

        var expanded by remember { mutableStateOf(false) }
        var selectedIndex by remember { mutableStateOf(0) }

        if (currenciesToSell.isNotEmpty()) {
            Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                currenciesToSell[selectedIndex].currency?.let {
                    Text(
                        modifier = Modifier.clickable(onClick = { expanded = true }),
                        text = it,
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    currenciesToSell.forEachIndexed { index, s ->
                        DropdownMenuItem(onClick = {
                            selectedIndex = index
                            expanded = false

                            s.currency?.let {
                                converterViewModel.updateCurrencySell(it)
                            }
                        }) {
                            s.currency?.let {
                                Text(text = it)
                            }
                        }
                    }
                }
            }
        }
    }

    Divider(
        modifier = Modifier.padding(
            start = 60.dp,
            end = 60.dp,
            top = 7.dp,
            bottom = 7.dp
        )
    )

    // Receive
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_receive),
            contentDescription = stringResource(R.string.image_content_description_receive)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = stringResource(R.string.field_receive_title),
            fontSize = 17.sp
        )

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        val formattedCalculatedValue = String.format(
            "%.2f",
            converterViewModel.calculatedReceivingAmount.collectAsState().value
        )

        Text(
            modifier = Modifier.width(80.dp),
            text ="+ $formattedCalculatedValue" ,
            textAlign = TextAlign.Start,
            color = colorResource(id = R.color.main_green)
        )

        Spacer(modifier = Modifier.width(20.dp))

        var expanded by remember { mutableStateOf(false) }
        var selectedIndex by remember { mutableStateOf(0) }

        if (currenciesListToReceive.isNotEmpty()) {
            Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                currenciesListToReceive[selectedIndex].currency?.let {
                    Text(
                        modifier = Modifier.clickable(onClick = { expanded = true }),
                        text = it,
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    currenciesListToReceive.forEachIndexed { index, s ->
                        DropdownMenuItem(onClick = {
                            selectedIndex = index
                            expanded = false

                            s.currency?.let {
                                converterViewModel.updateCurrencyReceive(it)
                            }
                        }) {
                            s.currency?.let {
                                Text(text = it)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Submit(
    converterViewModel: ConverterViewModel,
    submitMessage: SubmitMessage?
) {
    val openDialog = remember { mutableStateOf(false)  }
    val coroutineScope = rememberCoroutineScope()

    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(
                start = 20.dp,
                end = 20.dp
            ),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.main_blue)),
        onClick = {
            converterViewModel.submit()
            openDialog.value = true
        }
    ) {
        Text(
            text = stringResource(R.string.converter_button_submit).uppercase(Locale.getDefault()),
            color = Color.White,
            textAlign = TextAlign.Center,
            fontSize = 15.sp
        )

        if (submitMessage != null) {
//            AlertDialogComposable(
//                openDialog,
//                submitMessage.title,
//                submitMessage.message
//            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CurrencyExchangerTheme {

    }
}