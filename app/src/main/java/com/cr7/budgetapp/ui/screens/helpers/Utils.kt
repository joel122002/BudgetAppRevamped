package com.cr7.budgetapp.ui.screens.helpers

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