package com.example.testmmp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testmmp.resources.*

// A local selection screen; exercises are added only after confirmation.
@Composable
internal fun AssignmentExerciseScreen(dayTitle: String, onBack: () -> Unit, onAdd: (List<TrainingExercise>) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var group by rememberSaveable { mutableStateOf("Все группы") }
    var selected by rememberSaveable { mutableStateOf(listOf<Int>()) }
    var expanded by rememberSaveable { mutableIntStateOf(-1) }
    var parameters by rememberSaveable { mutableStateOf(listOf<String>()) }
    var filterMenu by remember { mutableStateOf(false) }
    var techniqueNotice by rememberSaveable { mutableStateOf(false) }
    fun exercise(index: Int): TrainingExercise {
        return parameters.map(::decodeExercise).find { it.catalogIndex == index } ?: TrainingExercise(0, index)
    }
    fun update(e: TrainingExercise) {
        parameters = parameters.filterNot { decodeExercise(it).catalogIndex == e.catalogIndex } + encodeExercise(e)
    }
    val valid = selected.all { validExercise(exercise(it)) }
    val selectionUnit = when {
        selected.size % 100 in 11..14 -> "упражнений"
        selected.size % 10 == 1 -> "упражнение"
        selected.size % 10 in 2..4 -> "упражнения"
        else -> "упражнений"
    }
    val visible = ExerciseNames.indices.filter { index ->
        ExerciseNames[index].contains(query, true) && when (group) {
            "Грудь" -> index == 5
            "Спина" -> index in listOf(2, 7)
            "Ноги" -> index in listOf(0, 1, 4, 6)
            else -> true
        }
    }
    WorkoutScaffold("Добавить упражнение", dayTitle, onBack, compactHeader = true) {
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) { ClientSearch(query, { query = it }, "Поиск упражнений") }
                    Box {
                        FitIconButton({ filterMenu = true }, Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color.White)) {
                            WorkoutIcon(Res.drawable.workout_filter, "Фильтр упражнений")
                        }
                        FitDropdownMenu(filterMenu, { filterMenu = false }) {
                            listOf("Все группы", "Грудь", "Спина", "Ноги").forEach { name ->
                                FitDropdownMenuItem({ Text(name) }, { group = name; filterMenu = false })
                            }
                        }
                    }
                }
            }
            item {
                Row(Modifier.padding(top = 12.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Все группы", "Грудь", "Спина", "Ноги").forEach { label ->
                        Text(label, Modifier.clip(RoundedCornerShape(10.dp)).background(if (group == label) Color.Black else Color(0xFFE0E0E0))
                            .clickable(role = Role.Tab) { group = label }.padding(12.dp, 8.dp),
                            color = if (group == label) Color.White else Ink, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            }
            item { WorkoutHeading("${group} (${visible.size})", Modifier.padding(vertical = 12.dp)) }
            if (visible.isEmpty()) item { Text("Упражнения не найдены", color = Muted) }
            items(visible.size, key = { visible[it] }) { position ->
                val index = visible[position]
                val active = index in selected
                val current = exercise(index)
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(
                    topStart = if (position == 0) 12.dp else 0.dp, topEnd = if (position == 0) 12.dp else 0.dp,
                    bottomStart = if (position == visible.lastIndex) 12.dp else 0.dp, bottomEnd = if (position == visible.lastIndex) 12.dp else 0.dp))
                    .background(if (expanded == index) Color(0xFFF3EDFF) else Color.White)) {
                Row(Modifier.fillMaxWidth()
                    .clickable(role = Role.Checkbox) { selected = if (active) selected - index else selected + index }.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FitCheckbox(active, { selected = if (it) selected + index else selected - index }, modifier = Modifier.size(24.dp))
                    ExercisePhoto(index)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(ExerciseNames[index], fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            WorkoutChip(current.repetitionLabel(), light = true)
                            WorkoutChip(current.weightLabel(), light = true)
                        }
                    }
                    FitIconButton({
                        expanded = if (expanded == index) -1 else index
                        if (expanded == index && !active) selected += index
                    }, Modifier.size(24.dp)) {
                        WorkoutIcon(if (expanded == index) Res.drawable.workout_up else Res.drawable.workout_disclosure,
                            "Параметры: ${ExerciseNames[index]}", size = if (expanded == index) 24.dp else 18.dp)
                    }
                }
                if (expanded == index) Column(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White)
                        .clickable(role = Role.Button) { techniqueNotice = true }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Техника упражнения", fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium, color = Ink)
                            Text("Доп. материалы не загружены", fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium, color = Purple)
                        }
                        WorkoutIcon(Res.drawable.workout_disclosure, null, size = 18.dp)
                    }
                    ExerciseParameters(current, ::update)
                }
                if (position < visible.lastIndex) HorizontalDivider(thickness = 1.dp, color = Page)
                }
            }
        }
        FitActionFooter {
            Text("Выбрано: ${selected.size} $selectionUnit", color = Purple, fontSize = 14.sp, lineHeight = 30.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WorkoutAction("В суперсет", { onAdd(selected.map { exercise(it).copy(supersetId = -1) }) },
                    Modifier.weight(1f), color = Color.Black, enabled = selected.size > 1 && valid,
                    fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold)
                WorkoutAction("По отдельности", { onAdd(selected.map(::exercise)) }, Modifier.weight(1f), enabled = selected.isNotEmpty() && valid,
                    fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
    if (techniqueNotice) FitDialog({ techniqueNotice = false }, title = { Text("Техника упражнения") },
        text = { Text("Дополнительные материалы пока не загружены.") },
        confirmButton = { WorkoutAction("Понятно", { techniqueNotice = false }) })
}

@Composable
internal fun FitStepper(value: Int, unit: String, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFF7F7F8)),
        verticalAlignment = Alignment.CenterVertically) {
        FitIconButton({ onChange(value - 1) }, Modifier.size(36.dp)) { Text("−", color = Muted, fontSize = 20.sp) }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value.toString(), color = Ink, fontSize = 18.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold)
            Text(unit, color = Muted, fontSize = 12.sp, lineHeight = 18.sp, maxLines = 1)
        }
        FitIconButton({ onChange(value + 1) }, Modifier.size(36.dp)) { Text("+", color = Muted, fontSize = 20.sp) }
    }
}
