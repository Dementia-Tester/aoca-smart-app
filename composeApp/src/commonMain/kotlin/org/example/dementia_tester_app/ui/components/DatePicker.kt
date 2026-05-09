package org.example.dementia_tester_app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.dementia_tester_app.utils.getMonthName

/**
 * A reusable date field component with date picker functionality
 *
 * @param date The current date in DD/MM/YYYY format
 * @param onDateChange Callback when the date changes
 * @param label The label for the date field
 * @param isError Whether the field is in the error state
 * @param isEditable Whether the field is editable
 * @param allowDatesAfterToday Whether to allow dates after today (true), before today (false), or all dates (null)
 * @param displayValue Optional custom value to display
 * @param modifier Additional modifier for the component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(
    date: String,
    onDateChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    isEditable: Boolean,
    allowDatesAfterToday: Boolean? = null,
    displayValue: String? = null,
    modifier: Modifier = Modifier
) {
    if (!isEditable) {
        Column(modifier = modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = displayValue ?: date,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
        return
    }

    val currentYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.year
    val parts = date.split("/")
    val validatedDate = try {
        val parsedDay = parts.getOrNull(0)?.toIntOrNull() ?: 1
        val parsedMonth = parts.getOrNull(1)?.toIntOrNull() ?: 1
        val parsedYear = parts.getOrNull(2)?.toIntOrNull() ?: currentYear

        LocalDate(
            parsedYear.coerceIn(currentYear - 120, currentYear),
            parsedMonth.coerceIn(1, 12),
            parsedDay.coerceIn(1, 31)
        )
    } catch (e: Exception) {
        LocalDate(currentYear, 1, 1)
    }

    var showDatePicker by remember { mutableStateOf(false) }

    val today = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    val minDate = remember {
        if (allowDatesAfterToday == false) null else today
    }

    Column(modifier = modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
                showDatePicker = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = date,
                onValueChange = { },
                readOnly = true,
                label = {
                    Text(label)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(
                        type = MenuAnchorType.PrimaryNotEditable
                    ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isError) FormColors.errorColor else FormColors.green,
                    unfocusedBorderColor = if (isError) FormColors.errorColor else FormColors.green,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    errorBorderColor = FormColors.errorColor
                ),
                isError = isError,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Select Date",
                        tint = FormColors.green
                    )
                }
            )
        }

        if (isError) {
            Spacer(Modifier.height(4.dp))
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (showDatePicker) {
            DatePickerDialog(
                initialDate = validatedDate,
                onDateSelected = { selectedDate ->
                    val formattedDate =
                        "${selectedDate.dayOfMonth}/${selectedDate.monthNumber}/${selectedDate.year}"
                    onDateChange(formattedDate)
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false },
                minDate = minDate,
                allowDatesAfterToday = allowDatesAfterToday
            )
        }
    }
}

/**
 * A calendar date picker dialog
 *
 * @param initialDate The initially selected date
 * @param onDateSelected Callback when a date is selected and confirmed
 * @param onDismiss Callback when the dialog is dismissed
 * @param minDate The minimum selectable date (defaults to null, no minimum)
 * @param allowDatesAfterToday Whether to allow dates after today (true), before today (false), or all dates (null)
 */
