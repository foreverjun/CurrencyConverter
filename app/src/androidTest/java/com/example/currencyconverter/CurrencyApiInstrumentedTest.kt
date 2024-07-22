package com.example.currencyconverter

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests to verify network requests to API
 */
@RunWith(AndroidJUnit4::class)
class CurrencyApiInstrumentedTest {

    @Test
    @Throws(Exception::class)
    fun testLoadCurrencies() {
        val viewModel = ConverterViewModel()
        viewModel.loadCurrencies()
        assert(viewModel.currenciesStatus is CurrenciesStatus.Loading)
        var count = 0
        while (viewModel.currenciesStatus is CurrenciesStatus.Loading) {
            Thread.sleep(1000)
            count++
            if (count > 10) {
                break
            }
        }
        assert(viewModel.currenciesStatus is CurrenciesStatus.Success)
        assert(((viewModel.currenciesStatus as? CurrenciesStatus.Success)?.data?.size ?: 0) > 0)
        assert((viewModel.currenciesStatus as? CurrenciesStatus.Success)?.data?.get("United States Dollar") == "USD")
    }

    @Test
    @Throws(Exception::class)
    fun testConvert() {
        val viewModel = ConverterViewModel()
        viewModel.loadCurrencies()
        var count = 0
        while (viewModel.currenciesStatus is CurrenciesStatus.Loading) {
            Thread.sleep(1000)
            count++
            if (count > 10) {
                break
            }
        }
        assert(viewModel.currenciesStatus is CurrenciesStatus.Success)
        viewModel.setAmount("1")
        viewModel.fromCurrency = "United States Dollar"
        viewModel.toCurrency = "United States Dollar"
        viewModel.convert()
        count = 0
        while (viewModel.converterStatus is ConverterStatus.Loading) {
            Thread.sleep(1000)
            count++
            if (count > 10) {
                break
            }
        }
        assert(viewModel.converterStatus is ConverterStatus.Success)

        assert((viewModel.converterStatus as? ConverterStatus.Success)?.data?.conversionResult == "1")
        assert((viewModel.converterStatus as? ConverterStatus.Success)?.data?.conversionRate == "1")
    }
}