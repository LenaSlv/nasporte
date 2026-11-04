package com.example.testmmp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testmmp.resources.*

@Composable
internal fun ProgramStructureScreen(
    program: WorkoutProgram, onChange: (WorkoutProgram) -> Unit, onBack: () -> Unit, onContinue: () -> Unit,
    onAddExercise: (Int) -> Unit, onEditExercise: (Int, Int) -> Unit, onQuestionnaire: () -> Unit,
    libraryMode: Boolean = false,
) {
    var collapsedWeeks by rememberSaveable { mutableStateOf(program.weeks.filter { it.days.isEmpty() }.map { it.id }) }
    var expandedDays by rememberSaveable { mutableStateOf(listOf<Int>()) }
    var namingDay by rememberSaveable { mutableIntStateOf(-1) }
    var namingProgram by rememberSaveable { mutableStateOf(false) }
    var showRestrictions by rememberSaveable { mutableStateOf(true) }
    var weekMenu by rememberSaveable { mutableIntStateOf(-1) }
    fun moveExercise(dayId: Int, index: Int) {
        if (index > 0) onChange(program.updateDay(dayId) { day ->
            val updated = day.exercises.toMutableList()
            updated.add(index - 1, updated.removeAt(index))
            day.copy(exercises = updated)
        })
    }
    fun toggleWeek(id: Int) { collapsedWeeks = if (id in collapsedWeeks) collapsedWeeks - id else collapsedWeeks + id }
    WorkoutScaffold(if (libraryMode) program.title else "Новая программа",
        if (libraryMode) "Редактор программы" else "Шаг 1", onBack, compactHeader = true) {
        LazyColumn(Modifier.weight(1f).background(Color.White), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (!libraryMode) item {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Page).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    WorkoutHeading("Название программы")
                    FitTextField(program.title, { onChange(program.copy(title = it.take(80))) },
                        trailingIcon = {
                            FitIconButton({ onChange(program.copy(title = "")) }, Modifier.size(24.dp)) {
                                WorkoutIcon(Res.drawable.field_clear, "Очистить название")
                            }
                        })
                    Text("Назовите программу так, чтобы легко находить её и понимать, на что она направлена.",
                        fontSize = 14.sp, lineHeight = 17.sp, color = Color(0xFF212121))
                }
            }
            item {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Page).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (!libraryMode && showRestrictions) {
                        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFF1D9D9)).padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                                WorkoutIcon(Res.drawable.workout_warning, null, size = 16.dp)
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Учитывайте ограничения клиента", color = Color(0xFFB3261E), fontSize = 14.sp,
                                        lineHeight = 17.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Чувствительность в колене. Избегать ударной нагрузки.", color = Color(0xFF212121),
                                        fontSize = 14.sp, lineHeight = 17.sp, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                                FitIconButton({ showRestrictions = false }, Modifier.size(16.dp)) {
                                    WorkoutIcon(Res.drawable.workout_close, "Скрыть ограничения", size = 16.dp)
                                }
                            }
                            WorkoutAction("К анкете клиента", onQuestionnaire, outlined = true, compact = true)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Структура программы", Modifier.weight(1f), fontSize = 18.sp, lineHeight = 20.sp,
                            color = Color(0xFF212121), fontWeight = FontWeight.SemiBold)
                        if (libraryMode) FitTextButton({ namingProgram = true }) { Text("Название", color = Purple, fontSize = 12.sp) }
                    }
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White)) {
                        program.weeks.forEachIndexed { weekIndex, week ->
                            if (weekIndex > 0) HorizontalDivider(thickness = 2.dp, color = Page)
                            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box {
                                        FitIconButton({ weekMenu = week.id }, Modifier.size(24.dp)) {
                                            WorkoutIcon(Res.drawable.week_drag, "Действия с неделей")
                                        }
                                        FitDropdownMenu(weekMenu == week.id, { weekMenu = -1 }) {
                                            if (weekIndex > 0) FitDropdownMenuItem({ Text("Переместить вверх") }, {
                                                val weeks = program.weeks.toMutableList()
                                                weeks.add(weekIndex - 1, weeks.removeAt(weekIndex))
                                                onChange(program.copy(weeks = weeks)); weekMenu = -1
                                            })
                                            if (program.weeks.size > 1) FitDropdownMenuItem({ Text("Удалить неделю", color = Color(0xFFB3261E)) }, {
                                                onChange(program.copy(weeks = program.weeks.filterNot { it.id == week.id })); weekMenu = -1
                                            })
                                        }
                                    }
                                    Column(Modifier.weight(1f).clickable { toggleWeek(week.id) }, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Неделя ${weekIndex + 1}", fontSize = 16.sp, lineHeight = 20.sp, color = Color(0xFF212121), fontWeight = FontWeight.Bold)
                                        if (week.days.isEmpty()) Text("Дни не назначены", fontSize = 14.sp, lineHeight = 17.sp, color = Color(0xFF212121))
                                    }
                                    FitIconButton({ toggleWeek(week.id) }, Modifier.size(24.dp)) {
                                        WorkoutIcon(if (week.id in collapsedWeeks) Res.drawable.workout_down else Res.drawable.workout_up,
                                            if (week.id in collapsedWeeks) "Развернуть неделю" else "Свернуть неделю")
                                    }
                                }
                                if (week.id !in collapsedWeeks) {
                                    week.days.forEach { day ->
                                        val expanded = day.id in expandedDays
                                        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                            .background(if (day.exercises.isEmpty()) Color(0xFFF0F0F0) else Color(0xFFEEEAF8)).padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                FitIconButton({ namingDay = day.id }, Modifier.size(20.dp)) {
                                                    WorkoutIcon(Res.drawable.exercise_drag, "Изменить тренировочный день", size = 20.dp)
                                                }
                                                Column(Modifier.weight(1f).clickable { namingDay = day.id }, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Text(day.title, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold, color = Ink)
                                                    if (day.exercises.isEmpty()) Text("Добавить упражнение", Modifier.clickable { onAddExercise(day.id) },
                                                        color = Purple, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold)
                                                    else Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        Text("${day.exercises.size} упражнений", fontSize = 14.sp, lineHeight = 17.sp, color = Ink)
                                                        Text("${day.exercises.size * 10} мин", Modifier.clip(RoundedCornerShape(20.dp)).background(Purple)
                                                            .padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 12.sp, lineHeight = 16.sp,
                                                            fontWeight = FontWeight.SemiBold, color = Color.White)
                                                    }
                                                }
                                                FitIconButton({ expandedDays = if (expanded) expandedDays - day.id else expandedDays + day.id }, Modifier.size(24.dp)) {
                                                    WorkoutIcon(if (expanded) Res.drawable.workout_up else Res.drawable.workout_down,
                                                        if (expanded) "Свернуть день" else "Развернуть день")
                                                }
                                            }
                                            if (expanded && day.exercises.isNotEmpty()) {
                                                day.exercises.forEachIndexed { index, exercise ->
                                                    if (index > 0) HorizontalDivider(color = Color(0xFFEAEAEA))
                                                    ExerciseRow(exercise, index, { onEditExercise(day.id, exercise.id) }, { moveExercise(day.id, index) })
                                                }
                                                FitTextButton({ onAddExercise(day.id) }) { Text("Добавить упражнение", color = Purple, fontSize = 14.sp) }
                                            }
                                        }
                                    }
                                    WorkoutAction("Добавить день", {
                                        val id = (program.weeks.flatMap { it.days }.maxOfOrNull { it.id } ?: 0) + 1
                                        onChange(program.copy(weeks = program.weeks.map {
                                            if (it.id == week.id) it.copy(days = it.days + TrainingDay(id, "День ${it.days.size + 1}")) else it
                                        }))
                                    }, outlined = true, compact = true)
                                }
                            }
                        }
                        Box(Modifier.padding(12.dp)) {
                            WorkoutAction("Добавить неделю", {
                                val id = (program.weeks.maxOfOrNull { it.id } ?: 0) + 1
                                onChange(program.copy(weeks = program.weeks + TrainingWeek(id, emptyList())))
                                collapsedWeeks += id
                            }, outlined = true, compact = true, fontSize = 14.sp, lineHeight = 20.sp, height = 44.dp)
                        }
                    }
                }
            }
        }
        FitActionFooter {
            WorkoutAction(if (libraryMode) "Сохранить программу" else "К настройке расписания", onContinue,
                enabled = program.exerciseCount > 0 && program.title.isNotBlank())
        }
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
    FitDialog(onDismiss, title = { Text(title) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FitTextField(name, { name = it.take(80) }, label = { Text("Название") })
            if (onDelete != null) FitTextButton(onDelete) { Text("Удалить день", color = Color(0xFFB3261E)) }
        }
    }, confirmButton = { WorkoutAction("Сохранить", { onSave(name.trim()) }, enabled = name.isNotBlank()) },
        dismissButton = { WorkoutAction("Отмена", onDismiss, outlined = true) })
}
