package com.cr7.budgetapp.ui.screens.laundry

import android.content.Context
import android.graphics.Paint
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cr7.budgetapp.data.local.BudgetItem
import com.cr7.budgetapp.ui.screens.helpers.getCurrentMonth
import com.cr7.budgetapp.ui.screens.helpers.getCurrentYear
import com.cr7.budgetapp.ui.screens.helpers.getFirstDayOfCurrentMonthAtMidnight
import com.cr7.budgetapp.ui.screens.helpers.getFirstDayOfMonth
import com.cr7.budgetapp.ui.screens.helpers.getLastDayOfMonth
import com.cr7.budgetapp.ui.screens.helpers.getMidnight
import com.cr7.budgetapp.ui.screens.helpers.getTomorrowAtMidnight
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaundryScreen() {
    var datePickerOpen by remember {
        mutableStateOf(false)
    }
    var month by remember {
        mutableStateOf(getCurrentMonth())
    }

    var year by remember {
        mutableStateOf(getCurrentYear())
    }

    var start by remember {
        mutableStateOf(getFirstDayOfCurrentMonthAtMidnight())
    }

    var end by remember {
        mutableStateOf(getTomorrowAtMidnight())
    }

    var total by remember {
        mutableStateOf(0)
    }

    var isRefreshing by remember {
        mutableStateOf(false)
    }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(month, year) {
        start = getMidnight(getFirstDayOfMonth(month, year))
        end = getLastDayOfMonth(start)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingActionButton(onClick = { datePickerOpen = true }) {
                Icon(imageVector = Icons.Filled.DateRange, contentDescription = "Date picker")
            }
            Text(text = SimpleDateFormat("MMMM yyyy").format(start))
        }
        MonthYearPicker(
            open = datePickerOpen,
            initialMonth = month,
            initialYear = year,
            onDismiss = { datePickerOpen = false },
            onConfirm = { newMonth, newYear ->
                month = newMonth
                year = newYear
                datePickerOpen = false
            })
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Total: $total"
            )

        }
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                coroutineScope.launch {
                    isRefreshing = true
//                    budgetItemViewModel.refreshData()
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
                item {
                    val dateMills = Date().toInstant().toEpochMilli()
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.fillMaxWidth(0.5f)) {
                            Text(text = "Some date")
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            Text(text = "24")
                        }

                        Button(onClick = { /*updatedItem = budgetItem*/ }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit item"
                            )
                        }
                    }
                }
            }
        }

        if (false) {
            EditItemDialog(
                onDismissRequest = {  },
                onConfirmation = { })
        }

        NewLaundryForm()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemDialog( onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
) {
    var items by remember {
        mutableStateOf("")
    }

    var datePickerOpen by remember {
        mutableStateOf(false)
    }

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
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        FloatingActionButton(onClick = { datePickerOpen = true }) {
                            Icon(
                                imageVector = Icons.Filled.DateRange,
                                contentDescription = "Select Date"
                            )
                        }
                        Text(
                            text = SimpleDateFormat("dd MMMM yyyy (EEEE)").format(
                                Date(datePickerState.selectedDateMillis!!)
                            ),
                            textAlign = TextAlign.End,
                        )
                    }

                    Box() {
                        OutlinedTextField(
                            value = items,
                            {
                                if (it.isEmpty() || it.matches(numberPattern))
                                    items = it
                            },
                            label = { Text("Items") },
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
                            onConfirmation()
                        },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Confirm")
                    }
                }
            }

        }
    }
    CustomDatePickerDialog(open = datePickerOpen, updateOpen = {datePickerOpen = it}, datePickerState = datePickerState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewLaundryForm(
) {
    var items by remember {
        mutableStateOf("")
    }

    var datePickerDialogOpen by remember {
        mutableStateOf(false)
    }

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

    val focusManager = LocalFocusManager.current
    val numberPattern = remember { Regex("^\\d+\$") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.5f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FloatingActionButton(onClick = {
                datePickerDialogOpen = true
            }) {
                Icon(
                    imageVector = Icons.Outlined.DateRange,
                    contentDescription = "Select date"
                )
            }
            Text(
                text = SimpleDateFormat("dd MMMM yyyy (EEEE)").format(
                    Date(datePickerState.selectedDateMillis!!)
                )
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = items,
                onValueChange = {
                    if (it.isEmpty() || it.matches(numberPattern))
                        items = it
                },
                label = { Text("Price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }


        FloatingActionButton(onClick = {
            val userDocRef =
                Firebase.firestore.collection("users").document(Firebase.auth.currentUser?.uid!!)
            val date = Date(datePickerState.selectedDateMillis!!)
            items = ""
            focusManager.clearFocus()
        }) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = "Add"
            )
        }
    }

    CustomDatePickerDialog(
        open = datePickerDialogOpen,
        updateOpen = { datePickerDialogOpen = it },
        datePickerState = datePickerState
    )
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

@Composable
fun MonthYearPicker(
    open: Boolean,
    initialMonth: Int,
    initialYear: Int,
    onDismiss: () -> Unit,
    onConfirm: (month: Int, year: Int) -> Unit
) {
    var month by remember {
        mutableStateOf(initialMonth)
    }
    var year by remember {
        mutableStateOf(initialYear)
    }
    val months = listOf(
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December"
    )
    val years = (1900..getCurrentYear()).toList()
    if (open) {
        Dialog(onDismissRequest = { onDismiss() }) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Select month and year",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        InfiniteCircularList(
                            itemHeight = 70.dp,
                            items = months, //(1..30).map { it.toString() },
                            initialItem = months[month],
                            textStyle = TextStyle(fontSize = 23.sp),
                            textColor = MaterialTheme.colorScheme.onBackground,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            onItemSelected = { i, item ->
                                month = i
                            }
                        )
                        InfiniteCircularList(
                            itemHeight = 70.dp,
                            items = years,
                            initialItem = year,
                            textStyle = TextStyle(fontSize = 23.sp),
                            textColor = MaterialTheme.colorScheme.onBackground,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            onItemSelected = { i, item ->
                                year = item
                            }
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
                    ) {
                        TextButton(onClick = { onDismiss() }) {
                            Text(text = "Dismiss")
                        }
                        TextButton(onClick = { onConfirm(month, year) }) {
                            Text(text = "Done")
                        }

                    }
                }
            }

        }
    }

}

