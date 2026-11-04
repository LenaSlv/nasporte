package com.example.testmmp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ExerciseLibraryDialog(onDismiss: () -> Unit, onAdd: (Int) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    FitDialog(onDismissRequest = onDismiss, title = { Text("Добавить упражнение") }, text = {
        Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FitTextField(query, { query = it }, label = { Text("Поиск упражнения") }, singleLine = true)
            val matches = ExerciseNames.indices.filter { ExerciseNames[it].contains(query, ignoreCase = true) }
            if (matches.isEmpty()) Text("Упражнения не найдены", color = Muted)
            matches.forEach { index ->
                Row(Modifier.fillMaxWidth().clickable { onAdd(index) }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExercisePhoto(index)
                    Text(ExerciseNames[index], fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }, confirmButton = { FitTextButton(onClick = onDismiss) { Text("Закрыть") } })
}

@Composable
internal fun ExerciseEditorDialog(exercise: TrainingExercise, onDismiss: () -> Unit, onSave: (TrainingExercise) -> Unit, onDelete: () -> Unit) {
    var record by rememberSaveable(exercise.id) { mutableStateOf(encodeExercise(exercise)) }
    val draft = decodeExercise(record)
    FitDialog(onDismissRequest = onDismiss, title = { Text(ExerciseNames[exercise.catalogIndex]) }, text = {
        Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ExerciseParameters(draft, { record = encodeExercise(it) })
            FitTextButton(onClick = onDelete) { Text("Удалить упражнение", color = MaterialTheme.colorScheme.error) }
        }
    }, confirmButton = { WorkoutAction("Сохранить", { onSave(draft) }, enabled = validExercise(draft)) },
        dismissButton = { WorkoutAction("Отмена", onDismiss, outlined = true) })
}
