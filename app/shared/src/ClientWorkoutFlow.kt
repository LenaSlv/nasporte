package com.example.testmmp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testmmp.resources.*
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun ClientWorkoutFlow(
    onHome: () -> Unit, onPrograms: () -> Unit, library: List<LibraryProgram>, onSaveProgram: (WorkoutProgram, Int) -> Int,
    registerBackHandler: @Composable (Boolean, () -> Unit) -> Unit,
    onChats: () -> Unit, onClients: () -> Unit, openChats: Boolean = false, onShareLink: ((String) -> Unit)? = null,
) {
    var route by rememberSaveable { mutableStateOf(if (openChats) "chats" else "clients") }
    var lastOpenChats by rememberSaveable { mutableStateOf(openChats) }
    val routeState = rememberSaveableStateHolder()
    LaunchedEffect(openChats) {
        if (lastOpenChats != openChats) { route = if (openChats) "chats" else "clients"; lastOpenChats = openChats }
    }
    var invitation by rememberSaveable { mutableStateOf(false) }
    var requestUnread by rememberSaveable { mutableStateOf(true) }
    var requestVisible by rememberSaveable { mutableStateOf(true) }
    var marinaAccepted by rememberSaveable { mutableStateOf(false) }
    var acceptedSheet by rememberSaveable { mutableStateOf(false) }
    var marinaProgram by rememberSaveable(stateSaver = WorkoutProgramSaver) { mutableStateOf(WorkoutProgram()) }
    var marinaAssigned by rememberSaveable { mutableStateOf(false) }
    var marinaLibraryId by rememberSaveable { mutableIntStateOf(-1) }
    var client by rememberSaveable { mutableIntStateOf(0) }
    var draft by rememberSaveable(stateSaver = WorkoutProgramSaver) { mutableStateOf(WorkoutProgram()) }
    var firstProgram by rememberSaveable(stateSaver = WorkoutProgramSaver) { mutableStateOf(WorkoutProgram()) }
    var secondProgram by rememberSaveable(stateSaver = WorkoutProgramSaver) { mutableStateOf(WorkoutProgram()) }
    var firstAssigned by rememberSaveable { mutableStateOf(false) }
    var secondAssigned by rememberSaveable { mutableStateOf(false) }
    var firstLibraryId by rememberSaveable { mutableIntStateOf(-1) }
    var secondLibraryId by rememberSaveable { mutableIntStateOf(-1) }
    var editingAssignment by rememberSaveable { mutableStateOf(false) }
    var returnToSchedule by rememberSaveable { mutableStateOf(false) }
    var singleWorkout by rememberSaveable { mutableStateOf(false) }
    var discard by rememberSaveable { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var addExerciseDay by rememberSaveable { mutableIntStateOf(-1) }
    var editDay by rememberSaveable { mutableIntStateOf(-1) }
    var editExercise by rememberSaveable { mutableIntStateOf(-1) }
    val clientName = if (client == 2) NewClientName else "Андрей Синицин"
    val assigned = when (client) { 0 -> firstAssigned; 2 -> marinaAssigned; else -> secondAssigned }
    val assignedProgram = when (client) { 0 -> firstProgram; 2 -> marinaProgram; else -> secondProgram }
    fun assignMarina() { client = 2; acceptedSheet = false; route = "select" }
    fun acceptMarina() { marinaAccepted = true; requestUnread = false; acceptedSheet = true }
    fun back() {
        when (route) {
            "clients" -> onHome()
            "chats" -> onHome()
            "request" -> route = "chats"
            "client" -> route = "clients"
            "select" -> route = "client"
            "structure" -> if (returnToSchedule) { returnToSchedule = false; route = "schedule" } else discard = true
            "schedule" -> route = "structure"
        }
    }
    registerBackHandler(true) {
        when {
            invitation -> invitation = false
            acceptedSheet -> acceptedSheet = false
            route == "chats" && requestUnread && requestVisible -> requestUnread = false
            addExerciseDay >= 0 -> addExerciseDay = -1
            editExercise >= 0 -> editExercise = -1
            discard -> discard = false
            message != null -> message = null
            else -> back()
        }
    }
    routeState.SaveableStateProvider(route) {
    when (route) {
        "clients" -> ClientListScreen(
            firstAssigned, secondAssigned, firstProgram, secondProgram,
            onHome = onHome,
            onPrograms = onPrograms, programCount = library.size,
            onClient = { client = it; route = "client" },
            onAssign = { client = it; route = "select" },
            onMessage = { message = it },
            onAdd = { invitation = true }, onChats = { route = "chats"; onChats() },
            marinaAccepted = marinaAccepted, marinaAssigned = marinaAssigned, marinaProgram = marinaProgram,
        )
        "chats" -> ClientChatsScreen(library.size, requestVisible,
            onNavigate = { when (it) { 0 -> onHome(); 1 -> { route = "clients"; onClients() }; 2 -> onPrograms() } },
            onRequest = { requestUnread = false; route = "request" },
            onOtherChat = { message = "В этой переписке новых заявок нет." })
        "request" -> ClientRequestChatScreen(marinaAccepted, ::back, ::acceptMarina, ::assignMarina,
            onCancel = { requestVisible = false; requestUnread = false; route = "chats" },
            onBlock = { requestVisible = false; requestUnread = false; route = "chats" })
        "client" -> ClientScheduleScreen(
            assigned, assignedProgram, onBack = ::back,
            onAssign = { route = "select" },
            onSingleWorkout = { singleWorkout = true; draft = WorkoutProgram(title = "Новая тренировка", repeatWeekly = false); route = "structure" },
            onEdit = { singleWorkout = false; editingAssignment = true; draft = assignedProgram; route = "structure" },
            onMessage = { message = it },
            clientName = clientName,
        )
        "select" -> ProgramSelectionScreen(
            library = library,
            clientName = clientName,
            onBack = ::back,
            onQuestionnaire = { message = "Анкета клиента еще не заполнена. Отправка анкет будет доступна после подключения сервера." },
            onCreate = { singleWorkout = false; editingAssignment = false; draft = WorkoutProgram(); route = "structure" },
            onSelect = { singleWorkout = false; editingAssignment = false; draft = it; route = "structure" },
        )
        "structure" -> ProgramStructureScreen(
            draft, onChange = { draft = it }, onBack = ::back,
            onContinue = { returnToSchedule = false; route = "schedule" },
            onAddExercise = { addExerciseDay = it },
            onEditExercise = { day, exercise -> editDay = day; editExercise = exercise },
            onQuestionnaire = { message = if (client == 2) "Анкета клиента еще не заполнена."
                else "Ограничения клиента\n\nЧувствительность в колене. Избегать ударной нагрузки и глубоких приседаний." },
        )
        "schedule" -> ProgramScheduleScreen(
            draft, onChange = { draft = it }, onBack = ::back,
            onEditProgram = { returnToSchedule = true; route = "structure" },
            onEditExercise = { day, exercise -> editDay = day; editExercise = exercise },
            onAddExercise = { addExerciseDay = it },
            onSave = {
                // Assign only on final confirmation; drafts never change the client's current program.
                if (singleWorkout && assigned) {
                    val merged = assignedProgram.copy(additionalWorkouts = assignedProgram.additionalWorkouts + draft.schedule())
                    when (client) { 0 -> firstProgram = merged; 2 -> marinaProgram = merged; else -> secondProgram = merged }
                } else {
                    if (client == 0) { firstProgram = draft; firstAssigned = true }
                    else if (client == 2) { marinaProgram = draft; marinaAssigned = true }
                    else { secondProgram = draft; secondAssigned = true }
                    if (!singleWorkout) {
                        val id = onSaveProgram(draft, if (editingAssignment) {
                            when (client) { 0 -> firstLibraryId; 2 -> marinaLibraryId; else -> secondLibraryId }
                        } else -1)
                        when (client) { 0 -> firstLibraryId = id; 2 -> marinaLibraryId = id; else -> secondLibraryId = id }
                    }
                }
                route = "client"
                message = if (singleWorkout) "Тренировка назначена" else "Программа назначена клиенту"
            },
        )
    }
    }
    if (invitation) ClientInvitationSheet({ invitation = false }, onShareLink)
    if (route == "chats" && requestUnread && requestVisible && !marinaAccepted) NewClientRequestDialog(
        onDismiss = { requestUnread = false }, onOpen = { requestUnread = false; route = "request" })
    if (acceptedSheet) ClientAcceptedSheet(onDismiss = { acceptedSheet = false }, onAssign = ::assignMarina)
    if (discard) AlertDialog(onDismissRequest = { discard = false }, title = { Text("Выйти из редактора?") },
        text = { Text("Изменения не будут назначены клиенту. Продолжить редактирование можно, оставшись на этом экране.") },
        confirmButton = { TextButton(onClick = { discard = false; route = if (singleWorkout) "client" else "select" }) { Text("Выйти") } },
        dismissButton = { TextButton(onClick = { discard = false }) { Text("Остаться") } })
    if (message != null) AlertDialog(onDismissRequest = { message = null }, text = { Text(message.orEmpty()) },
        confirmButton = { TextButton(onClick = { message = null }) { Text("Понятно") } })
    if (addExerciseDay >= 0) ExerciseLibraryDialog(onDismiss = { addExerciseDay = -1 }) { catalogIndex ->
        val nextId = (draft.weeks.flatMap { it.days }.flatMap { it.exercises }.maxOfOrNull { it.id } ?: 0) + 1
        draft = draft.updateDay(addExerciseDay) { it.copy(exercises = it.exercises + TrainingExercise(nextId, catalogIndex)) }
        addExerciseDay = -1
    }
    val editing = draft.day(editDay)?.exercises?.find { it.id == editExercise }
    if (editing != null) ExerciseEditorDialog(editing, onDismiss = { editExercise = -1 },
        onSave = { updated ->
            draft = draft.updateDay(editDay) { day -> day.copy(exercises = day.exercises.map { if (it.id == updated.id) updated else it }) }
            editExercise = -1
        }, onDelete = {
            draft = draft.updateDay(editDay) { day -> day.copy(exercises = day.exercises.filterNot { it.id == editExercise }) }
            editExercise = -1
        })
}

@Composable
private fun ClientListScreen(
    firstAssigned: Boolean, secondAssigned: Boolean, first: WorkoutProgram, second: WorkoutProgram,
    onHome: () -> Unit, onPrograms: () -> Unit, programCount: Int,
    onClient: (Int) -> Unit, onAssign: (Int) -> Unit, onMessage: (String) -> Unit,
    onAdd: () -> Unit, onChats: () -> Unit,
    marinaAccepted: Boolean, marinaAssigned: Boolean, marinaProgram: WorkoutProgram,
) {
    var search by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableIntStateOf(0) }
    var reverse by rememberSaveable { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().background(Page)) {
        Column(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xFF020B07), Forest)))) {
            ClientTopBar("Мои клиенты", "${15 + if (marinaAccepted) 1 else 0} в работе") {
                IconButton(onClick = onAdd, modifier = Modifier.semantics { contentDescription = "Добавить клиента" }) {
                    Text("+", fontSize = 28.sp, color = Color.White)
                }
            }
            LazyColumn(Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)).background(Page),
                contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { ClientSearch(search, { search = it }) }
                item {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { reverse = !reverse }, modifier = Modifier.size(32.dp).background(Color(0xFFE0E0E0), CircleShape)) {
                            WorkoutIcon(Res.drawable.client_sort, "Изменить порядок клиентов", size = 14.dp)
                        }
                        listOf("Все (${if (marinaAccepted) 3 else 2})", "Требуют внимания (1)", "Новые", "Архив").forEachIndexed { index, label ->
                            Text(label, Modifier.clip(RoundedCornerShape(20.dp)).background(if (filter == index) Forest else Color(0xFFE0E0E0))
                                .clickable { filter = index }.padding(12.dp, 8.dp), color = if (filter == index) Color.White else Ink, fontSize = 12.sp)
                        }
                    }
                }
                val visible = (if (reverse) listOf(2, 1, 0) else listOf(0, 1, 2)).filter { index ->
                    (index != 2 || marinaAccepted) && (if (index == 2) NewClientName else "Андрей Синицин").contains(search, true) &&
                        when (filter) { 1 -> index == 1; 2 -> if (index == 2) !marinaAssigned else index == 0 && !firstAssigned; 3 -> false; else -> true }
                }
                if (visible.isNotEmpty()) {
                    items(visible.size) { position ->
                        val index = visible[position]
                        if (index == 2) {
                            WorkoutPanel(color = Color.White) {
                                Row(Modifier.fillMaxWidth().clickable { onClient(2) }, verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                                    ClientMarinaPhoto()
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        if (!marinaAssigned) Text("Новый", Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFFFF9500)).padding(8.dp, 2.dp),
                                            fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                        Text(NewClientName, fontSize = 16.sp, color = Ink, fontWeight = FontWeight.SemiBold)
                                        Text(if (marinaAssigned) marinaProgram.title else "Без программы", fontSize = 14.sp, color = Muted)
                                    }
                                    WorkoutIcon(Res.drawable.workout_disclosure, "Открыть клиента")
                                }
                                if (!marinaAssigned) WorkoutAction("Назначить программу", { onAssign(2) }, compact = true)
                            }
                        } else {
                        val assigned = if (index == 0) firstAssigned else secondAssigned
                        val program = if (index == 0) first else second
                        WorkoutPanel(color = Card) {
                            Row(Modifier.fillMaxWidth().clickable { onClient(index) }, verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Image(painterResource(Res.drawable.workout_client), null,
                                    Modifier.size(42.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("Андрей Синицин", color = Ink, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold)
                                    Text(if (assigned) program.title else "Программа не назначена", fontSize = 11.sp, color = Color(0xFF99A29C))
                                    if (!assigned) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        WorkoutIcon(if (index == 0) Res.drawable.workout_new_client else Res.drawable.workout_alert, null, size = 16.dp)
                                        Text(if (index == 0) "Новый клиент" else "В зоне риска · 3 пропуска подряд", fontSize = 12.sp,
                                            color = if (index == 0) Color(0xFFF2AA32) else Color(0xFFD75B63))
                                    }
                                }
                                WorkoutIcon(Res.drawable.workout_disclosure, "Расписание клиента", size = 18.dp)
                            }
                            HorizontalDivider(color = Color(0xFF9EA89C), thickness = .5.dp)
                            if (index == 0 && !assigned) {
                                WorkoutAction("Назначить программу", { onAssign(index) }, color = Purple, compact = true)
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                    listOf("Следующая тренировка" to (program.schedule().firstOrNull()?.let { "${it.date.display}, ${it.time}" } ?: "8 сент., 20:00"),
                                        "Запросы" to "2", "Отчёты" to "—").forEachIndexed { metric, pair ->
                                        Column(Modifier.weight(if (metric == 0) 2f else 1f).clip(RoundedCornerShape(9.dp))
                                            .background(Color(0xFFD7D9DB)).clickable { if (metric == 0) onClient(index) else onMessage("${pair.first}: ${pair.second}") }
                                            .padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(pair.first, fontSize = 11.sp, color = Ink)
                                            Text(pair.second, fontSize = 12.sp, color = Ink, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                        }
                    }
                } else item { Text("Клиенты не найдены", color = Muted) }
            }
        }
        HomeNavigation(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 10.dp), selectedTab = 1, programCount = programCount) {
            when (it) { 0 -> onHome(); 2 -> onPrograms(); 3 -> onChats() }
        }
    }
}

