import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.currencyconverter.ConverterStatus
import com.example.currencyconverter.ConverterViewModel
import com.example.currencyconverter.CurrenciesStatus
import com.example.currencyconverter.R

@Composable
fun ConverterHolder(viewModel: ConverterViewModel) {
    when (val status = viewModel.currenciesStatus) {
        is CurrenciesStatus.Loading -> {
            LoadingScreen()
        }

        is CurrenciesStatus.Error -> {
            ErrorScreen(msg =status.msg)
        }

        is CurrenciesStatus.Success -> {
            CurrencyConverter(viewModel, status.data.keys.toTypedArray().sorted())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConverter(viewModel: ConverterViewModel, currencies: List<String>){
    var expanded1 by remember { mutableStateOf(false) }
    var expanded2 by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(modifier = Modifier
        .padding(16.dp)
        .verticalScroll(scrollState)) {

        OutlinedTextField(
            value = viewModel.fromAmount,
            onValueChange = { viewModel.setAmount(it) },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Decimal
            ),

            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded1,
                onExpandedChange = {
                    expanded1 = !expanded1
                }
            ) {
                OutlinedTextField(
                    value = viewModel.fromCurrency,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded1) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded1,
                    onDismissRequest = { expanded1 = false }
                ) {
                    LazyColumn(modifier = Modifier.size(310.dp)) {
                        items(currencies) { item ->
                            DropdownMenuItem(
                                text = { Text(text = item) },
                                onClick = {
                                    viewModel.fromCurrency = item
                                    expanded1 = false
                                }
                            )
                        }
                    }
                }
            }
        }
        // Swapping Icon
        Icon(
            painter = painterResource(id = R.drawable.baseline_swap_vert_24),
            contentDescription = "Swap",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clickable {
                    val tempCurrency = viewModel.fromCurrency
                    viewModel.fromCurrency = viewModel.toCurrency
                    viewModel.toCurrency = tempCurrency
                }
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 16.dp)
        )

        // Dropdown Menu for Second Currency

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded2,
                onExpandedChange = {
                    expanded2 = !expanded2
                }
            ) {
                OutlinedTextField(
                    value = viewModel.toCurrency,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded2) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded2,
                    onDismissRequest = { expanded2 = false }
                ) {


                    LazyColumn(modifier = Modifier.size(310.dp)) {
                        items(currencies) { item ->
                            DropdownMenuItem(
                                text = { Text(text = item) },
                                onClick = {
                                    viewModel.toCurrency = item
                                    expanded2 = false
                                }
                            )
                        }
                    }

                }
            }
        }

        Button(onClick ={
            viewModel.convertAndNavigate()
                        }, modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(50.dp)) {
            Text("Convert")

        }
    }
}

@Composable
fun LoadingScreen() {

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(100.dp)
            .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(8.dp))
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorScreen(msg: String) {
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(painter = painterResource(id = R.drawable.baseline_error_outline_24), contentDescription = "Error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(50.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = msg, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)

    }
}

@Composable
fun ResultHolder (viewModel: ConverterViewModel) {
    when (val status = viewModel.converterStatus) {
        is ConverterStatus.Loading -> {
            LoadingScreen()
        }

        is ConverterStatus.Error -> {
            ErrorScreen(msg = status.msg)
        }

        is ConverterStatus.Success -> {
            val currenciesMap = (viewModel.currenciesStatus as? CurrenciesStatus.Success)?.data
            val fromCurrency = currenciesMap?.get(viewModel.fromCurrency) ?: ""
            val toCurrency = currenciesMap?.get(viewModel.toCurrency) ?: ""
            val conversionRate = status.data.conversionRate ?: ""
            val toAmount = status.data.conversionResult ?: ""
            val fromAmount = viewModel.fromAmount
            CurrencyConversionResult(
                fromCurrency = fromCurrency,
                toCurrency = toCurrency,
                fromAmount = fromAmount,
                toAmount = toAmount,
                conversionRate = conversionRate
            )

        }
    }
}


@Composable
fun CurrencyConversionResult(
    fromCurrency: String,
    toCurrency: String,
    fromAmount: String,
    toAmount: String,
    conversionRate: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(
                    MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp),

            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "$fromAmount $fromCurrency",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Arrow Icon",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "$toAmount $toCurrency",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f)
                )
            }
            Text(
                text = "Conversion Rate: 1 $fromCurrency = $conversionRate $toCurrency",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }
    }
}

