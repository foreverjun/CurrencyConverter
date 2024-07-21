package com.example.currencyconverter

import ConverterHolder
import ResultHolder
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

enum class ConverterScreen(@StringRes val title: Int) {
    START(title = R.string.converter_name),
    RESULT(title = R.string.result_name)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterAppBar(
    screen: ConverterScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    reload: () -> Unit
) {
    TopAppBar(title = { Text(text = stringResource(id = screen.title)) },colors = TopAppBarDefaults.mediumTopAppBarColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer), navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.back_button)
                    )
                }
            }
    },
        actions = {
            IconButton(onClick = reload) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = stringResource(id = R.string.reload_button)
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterApp(viewModel: ConverterViewModel = ConverterViewModel(), navController: NavHostController = rememberNavController()) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentScreen = ConverterScreen.values().find {
        it.name == (backStackEntry.value?.destination?.route ?: ConverterScreen.START.name)
    }
    val navigateToResult by viewModel.navigateToConvert.observeAsState()
    if (navigateToResult == true) {
        navController.navigate(ConverterScreen.RESULT.name)
        viewModel.navigateToConvertDone()
    }
    Scaffold(
        topBar = {
            ConverterAppBar(
                screen = currentScreen ?: ConverterScreen.START,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() },
                reload = {
                    when (currentScreen) {
                        ConverterScreen.START -> viewModel.loadCurrencies()
                        ConverterScreen.RESULT -> viewModel.convert()
                        else -> {}
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = ConverterScreen.START.name, modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            composable(ConverterScreen.START.name) {
                ConverterHolder(viewModel = viewModel)
            }
            composable(ConverterScreen.RESULT.name) {
                ResultHolder(viewModel = viewModel)
            }

        }
    }
}

