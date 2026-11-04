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
    var requestPrompt by rememberSaveable { mutableStateOf(false) }
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
    var editorSession by rememberSaveable { mutableIntStateOf(0) }
    var assignmentNotice by rememberSaveable { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(assignmentNotice) {
        assignmentNotice?.let { snackbar.showSnackbar(it); assignmentNotice = null }
    }
    val clientName = when (client) { 2 -> NewClientName; 1 -> "Константин Салтыков-Щедрин"; else -> "Андрей Синицин" }
    val assigned = when (client) { 0 -> firstAssigned; 2 -> marinaAssigned; else -> secondAssigned }
    val assignedProgram = when (client) { 0 -> firstProgram; 2 -> marinaProgram; else -> secondProgram }
    fun assignMarina() { client = 2; acceptedSheet = false; route = "select" }
    fun acceptMarina() { marinaAccepted = true; requestUnread = false; acceptedSheet = true }
    fun closeInvitation() {
        invitation = false
        requestPrompt = requestUnread && requestVisible && !marinaAccepted
    }
    fun dismissRequestPrompt() {
        requestPrompt = false
        requestUnread = false
    }
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
            invitation -> closeInvitation()
            requestPrompt -> dismissRequestPrompt()
            acceptedSheet -> acceptedSheet = false
            addExerciseDay >= 0 -> addExerciseDay = -1
            editExercise >= 0 -> editExercise = -1
            discard -> discard = false
            message != null -> message = null
            else -> back()
        }
    }
    Box(Modifier.fillMaxSize()) {
    routeState.SaveableStateProvider("$route:$client:$editorSession") {
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
            onQuestionnaire = { if (client == 2) route = "request" else message = "Анкета клиента еще не заполнена." },
            onCreate = { singleWorkout = false; editingAssignment = false; editorSession++;
                draft = WorkoutProgram(weeks = List(3) { index -> TrainingWeek(index + 1,
                    if (index == 0) listOf(TrainingDay(1, "День 1")) else emptyList()) }); route = "structure" },
            onSelect = { singleWorkout = false; editingAssignment = false; editorSession++; draft = it; route = "structure" },
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
                assignmentNotice = if (singleWorkout) "Тренировка назначена" else "Программа назначена клиенту"
            },
        )
    }
    }
    SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(16.dp))
    if (addExerciseDay >= 0) AssignmentExerciseScreen(
        dayTitle = draft.day(addExerciseDay)?.title.orEmpty(), onBack = { addExerciseDay = -1 },
        onAdd = { selected ->
            val nextId = (draft.weeks.flatMap { it.days }.flatMap { it.exercises }.maxOfOrNull { it.id } ?: 0) + 1
            val nextGroup = (draft.weeks.flatMap { it.days }.flatMap { it.exercises }.maxOfOrNull { it.supersetId } ?: 0) + 1
            draft = draft.updateDay(addExerciseDay) { day -> day.copy(exercises = day.exercises +
                selected.mapIndexed { index, exercise -> exercise.copy(id = nextId + index,
                    supersetId = if (exercise.supersetId < 0) nextGroup else exercise.supersetId) }) }
            addExerciseDay = -1
        })
    }
    if (invitation) ClientInvitationSheet(::closeInvitation, onShareLink)
    if (requestPrompt && !invitation && requestUnread && requestVisible && !marinaAccepted) NewClientRequestDialog(
        onDismiss = ::dismissRequestPrompt, onOpen = { dismissRequestPrompt(); route = "request" })
    if (acceptedSheet) ClientAcceptedSheet(onDismiss = { acceptedSheet = false; route = "clients"; onClients() }, onAssign = ::assignMarina)
    if (discard) FitDialog(onDismissRequest = { discard = false }, title = { Text("Выйти из редактора?") },
        text = { Text("Изменения не будут назначены клиенту. Продолжить редактирование можно, оставшись на этом экране.") },
        confirmButton = { FitTextButton(onClick = { discard = false; route = if (singleWorkout) "client" else "select" }) { Text("Выйти") } },
        dismissButton = { FitTextButton(onClick = { discard = false }) { Text("Остаться") } })
    if (message != null) FitDialog(onDismissRequest = { message = null }, text = { Text(message.orEmpty()) },
        confirmButton = { FitTextButton(onClick = { message = null }) { Text("Понятно") } })
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
