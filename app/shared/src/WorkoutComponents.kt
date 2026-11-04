package com.example.testmmp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testmmp.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

internal val Purple = Color(0xFF6C45E5)
internal val SoftPurple = Color(0xFFF5F0FB)
internal val SoftGreen = Color(0xFFEDF7BC)

@Composable
internal fun WorkoutIcon(asset: DrawableResource, description: String?, modifier: Modifier = Modifier, size: Dp = 24.dp) {
    Image(painterResource(asset), description, modifier.size(size))
}

@Composable
internal fun WorkoutIconButton(asset: DrawableResource, description: String, onClick: () -> Unit, light: Boolean = false, size: Dp = 24.dp) {
    IconButton(onClick = onClick) {
        Icon(painterResource(asset), description, Modifier.size(size), tint = if (light) Color.White else Ink)
    }
}

@Composable
internal fun WorkoutAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Lime,
    outlined: Boolean = false,
    enabled: Boolean = true,
    compact: Boolean = false,
) {
    val shape = RoundedCornerShape(if (compact) 12.dp else 16.dp)
    val content = if (color == Color.Black || color == Purple) Color.White else Ink
    Box(modifier.fillMaxWidth().heightIn(min = if (compact) 34.dp else 52.dp)
        .clip(shape).background(if (!enabled) Color(0xFFE0E4D6) else if (outlined) Color.Transparent else color)
        .then(if (outlined) Modifier.border(BorderStroke(1.dp, if (enabled) Ink else Muted), shape) else Modifier)
        .clickable(enabled = enabled, role = Role.Button, onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center) {
        Text(text, color = if (enabled) content else Muted, fontSize = 13.sp, lineHeight = 18.sp,
            fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
    }
}

@Composable
internal fun WorkoutScaffold(title: String, subtitle: String, onBack: () -> Unit, compactHeader: Boolean = false,
    content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xFF020B07), Forest)))) {
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(top = 8.dp, bottom = if (compactHeader) 8.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically) {
            WorkoutIconButton(Res.drawable.workout_left, "Назад", onBack, light = true)
            Column(Modifier.weight(1f).padding(end = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Text(subtitle, fontSize = 14.sp, lineHeight = 18.sp, color = Color(0xFF99A29C))
            }
        }
        Column(Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
            .background(Page).navigationBarsPadding(), content = content)
    }
}

@Composable
internal fun WorkoutPanel(modifier: Modifier = Modifier, color: Color = Color.White, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(color).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
}

