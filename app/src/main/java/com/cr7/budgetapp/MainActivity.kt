package com.cr7.budgetapp

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.lifecycleScope
import com.cr7.budgetapp.data.local.BudgetItem
import com.cr7.budgetapp.ui.theme.BudgetAppTheme
import com.cr7.budgetapp.ui.viewmodel.BudgetItemViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.Date

val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val budgetItemViewModel = BudgetItemViewModel(application)
        lifecycleScope.launch {
            budgetItemViewModel.refreshData()
        }

        setContent {
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
            var isRefreshing by remember {
                mutableStateOf(false)
            }
            var itemName by remember {
                mutableStateOf("")
            }
            var price by remember {
                mutableStateOf("")
            }

            var totalPrice by remember {
                mutableStateOf(0f)
            }
            val focusManager = LocalFocusManager.current
            val numberPattern = remember { Regex("^\\d+\$") }


            val selectedDateMills = datePickerState.selectedDateMillis!!

            LaunchedEffect(selectedDateMills) {
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

            BudgetAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppBar { innerPadding ->
                        Column(
                            modifier = Modifier
                                .padding(innerPadding)
                                .padding(PaddingValues(12.dp))
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (isDatePickerOpen) {
                                DatePickerDialog(
                                    onDismissRequest = { },
                                    confirmButton = {
                                        TextButton(onClick = {
                                            isDatePickerOpen = false
                                        }) { Text(text = "Done") }
                                    },
//
                                    content = { DatePicker(state = datePickerState) }
                                )
                            }

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
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = "Total: $totalPrice"
                                )

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
                                    .fillMaxWidth(),
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
                                                Box(modifier = Modifier.fillMaxWidth(0.5f)) {
                                                    Text(text = budgetItem.name)
                                                }
                                                Box(modifier = Modifier.weight(1f)) {
                                                    Text(text = "${budgetItem.price}")
                                                }

                                                Button(onClick = { updatedItem = budgetItem }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "Edit item"
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


                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.fillMaxWidth(0.5f)) {
                                    OutlinedTextField(
                                        value = itemName,
                                        onValueChange = { itemName = it },
                                        label = { Text("Item") },
                                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                                    )
                                }

                                Box(modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(
                                        value = price,
                                        onValueChange = {
                                            if (it.isEmpty() || it.matches(numberPattern))
                                                price = it
                                        },
                                        label = { Text("Price") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }


                                FloatingActionButton(onClick = {
                                    val budgetItem = BudgetItem(
                                        createdAt = Date(),
                                        updatedAt = Date(),
                                        name = itemName.trim(),
                                        price = price.toFloat(),
                                        date = Date(datePickerState.selectedDateMillis!!),
                                        sync = false
                                    )
                                    itemName = ""
                                    price = ""
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
    var price by remember {
        mutableStateOf(budgetItem.price.toString())
    }

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
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = price,
                            {
                                if (it.isEmpty() || it.matches(numberPattern))
                                    price = it
                            },
                            label = { Text("Price") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
                            budgetItem.price = price.toFloat()
                            budgetItem.name = itemName
                            budgetItem.sync = false

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
fun AppBar(content: @Composable() (innerPadding: PaddingValues) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Budget App")
                }
            )
        },
    ) { innerPadding ->
        content(innerPadding)
    }
}
