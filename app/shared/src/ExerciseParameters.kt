package com.example.testmmp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun FitSegments(labels: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier,
    compact: Boolean = false, background: Color = Color(0xFFF0F0F0)) {
    val family = fitInter()
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    BoxWithConstraints(modifier.fillMaxWidth().height(if (compact) 30.dp else 38.dp)
        .clip(RoundedCornerShape(12.dp)).background(background).padding(2.dp)) {
        // Keep equal segments and one baseline, including long labels on narrow screens.
        val available = with(density) { (maxWidth / labels.size - if (compact) 8.dp else 16.dp).toPx() }
        val sizes = if (compact) listOf(12f, 11.5f, 11f) else listOf(14f, 13.5f, 13f, 12.5f, 12f, 11.5f)
        val size = sizes.firstOrNull { candidate -> labels.all { label ->
            measurer.measure(label, TextStyle(fontFamily = family, fontSize = candidate.sp,
                fontWeight = FontWeight.Normal, letterSpacing = if (compact) 0.sp else .2.sp),
                softWrap = false, maxLines = 1).size.width <= available
        } } ?: sizes.last()
        Row(Modifier.fillMaxSize().selectableGroup(), verticalAlignment = Alignment.CenterVertically) {
        labels.forEachIndexed { index, label ->
            val shape = RoundedCornerShape(11.dp)
            Box(Modifier.weight(1f).fillMaxHeight()
                .then(if (index == selected) Modifier.shadow(2.dp, shape, clip = false,
                    ambientColor = Color(0x14001A34), spotColor = Color(0x14001A34)) else Modifier)
                .clip(shape)
                .background(if (index == selected) Color.White else Color.Transparent)
                .selectable(selected = index == selected, role = Role.Tab) { onSelect(index) }
                .padding(horizontal = if (compact) 4.dp else 8.dp), contentAlignment = Alignment.Center) {
                Text(label, fontFamily = family, fontWeight = FontWeight.Normal, fontSize = size.sp, lineHeight = 18.sp,
                    letterSpacing = if (compact) 0.sp else .2.sp, color = Color(0xFF212121),
                    textAlign = TextAlign.Center, softWrap = false, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        }
    }
}

internal fun validExercise(e: TrainingExercise): Boolean = e.weights.split(',').withIndex().all { (index, value) ->
    (if (e.perSetWeights) index else 0) in e.maxWeightSets || value.trim().toDoubleOrNull()?.let { weight -> weight in 0.0..1000.0 } == true
} && e.sets in 1..20 && e.reps in 1..200 && e.seconds in 0..600 && e.restMinutes in 0..30 &&
    (e.seconds == 0 || e.sameDuration || e.setDurations.all { it in 1..600 })

@Composable
internal fun ExerciseParameters(exercise: TrainingExercise, onChange: (TrainingExercise) -> Unit) {
    val timed = exercise.seconds > 0
    fun weights() = List(exercise.sets) { index ->
        val items = exercise.weights.split(',')
        items.getOrNull(index)?.trim() ?: items.lastOrNull()?.trim() ?: "0"
    }
    fun updateWeight(index: Int, value: String) {
        val values = weights().mapIndexed { i, old -> if (!exercise.perSetWeights || i == index) value else old }
        onChange(exercise.copy(weights = values.joinToString(",")))
    }
    WorkoutPanel {
        Text("Подходы и повторы", fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        FitSegments(listOf("Повторения", "Время"), if (timed) 1 else 0, {
            onChange(exercise.copy(seconds = if (it == 1) 30 else 0, setDurations = emptyList()))
        })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FitStepper(exercise.sets, "подхода", {
                val count = it.coerceIn(1, 20)
                val values = weights()
                onChange(exercise.copy(sets = count, weights = List(count) { i -> values.getOrElse(i) { values.last() } }.joinToString(","),
                    setDurations = List(count) { i -> exercise.setDurations.getOrNull(i) ?: exercise.seconds.coerceAtLeast(1) },
                    maxWeightSets = exercise.maxWeightSets.filter { index -> index < count }))
            }, Modifier.weight(1f))
            FitStepper(if (timed) exercise.seconds else exercise.reps, if (timed) "сек" else "повт.", {
                onChange(if (timed) exercise.copy(seconds = it.coerceIn(1, 600)) else exercise.copy(reps = it.coerceIn(1, 200)))
            }, Modifier.weight(1f))
        }
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFF3EDFF)).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (timed) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Одинаковое время", fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium, color = Ink)
                        Text("Все подходы выполняются с одним и тем же временем", fontSize = 12.sp, lineHeight = 18.sp, color = Muted)
                    }
                    FitSwitch(exercise.sameDuration, { same ->
                        onChange(exercise.copy(sameDuration = same,
                            setDurations = List(exercise.sets) { i -> exercise.setDurations.getOrNull(i) ?: exercise.seconds }))
                    })
                }
                if (!exercise.sameDuration) repeat(exercise.sets) { index ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${index + 1} подход", Modifier.weight(1f), fontSize = 13.sp, lineHeight = 18.sp, color = Ink)
                        FitStepper(exercise.setDurations.getOrNull(index) ?: exercise.seconds, "сек", { value ->
                            onChange(exercise.copy(setDurations = List(exercise.sets) { i ->
                                if (i == index) value.coerceIn(1, 600) else exercise.setDurations.getOrNull(i) ?: exercise.seconds
                            }))
                        }, Modifier.width(140.dp))
                    }
                }
                HorizontalDivider(color = Color(0xFFD7D6DE), thickness = .5.dp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Отдых между подходами", Modifier.weight(1f), fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium, color = Ink)
                FitSwitch(exercise.restEnabled, { onChange(exercise.copy(restEnabled = it)) })
            }
            if (exercise.restEnabled) FitStepper(exercise.restMinutes, "мин", { onChange(exercise.copy(restMinutes = it.coerceIn(0, 30))) })
        }
    }
    WorkoutPanel {
        Text("Рабочий вес", fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        FitSegments(listOf("Один вес", "По подходам"), if (exercise.perSetWeights) 1 else 0, { index ->
            onChange(exercise.copy(perSetWeights = index == 1,
                weights = if (index == 0) List(exercise.sets) { weights().first() }.joinToString(",") else weights().joinToString(","),
                maxWeightSets = if (index == 0 && exercise.maxWeightSets.isNotEmpty()) listOf(0) else exercise.maxWeightSets))
        })
        val count = if (exercise.perSetWeights) exercise.sets else 1
        repeat(count) { index ->
            val max = index in exercise.maxWeightSets
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFF3EDFF)).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(if (exercise.perSetWeights) "${index + 1} подход" else "Все подходы", Modifier.weight(1f),
                        fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium, color = Ink)
                    FitSegments(listOf("Кг", "Max"), if (max) 1 else 0, { choice ->
                        onChange(exercise.copy(maxWeightSets = if (choice == 1) (exercise.maxWeightSets + index).distinct() else exercise.maxWeightSets - index))
                    }, Modifier.width(96.dp), compact = true)
                }
                val value = weights()[index]
                Row(Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFF9F9F9))
                    .border(.5.dp, Color(0xFFDDDDDD), RoundedCornerShape(10.dp)), verticalAlignment = Alignment.CenterVertically) {
                    FitIconButton({ updateWeight(index, ((value.toDoubleOrNull() ?: 0.0) - 1).coerceAtLeast(0.0).weightText()) },
                        Modifier.size(40.dp), enabled = !max) { Text("−", fontSize = 20.sp, color = Muted) }
                    if (max) Text("Max", Modifier.weight(1f), fontSize = 18.sp, lineHeight = 22.sp,
                        fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, color = Ink)
                    else BasicTextField(value, { raw -> updateWeight(index, raw.filter { it.isDigit() || it == '.' }.take(7)) },
                        Modifier.weight(1f), singleLine = true, cursorBrush = SolidColor(Purple),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = TextStyle(fontFamily = fitInter(), fontSize = 18.sp, lineHeight = 22.sp,
                            fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, color = Ink))
                    if (!max) Text("кг", fontSize = 12.sp, lineHeight = 18.sp, color = Muted)
                    FitIconButton({ updateWeight(index, ((value.toDoubleOrNull() ?: 0.0) + 1).coerceAtMost(1000.0).weightText()) },
                        Modifier.size(40.dp), enabled = !max) { Text("+", fontSize = 20.sp, color = Muted) }
                }
            }
        }
        if (exercise.maxWeightSets.isNotEmpty()) Text("MAX — клиент самостоятельно подбирает вес и выполняет подход до отказа.",
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFF5FFD6)).padding(12.dp),
            fontSize = 12.sp, lineHeight = 16.sp, color = Ink)
    }
}

private fun Double.weightText() = if (this == toInt().toDouble()) toInt().toString() else toString()

internal fun TrainingExercise.repetitionLabel(): String = if (seconds > 0) {
    if (sameDuration) "${sets}×${seconds} сек" else "${sets}×время"
} else "${sets}×${reps}"

internal fun TrainingExercise.weightLabel(): String = when {
    maxWeightSets.isNotEmpty() && !perSetWeights -> "Max"
    maxWeightSets.isNotEmpty() -> weights.split(',').mapIndexed { index, value -> if (index in maxWeightSets) "Max" else value }.joinToString(",")
    !perSetWeights -> "${weights.substringBefore(',')}кг"
    else -> "${weights}кг"
}
