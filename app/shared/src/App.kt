package com.example.testmmp

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.graphics.Color

@Composable
fun App(registerBackHandler: @Composable (Boolean, () -> Unit) -> Unit = { _, _ -> }, onShareLink: ((String) -> Unit)? = null) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val screenState = rememberSaveableStateHolder()
    var library by rememberSaveable(stateSaver = WorkoutLibrarySaver) { mutableStateOf(initialProgramLibrary()) }
    fun saveProgram(program: WorkoutProgram, existingId: Int): Int {
        val existing = library.find { it.id == existingId && !it.isTemplate }
        val id = existing?.id ?: ((library.maxOfOrNull { it.id } ?: 0) + 1)
        library = if (existing == null) library + LibraryProgram(id, program)
        else library.map { if (it.id == id) it.copy(program = program) else it }
        return id
    }
    MaterialTheme(
        typography = fitTypography(),
        colorScheme = lightColorScheme(
            primary = Color(0xFF0E2E16),
            secondary = Color(0xFFB6F500),
            background = Color(0xFFEDEDEE),
            surface = Color(0xFFF7F7F8),
            onSurface = Color(0xFF060607),
        ),
    ) {
        screenState.SaveableStateProvider(if (selectedTab == 3) 1 else selectedTab) {
            when (selectedTab) {
                1, 3 -> ClientWorkoutFlow(onHome = { selectedTab = 0 }, onPrograms = { selectedTab = 2 },
                    onChats = { selectedTab = 3 }, onClients = { selectedTab = 1 }, openChats = selectedTab == 3,
                    onShareLink = onShareLink, library = library, onSaveProgram = ::saveProgram, registerBackHandler = registerBackHandler)
                2 -> ProgramLibraryFlow(library = library, onSave = ::saveProgram,
                    onDelete = { id -> library = library.filterNot { it.id == id && !it.isTemplate } },
                    onNavigate = { selectedTab = it }, registerBackHandler = registerBackHandler)
                else -> TrainerHomeScreen(onClients = { selectedTab = 1 }, onPrograms = { selectedTab = 2 },
                    onChats = { selectedTab = 3 }, programCount = library.size)
            }
        }
    }
}
