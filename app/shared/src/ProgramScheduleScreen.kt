package com.example.testmmp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testmmp.resources.*

@Composable
internal fun ProgramScheduleScreen(
    program: WorkoutProgram, onChange: (WorkoutProgram) -> Unit, onBack: () -> Unit, onSave: () -> Unit,
    onEditProgram: () -> Unit,
    onEditExercise: (Int, Int) -> Unit, onAddExercise: (Int) -> Unit,
) {
    var monthKey by rememberSaveable { mutableStateOf(program.start.key) }
    var selectedKey by rememberSaveable { mutableStateOf(program.start.key) }
    var showDays by rememberSaveable { mutableStateOf(false) }
    var showDate by rememberSaveable { mutableStateOf(false) }
    var showAll by rememberSaveable { mutableStateOf(false) }
    val workouts = program.schedule()
    val validTimes = program.weekdays.all { Regex("([01][0-9]|2[0-3]):[0-5][0-9]").matches(program.times[it]) }
    val monthNames = listOf("января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря")
    val selectedDate = WorkoutDate.parse(selectedKey)
    val lastDate = workouts.maxByOrNull { it.date.key }?.date ?: program.start
    val dateRange = if (lastDate.month == program.start.month && lastDate.year == program.start.year)
        "С ${program.start.day}–${lastDate.day} ${monthNames[lastDate.month - 1]}"
    else "С ${program.start.display} по ${lastDate.display}"
    val scheduleSummary = dateRange + " · " + program.weekdays.sorted().joinToString(", ") { "${ShortWeekdays[it]} ${program.times[it]}" }
    WorkoutScaffold("Расписание", "Шаг 2", onBack, compactHeader = true) {
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                WorkoutPanel {
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFF0F0F0))
                        .clickable(role = Role.Button, onClick = onEditProgram).padding(12.dp),
                        verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(program.title, fontSize = 14.sp, lineHeight = 20.sp, color = Ink, fontWeight = FontWeight.Bold)
                            Text("Редактировать", fontSize = 14.sp, lineHeight = 20.sp,
                                color = Color(0xFF7D39EB), fontWeight = FontWeight.SemiBold)
                        }
                        WorkoutIcon(Res.drawable.schedule_edit, "Редактировать программу")
                    }
                    if (program.scheduleApplied && !showDays) {
                        HorizontalDivider(thickness = .5.dp, color = Color(0xFFADB5BD))
                        Row(Modifier.fillMaxWidth().clickable(role = Role.Button) { showDays = true },
                            horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Расписание тренировок", fontSize = 16.sp, color = Ink, fontWeight = FontWeight.SemiBold)
                                Text(scheduleSummary, fontSize = 14.sp, lineHeight = 18.sp, color = Ink)
                            }
                            WorkoutIcon(Res.drawable.workout_down, "Настроить расписание")
                        }
                    } else if (!showDays) {
                        WorkoutAction("Настроить расписание", { showDays = true }, color = Color.Black, compact = true)
                    }
                    if (showDays) {
                        HorizontalDivider(thickness = .5.dp, color = Color(0xFFADB5BD))
                        Row(Modifier.fillMaxWidth().clickable(role = Role.Button) { showDays = false },
                            verticalAlignment = Alignment.CenterVertically) {
                            Text("Расписание тренировок", Modifier.weight(1f), fontSize = 16.sp, color = Ink, fontWeight = FontWeight.SemiBold)
                            WorkoutIcon(Res.drawable.workout_up, "Свернуть настройки")
                        }
                        Text("Выберите дату начала программы", fontSize = 14.sp, color = Ink)
                        OutlinedTextField(program.start.display, {}, Modifier.fillMaxWidth(), readOnly = true, singleLine = true,
                            shape = RoundedCornerShape(12.dp), trailingIcon = {
                                WorkoutIconButton(Res.drawable.workout_calendar, "Выбрать дату начала", { showDate = true })
                            })
                        Text("Выберите дни занятий", fontSize = 16.sp, color = Ink, fontWeight = FontWeight.Medium)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ShortWeekdays.forEachIndexed { index, label ->
                                val active = index in program.weekdays
                                Box(Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(10.dp))
                                    .background(if (active) Color(0xFF121713) else Page)
                                    .clickable(role = Role.Checkbox) {
                                        onChange(program.copy(weekdays = if (active) program.weekdays - index else program.weekdays + index,
                                            scheduleApplied = false))
                                    }.semantics { selected = active }, contentAlignment = Alignment.Center) {
                                    Text(label, color = if (active) Color.White else Ink, fontSize = 14.sp)
                                }
                            }
                        }
                        WorkoutPanel(color = Color(0xFFF0F0F0)) {
                            Text("Время тренировок", fontSize = 16.sp, color = Ink, fontWeight = FontWeight.Medium)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Повторяется каждую неделю", fontSize = 14.sp, lineHeight = 20.sp,
                                        fontWeight = FontWeight.Medium, color = Ink)
                                    Text("Это время будет подставлено в каждую тренировку", fontSize = 12.sp,
                                        lineHeight = 15.sp, color = Color(0xFF545B66))
                                }
                                Switch(program.repeatWeekly, { onChange(program.copy(repeatWeekly = it, scheduleApplied = false)) },
                                    colors = SwitchDefaults.colors(checkedTrackColor = Purple, checkedThumbColor = Color.White))
                            }
                            HorizontalDivider(thickness = .5.dp, color = Color(0xFF939CA7))
                            if (program.weekdays.isEmpty()) Text("Выберите дни недели", color = Muted, fontSize = 13.sp)
                            program.weekdays.sorted().forEach { index ->
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(WeekdayNames[index], Modifier.weight(1f), fontSize = 14.sp, color = Ink)
                                    OutlinedTextField(program.times[index], { value ->
                                        onChange(program.copy(times = program.times.mapIndexed { i, time ->
                                            if (i == index) value.filter { it.isDigit() || it == ':' }.take(5) else time
                                        }, scheduleApplied = false))
                                    }, Modifier.width(116.dp).semantics { contentDescription = "Время: ${WeekdayNames[index]}" }, singleLine = true,
                                        isError = !Regex("([01][0-9]|2[0-3]):[0-5][0-9]").matches(program.times[index]),
                                        trailingIcon = { WorkoutIcon(Res.drawable.workout_clock, null) },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White))
                                }
                            }
                            if (!validTimes) Text("Укажите время в формате ЧЧ:ММ от 00:00 до 23:59", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                        Text("Позже время можно будет изменить в конкретной тренировке по необходимости",
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFFAFAFA)).padding(12.dp),
                            fontSize = 12.sp, lineHeight = 15.sp, color = Ink)
                        WorkoutAction("Применить", {
                            onChange(program.copy(scheduleApplied = true))
                            showDays = false
                            selectedKey = program.copy(scheduleApplied = true).schedule().firstOrNull()?.date?.key ?: program.start.key
                            monthKey = selectedKey
                        }, color = Color.Black, compact = true, enabled = program.weekdays.isNotEmpty() && validTimes)
                    }
                }
            }
            item { WorkoutCalendar(WorkoutDate.parse(monthKey), WorkoutDate.parse(selectedKey), { monthKey = it.key },
                { selectedKey = it.key; monthKey = it.key }, workouts.map { it.date.key }.toSet(),
                start = if (program.scheduleApplied) program.start else null, assignmentStyle = true) }
            if (program.scheduleApplied) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        WorkoutHeading("${selectedDate.day} ${monthNames[selectedDate.month - 1]}", Modifier.weight(1f))
                        TextButton(onClick = { showAll = !showAll }) { Text(if (showAll) "На день" else "Все ›", color = Ink) }
                    }
                }
                val visible = if (showAll) workouts else workouts.filter { it.date.key == selectedKey }
                if (visible.isEmpty()) item { Text("На выбранный день тренировок нет", color = Muted, fontSize = 14.sp) }
                items(visible.size) { index ->
                    val workout = visible[index]
                    WorkoutPanel {
                        Text(workout.day.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("${workout.day.exercises.size} упражнений", fontSize = 12.sp, color = Muted)
                            WorkoutChip("${workout.day.exercises.size * 10} мин")
                            Text(workout.time, color = Muted, fontSize = 12.sp)
                        }
                        if (showAll) Text(workout.date.display, color = Muted, fontSize = 12.sp)
                        workout.day.exercises.forEachIndexed { exerciseIndex, exercise ->
                            HorizontalDivider(color = Color(0xFFEAEAEA))
                            ExerciseRow(exercise, exerciseIndex, { onEditExercise(workout.day.id, exercise.id) }, {
                                if (exerciseIndex > 0) onChange(program.updateDay(workout.day.id) { day ->
                                    val exercises = day.exercises.toMutableList()
                                    exercises.add(exerciseIndex - 1, exercises.removeAt(exerciseIndex))
                                    day.copy(exercises = exercises)
                                })
                            })
                        }
                        WorkoutAction("Добавить упражнение", { onAddExercise(workout.day.id) }, color = Color.Black, compact = true)
                    }
                }
            }
        }
        Box(Modifier.fillMaxWidth().background(Color.White).padding(top = 4.dp)) {
            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)).background(Page)
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)) {
                WorkoutAction("Сохранить и назначить", {
                    if (program.scheduleApplied && workouts.isNotEmpty() && validTimes && program.exerciseCount > 0) onSave()
                    else if (program.exerciseCount == 0) onEditProgram()
                    else showDays = true
                })
            }
        }
    }
    if (showDate) {
        var pickerMonth by rememberSaveable { mutableStateOf(program.start.key) }
        var pickerDate by rememberSaveable { mutableStateOf(program.start.key) }
        AlertDialog(onDismissRequest = { showDate = false }, title = { Text("Начало программы") }, text = {
            WorkoutCalendar(WorkoutDate.parse(pickerMonth), WorkoutDate.parse(pickerDate), { pickerMonth = it.key }, { pickerDate = it.key })
        }, confirmButton = { TextButton(onClick = {
            onChange(program.copy(start = WorkoutDate.parse(pickerDate), scheduleApplied = false))
            monthKey = pickerDate; selectedKey = pickerDate; showDate = false
        }) { Text("Выбрать") } }, dismissButton = { TextButton(onClick = { showDate = false }) { Text("Отмена") } })
    }
}
