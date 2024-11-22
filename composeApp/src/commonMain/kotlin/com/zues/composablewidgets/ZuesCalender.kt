package com.zues.composablewidgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayAt
import kotlinx.datetime.todayIn

@Composable
public fun ZuesCalenderView(
    selectionType: SelectionType,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    selectedStartDate: LocalDate?,
    selectedEndDate: LocalDate?,
    onDateRangeSelected: (LocalDate, LocalDate) -> Unit,
    onReset: () -> Unit
) {
    var displayedMonth by remember { mutableStateOf(getCurrentYearMonth()) }
    val initialDate = selectedDate ?: Clock.System.todayIn(TimeZone.currentSystemDefault())

    Column(modifier = Modifier.padding(16.dp)) {
        CalendarHeader(
            displayedMonth = displayedMonth,
            onPreviousMonth = { displayedMonth = displayedMonth.minus(1, DateTimeUnit.MONTH) },
            onNextMonth = { displayedMonth = displayedMonth.plus(1, DateTimeUnit.MONTH) },
            onReset = onReset
        )

        CalendarGrid(
            displayedMonth = displayedMonth,
            selectionType = selectionType,
            selectedDate = initialDate,
            onDateSelected = onDateSelected,
            selectedStartDate = selectedStartDate,
            selectedEndDate = selectedEndDate,
            onDateRangeSelected = onDateRangeSelected
        )
    }
}

@Composable
private fun CalendarHeader(
    displayedMonth: LocalDate,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = displayedMonth.month.name.lowercase()
                    .replaceFirstChar { it.uppercase() },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = displayedMonth.year.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Light
            )
        }

        IconButton(onClick = onNextMonth) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
        }

        TextButton(onClick = onReset) {
            Text("Reset", color = Color.Blue, fontSize = 16.sp)
        }
    }
}

@Composable
private fun CalendarGrid(
    displayedMonth: LocalDate,
    selectionType: SelectionType,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    selectedStartDate: LocalDate?,
    selectedEndDate: LocalDate?,
    onDateRangeSelected: (LocalDate, LocalDate) -> Unit
) {
    val daysInMonth = displayedMonth.lengthOfMonth()
    val firstDayOfWeek = (displayedMonth.atDay(1).dayOfWeek.isoDayNumber + 5) % 7

    Column {
        DayOfWeekHeader()

        val days = (1..daysInMonth).toList()
        val leadingEmptyDays = (0 until firstDayOfWeek).map { null }
        val daysWithLeadingEmptyDays = leadingEmptyDays + days

        // Chunk days into weeks and pad the last week with null values if necessary
        val weeks = daysWithLeadingEmptyDays.chunked(7) {
            if (it.size < 7) {
                it + List(7 - it.size) { null }
            } else {
                it
            }
        }

        for (week in weeks) {
            Row {
                for (day in week) {
                    if (day != null) {
                        CalendarDay(
                            day = day,
                            month = displayedMonth,
                            selectionType = selectionType,
                            selectedDate = selectedDate,
                            onDateSelected = onDateSelected,
                            selectedStartDate = selectedStartDate,
                            selectedEndDate = selectedEndDate,
                            onDateRangeSelected = onDateRangeSelected
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayOfWeekHeader() {
    Row {
        for (day in listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")) {
            Text(
                text = day,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RowScope.CalendarDay(
    day: Int,
    month: LocalDate,
    selectionType: SelectionType,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    selectedStartDate: LocalDate?,
    selectedEndDate: LocalDate?,
    onDateRangeSelected: (LocalDate, LocalDate) -> Unit
) {
    val date = month.atDay(day)
    val isSelected = date == selectedDate
    val isInRange = selectedStartDate != null && selectedEndDate != null && date in selectedStartDate..selectedEndDate
    val modifier = if (isSelected) {
        Modifier
            .background(Color.Blue, CircleShape)
            .padding(8.dp)
    } else if (isInRange) {
        Modifier
            .background(Color.LightGray, CircleShape)
            .padding(8.dp)
    } else {
        Modifier.padding(8.dp)
    }

    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .clickable {
                when (selectionType) {
                    is SelectionType.DateSelection -> {
                        onDateSelected(date)
                    }
                    is SelectionType.RangeSelection -> {
                        if (selectedStartDate == null || (selectedEndDate != null)) {
                            onDateRangeSelected(date, date)
                        } else {
                            if (date < selectedStartDate) {
                                onDateRangeSelected(date, selectedStartDate)
                            } else {
                                onDateRangeSelected(selectedStartDate, date)
                            }
                        }
                    }
                }
            }
            .then(modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            color = if (isSelected) Color.White else Color.Black,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

private fun LocalDate.lengthOfMonth(): Int {
    return this.month.daysIn(this.year)
}

private fun LocalDate.atDay(day: Int): LocalDate {
    return LocalDate(this.year, this.month, day)
}

private fun getCurrentYearMonth(): LocalDate {
    val currentDate = Clock.System.todayAt(TimeZone.currentSystemDefault())
    return LocalDate(currentDate.year, currentDate.month, dayOfMonth = 1)
}

private fun Month.daysIn(year: Int): Int {
    return when (this) {
        Month.FEBRUARY -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
        else -> 31
    }
}

public sealed class SelectionType {
    public data object DateSelection : SelectionType()
    public data object RangeSelection : SelectionType()
}

// public data class YearMonth(val year: Int, val month: Month)
