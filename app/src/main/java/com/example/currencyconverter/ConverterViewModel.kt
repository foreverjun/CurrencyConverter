package com.example.currencyconverter

import io.reactivex.rxjava3.schedulers.Schedulers
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.observers.DisposableSingleObserver

sealed interface ConverterStatus {
    object Loading : ConverterStatus
    data class Error(val msg: String) : ConverterStatus
    data class Success(val data: ConverterResponse) : ConverterStatus
}

sealed interface CurrenciesStatus {
    object Loading : CurrenciesStatus
    data class Error(val msg: String) : CurrenciesStatus
    data class Success(val data: Map<String, String>) : CurrenciesStatus
}

class ConverterViewModel : ViewModel() {

    var fromAmount by mutableStateOf("0")
        private set
    var fromCurrency by mutableStateOf("")
    var toCurrency by mutableStateOf("")
    var converterStatus by mutableStateOf<ConverterStatus>(ConverterStatus.Loading)
        private set
    var currenciesStatus by mutableStateOf<CurrenciesStatus>(CurrenciesStatus.Loading)
        private set

    private val _navigateToConvert : MutableLiveData<Boolean> = MutableLiveData(false)
    val navigateToConvert: LiveData<Boolean> = _navigateToConvert
    private val _errorMsg = MutableLiveData<String?>(null)
    val errorMsg: LiveData<String?> = _errorMsg

    init {
        loadCurrencies()
    }


    fun setAmount(amount: String) {
        if (amount.isEmpty() || amount.matches(Regex("^(0|([1-9]\\d*))?(\\.\\d{0,2})?$"))) {
            fromAmount = amount
        }
    }

    fun convertAndNavigate() {
        if (!checkInput()) return
        _navigateToConvert.value = true
        convert()
    }

    fun convert() {
        converterStatus = ConverterStatus.Loading
        val curMap = (currenciesStatus as? CurrenciesStatus.Success)?.data
        if (curMap == null) {
            converterStatus = ConverterStatus.Error("No currencies found")
            return
        }
        val fromCur = curMap[fromCurrency]
        val toCur = curMap[toCurrency]
        if (fromCur == null || toCur == null) {
            converterStatus = ConverterStatus.Error("No currencies found")
            return
        }
        val result = ConverterApi.retrofitServ.getConvertedAmount(fromCur,
            toCur,
            fromAmount.isBlank().let { if (it) "0" else fromAmount })
        result.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableSingleObserver<ConverterResponse>() {
                override fun onSuccess(it: ConverterResponse) {
                    if (it.result != "success") {
                        converterStatus = ConverterStatus.Error("Error: ${getErrorMsg(it.result)}")
                        return
                    }
                    converterStatus = ConverterStatus.Success(it)
                }

                override fun onError(e: Throwable) {
                    converterStatus =
                        ConverterStatus.Error("Network error: ${e.message ?: "Unknown error"}")
                }
            })
    }

    fun loadCurrencies() {
        currenciesStatus = CurrenciesStatus.Loading
        val result = ConverterApi.retrofitServ.getSupportedCurrencies()
        result.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableSingleObserver<CurrenciesResponse>() {
                override fun onSuccess(t: CurrenciesResponse) {
                    if (t.result != "success") {
                        currenciesStatus = CurrenciesStatus.Error("Error: ${getErrorMsg(t.result)}")
                        return
                    }
                    val map = mutableMapOf<String, String>()
                    t.supportedCodes?.forEach { list ->
                        map[list[1]] = list[0]
                    }
                    if (map.isEmpty()) {
                        currenciesStatus = CurrenciesStatus.Error("No currencies found")
                        return
                    }
                    fromCurrency = map.keys.first()
                    toCurrency = map.keys.first()
                    currenciesStatus = CurrenciesStatus.Success(map)

                }

                override fun onError(e: Throwable) {
                    currenciesStatus =
                        CurrenciesStatus.Error("Network error: ${e.message ?: "Unknown error"}")
                }
            })
    }

    private fun checkInput(): Boolean {
        if (fromAmount.isBlank() || fromAmount == "0") {
            _errorMsg.value = "Please enter an amount"
            return false
        }
        if (fromCurrency == toCurrency) {
            _errorMsg.value = "Please select different currencies"
            return false
        }

        return true
    }

    fun showErrorDone() {
        _errorMsg.value = null
    }

    fun navigateToConvertDone() {
        _navigateToConvert.value = false
    }
}
