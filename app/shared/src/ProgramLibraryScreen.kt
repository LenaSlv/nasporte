package com.example.testmmp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
internal fun ProgramLibraryFlow(
    library: List<LibraryProgram>, onSave: (WorkoutProgram, Int) -> Int, onDelete: (Int) -> Unit,
    onNavigate: (Int) -> Unit, registerBackHandler: @Composable (Boolean, () -> Unit) -> Unit,
) {
    var editing by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableIntStateOf(-1) }
    var draft by rememberSaveable(stateSaver = WorkoutProgramSaver) { mutableStateOf(WorkoutProgram()) }
    var original by rememberSaveable(stateSaver = WorkoutProgramSaver) { mutableStateOf(WorkoutProgram()) }
    var showTemplates by rememberSaveable { mutableStateOf(false) }
    var discard by rememberSaveable { mutableStateOf(false) }
    var deletingId by rememberSaveable { mutableIntStateOf(-1) }
    var addExerciseDay by rememberSaveable { mutableIntStateOf(-1) }
    var editDay by rememberSaveable { mutableIntStateOf(-1) }
    var editExercise by rememberSaveable { mutableIntStateOf(-1) }
    var notice by rememberSaveable { mutableStateOf<String?>(null) }
    var chatMessage by rememberSaveable { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(notice) {
        notice?.let { snackbar.showSnackbar(it); notice = null }
    }
    fun openEditor(entry: LibraryProgram?) {
        editingId = if (entry != null && !entry.isTemplate) entry.id else -1
        draft = when {
            entry == null -> WorkoutProgram()
            entry.isTemplate -> entry.program.copy(title = "${entry.program.title} — копия", weekdays = emptyList(),
                scheduleApplied = false, additionalWorkouts = emptyList())
            else -> entry.program
        }
        original = draft
        addExerciseDay = -1
        editExercise = -1
        editing = true
    }
    fun back() {
        if (editing) {
            if (draft == original) editing = false else discard = true
        } else onNavigate(0)
    }
    registerBackHandler(true) {
        when {
            addExerciseDay >= 0 -> addExerciseDay = -1
            editExercise >= 0 -> editExercise = -1
            discard -> discard = false
            deletingId >= 0 -> deletingId = -1
            chatMessage -> chatMessage = false
            else -> back()
        }
    }
    if (editing) {
        ProgramStructureScreen(draft, onChange = { draft = it }, onBack = ::back,
            onContinue = {
                onSave(draft, editingId)
                editing = false
                showTemplates = false
                notice = "Программа сохранена"
            },
            onAddExercise = { addExerciseDay = it },
            onEditExercise = { day, exercise -> editDay = day; editExercise = exercise },
            onQuestionnaire = {}, libraryMode = true)
    } else {
        ProgramLibraryScreen(library, showTemplates, { showTemplates = it },
            onCreate = { openEditor(null) }, onOpen = ::openEditor,
            onDuplicate = { entry ->
                onSave(entry.program.copy(title = "${entry.program.title} — копия", weekdays = emptyList(),
                    scheduleApplied = false, additionalWorkouts = emptyList()), -1)
                showTemplates = false
                notice = "Копия программы создана"
            },
            onDelete = { deletingId = it },
            onNavigate = { if (it == 3) chatMessage = true else onNavigate(it) }, snackbar = snackbar)
    }
    if (addExerciseDay >= 0) ExerciseLibraryDialog(onDismiss = { addExerciseDay = -1 }) { catalogIndex ->
        val id = (draft.weeks.flatMap { it.days }.flatMap { it.exercises }.maxOfOrNull { it.id } ?: 0) + 1
        draft = draft.updateDay(addExerciseDay) { it.copy(exercises = it.exercises + TrainingExercise(id, catalogIndex)) }
        addExerciseDay = -1
    }
    draft.day(editDay)?.exercises?.find { it.id == editExercise }?.let { exercise ->
        ExerciseEditorDialog(exercise, onDismiss = { editExercise = -1 }, onSave = { updated ->
            draft = draft.updateDay(editDay) { day -> day.copy(exercises = day.exercises.map { if (it.id == updated.id) updated else it }) }
            editExercise = -1
        }, onDelete = {
            draft = draft.updateDay(editDay) { day -> day.copy(exercises = day.exercises.filterNot { it.id == editExercise }) }
            editExercise = -1
        })
    }
    if (discard) AlertDialog(onDismissRequest = { discard = false }, title = { Text("Выйти без сохранения?") },
        text = { Text("Изменения в программе будут потеряны.") },
        confirmButton = { TextButton(onClick = { discard = false; editing = false }) { Text("Выйти") } },
        dismissButton = { TextButton(onClick = { discard = false }) { Text("Продолжить редактирование") } })
    library.find { it.id == deletingId && !it.isTemplate }?.let { entry ->
        AlertDialog(onDismissRequest = { deletingId = -1 }, title = { Text("Удалить программу?") },
            text = { Text("«${entry.program.title}» будет удалена из библиотеки. Назначенные клиентам копии сохранятся.") },
            confirmButton = { TextButton(onClick = {
                onDelete(entry.id); deletingId = -1; notice = "Программа удалена"
            }) { Text("Удалить", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { deletingId = -1 }) { Text("Отмена") } })
    }
    if (chatMessage) AlertDialog(onDismissRequest = { chatMessage = false }, text = { Text("Раздел чатов будет доступен позже.") },
        confirmButton = { TextButton(onClick = { chatMessage = false }) { Text("Понятно") } })
}

@Composable
private fun ProgramLibraryScreen(
    library: List<LibraryProgram>, showTemplates: Boolean, onTemplates: (Boolean) -> Unit,
    onCreate: () -> Unit, onOpen: (LibraryProgram) -> Unit, onDuplicate: (LibraryProgram) -> Unit,
    onDelete: (Int) -> Unit, onNavigate: (Int) -> Unit, snackbar: SnackbarHostState,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val visible = library.filter { it.isTemplate == showTemplates && it.program.title.contains(query.trim(), ignoreCase = true) }
    Box(Modifier.fillMaxSize().background(Page)) {
        Column(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xFF020B07), Forest)))) {
            Column(Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Программы", color = Color.White, fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold)
                Text("${library.count { !it.isTemplate }} в библиотеке · ${library.count { it.isTemplate }} шаблона",
                    color = Color(0xFF99A29C), fontSize = 12.sp, lineHeight = 16.sp)
            }
            Column(Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)).background(Page)) {
                Row(Modifier.fillMaxWidth().padding(16.dp).clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFD7D6DE)).padding(2.dp)) {
                    listOf("Мои программы", "Шаблоны").forEachIndexed { index, label ->
                        val active = showTemplates == (index == 1)
                        Box(Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(if (active) Color.White else Color.Transparent)
                            .clickable(role = Role.Tab) { onTemplates(index == 1) }.semantics { selected = active }
                            .padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                            Text(label, color = Ink, fontSize = 13.sp, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
                        }
                    }
                }
                OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    placeholder = { Text("Поиск программы") }, singleLine = true, shape = RoundedCornerShape(12.dp),
                    leadingIcon = { WorkoutIcon(Res.drawable.workout_search, null) },
                    trailingIcon = { if (query.isNotEmpty()) WorkoutIconButton(Res.drawable.workout_close, "Очистить поиск", { query = "" }) },
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White, unfocusedBorderColor = Color.Transparent))
                LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (visible.isEmpty()) item {
                        WorkoutPanel {
                            WorkoutHeading(if (query.isBlank()) "Пока нет программ" else "Ничего не найдено")
                            Text(if (query.isBlank()) "Создайте программу или сохраните копию шаблона."
                                else "Попробуйте другое название программы.", color = Muted, fontSize = 14.sp, lineHeight = 20.sp)
                        }
                    }
                    items(visible, key = { it.id }) { entry ->
                        ProgramLibraryCard(entry, { onOpen(entry) }, { onDuplicate(entry) }, { onDelete(entry.id) })
                    }
                }
                WorkoutAction("+ Создать программу", onCreate, Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp))
                Spacer(Modifier.navigationBarsPadding().height(82.dp))
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 150.dp))
        HomeNavigation(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 10.dp),
            selectedTab = 2, programCount = library.size, onNavigate = onNavigate)
    }
}

