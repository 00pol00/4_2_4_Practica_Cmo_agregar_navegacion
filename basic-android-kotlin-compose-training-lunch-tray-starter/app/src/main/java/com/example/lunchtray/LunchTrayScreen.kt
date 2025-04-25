/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.lunchtray

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lunchtray.datasource.DataSource
import com.example.lunchtray.model.MenuItem
import com.example.lunchtray.ui.AccompanimentMenuScreen
import com.example.lunchtray.ui.CheckoutScreen
import com.example.lunchtray.ui.EntreeMenuScreen
import com.example.lunchtray.ui.OrderViewModel
import com.example.lunchtray.ui.SideDishMenuScreen
import com.example.lunchtray.ui.StartOrderScreen


// TODO: Screen enum

enum class nombrePantalla(@StringRes val nombre : Int)
{
    Inicio(R.string.start_order),
    Menu_platos_principales(R.string.choose_entree),
    Menu_guarniciones(R.string.choose_accompaniment),
    Menu_Acompaniamiento(R.string.choose_side_dish),
    Confirmacion_compra(R.string.order_summary)
}
// TODO: AppBar


@Composable
fun LunchTrayApp() {
    // TODO: Create Controller and initialization
    val navHostController: NavHostController= rememberNavController()

    //obtener el estado actual de la pila de navegación (backstack)
    //Esta función devuelve un State que representa la entrada más reciente en el backstack,
    // es decir, la pantalla que está actualmente visible en la interfaz.
    val backStackEntry by navHostController.currentBackStackEntryAsState()
    val currentScreen = nombrePantalla.valueOf(
        backStackEntry?.destination?.route ?: nombrePantalla.Inicio.name)
    // Create ViewModel

    val viewModel: OrderViewModel = viewModel()

    Scaffold(
        topBar = {
            // TODO: AppBar
            LunchTrayTopBar(
                titulo=currentScreen,
                puedeNavegarAtras = navHostController.previousBackStackEntry != null,
                navegarAtras = {navHostController.navigateUp()},

                )
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()

        // TODO: Navigation host
        NavHost(
            navController = navHostController,
            startDestination = nombrePantalla.Inicio.name,
            modifier = Modifier.padding(innerPadding)

        ){

            composable(route= nombrePantalla.Inicio.name) {
                StartOrderScreen(
                    onStartOrderButtonClicked = {navHostController.navigate(nombrePantalla.Menu_platos_principales.name)},
                    modifier= Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(R.dimen.padding_medium))
                )

            }
            composable(route= nombrePantalla.Menu_platos_principales.name) {

                EntreeMenuScreen(
                    options = DataSource.entreeMenuItems,
                    onSelectionChanged = {selectedItem: MenuItem.EntreeItem-> viewModel.updateEntree(selectedItem)},
                    onCancelButtonClicked = {cancelOrderAndNavigateToStart(viewModel,navHostController)},
                    onNextButtonClicked = {navHostController.navigate(nombrePantalla.Menu_Acompaniamiento.name)},
                )

            }
            composable(route= nombrePantalla.Menu_Acompaniamiento.name) {

                AccompanimentMenuScreen(
                    options = DataSource.accompanimentMenuItems,
                    onSelectionChanged = {viewModel.updateAccompaniment(it)},
                    onCancelButtonClicked = {cancelOrderAndNavigateToStart(viewModel,navHostController)},
                    onNextButtonClicked = {navHostController.navigate(nombrePantalla.Menu_guarniciones.name)},
                )
            }
            composable(route= nombrePantalla.Menu_guarniciones.name) {
                SideDishMenuScreen(
                    options = DataSource.sideDishMenuItems,
                    onSelectionChanged = {viewModel.updateSideDish(it)},
                    onCancelButtonClicked = {cancelOrderAndNavigateToStart(viewModel,navHostController)},
                    onNextButtonClicked = {navHostController.navigate(nombrePantalla.Confirmacion_compra.name)},
                )

            }

            composable(route= nombrePantalla.Confirmacion_compra.name) {
                CheckoutScreen(
                    orderUiState =uiState,
                    onCancelButtonClicked = {cancelOrderAndNavigateToStart(viewModel,navHostController)},
                    onNextButtonClicked = {navHostController.navigate(nombrePantalla.Inicio.name)},

                )
            }

        }
    }
}

private fun cancelOrderAndNavigateToStart(
    viewModel: OrderViewModel,
    navController: NavHostController
) {
    viewModel.resetOrder()
    navController.popBackStack(nombrePantalla.Inicio.name, inclusive = false)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LunchTrayTopBar(
    titulo: nombrePantalla,
    puedeNavegarAtras: Boolean,
    navegarAtras:()-> Unit,
    modifier: Modifier= Modifier
)
{

    TopAppBar(
        title ={
            Text(stringResource(titulo.nombre))
               },
        navigationIcon= {
            if(puedeNavegarAtras)
            {
                IconButton(onClick = navegarAtras) {
                    Icon(
                        imageVector= Icons.Filled.ArrowBack,
                        contentDescription=R.string.back_button.toString()
                    )
                }
            }
        }

    )
}



