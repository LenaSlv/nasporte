package com.example.testmmp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testmmp.resources.*
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProgramSelectionScreen(
    library: List<LibraryProgram>,
    onBack: () -> Unit, onQuestionnaire: () -> Unit,
    onCreate: () -> Unit, onSelect: (WorkoutProgram) -> Unit,
    clientName: String = "Андрей Синицин",
) {
    var fromClients by rememberSaveable { mutableStateOf(true) }
    var query by rememberSaveable { mutableStateOf("") }
    var selectedId by rememberSaveable { mutableIntStateOf(-1) }
    var showWarning by rememberSaveable { mutableStateOf(true) }
    var showFilters by rememberSaveable { mutableStateOf(false) }
    var minWeeks by rememberSaveable { mutableStateOf("") }
    var maxWeeks by rememberSaveable { mutableStateOf("") }
    var family by rememberSaveable { mutableStateOf("Все шаблоны") }
    val visible = library.filter {
        val program = it.program
        val days = program.weeks.firstOrNull()?.days?.size ?: 0
        !it.isTemplate == fromClients && program.title.contains(query, ignoreCase = true) &&
            program.weeks.size >= (minWeeks.toIntOrNull() ?: 0) && program.weeks.size <= (maxWeeks.toIntOrNull() ?: Int.MAX_VALUE) &&
            (family == "Все шаблоны" || if (family == "Силовая база") days >= 3 else days <= 3)
    }
    val chosen = visible.find { it.id == selectedId }
    WorkoutScaffold("Назначить программу", "Клиент: $clientName", onBack) {
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (showWarning) item {
                WorkoutPanel(color = Color(0xFFF1D3D8)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Клиент еще не заполнил анкету", fontSize = 14.sp, color = Color(0xFFFF3547), fontWeight = FontWeight.Medium)
                            Text("Отправьте анкету клиенту, чтобы получить данные для программы", fontSize = 14.sp, color = Ink)
                        }
                        WorkoutIconButton(Res.drawable.workout_close, "Скрыть уведомление", { showWarning = false })
                    }
                    WorkoutAction("Отправить анкету", onQuestionnaire, outlined = true, compact = true)
                }
            }
            item { WorkoutHeading("Создать на основе") }
            item {
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFD7D6DE)).padding(2.dp)) {
                    listOf("Шаблоны", "Мои программы").forEachIndexed { index, text ->
                        val active = fromClients == (index == 1)
                        Box(Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (active) Color.White else Color.Transparent)
                            .clickable(role = Role.Tab) { fromClients = index == 1; selectedId = -1 }.semantics { this.selected = active }
                            .padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                            Text(text, fontSize = 13.sp, color = Ink)
                        }
                    }
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(query, { query = it }, Modifier.weight(1f), placeholder = { Text("Поиск") }, singleLine = true,
                        leadingIcon = { WorkoutIcon(Res.drawable.workout_search, null) }, shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White, unfocusedBorderColor = Color.Transparent))
                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Color.White), contentAlignment = Alignment.Center) {
                        WorkoutIconButton(Res.drawable.workout_filter, "Фильтры программ", { showFilters = true })
                    }
                }
                if (minWeeks.isNotEmpty() || maxWeeks.isNotEmpty() || family != "Все шаблоны") {
                    Text("${family} · ${minWeeks.ifEmpty { "0" }}–${maxWeeks.ifEmpty { "∞" }} нед.",
                        Modifier.padding(top = 8.dp), color = Muted, fontSize = 12.sp)
                }
            }
            if (visible.isEmpty()) item { Text("Программы не найдены. Измените поиск или фильтры.", color = Muted, fontSize = 14.sp) }
            items(visible.size) { index ->
                val entry = visible[index]
                val template = entry.program
                val active = entry.id == selectedId
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White)
                    .then(if (active) Modifier.border(BorderStroke(1.5.dp, Ink), RoundedCornerShape(16.dp)) else Modifier)
                    .clickable(role = Role.RadioButton) { selectedId = entry.id }.semantics { this.selected = active }.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(template.title, Modifier.weight(1f), color = Color(0xFF172C22), fontSize = 16.sp,
                            lineHeight = 20.sp, fontWeight = FontWeight.SemiBold)
                        WorkoutIcon(Res.drawable.workout_disclosure, null, size = 18.dp)
                    }
                    Text("${template.weeks.size} нед.  |  ${template.weeks.firstOrNull()?.days?.size ?: 0} дня/нед", fontSize = 14.sp, color = Color(0xFF8A958F))
                    if (fromClients) Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                        Image(painterResource(Res.drawable.program_avatar), null, Modifier.size(24.dp).clip(CircleShape))
                        if (index > 0) WorkoutIcon(Res.drawable.workout_group, null, size = 24.dp)
                    }
                }
            }
        }
        Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            WorkoutAction("Создать новую программу", onCreate, outlined = true)
            WorkoutAction(if (fromClients) "Создать копию выбранной" else "Продолжить с выбранной",
                { chosen?.let { onSelect(it.program.copy(weekdays = emptyList(), scheduleApplied = false, additionalWorkouts = emptyList())) } }, enabled = chosen != null)
        }
    }
    if (showFilters) {
        var pendingMin by rememberSaveable { mutableStateOf(minWeeks) }
        var pendingMax by rememberSaveable { mutableStateOf(maxWeeks) }
        var pendingFamily by rememberSaveable { mutableStateOf(family) }
        val valid = (pendingMin.isEmpty() || pendingMin.toIntOrNull() in 1..52) &&
            (pendingMax.isEmpty() || pendingMax.toIntOrNull() in 1..52) &&
            (pendingMin.toIntOrNull() ?: 0) <= (pendingMax.toIntOrNull() ?: 52)
        ModalBottomSheet(onDismissRequest = { showFilters = false }, containerColor = Card) {
            Column(Modifier.fillMaxWidth().imePadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Фильтры", Modifier.weight(1f), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    WorkoutIconButton(Res.drawable.workout_close, "Закрыть фильтры", { showFilters = false })
                }
                WorkoutHeading("Шаблон")
                FilterFamilyDropdown(pendingFamily) { pendingFamily = it }
                Text("Покажем только программы, созданные на основе этого шаблона", fontSize = 14.sp)
                WorkoutHeading("Длительность программы, недель")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(pendingMin, { pendingMin = it.filter(Char::isDigit).take(2) }, Modifier.weight(1f),
                        label = { Text("От") }, singleLine = true, isError = !valid)
                    OutlinedTextField(pendingMax, { pendingMax = it.filter(Char::isDigit).take(2) }, Modifier.weight(1f),
                        label = { Text("До") }, singleLine = true, isError = !valid)
                }
                if (!valid) Text("Укажите диапазон от 1 до 52 недель", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                WorkoutAction("Сбросить", { pendingMin = ""; pendingMax = ""; pendingFamily = "Все шаблоны" }, outlined = true)
                WorkoutAction("Применить фильтры", {
                    minWeeks = pendingMin; maxWeeks = pendingMax; family = pendingFamily; showFilters = false
                }, enabled = valid)
            }
        }
    }
}

@Composable
private fun FilterFamilyDropdown(value: String, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        WorkoutAction("$value  ▾", { expanded = true }, outlined = true)
        DropdownMenu(expanded, { expanded = false }) {
            listOf("Все шаблоны", "Силовая база", "Фулбоди для начинающих").forEach {
                DropdownMenuItem(text = { Text(it) }, onClick = { onChange(it); expanded = false })
            }
        }
    }
}