@Composable
private fun ProgramLibraryCard(entry: LibraryProgram, onOpen: () -> Unit, onDuplicate: () -> Unit, onDelete: () -> Unit) {
    var menu by remember { mutableStateOf(false) }
    WorkoutPanel {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f).clickable(role = Role.Button, onClick = onOpen), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(entry.program.title, color = Color(0xFF172C22), fontSize = 16.sp, lineHeight = 21.sp, fontWeight = FontWeight.SemiBold)
                Text("${entry.program.weeks.size} нед. · ${entry.program.weeks.sumOf { it.days.size }} тренировочных дней",
                    color = Color(0xFF8A958F), fontSize = 12.sp, lineHeight = 17.sp)
            }
            Box {
                TextButton(onClick = { menu = true }, Modifier.size(40.dp).semantics { contentDescription = "Действия с программой ${entry.program.title}" },
                    contentPadding = PaddingValues(0.dp)) { Text("⋮", fontSize = 24.sp, color = Ink) }
                DropdownMenu(menu, { menu = false }) {
                    DropdownMenuItem(text = { Text(if (entry.isTemplate) "Создать по шаблону" else "Редактировать") },
                        onClick = { menu = false; onOpen() })
                    DropdownMenuItem(text = { Text("Создать копию") }, onClick = { menu = false; onDuplicate() })
                    if (!entry.isTemplate) DropdownMenuItem(text = { Text("Удалить", color = MaterialTheme.colorScheme.error) },
                        onClick = { menu = false; onDelete() })
                }
            }
        }
        Row(Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onOpen), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WorkoutChip("${entry.program.exerciseCount} упражнений")
            Text(if (entry.isTemplate) "Создать по шаблону" else "Открыть программу", Modifier.weight(1f),
                color = Forest, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            WorkoutIcon(Res.drawable.workout_disclosure, null, size = 18.dp)
        }
    }
}
