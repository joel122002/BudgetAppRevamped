package com.cr7.budgetapp.ui.screens.main

import android.app.Application
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cr7.budgetapp.R
import com.cr7.budgetapp.data.local.BudgetItem
import com.cr7.budgetapp.ui.screens.helpers.LocalAuthViewModel
import com.cr7.budgetapp.ui.screens.helpers.LocalNavController
import com.cr7.budgetapp.ui.screens.helpers.Routes
import com.cr7.budgetapp.ui.screens.helpers.isValidError
import com.cr7.budgetapp.ui.screens.helpers.isValidItemName
import com.cr7.budgetapp.ui.screens.helpers.isValidNumeric
import com.cr7.budgetapp.ui.screens.laundry.LaundryScreen
import com.cr7.budgetapp.ui.theme.BudgetAppTheme
import com.cr7.budgetapp.ui.viewmodel.BudgetItemViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainComposable(
    application: Application,
    lifecycleScope: LifecycleCoroutineScope,
//    budgetItemViewModel: BudgetItemViewModel,
) {
    val budgetItemViewModel = viewModel {
        BudgetItemViewModel(application)
    }
    val budgetItems = budgetItemViewModel.budgetItems.collectAsState(emptyList()).value
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Date().toInstant().toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis < Date().toInstant().toEpochMilli()
            }

            override fun isSelectableYear(year: Int): Boolean {
                return year <= Date().toInstant().atZone(ZoneId.systemDefault())
                    .toLocalDate().year
            }
        })
    val coroutineScope = rememberCoroutineScope()
    var isDatePickerOpen by remember {
        mutableStateOf(false)
    }
    var updatedItem by remember {
        mutableStateOf<BudgetItem?>(null)
    }

    var deletedItem by remember {
        mutableStateOf<BudgetItem?>(null)
    }

    var isRefreshing by remember {
        mutableStateOf(false)
    }

    var totalPrice by remember {
        mutableStateOf(0f)
    }

    val totalPriceAnim by animateFloatAsState(
        targetValue = totalPrice, animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ), label = "Day's total"
    )


    val selectedDateMills = datePickerState.selectedDateMillis!!

    LaunchedEffect(budgetItems) {
        totalPrice = 0f
        budgetItems.forEach { item ->
            val dateMills = item.date.toInstant().toEpochMilli()
            if (dateMills >= selectedDateMills && dateMills < (selectedDateMills + 86400000)) {
                totalPrice += item.price
            }
        }
    }

    LaunchedEffect(selectedDateMills) {
        lifecycleScope.launch {
            budgetItemViewModel.refreshData()
        }
        budgetItemViewModel.changeDate(
            // Start is selected day - 2 days
            Date(selectedDateMills - (86400000 * 2)),
            // End is selected day + 2 days
            Date(selectedDateMills + (86400000 * 2))
        )
        totalPrice = 0f
        budgetItems.forEach { item ->
            val dateMills = item.date.toInstant().toEpochMilli()
            if (dateMills >= selectedDateMills && dateMills < (selectedDateMills + 86400000)) {
                totalPrice += item.price
            }
        }
    }

    // A surface container using the 'background' color from the theme
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .padding(PaddingValues(12.dp))
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            CustomDatePickerDialog(
                open = isDatePickerOpen,
                updateOpen = { open -> isDatePickerOpen = open },
                datePickerState = datePickerState
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                FloatingActionButton(onClick = {
                    isDatePickerOpen = true
                }) {
                    Icon(
                        imageVector = Icons.Outlined.DateRange,
                        contentDescription = "Date Picker"
                    )
                }

                Text(
                    text = SimpleDateFormat("dd MMMM yyyy (EEEE)").format(
                        Date(selectedDateMills)
                    )
                )

            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Card(
                    colors = CardColors(
                        contentColor = MaterialTheme.colorScheme.primaryContainer,
                        containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        disabledContentColor = MaterialTheme.colorScheme.primaryContainer,
                        disabledContainerColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp, 42.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Total",
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = "%.0f".format(totalPriceAnim),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    coroutineScope.launch {
                        isRefreshing = true
                        budgetItemViewModel.refreshData()
                        isRefreshing = false
                    }
                },
                modifier = Modifier
                    .weight(1f, true)
                    .fillMaxWidth().padding(top = 12.dp),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(budgetItems) { budgetItem ->

                        val dateMills = budgetItem.date.toInstant().toEpochMilli()
                        if (dateMills >= selectedDateMills && dateMills < (selectedDateMills + 86400000)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    Text(text = budgetItem.name)
                                }
                                Box() {
                                    Text(text = "%.0f".format(budgetItem.price))
                                }
                                Button(onClick = { updatedItem = budgetItem }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit item"
                                    )
                                }
                                Button(onClick = { deletedItem = budgetItem }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Item"
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (updatedItem != null) {
                EditItemDialog(
                    updatedItem!!,
                    onDismissRequest = { updatedItem = null },
                    onConfirmation = { budgetItem ->

                        budgetItemViewModel.insert(budgetItem = budgetItem)
                        updatedItem = null
                    })
            }

            if (deletedItem != null) {
                DeleteItemDialog(
                    deletedItem!!,
                    onDismissRequest = { deletedItem = null },
                    onConfirmation = { budgetItem ->
                        budgetItemViewModel.delete(budgetItem = deletedItem!!)
                        deletedItem = null
                    })
            }

            NewItemForm(
                budgetItemViewModel = budgetItemViewModel,
                datePickerState = datePickerState,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerDialog(
    open: Boolean,
    updateOpen: (Boolean) -> Unit,
    datePickerState: DatePickerState
) {
    if (open) {
        DatePickerDialog(
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = {
                    updateOpen(false)
                }) { Text(text = "Done") }
            },
            content = { DatePicker(state = datePickerState) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewItemForm(
    budgetItemViewModel: BudgetItemViewModel,
    datePickerState: DatePickerState,
) {
    var itemName by remember {
        mutableStateOf("")
    }
    var itemNameError by remember {
        mutableStateOf("")
    }
    var price by remember {
        mutableStateOf("")
    }
    var priceError by remember {
        mutableStateOf("")
    }

    val authViewModel = LocalAuthViewModel.current
    val focusManager = LocalFocusManager.current
    val numberPattern = remember { Regex("^\\d+\$") }


    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.fillMaxWidth(0.5f)) {
            OutlinedTextField(
                value = itemName,
                onValueChange = { itemName = it },
                singleLine = true,
                label = { Text("Item") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                supportingText = {
                    if (isValidError(itemNameError)) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = itemNameError,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = price,
                onValueChange = {
                    if (it.isEmpty() || it.matches(numberPattern))
                        price = it
                },
                singleLine = true,
                label = { Text("Price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = {
                    if (isValidError(priceError)) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = priceError,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
            )
        }


        FloatingActionButton(onClick = {
            if (!isValidNumeric(price)) {
                priceError = "Invalid price"
            }
            if (!isValidItemName(itemName)) {
                itemNameError = "Invalid item name"
            }
            if (!isValidItemName(itemName) || !isValidNumeric(price)) {
                return@FloatingActionButton
            }
            val userDocRef =
                authViewModel.getUserDocumentRef()
            val budgetItem = BudgetItem(
                createdAt = Date(),
                updatedAt = Date(),
                name = itemName.trim(),
                price = price.toFloat(),
                date = Date(datePickerState.selectedDateMillis!!),
                userDoc = userDocRef,
                sync = false
            )
            itemName = ""
            price = ""
            itemNameError = ""
            priceError = ""
            focusManager.clearFocus()
            budgetItemViewModel.insert(budgetItem = budgetItem)
        }) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = "Add"
            )
        }
    }
}

@Composable
fun DeleteItemDialog(
    budgetItem: BudgetItem, onDismissRequest: () -> Unit,
    onConfirmation: (budgetItem: BudgetItem) -> Unit,
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    modifier = Modifier.padding(bottom = 16.dp),
                    text = "Delete Item",
                    style = MaterialTheme.typography.headlineSmall
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Are you sure you want to delete this item?")
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = { onDismissRequest() },
                    ) {
                        Text("Dismiss")
                    }
                    TextButton(
                        onClick = {
                            onConfirmation(budgetItem)
                        },
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

@Composable
fun EditItemDialog(
    budgetItem: BudgetItem, onDismissRequest: () -> Unit,
    onConfirmation: (budgetItem: BudgetItem) -> Unit,
) {
    var itemName by remember {
        mutableStateOf(budgetItem.name)
    }
    var itemNameError by remember {
        mutableStateOf("")
    }
    var price by remember {
        mutableStateOf(budgetItem.price.toString())
    }
    var priceError by remember {
        mutableStateOf("")
    }
    val authViewModel = LocalAuthViewModel.current

    val numberPattern = remember { Regex("^\\d+\$") }
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 16.dp, 16.dp, 0.dp)
            ) {
                Text(
                    modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 16.dp),
                    text = "Update Item",
                    style = MaterialTheme.typography.bodyLarge
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.fillMaxWidth(0.5f)) {
                        OutlinedTextField(
                            value = itemName,
                            onValueChange = { itemName = it },
                            label = { Text("Item") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                            supportingText = {
                                if (isValidError(itemNameError)) {
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = itemNameError,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = price,
                            {
                                if (it.isEmpty() || it.matches(numberPattern))
                                    price = it
                            },
                            singleLine = true,
                            label = { Text("Price") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            supportingText = {
                                if (isValidError(priceError)) {
                                    Text(
                                        modifier = Modifier.fillMaxWidth(),
                                        text = priceError,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Dismiss")
                    }
                    TextButton(
                        onClick = {
                            if (!isValidNumeric(price)) {
                                priceError = "Invalid price"
                            }
                            if (!isValidItemName(itemName)) {
                                itemNameError = "Invalid item name"
                            }
                            if (!isValidItemName(itemName) || !isValidItemName(price)) {
                                return@TextButton
                            }
                            budgetItem.price = price.toFloat()
                            budgetItem.name = itemName.trim()
                            budgetItem.sync = false
                            budgetItem.userDoc = authViewModel.getUserDocumentRef()

                            onConfirmation(budgetItem)
                        },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Confirm")
                    }
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    application: Application,
    lifecycleScope: LifecycleCoroutineScope
) {
    var dropdownOpen by remember {
        mutableStateOf(false)
    }
    var screen by remember {
        mutableStateOf(Routes.budget.route)
    }
    val navController = LocalNavController.current
    val authViewModel = LocalAuthViewModel.current
    val navControllerLocal = rememberNavController()
    BudgetAppTheme {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary,
                        ),
                        title = {
                            Text("Budget App")
                        },
                        actions = {
                            IconButton(onClick = { navController.navigate(Routes.calculate.route) }) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_calculator),
                                    contentDescription = "Localized description"
                                )
                            }
                            IconButton(onClick = { dropdownOpen = !dropdownOpen }) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = "Localized description"
                                )
                            }
                            DropdownMenu(
                                expanded = dropdownOpen,
                                onDismissRequest = { dropdownOpen = false },
                            ) {
                                DropdownMenuItem(onClick = {
                                    authViewModel.signOut()
                                    dropdownOpen = false
                                }, text = { Text(text = "Logout") })
                            }
                        },
                    )
                },
                bottomBar = {
                    NavigationBar {
                        //getting the list of bottom navigation items for our data class

                        //iterating all items with their respective indexes
                        NavigationBarItem(
                            selected = screen == Routes.budget.route,
                            label = {
                                Text("Budget")
                            },
                            icon = {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_wallet),
                                    contentDescription = "Budget"
                                )
                            },
                            onClick = {
                                navControllerLocal.navigate(Routes.budget.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                                screen = Routes.budget.route
                            }
                        )
                        NavigationBarItem(
                            selected = screen == Routes.laundry.route,
                            label = {
                                Text("Laundry")
                            },
                            icon = {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_laundry),
                                    contentDescription = "Laundry"
                                )
                            },
                            onClick = {
                                navControllerLocal.navigate(Routes.laundry.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                                screen = Routes.laundry.route
                            }
                        )
                    }
                }
            ) { innerPadding ->

                NavHost(
                    navController = navControllerLocal,
                    startDestination = Routes.budget.route,
                    modifier = Modifier.padding(paddingValues = innerPadding)
                ) {
                    composable(Routes.budget.route, enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(300)
                        )
                    },
                        exitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            )
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            )
                        }) {
                        MainComposable(application = application, lifecycleScope = lifecycleScope)
                    }
                    composable(Routes.laundry.route, enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(300)
                        )
                    },
                        exitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(300)
                            )
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(300)
                            )
                        }) {
                        LaundryScreen()
                    }
                }
            }
        }
    }
}