@Composable
fun DatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    minDate: LocalDate? = null,
    allowDatesAfterToday: Boolean? = null
) {
    var currentMonth by remember { mutableStateOf(initialDate.monthNumber) }
    var currentYear by remember { mutableStateOf(initialDate.year) }
    var selectedDate by remember { mutableStateOf(initialDate) }
    
    // State for showing year or month selection
    var showYearSelection by remember { mutableStateOf(false) }
    var showMonthSelection by remember { mutableStateOf(false) }
    
    // Calculate days in the current month
    val daysInMonth = try {
        val lastDay = when (currentMonth) {
            2 -> if (currentYear % 4 == 0 && (currentYear % 100 != 0 || currentYear % 400 == 0)) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }
        lastDay
    } catch (e: Exception) {
        31
    }
    
    // Calculate the first day of the month
    val firstDayOfMonth = try {
        LocalDate(currentYear, currentMonth, 1)
    } catch (e: Exception) {
        initialDate
    }
    
    // Get the day of week for the first day (0 = Monday, 6 = Sunday)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.ordinal
    
    // Get today's date for comparison
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    
    // Create a list of day cells to display
    val days = (1..daysInMonth).map { day ->
        val date = try {
            LocalDate(currentYear, currentMonth, day)
        } catch (e: Exception) {
            null
        }
        
        // Determine if the date is selectable based on allowDatesAfterToday parameter
        val isSelectable = date != null && when (allowDatesAfterToday) {
            true -> date >= today // Only allow dates after or equal to today
            false -> date <= today // Only allow dates before or equal to today
            null -> true // Allow all dates if parameter is null
        } && (minDate == null || date >= minDate) // Still respect minDate if provided
        
        DayCell(
            day = day,
            isSelected = date == selectedDate,
            isSelectable = isSelectable,
            date = date
        )
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous month button
                IconButton(onClick = {
                    if (currentMonth == 1) {
                        currentMonth = 12
                        currentYear--
                    } else {
                        currentMonth--
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Month"
                    )
                }
                
                // Month and year display
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = getMonthName(currentMonth),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clickable { 
                                showMonthSelection = !showMonthSelection
                                showYearSelection = false
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .background(
                                color = if (showMonthSelection) FormColors.green.copy(alpha = 0.1f) else Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        text = currentYear.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clickable { 
                                showYearSelection = !showYearSelection
                                showMonthSelection = false
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .background(
                                color = if (showYearSelection) FormColors.green.copy(alpha = 0.1f) else Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
                
                // Next month button
                IconButton(onClick = {
                    if (currentMonth == 12) {
                        currentMonth = 1
                        currentYear++
                    } else {
                        currentMonth++
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Month"
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (showYearSelection) {
                    // Year selection grid
                    // Get the current year for reference
                    val currentSystemYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.year
                    
                    // Create a range of years (100 years before and 5 years after current year)
                    val startYear = currentSystemYear - 100
                    val endYear = currentSystemYear + 5
                    val years = (startYear..endYear).toList()
                    
                    Text(
                        text = "Select Year",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.height(240.dp)
                    ) {
                        items(years) { year ->
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (year == currentYear) FormColors.green
                                        else Color.Transparent
                                    )
                                    .clickable {
                                        currentYear = year
                                        showYearSelection = false
                                    }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = year.toString(),
                                    color = if (year == currentYear) Color.White else Color.Black,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else if (showMonthSelection) {
                    // Month selection grid
                    Text(
                        text = "Select Month",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    
                    // Create a list of month names
                    val months = (1..12).map { monthNumber -> 
                        Pair(monthNumber, getMonthName(monthNumber))
                    }
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.height(240.dp)
                    ) {
                        items(months) { (monthNumber, monthName) ->
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (monthNumber == currentMonth) FormColors.green
                                        else Color.Transparent
                                    )
                                    .clickable {
                                        currentMonth = monthNumber
                                        showMonthSelection = false
                                    }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = monthName,
                                    color = if (monthNumber == currentMonth) Color.White else Color.Black,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    // Day of week headers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Calendar grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier.height(240.dp)
                    ) {
                        // Add empty cells for days before the first day of the month
                        items(firstDayOfWeek) {
                            Box(modifier = Modifier.aspectRatio(1f)) {}
                        }
                        
                        // Add day cells
                        items(days) { dayCell ->
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (dayCell.isSelected) FormColors.green
                                        else Color.Transparent
                                    )
                                    .then(
                                        if (dayCell.isSelectable) {
                                            Modifier.clickable {
                                                dayCell.date?.let { selectedDate = it }
                                            }
                                        } else {
                                            Modifier
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayCell.day.toString(),
                                    color = when {
                                        dayCell.isSelected -> Color.White
                                        !dayCell.isSelectable -> Color.Gray
                                        else -> Color.Black
                                    },
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDateSelected(selectedDate)
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
    )
}

/**
 * Data class representing a day cell in the calendar
 */
data class DayCell(
    val day: Int,
    val isSelected: Boolean,
    val isSelectable: Boolean,
    val date: LocalDate?
)