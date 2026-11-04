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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testmmp.resources.*

@Composable
internal fun ProgramStructureScreen(
    program: WorkoutProgram, onChange: (WorkoutProgram) -> Unit, onBack: () -> Unit, onContinue: () -> Unit,
    onAddExercise: (Int) -> Unit, onEditExercise: (Int, Int) -> Unit, onQuestionnaire: () -> Unit,
    libraryMode: Boolean = false,
) {
    var collapsedWeeks by rememberSaveable { mutableStateOf(listOf<Int>()) }
    var expandedDays by rememberSaveable { mutableStateOf(listOfNotNull(program.weeks.firstOrNull()?.days?.firstOrNull()?.id)) }
    var namingDay by rememberSaveable { mutableIntStateOf(-1) }
    var namingProgram by rememberSaveable { mutableStateOf(false) }
    fun moveExercise(dayId: Int, index: Int) {
        if (index > 0) onChange(program.updateDay(dayId) { day ->
            val updated = day.exercises.toMutableList()
            val exercise = updated.removeAt(index)
            updated.add(index - 1, exercise)
            day.copy(exercises = updated)
        })
    }
    WorkoutScaffold(program.title, if (libraryMode) "Редактор программы" else "Шаг 1", onBack) {
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (!libraryMode) item {
                WorkoutPanel(color = Color(0xFFF1D3D8)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        WorkoutIcon(Res.drawable.workout_warning, null, size = 16.dp)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Учитывайте ограничения клиента", color = Color(0xFFFF3547), fontSize = 13.sp)
                            Text("Чувствительность в колене. Избегать ударной нагрузки.", color = Ink, fontSize = 13.sp, lineHeight = 17.sp)
                        }
                    }
                    WorkoutAction("К анкете клиента", onQuestionnaire, outlined = true, compact = true)
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WorkoutHeading("Структура программы", Modifier.weight(1f))
                    TextButton(onClick = { namingProgram = true }) { Text("Название", color = Forest, fontSize = 12.sp) }
                }
            }
            items(program.weeks.size, key = { program.weeks[it].id }) { weekIndex ->
                val week = program.weeks[weekIndex]
                WorkoutPanel {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.width(28.dp).height(40.dp).clickable {
                            if (weekIndex > 0) {
                                val weeks = program.weeks.toMutableList()
                                weeks.add(weekIndex - 1, weeks.removeAt(weekIndex))
                                onChange(program.copy(weeks = weeks))
                            }
                        }, contentAlignment = Alignment.Center) {
                            WorkoutIcon(Res.drawable.week_drag, "Переместить неделю вверх")
                        }
                        Text("Неделя ${weekIndex + 1}", Modifier.weight(1f), color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        WorkoutIconButton(if (week.id in collapsedWeeks) Res.drawable.workout_down else Res.drawable.workout_up,
                            if (week.id in collapsedWeeks) "Развернуть неделю" else "Свернуть неделю", {
                                collapsedWeeks = if (week.id in collapsedWeeks) collapsedWeeks - week.id else collapsedWeeks + week.id
                            })
                    }
                    if (week.id !in collapsedWeeks) {
                        HorizontalDivider(color = Color(0xFFE1E6E2))
                        week.days.forEach { day ->
                            val expanded = day.id in expandedDays
                            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                .background(if (day.exercises.isEmpty()) Color(0xFFF3F6F5) else SoftPurple).padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f).clickable { namingDay = day.id }, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(day.title, fontSize = 14.sp, lineHeight = 20.sp, color = Ink, fontWeight = FontWeight.Bold)
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(if (day.exercises.isEmpty()) "Упражнения не назначены" else "${day.exercises.size} упражнений",
                                                fontSize = 12.sp, color = Color(0xFF66716B))
                                            if (day.exercises.isNotEmpty()) WorkoutChip("${day.exercises.size * 10} мин")
                                        }
                                    }
                                    WorkoutIconButton(if (expanded) Res.drawable.workout_up else Res.drawable.workout_down,
                                        if (expanded) "Свернуть день" else "Развернуть день", {
                                            expandedDays = if (expanded) expandedDays - day.id else expandedDays + day.id
                                        })
                                }
                                if (expanded) {
                                    day.exercises.forEachIndexed { index, exercise ->
                                        HorizontalDivider(color = Color(0xFFEAEAEA))
                                        ExerciseRow(exercise, index, { onEditExercise(day.id, exercise.id) }, { moveExercise(day.id, index) })
                                    }
                                    WorkoutAction("Добавить упражнение", { onAddExercise(day.id) }, color = Color.Black, compact = true)
                                }
                            }
                        }
                        WorkoutAction("Добавить день", {
                            val id = (program.weeks.flatMap { it.days }.maxOfOrNull { it.id } ?: 0) + 1
                            onChange(program.copy(weeks = program.weeks.map {
                                if (it.id == week.id) it.copy(days = it.days + TrainingDay(id, "День ${it.days.size + 1}")) else it
                            }))
                            expandedDays += id
                        }, outlined = true, compact = true)
                        if (program.weeks.size > 1) TextButton(onClick = {
                            onChange(program.copy(weeks = program.weeks.filterNot { it.id == week.id }))
                        }) { Text("Удалить неделю", color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                    } else {
                        Text("${week.days.size} тренировочных дня", color = Muted, fontSize = 12.sp)
                    }
                }
            }
            item {
                WorkoutAction("Добавить неделю", {
                    val weekId = (program.weeks.maxOfOrNull { it.id } ?: 0) + 1
                    val dayId = (program.weeks.flatMap { it.days }.maxOfOrNull { it.id } ?: 0) + 1
                    onChange(program.copy(weeks = program.weeks + TrainingWeek(weekId, listOf(TrainingDay(dayId, "День 1")))))
                    expandedDays += dayId
                }, color = Color.Black, compact = true)
            }
            if (program.exerciseCount == 0) item { Text(if (libraryMode) "Добавьте хотя бы одно упражнение, чтобы сохранить программу."
                else "Добавьте хотя бы одно упражнение, чтобы перейти к расписанию.", color = Muted, fontSize = 12.sp) }
        }
        WorkoutAction(if (libraryMode) "Сохранить программу" else "Сохранить и продолжить", onContinue, Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            enabled = program.exerciseCount > 0)
    }
    if (namingProgram) NameDialog("Название программы", program.title, { namingProgram = false }, {
        onChange(program.copy(title = it)); namingProgram = false
    })
    program.day(namingDay)?.let { day ->
        NameDialog("Название тренировочного дня", day.title, { namingDay = -1 }, {
            onChange(program.updateDay(day.id) { current -> current.copy(title = it) }); namingDay = -1
        }, onDelete = {
            onChange(program.copy(weeks = program.weeks.map { week -> week.copy(days = week.days.filterNot { it.id == day.id }) }))
            namingDay = -1
        })
    }
}

@Composable
private fun NameDialog(title: String, initial: String, onDismiss: () -> Unit, onSave: (String) -> Unit, onDelete: (() -> Unit)? = null) {
    var name by rememberSaveable(initial) { mutableStateOf(initial) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = {
        Column {
            OutlinedTextField(name, { name = it.take(80) }, singleLine = true, label = { Text("Название") })
            if (onDelete != null) TextButton(onClick = onDelete) { Text("Удалить день", color = MaterialTheme.colorScheme.error) }
        }
    }, confirmButton = { TextButton(onClick = { onSave(name.trim()) }, enabled = name.isNotBlank()) { Text("Сохранить") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } })
}
