package com.example.testmmp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testmmp.resources.*
import org.jetbrains.compose.resources.painterResource

/** Client detail layout from Figma 1069:11931; its measurements are independent of the program editor. */
@Composable
internal fun ClientScheduleScreen(
    assigned: Boolean, program: WorkoutProgram, onBack: () -> Unit, onAssign: () -> Unit,
    onSingleWorkout: () -> Unit, onEdit: () -> Unit, onMessage: (String) -> Unit,
    clientName: String = "Андрей Синицин",
) {
    val initialDate = if (assigned) program.start else WorkoutDate(2026, 10, 25)
    var monthKey by rememberSaveable { mutableStateOf(initialDate.key) }
    var selectedKey by rememberSaveable { mutableStateOf(initialDate.key) }
    var showAll by rememberSaveable { mutableStateOf(false) }
    val workouts = if (assigned) program.schedule() else emptyList()
    ProvideTextStyle(TextStyle(letterSpacing = 0.sp)) {
    Column(Modifier.fillMaxSize().background(Color(0xFF0A0D0B)).background(FitScreenBrush)) {
        Row(Modifier.fillMaxWidth().statusBarsPadding().height(64.dp), verticalAlignment = Alignment.CenterVertically) {
            WorkoutIconButton(Res.drawable.workout_left, "Назад", onBack, light = true)
            Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.size(32.dp).background(Color(0xFF6750A4), CircleShape), contentAlignment = Alignment.Center) {
                    Text("А", color = Color.White, fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium,
                        letterSpacing = .15.sp, fontFamily = fitInter())
                }
            }
            Column(Modifier.weight(1f).padding(start = 8.dp, end = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(clientName, fontFamily = fitInter(), fontSize = 20.sp, lineHeight = 28.sp,
                    fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Клиент", fontFamily = fitInter(), fontSize = 14.sp, lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium, letterSpacing = .5.sp, color = Color(0xFF7F8981))
            }
            FitIconButton({ onMessage("Чат с клиентом") }) {
                WorkoutIcon(Res.drawable.client_profile_chat_icon, "Написать клиенту", size = 24.dp)
            }
            WorkoutIconButton(Res.drawable.client_more, "Действия с клиентом", { onMessage("Информация о клиенте\n$clientName") }, light = true)
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp, start = 16.dp, end = 16.dp).horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Расписание", "Отчёты", "Запросы", "Инфо").forEachIndexed { index, label ->
                Column(Modifier.height(32.dp).clickable(role = Role.Tab) {
                    if (index > 0) onMessage(when (index) { 1 -> "Отчётов пока нет"; 2 -> "Новых запросов нет"; else -> "$clientName\nКлиент" })
                }.semantics { selected = index == 0 }.padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(label, fontSize = 14.sp, lineHeight = 16.sp, fontWeight = if (index == 0) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (index == 0) Lime else Color(0xFFB9C8BF))
                    if (index == 0) Box(Modifier.width(83.dp).height(2.dp).clip(CircleShape).background(Lime))
                }
            }
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
            .background(Color(0xFFF0F0F0)).navigationBarsPadding(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFFAFAFA)).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(if (assigned) program.title else "Программа не назначена", fontSize = 14.sp, lineHeight = 17.sp,
                            fontWeight = FontWeight.SemiBold, color = Color(0xFF212121))
                        Text(if (assigned) "${program.weeks.size} нед. · ${program.exerciseCount} упражнений" else "Создайте новую программу или выберите готовую",
                            fontSize = 12.sp, lineHeight = 15.sp, color = Color(0xFF212121))
                    }
                    ProfileAction(if (assigned) "Редактировать программу" else "Назначить программу", if (assigned) onEdit else onAssign)
                }
            }
            item {
                ClientProfileCalendar(WorkoutDate.parse(monthKey), WorkoutDate.parse(selectedKey),
                    { monthKey = it.key }, { selectedKey = it.key; monthKey = it.key }, workouts.map { it.date.key }.toSet())
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth().height(24.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Тренировки", Modifier.weight(1f), color = Ink, fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold)
                        Row(Modifier.width(if (showAll) 66.dp else 57.dp).fillMaxHeight().clip(RoundedCornerShape(8.dp)).background(Color(0xFFE0E0E0))
                            .clickable(role = Role.Button) { showAll = !showAll }.padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(if (showAll) "День" else "Все", fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121))
                            WorkoutIcon(Res.drawable.client_profile_all, null, size = 16.dp)
                        }
                    }
                    val visible = if (showAll) workouts else workouts.filter { it.date.key == selectedKey }
                    if (visible.isEmpty()) {
                        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White).padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(Modifier.padding(top = 1.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("На этот день тренировок нет", Modifier.fillMaxWidth(), color = Color(0xFF212121), fontSize = 16.sp,
                                    lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                                Text("Вы можете назначить дополнительную тренировку вне основной программы.", Modifier.fillMaxWidth(),
                                    color = Color(0xFF212121), fontSize = 14.sp, lineHeight = 20.sp, textAlign = TextAlign.Center)
                            }
                            ProfileAction("Назначить тренировку", onSingleWorkout, outlined = true)
                        }
                    } else visible.forEach { workout ->
                        WorkoutPanel {
                            Text("${workout.date.display} · ${workout.time}", color = Muted, fontSize = 13.sp, lineHeight = 18.sp)
                            Text(workout.day.title, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold, color = Ink)
                            workout.day.exercises.forEach { exercise ->
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ExercisePhoto(exercise.catalogIndex)
                                    Column(Modifier.weight(1f)) {
                                        Text(ExerciseNames[exercise.catalogIndex], fontSize = 14.sp, lineHeight = 20.sp, color = Ink)
                                        Text(listOfNotNull(exercise.repetitionLabel(), exercise.weightLabel(),
                                            if (exercise.restEnabled) "${exercise.restMinutes} мин" else null,
                                            if (exercise.supersetId > 0) "Суперсет ${exercise.supersetId}" else null).joinToString(" · "),
                                            color = Muted, fontSize = 11.sp, lineHeight = 16.sp)
                                    }
                                }
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
private fun ProfileAction(label: String, onClick: () -> Unit, outlined: Boolean = false) {
    val shape = RoundedCornerShape(if (outlined) 14.dp else 12.dp)
    Box(Modifier.fillMaxWidth().height(if (outlined) 44.dp else 34.dp).clip(shape)
        .background(if (outlined) Color.Transparent else Color(0xFF7D39EB))
        .then(if (outlined) Modifier.border(1.dp, Color.Black, shape) else Modifier)
        .clickable(role = Role.Button, onClick = onClick), contentAlignment = Alignment.Center) {
        Text(label, fontSize = if (outlined) 14.sp else 13.sp, lineHeight = if (outlined) 20.sp else 18.sp,
            fontWeight = FontWeight.SemiBold, color = if (outlined) Color(0xFF212121) else Color.White)
    }
}

@Composable
private fun ClientProfileCalendar(month: WorkoutDate, selectedDate: WorkoutDate, onMonth: (WorkoutDate) -> Unit,
    onSelect: (WorkoutDate) -> Unit, scheduled: Set<String>) {
    val font = fitInter()
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFE0E0E0)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth().height(40.dp), verticalAlignment = Alignment.CenterVertically) {
            FitIconButton({ onMonth(month.plusMonths(-1)) }, Modifier.size(40.dp).background(Color(0xFFFAFAFA), CircleShape)) {
                WorkoutIcon(Res.drawable.client_profile_prev, "Предыдущий месяц", size = 20.dp)
            }
            Text(MonthNames[month.month - 1], Modifier.weight(1f), fontFamily = font, fontSize = 16.sp, lineHeight = 22.4.sp,
                letterSpacing = .2.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121), textAlign = TextAlign.Center)
            FitIconButton({ onMonth(month.plusMonths(1)) }, Modifier.size(40.dp).background(Color(0xFFFAFAFA), CircleShape)) {
                WorkoutIcon(Res.drawable.client_profile_next, "Следующий месяц", size = 20.dp)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth().height(29.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ShortWeekdays.forEach { label -> Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    Text(label, fontFamily = font, fontSize = 14.sp, lineHeight = 21.sp, fontWeight = FontWeight.Medium, color = Color(0xFF212121))
                } }
            }
            val first = month.copy(day = 1)
            val count = ((first.weekday + first.daysInMonth + 6) / 7) * 7
            (0 until count step 7).forEach { offset ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(7) { column ->
                        val number = offset + column - first.weekday + 1
                        val date = when {
                            number < 1 -> first.plusMonths(-1).let { it.copy(day = it.daysInMonth + number) }
                            number > first.daysInMonth -> first.plusMonths(1).copy(day = number - first.daysInMonth)
                            else -> first.copy(day = number)
                        }
                        val active = date == selectedDate
                        Box(Modifier.weight(1f).height(36.dp).clip(RoundedCornerShape(8.dp))
                            .background(if (active) Color(0xFF1A201C) else if (date.key in scheduled) SoftGreen else Color.White)
                            .clickable(role = Role.RadioButton) { onSelect(date) }.semantics { selected = active }, contentAlignment = Alignment.Center) {
                            Text(date.day.toString(), fontFamily = font, fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium,
                                color = if (active) Lime else if (number !in 1..first.daysInMonth) Color(0xFF757575) else Color(0xFF212121))
                            if (date.key in scheduled) Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp).size(3.dp).background(if (active) Lime else Forest, CircleShape))
                        }
                    }
                }
            }
        }
    }
}
