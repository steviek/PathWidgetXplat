package com.desaiwang.transit.path.app.ui.advancedsettings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.desaiwang.transit.path.app.settings.CommutingConfiguration
import com.desaiwang.transit.path.app.settings.activeSchedule
import com.desaiwang.transit.path.app.ui.PathBottomSheet
import com.desaiwang.transit.path.app.ui.advancedsettings.TimePickerMode.End
import com.desaiwang.transit.path.app.ui.advancedsettings.TimePickerMode.Start
import com.desaiwang.transit.path.app.ui.settings.SettingsHeader
import com.desaiwang.transit.path.time.UserPreferenceDayOfWeekComparator
import com.desaiwang.transit.path.time.is24HourClock
import com.desaiwang.transit.path.widget.WidgetDataFormatter.displayLabel
import com.desaiwang.transit.path.widget.WidgetDataFormatter.formatted
import com.desaiwang.transit.path.widget.WidgetDataFormatter.singleLetterLabel
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource
import pathwidgetxplat.composeapp.generated.resources.Res.string
import pathwidgetxplat.composeapp.generated.resources.cancel
import pathwidgetxplat.composeapp.generated.resources.commuting_schedule
import pathwidgetxplat.composeapp.generated.resources.commuting_schedule_description
import pathwidgetxplat.composeapp.generated.resources.days
import pathwidgetxplat.composeapp.generated.resources.end
import pathwidgetxplat.composeapp.generated.resources.ok
import pathwidgetxplat.composeapp.generated.resources.start

@Composable
fun CommutingScheduleBottomSheet(
    isShown: Boolean,
    configuration: CommutingConfiguration,
    onChanged: (CommutingConfiguration) -> Unit,
    onDismiss: () -> Unit,
) {
    val schedule = configuration.activeSchedule
    var timePickerMode by remember { mutableStateOf<TimePickerMode?>(null) }

    PathBottomSheet(
        isShown = isShown,
        onDismissRequest = onDismiss,
        title = stringResource(string.commuting_schedule),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(string.commuting_schedule_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(Modifier.height(16.dp))

        BoxWithConstraints {
            val density = LocalDensity.current
            val textSize = remember(constraints.maxWidth, density) {
                with(density) {
                    (constraints.maxWidth / 12).toSp()
                }
            }

            @Composable
            fun TimeText(time: LocalTime) {
                Text(
                    text = time.formatted(includePeriodMarker = true),
                    fontSize = textSize,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f).clickable { timePickerMode = TimePickerMode.Start }) {
                    SettingsHeader(text = stringResource(string.start))

                    TimeText(schedule.start)
                }

                Column(Modifier.weight(1f).clickable { timePickerMode = TimePickerMode.End }) {
                    SettingsHeader(text = stringResource(string.end))

                    TimeText(schedule.end)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        SettingsHeader(text = stringResource(string.days))
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DayOfWeek.entries.sortedWith(UserPreferenceDayOfWeekComparator()).forEach { day ->
                DayToggle(
                    day = day,
                    checked = day in schedule.days,
                    onCheckedChanged = { isChecked ->
                        val newDays = if (isChecked) schedule.days + day else schedule.days - day
                        onChanged(
                            configuration.copy(schedules = listOf(schedule.copy(days = newDays)))
                        )

                    },
                )
            }
        }

        Spacer(Modifier.height(64.dp))

        when (val mode = timePickerMode) {
            Start -> {
                TimePickerDialog(
                    mode = mode,
                    initialTime = schedule.start,
                    onConfirm = { time ->
                        onChanged(
                            configuration.copy(schedules = listOf(schedule.copy(start = time))),
                        )
                        timePickerMode = null
                    },
                    onDismiss = { timePickerMode = null }
                )
            }

            End -> {
                TimePickerDialog(
                    mode = mode,
                    initialTime = schedule.end,
                    onConfirm = { time ->
                        onChanged(configuration.copy(schedules = listOf(schedule.copy(end = time))))
                        timePickerMode = null
                    },
                    onDismiss = { timePickerMode = null }
                )
            }

            null -> {}
        }
    }
}

@Composable
private fun TimePickerDialog(
    mode: TimePickerMode,
    initialTime: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val pickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = is24HourClock()
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime(pickerState.hour, pickerState.minute)) }) {
                Text(stringResource(string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(string.cancel))
            }
        },
        title = {
            val label = when (mode) {
                Start -> string.start
                End -> string.end
            }
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                Text(stringResource(label))
            }
        },
        text = {
            TimePicker(state = pickerState)
        }
    )
}

@Composable
private fun DayToggle(day: DayOfWeek, checked: Boolean, onCheckedChanged: (Boolean) -> Unit) {
    FilledIconToggleButton(
        checked = checked,
        onCheckedChange = onCheckedChanged,
    ) {
        Text(
            day.singleLetterLabel(),
            modifier = Modifier.semantics { contentDescription = day.displayLabel() })
    }
}

private enum class TimePickerMode { Start, End }