@Composable
internal fun WorkoutHeading(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier, color = Color(0xFF18303A), fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
internal fun WorkoutCalendar(
    month: WorkoutDate,
    selected: WorkoutDate,
    onMonth: (WorkoutDate) -> Unit,
    onSelect: (WorkoutDate) -> Unit,
    scheduled: Set<String> = emptySet(),
    start: WorkoutDate? = null,
    assignmentStyle: Boolean = false,
) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(if (assignmentStyle) 12.dp else 14.dp))
        .background(if (assignmentStyle) Color(0xFFE0E0E0) else Color(0xFFD7D9DB)).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(if (assignmentStyle) 12.dp else 4.dp)) {
        Row(Modifier.fillMaxWidth().padding(bottom = if (assignmentStyle) 0.dp else 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(if (assignmentStyle) Color(0xFFFAFAFA) else Card), contentAlignment = Alignment.Center) {
                WorkoutIconButton(Res.drawable.workout_left, "Предыдущий месяц", { onMonth(month.plusMonths(-1)) }, size = if (assignmentStyle) 20.dp else 24.dp)
            }
            Text(if (assignmentStyle) MonthNames[month.month - 1] else "${MonthNames[month.month - 1]} ${month.year}", Modifier.weight(1f), fontSize = 16.sp,
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = Ink)
            Box(Modifier.size(40.dp).clip(CircleShape).background(if (assignmentStyle) Color(0xFFFAFAFA) else Card), contentAlignment = Alignment.Center) {
                WorkoutIconButton(Res.drawable.workout_right, "Следующий месяц", { onMonth(month.plusMonths(1)) }, size = if (assignmentStyle) 20.dp else 24.dp)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(if (assignmentStyle) 4.dp else 0.dp)) {
                ShortWeekdays.forEach { Text(it, Modifier.weight(1f).padding(vertical = if (assignmentStyle) 4.dp else 6.dp), textAlign = TextAlign.Center,
                    color = Ink, fontSize = 14.sp, fontWeight = if (assignmentStyle) FontWeight.Bold else FontWeight.Medium) }
            }
            val first = month.copy(day = 1)
            val count = ((first.weekday + first.daysInMonth + 6) / 7) * 7
            (0 until count step 7).forEach { offset ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(7) { column ->
                        val day = offset + column - first.weekday + 1
                        if (!assignmentStyle && day !in 1..first.daysInMonth) {
                            Spacer(Modifier.weight(1f).height(36.dp))
                        } else {
                            val inMonth = day in 1..first.daysInMonth
                            val date = when {
                                day < 1 -> first.plusMonths(-1).let { it.copy(day = it.daysInMonth + day) }
                                day > first.daysInMonth -> first.plusMonths(1).copy(day = day - first.daysInMonth)
                                else -> first.copy(day = day)
                            }
                            val active = date == selected
                            Column(Modifier.weight(1f).height(36.dp).clip(RoundedCornerShape(7.dp))
                                .background(if (active) { if (assignmentStyle) Color(0xFF1A201C) else Forest }
                                    else if (date.key in scheduled) SoftGreen else Color.White)
                                .then(if (date == start) Modifier.border(1.dp, Ink, RoundedCornerShape(7.dp)) else Modifier)
                                .clickable(role = Role.RadioButton) { onSelect(date) }.semantics { this.selected = active },
                                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text(date.day.toString(), fontSize = 16.sp, lineHeight = 20.sp,
                                    fontWeight = if (assignmentStyle) FontWeight.Bold else FontWeight.Normal,
                                    color = if (active) Lime else if (assignmentStyle && (!inMonth || scheduled.isNotEmpty() && date.key !in scheduled)) Muted else Ink)
                                if (date.key in scheduled) Box(Modifier.size(3.dp).background(if (active) Lime else Forest, CircleShape))
                            }
                        }
                    }
                }
            }
        }
    }
}

private val ExerciseImages: List<DrawableResource> get() = listOf(
    Res.drawable.exercise_lunge, Res.drawable.exercise_bridge, Res.drawable.exercise_deadlift,
    Res.drawable.exercise_plank, Res.drawable.exercise_cable,
)

@Composable
internal fun ExercisePhoto(index: Int, modifier: Modifier = Modifier) {
    Image(painterResource(ExerciseImages[index]), null, modifier.size(40.dp).clip(RoundedCornerShape(6.dp)),
        contentScale = ContentScale.Fit)
}

@Composable
internal fun WorkoutChip(text: String) {
    Text(text, Modifier.clip(RoundedCornerShape(10.dp)).background(SoftGreen).padding(horizontal = 6.dp, vertical = 4.dp),
        fontSize = 10.sp, lineHeight = 12.sp, color = Color(0xFF00100E))
}

@Composable
internal fun ExerciseRow(exercise: TrainingExercise, index: Int, onEdit: () -> Unit, onMoveUp: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 60.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.width(20.dp).heightIn(min = 48.dp).clickable(role = Role.Button, onClick = onMoveUp),
            contentAlignment = Alignment.Center) {
            WorkoutIcon(Res.drawable.exercise_drag, "Переместить ${ExerciseNames[exercise.catalogIndex]} вверх", size = 20.dp)
        }
        Text("${index + 1}", Modifier.size(20.dp).background(Color(0xFFEEEEEE), CircleShape),
            fontSize = 12.sp, lineHeight = 20.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = Ink)
        Row(Modifier.weight(1f).clickable(role = Role.Button, onClick = onEdit).padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExercisePhoto(exercise.catalogIndex)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(ExerciseNames[exercise.catalogIndex], fontSize = 14.sp, lineHeight = 20.sp,
                    color = Ink, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    WorkoutChip("${exercise.sets}×${exercise.reps}")
                    WorkoutChip("${exercise.weights}кг")
                    WorkoutChip("${exercise.restMinutes} мин")
                }
            }
            WorkoutIcon(Res.drawable.exercise_chevron, null)
        }
    }
}
