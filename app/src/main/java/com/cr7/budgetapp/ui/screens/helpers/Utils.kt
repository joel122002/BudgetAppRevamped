package com.cr7.budgetapp.ui.screens.helpers

import android.app.Application
import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.cr7.budgetapp.data.local.BudgetItem
import com.cr7.budgetapp.ui.viewmodel.AuthViewModel
import com.cr7.budgetapp.ui.viewmodel.UserViewModel
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Date

fun getTomorrowAtMidnight(): Date {
    val tomorrow = LocalDate.now().plusDays(1)
    val midnight = LocalTime.MIDNIGHT
    val tomorrowAtMidnight = LocalDateTime.of(tomorrow, midnight)
    return Date.from(tomorrowAtMidnight.atZone(ZoneId.of("UTC")).toInstant())
}

fun getFirstDayOfCurrentMonthAtMidnight(): Date {
    val firstDayOfMonth = LocalDate.now().withDayOfMonth(1)
    val midnight = LocalTime.MIDNIGHT
    val firstDayAtMidnight = LocalDateTime.of(firstDayOfMonth, midnight)
    return Date.from(firstDayAtMidnight.atZone(ZoneId.of("UTC")).toInstant())
}

fun getFirstDayOfPreviousMonth(date: Date): Date {
    val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    val previousMonth = localDate.minusMonths(1).withDayOfMonth(1)
    return Date.from(previousMonth.atStartOfDay(ZoneId.systemDefault()).toInstant())
}

fun getFirstDayOfNextMonth(date: Date): Date {
    val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    val previousMonth = localDate.plusMonths(1).withDayOfMonth(1)
    return Date.from(previousMonth.atStartOfDay(ZoneId.systemDefault()).toInstant())
}

fun getLastDayOfMonth(date: Date): Date {
    val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    val lastDayOfMonth = localDate.withDayOfMonth(localDate.lengthOfMonth())
    return Date.from(lastDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant())
}

fun getMidnight(date: Date): Date {
    val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    val midnight = LocalTime.MIDNIGHT
    val tomorrowAtMidnight = LocalDateTime.of(localDate, midnight)
    return Date.from(tomorrowAtMidnight.atZone(ZoneId.of("UTC")).toInstant())
}

fun getFirstDayOfMonth(month: Int, year: Int): Date {
    // Create an instance of Calendar
    val calendar = Calendar.getInstance()

    // Set the calendar to the first day of the specified month and year
    calendar.set(Calendar.YEAR, year)
    calendar.set(Calendar.MONTH, month)
    calendar.set(Calendar.DAY_OF_MONTH, 1)

    // Return the Date object representing the first day of the month
    return calendar.time
}

fun getCurrentMonth(): Int {
    val calendar = Calendar.getInstance()
    // Note: Calendar.MONTH is zero-based, so January is 0
    return calendar.get(Calendar.MONTH)
}

fun getCurrentYear(): Int {
    val calendar = Calendar.getInstance()
    return calendar.get(Calendar.YEAR)
}

@RequiresApi(Build.VERSION_CODES.Q)
fun saveToDownloads(fileUri: Uri, application: Application, fileName: String) {
    val contentValues = ContentValues()
    // Enter the name of the file here. Note the extension isn't necessary
    contentValues.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
    // Here we define the file type. Do check the MIME_TYPE for your file. For jpegs it is "image/jpeg"
    contentValues.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
    contentValues.put(MediaStore.Downloads.IS_PENDING, true);
    val downloadsUri = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    val destinationFileUri =
        application.contentResolver.insert(downloadsUri, contentValues) ?: return
    val inputStream = application.contentResolver.openInputStream(fileUri) ?: return
    val outputStream = application.contentResolver.openOutputStream(destinationFileUri) ?: return
    val buf = ByteArray(1024)
    var len: Int
    while (inputStream.read(buf).also { len = it } > 0) {
        outputStream.write(buf, 0, len)
    }
    inputStream.close()
    outputStream.close()
    contentValues.put(MediaStore.Images.Media.IS_PENDING, false)
    application.contentResolver.update(destinationFileUri, contentValues, null, null)
}

fun legacySaveToDownloads(uri: Uri, application: Application, fileName: String) {
    val request = DownloadManager.Request(uri)
    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) // to notify when download is complete f you want to be available from media players
    val manager =
        application.baseContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
    if (manager == null)
        return
    manager.enqueue(request)
}

fun generateFilenamePrefix(start: Date, end: Date): String {
    val startDateString = SimpleDateFormat("dd MMMM yyyy").format(start)
    val endDateString = SimpleDateFormat("dd MMMM yyyy").format(end)
    return "$startDateString - $endDateString"
}

suspend fun createCurrentUserCSV(
    start: Date,
    end: Date,
    userMap: Map<String, String>,
    budgetItems: List<BudgetItem>,
    userViewModel: UserViewModel,
    authViewModel: AuthViewModel,
    application: Application,
) {
    withContext(Dispatchers.IO) {
        val rows = mutableListOf(listOf("Date", "Item", "Price", "User"))
        userViewModel.resolveUsers(budgetItems)
        budgetItems.forEach { budgetItem ->
            if (budgetItem.userDoc != null && budgetItem.userDoc!!.path == authViewModel.getUserDocumentRef().path) {
                rows.add(
                    listOf(
                        SimpleDateFormat("dd/MM/yyyy").format(budgetItem.date),
                        budgetItem.name,
                        budgetItem.price.toString(),
                        (userMap[budgetItem.userDoc!!.path] ?: "")
                    )
                )
            }
        }
        val path = application.baseContext.getFilesDir()
        csvWriter().writeAll(rows = rows, "$path/test.csv")
        val uri = File("$path/test.csv").toUri()
        val filename = "${
            generateFilenamePrefix(
                start,
                end
            )
        } ${userMap[authViewModel.getUserDocumentRef().path]}.csv"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToDownloads(uri, application, filename)
        } else {
            legacySaveToDownloads(uri, application, filename)
        }
    }
}

suspend fun createAllUserCSV(
    start: Date,
    end: Date,
    userMap: Map<String, String>,
    budgetItems: List<BudgetItem>,
    userViewModel: UserViewModel,
    application: Application,
) {
    withContext(Dispatchers.IO) {
        val rows = mutableListOf(listOf("Date", "Item", "Price", "User"))
        userViewModel.resolveUsers(budgetItems)
        budgetItems.forEach { budgetItem ->
            rows.add(
                listOf(
                    SimpleDateFormat("dd/MM/yyyy").format(budgetItem.date),
                    budgetItem.name,
                    budgetItem.price.toString(),
                    (userMap[budgetItem.userDoc!!.path] ?: "")
                )
            )
        }
        val path = application.baseContext.getFilesDir()
        csvWriter().writeAll(rows = rows, "$path/test.csv")
        val uri = File("$path/test.csv").toUri()
        val filename = "${generateFilenamePrefix(start, end)} All Users.csv"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToDownloads(uri, application, filename)
        } else {
            legacySaveToDownloads(uri, application, filename)
        }
    }
}