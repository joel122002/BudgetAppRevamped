package com.cr7.budgetapp.ui.screens.calculate

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cr7.budgetapp.R
import com.cr7.budgetapp.data.remote.FirebaseAuthenticatedUser
import com.cr7.budgetapp.data.remote.FirebaseService
import com.cr7.budgetapp.ui.screens.helpers.LocalApplication
import com.cr7.budgetapp.ui.screens.helpers.LocalNavController
import com.cr7.budgetapp.ui.screens.helpers.getFirstDayOfCurrentMonthAtMidnight
import com.cr7.budgetapp.ui.screens.helpers.getFirstDayOfNextMonth
import com.cr7.budgetapp.ui.screens.helpers.getFirstDayOfPreviousMonth
import com.cr7.budgetapp.ui.screens.helpers.getLastDayOfMonth
import com.cr7.budgetapp.ui.screens.helpers.getMidnight
import com.cr7.budgetapp.ui.screens.helpers.getTomorrowAtMidnight
import com.cr7.budgetapp.ui.theme.BudgetAppTheme
import com.cr7.budgetapp.ui.viewmodel.AuthViewModel
import com.cr7.budgetapp.ui.viewmodel.BudgetItemViewModel
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.Date

@Composable
fun CalculateScreen() {
    val application = LocalApplication.current
    val budgetItemViewModel = viewModel {
        BudgetItemViewModel(application)
    }
    val authViewModel = viewModel {
        AuthViewModel()
    }

    var end by remember {
        mutableStateOf(getTomorrowAtMidnight())
    }
    var start by remember {
        mutableStateOf(getFirstDayOfCurrentMonthAtMidnight())
    }

    var datePickerIsOpen by remember {
        mutableStateOf(false)
    }

    var totalAll by remember {
        mutableStateOf(0f)
    }
    val totalAllAnim by animateFloatAsState(targetValue = totalAll, animationSpec = tween(
        durationMillis = 1000,
        easing = FastOutSlowInEasing
    ))
    var total by remember {
        mutableStateOf(0f)
    }
    val totalAnim by animateFloatAsState(targetValue = total, animationSpec = tween(
        durationMillis = 1000,
        easing = FastOutSlowInEasing
    ))
    val items = budgetItemViewModel.allUsersBudgetItems.collectAsState(emptyList())
    LaunchedEffect(items.value) {
        items.value.forEach { budgetItem ->
            if (budgetItem.userDoc != null && budgetItem.userDoc!!.path == authViewModel.getUserDocumentRef().path) {
                total += budgetItem.price
            }
            totalAll += budgetItem.price
        }
    }

    LaunchedEffect(start, end) {
        total = 0f
        totalAll = 0f
        budgetItemViewModel.changeAllUserDate(start, end)
    }
    BudgetAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppBar {
                Column(modifier = Modifier
                    .padding(it)
                    .padding(12.dp)
                    .fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            FloatingActionButton(onClick = { datePickerIsOpen = true }) {
                                Icon(imageVector = ImageVector.vectorResource(id = R.drawable.ic_calculator_range), contentDescription = "Start Date")
                            }
                            Text(text = "${SimpleDateFormat(" d MMM yyyy ").format(start)} - ${SimpleDateFormat(" d MMM yyyy ").format(end)}")

                        }
                        Row(Modifier.fillMaxWidth()) {
                            ExpenseCard(modifier = Modifier
                                .weight(1f)
                                .padding(12.dp), title = "Current User", expense = totalAnim)
                            ExpenseCard(modifier = Modifier
                                .weight(1f)
                                .padding(12.dp), title = "All Users", expense = totalAllAnim)
                        }

                        DateRangePickerModal(
                            open = datePickerIsOpen,
                            start = start,
                            end = end,
                            onDateRangeSelected = {
                                if (it.first != null && it.second != null) {
                                    start = Date(it.first!!)
                                    end = Date(it.second!!)
                                }
                                datePickerIsOpen = false },
                            onDismiss = { datePickerIsOpen = false })
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        FloatingActionButton(onClick = { start = getMidnight(getFirstDayOfPreviousMonth(start))
                        end = getMidnight(getLastDayOfMonth(start))
                        }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Start Date")
                        }
                        FloatingActionButton(onClick = { datePickerIsOpen = true }) {
                            Icon(imageVector = ImageVector.vectorResource(id = R.drawable.ic_download), contentDescription = "Start Date")
                        }
                        FloatingActionButton(onClick = { start = getMidnight(getFirstDayOfNextMonth(start))
                            end = getMidnight(getLastDayOfMonth(start)) }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Start Date")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseCard(modifier: Modifier,title: String, expense: Float) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        colors = CardColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer, disabledContainerColor = MaterialTheme.colorScheme.onSurface, disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer),
        modifier = modifier,
    ) {
        Column( modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 42.dp),horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(
                text = "All users",
                textAlign = TextAlign.Center,
            )
            Text(
                text = "%.2f".format(expense),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerModal(
    open: Boolean,
    start: Date,
    end: Date,
    onDateRangeSelected: (Pair<Long?, Long?>) -> Unit,
    onDismiss: () -> Unit
) {
    if (open) {
        val state = rememberDateRangePickerState(
            initialSelectedStartDateMillis = start.toInstant().toEpochMilli(),
            initialSelectedEndDateMillis = end.toInstant().toEpochMilli(),
            selectableDates = object :
                SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis < Date().toInstant().toEpochMilli()
                }

                override fun isSelectableYear(year: Int): Boolean {
                    return year <= Date().toInstant().atZone(ZoneId.systemDefault())
                        .toLocalDate().year
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        onDateRangeSelected(
                            Pair(
                                state.selectedStartDateMillis,
                                state.selectedEndDateMillis
                            )
                        )
                        onDismiss()
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        ) {
            DateRangePicker(
                state = state,
                title = {
                    Text(
                        text = "Select date range"
                    )
                },
                showModeToggle = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .padding(16.dp)
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
fun AppBar(content: @Composable() (innerPadding: PaddingValues) -> Unit) {
    var dropdownOpen by remember {
        mutableStateOf(false)
    }
    val navController = LocalNavController.current
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Monthly calculation")
                },
                actions = {
                },
            )
        },
    ) { innerPadding ->
        content(innerPadding)
    }
}