@Composable
fun <T> InfiniteCircularList(
    itemHeight: Dp,
    numberOfDisplayedItems: Int = 3,
    items: List<T>,
    initialItem: T,
    itemScaleFact: Float = 1.5f,
    textStyle: TextStyle,
    textColor: Color,
    selectedTextColor: Color,
    onItemSelected: (index: Int, item: T) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    fun calculateMaxTextWidthInDp(context: Context, texts: List<T>, textSizeSp: Float): Float {
        // Get the screen's density scale
        val metrics: DisplayMetrics = context.resources.displayMetrics
        val scale: Float = metrics.density

        // Convert sp to px
        val textSizePx: Float =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSp, metrics)

        // Initialize a Paint object
        val paint = Paint()
        paint.textSize = textSizePx

        // Measure the width of each text in pixels and find the maximum width
        var maxTextWidthPx: Float = 0f
        for (text in texts) {
            val textWidthPx: Float = paint.measureText(text.toString())
            if (textWidthPx > maxTextWidthPx) {
                maxTextWidthPx = textWidthPx
            }
        }

        // Convert the maximum width from px to dp
        val maxTextWidthDp: Float = maxTextWidthPx / scale

        return maxTextWidthDp
    }

    val width = calculateMaxTextWidthInDp(context, items, textStyle.fontSize.value * itemScaleFact)
    val itemHalfHeight = LocalDensity.current.run { itemHeight.toPx() / 2f }
    val scrollState = rememberLazyListState(0)
    var lastSelectedIndex by remember {
        mutableStateOf(0)
    }
    var itemsState by remember {
        mutableStateOf(items)
    }
    LaunchedEffect(items) {
        var targetIndex = items.indexOf(initialItem) - 1
        targetIndex += ((Int.MAX_VALUE / 2) / items.size) * items.size
        itemsState = items
        lastSelectedIndex = targetIndex
        scrollState.scrollToItem(targetIndex)
    }
    LazyColumn(
        modifier = Modifier
            .width(width.dp)
            .height(itemHeight * numberOfDisplayedItems),
        state = scrollState,
        flingBehavior = rememberSnapFlingBehavior(
            lazyListState = scrollState
        )
    ) {
        items(
            count = Int.MAX_VALUE,
            itemContent = { i ->
                val item = itemsState[i % itemsState.size]
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            val y = coordinates.positionInParent().y - itemHalfHeight
                            val parentHalfHeight =
                                (coordinates.parentCoordinates?.size?.height ?: 0) / 2f
                            val isSelected =
                                (y > parentHalfHeight - itemHalfHeight && y < parentHalfHeight + itemHalfHeight)
                            if (isSelected && lastSelectedIndex != i) {
                                onItemSelected(i % itemsState.size, item)
                                lastSelectedIndex = i
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.toString(),
                        style = textStyle,
                        color = if (lastSelectedIndex == i) {
                            selectedTextColor
                        } else {
                            textColor
                        },
                        fontSize = if (lastSelectedIndex == i) {
                            textStyle.fontSize * itemScaleFact
                        } else {
                            textStyle.fontSize
                        }
                    )
                }
            }
        )
    }
}