@Composable
private fun ClientScheduleScreen(
    assigned: Boolean, program: WorkoutProgram, onBack: () -> Unit, onAssign: () -> Unit,
    onSingleWorkout: () -> Unit, onEdit: () -> Unit, onMessage: (String) -> Unit,
    clientName: String = "Андрей Синицин",
) {
    var monthKey by rememberSaveable { mutableStateOf(program.start.key) }
    var selectedKey by rememberSaveable { mutableStateOf(program.start.key) }
    var showAll by rememberSaveable { mutableStateOf(false) }
    val workouts = if (assigned) program.schedule() else emptyList()
    WorkoutScaffold(clientName, "Клиент", onBack) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("Расписание", "Отчёты", "Запросы", "Инфо").forEachIndexed { index, text ->
                TextButton(onClick = { if (index > 0) onMessage(when (index) {
                    1 -> "Отчётов пока нет"; 2 -> "Новых запросов нет"; else -> "$clientName\nКлиент"
                }) }, contentPadding = PaddingValues(0.dp)) {
                    Text(text, color = if (index == 0) Forest else Muted, fontSize = 13.sp,
                        fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                WorkoutPanel(color = Card) {
                    Text(if (assigned) program.title else "Программа не назначена", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Text(if (assigned) "${program.weeks.size} нед. · ${program.exerciseCount} упражнений" else "Создайте новую программу или выберите готовую", fontSize = 12.sp, color = Ink)
                    WorkoutAction(if (assigned) "Редактировать программу" else "Назначить программу", if (assigned) onEdit else onAssign,
                        color = Purple, compact = true)
                }
            }
            item { WorkoutCalendar(WorkoutDate.parse(monthKey), WorkoutDate.parse(selectedKey), { monthKey = it.key },
                { selectedKey = it.key }, workouts.map { it.date.key }.toSet()) }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WorkoutHeading("Тренировки", Modifier.weight(1f))
                    TextButton(onClick = { showAll = !showAll }) { Text(if (showAll) "На день" else "Все ›", color = Ink) }
                }
            }
            val visible = if (showAll) workouts else workouts.filter { it.date.key == selectedKey }
            if (visible.isEmpty()) item {
                WorkoutPanel {
                    WorkoutHeading("На этот день тренировок нет")
                    Text("Вы можете назначить дополнительную тренировку вне основной программы.", color = Muted, fontSize = 14.sp)
                    WorkoutAction("Назначить тренировку", onSingleWorkout, outlined = true)
                }
            } else {
                items(visible.size) { index ->
                    val workout = visible[index]
                    WorkoutPanel {
                        Text("${workout.date.display} · ${workout.time}", color = Muted, fontSize = 13.sp)
                        Text(workout.day.title, fontWeight = FontWeight.Bold, color = Ink)
                        workout.day.exercises.forEach { exercise ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ExercisePhoto(exercise.catalogIndex)
                                Column { Text(ExerciseNames[exercise.catalogIndex], fontSize = 14.sp, color = Ink)
                                    Text("${exercise.sets}×${exercise.reps} · ${exercise.weights} кг · ${exercise.restMinutes} мин", color = Muted, fontSize = 11.sp) }
                            }
                        }
                    }
                }
            }
        }
    }
}
