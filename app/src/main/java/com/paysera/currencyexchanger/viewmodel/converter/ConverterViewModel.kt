package com.paysera.currencyexchanger.viewmodel.converter

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.paysera.currencyexchanger.model.db.BalanceRepository
import com.paysera.currencyexchanger.model.db.CurrencyDatabase
import com.paysera.currencyexchanger.model.db.CurrencyRepository
import com.paysera.currencyexchanger.model.db.entity.Balance
import com.paysera.currencyexchanger.model.db.entity.Currency
import com.paysera.currencyexchanger.model.networking.api.RetrofitInstance
import com.paysera.currencyexchanger.model.networking.model.ExchangeRatesResponse
import com.paysera.currencyexchanger.model.sharedpreferences.GeneralSharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ConverterViewModel

constructor(application: Application): AndroidViewModel(application) {
    private val COMMISSION_FEE = 0.7
    private val BASE_CURRENCY = "EUR"

    private val currencyRepository: CurrencyRepository
    private val balanceRepository: BalanceRepository

    var currenciesToReceive: MutableLiveData<List<Currency>> = MutableLiveData<List<Currency>>()
    var currenciesToSell: MutableLiveData<List<Balance>> = MutableLiveData<List<Balance>>()

    private val _enteredAmount = MutableStateFlow("0.00")
    val enteredAmount = _enteredAmount.asStateFlow()
    val calculatedReceivingAmount = MutableStateFlow(0.00)
    private val selectedCurrencyToSell = MutableStateFlow("")
    val selectedCurrencyToReceive = MutableStateFlow("")
    val submitMessage: MutableState<SubmitMessage> = mutableStateOf(SubmitMessage("", ""))
    private var calculatedCommission = 0.0

    init {
        val currencyDao = CurrencyDatabase.getDatabase(application).getCurrencyDao()
        val balanceDao = CurrencyDatabase.getDatabase(application).getBalanceDao()
        currencyRepository = CurrencyRepository(currencyDao)
        balanceRepository = BalanceRepository(balanceDao)

        viewModelScope.launch {
            // Inserting dummy data.
            val sharedPrefs = GeneralSharedPreferences(application)

            if (sharedPrefs.isFirstStart) {
                sharedPrefs.isFirstStart = false
                insertBalanceToDb(
                    currency = BASE_CURRENCY,
                    amount = 1000.00
                )
            }

            saveExchangeRatesFromApi(true)

            val mainHandler = Handler(Looper.getMainLooper())
            mainHandler.post(object : Runnable {
                override fun run() {
                    saveExchangeRatesFromApi(false)
                    mainHandler.postDelayed(this, 5000)
                }
            })

            val allBalances = balanceRepository.getAllBalances()

            if (allBalances.isNotEmpty()) {
                currenciesToSell.value = allBalances
                selectedCurrencyToSell.value = allBalances[0].currency.toString()
            }
        }
    }

    fun insertCurrencies(currencies: List<Currency>) {
        viewModelScope.launch {
            currencyRepository.insertCurrencies(currencies)
        }
    }

    private fun saveExchangeRatesFromApi(isPassedOnce: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                RetrofitInstance.api.getExchangeRates().enqueue(object : Callback<ExchangeRatesResponse> {
                    override fun onResponse(
                        call: Call<ExchangeRatesResponse>,
                        response: Response<ExchangeRatesResponse>
                    ) {
                        val rates = response.body()?.rates
                        val currenciesList = ArrayList<Currency>()

                        for (i in 0 until rates!!.size) {
                            val currency = Currency(
                                currency = rates.keys.elementAt(i),
                                rate = rates.getValue(rates.keys.elementAt(i))
                            )

                            currenciesList.add(currency)
                        }

                        insertCurrencies(currenciesList)

                        currenciesToReceive.value = currenciesList

                        if (isPassedOnce)
                            selectedCurrencyToReceive.value = currenciesList[0].currency.toString()
                    }

                    override fun onFailure(call: Call<ExchangeRatesResponse>, t: Throwable) {
                        failedApiAlertDialog()
                    }

                })
            } catch (e: Exception) {
                failedApiAlertDialog()
            }
        }
    }

    fun failedApiAlertDialog() {
        val msg = SubmitMessage("Currency not converted", "A problem has occurred. Please try again later.")
        submitMessage.value = msg
    }

    private suspend fun insertBalanceToDb(currency: String, amount: Double) {
        val balance = Balance(
            currency = currency,
            balance = amount
        )
        balanceRepository.insertBalance(balance)
    }

    private fun getAllBalancesFromDb() {
        viewModelScope.launch {
            currenciesToSell.value = balanceRepository.getAllBalances()
        }
    }

    fun updateEnteredValue(input: String) {
        viewModelScope.launch {
            if (input.isNotEmpty()) {
                _enteredAmount.value = input
                calculateReceivingAmount(
                    currencyToSell = selectedCurrencyToSell.value,
                    currencyToBuy = selectedCurrencyToReceive.value,
                    value = input.toDouble()
                )
            }
        }
    }

    fun updateCurrencySell(currency: String) {
        viewModelScope.launch {
            selectedCurrencyToSell.value = currency

            calculateReceivingAmount(
                currencyToSell = selectedCurrencyToSell.value,
                currencyToBuy = selectedCurrencyToReceive.value,
                value = enteredAmount.value.toDouble()
            )
        }
    }

    fun updateCurrencyReceive(currency: String) {
        viewModelScope.launch {
            selectedCurrencyToReceive.value = currency

            calculateReceivingAmount(
                currencyToSell = selectedCurrencyToSell.value,
                currencyToBuy = selectedCurrencyToReceive.value,
                value = enteredAmount.value.toDouble()
            )
        }
    }

    private fun calculateReceivingAmount(
        currencyToSell: String,
        currencyToBuy: String,
        value: Double
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val buyRate = currencyRepository.getCurrency(currencyToBuy).rate
            val endValue: Double
            val sharedPrefs = GeneralSharedPreferences(getApplication())

            val rate = if (currencyToSell == BASE_CURRENCY) {
                buyRate!!
            } else {
                // Cross exchange
                val sellRate = currencyRepository.getCurrency(currencyToSell).rate
                (1 / sellRate!!) * (buyRate!! / 1)
            }

            endValue = if (sharedPrefs.transactionCount > 5) {
                calculatedCommission = enteredAmount.value.toDouble() * COMMISSION_FEE / 100
                (value * rate) - calculatedCommission
            } else {
                calculatedCommission = 0.0
                sharedPrefs.transactionCount++
                value * rate
            }

            calculatedReceivingAmount.value = endValue
        }
    }

    fun submit() {
        var msg = SubmitMessage("", "")
        submitMessage.value = msg

        viewModelScope.launch(Dispatchers.IO) {
            val currentBalanceSold = balanceRepository.getBalance(selectedCurrencyToReceive.value)

            calculateReceivingAmount(
                currencyToSell = selectedCurrencyToSell.value,
                currencyToBuy = selectedCurrencyToReceive.value,
                value = enteredAmount.value.toDouble()
            )

            val currentBalance = balanceRepository.getBalance(selectedCurrencyToSell.value)
            val balanceLeft = currentBalance.balance?.minus(enteredAmount.value.toDouble())

            if (balanceLeft!! >= 0) {
                // Insert sold currency
                insertBalanceToDb(
                    currency = selectedCurrencyToSell.value,
                    amount = balanceLeft
                )

                // Insert received currency
                if (currentBalanceSold == null) {
                    insertBalanceToDb(
                        currency = selectedCurrencyToReceive.value,
                        amount = calculatedReceivingAmount.value
                    )
                } else {
                    insertBalanceToDb(
                        currency = selectedCurrencyToReceive.value,
                        amount = currentBalanceSold.balance!!.plus(calculatedReceivingAmount.value)
                    )
                }

                viewModelScope.launch(Dispatchers.Main) {
                    msg = SubmitMessage(
                        title = "Currency converted",
                        message = "You have converted ${String.format("%.2f", enteredAmount.value.toDouble())} ${selectedCurrencyToSell.value} " +
                                "to ${String.format("%.2f", calculatedReceivingAmount.value)} ${selectedCurrencyToReceive.value}. " +
                                "Commission Fee - ${String.format("%.2f", calculatedCommission)} ${selectedCurrencyToSell.value}."
                    )
                    submitMessage.value = msg
                }

                getAllBalancesFromDb()
            } else {
                msg = SubmitMessage(
                    "Currency not converted",
                    "You can't have balance below zero"
                )
                submitMessage.value = msg
            }
        }
    }
}