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
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Добавить упражнение") }, text = {
        Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(query, { query = it }, label = { Text("Поиск упражнения") }, singleLine = true)
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
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } })
}

@Composable
internal fun ExerciseEditorDialog(exercise: TrainingExercise, onDismiss: () -> Unit, onSave: (TrainingExercise) -> Unit, onDelete: () -> Unit) {
    var sets by rememberSaveable(exercise.id) { mutableStateOf(exercise.sets.toString()) }
    var reps by rememberSaveable(exercise.id) { mutableStateOf(exercise.reps.toString()) }
    var weights by rememberSaveable(exercise.id) { mutableStateOf(exercise.weights) }
    var rest by rememberSaveable(exercise.id) { mutableStateOf(exercise.restMinutes.toString()) }
    val validWeights = weights.split(',').all { part -> part.trim().toDoubleOrNull()?.let { it in 0.0..1000.0 } == true }
    val valid = sets.toIntOrNull()?.let { it in 1..20 } == true && reps.toIntOrNull()?.let { it in 1..200 } == true &&
        rest.toIntOrNull()?.let { it in 0..30 } == true && validWeights
    AlertDialog(onDismissRequest = onDismiss, title = { Text(ExerciseNames[exercise.catalogIndex]) }, text = {
        Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(sets, { sets = it.filter(Char::isDigit).take(2) }, Modifier.weight(1f), label = { Text("Подходы") },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(reps, { reps = it.filter(Char::isDigit).take(3) }, Modifier.weight(1f), label = { Text("Повторения") },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
            OutlinedTextField(weights, { weights = it.filter { c -> c.isDigit() || c == ',' || c == '.' || c == ' ' }.take(60) },
                label = { Text("Вес по подходам, кг") }, supportingText = { Text("Через запятую, например: 10,12,15") },
                singleLine = true, isError = !validWeights)
            OutlinedTextField(rest, { rest = it.filter(Char::isDigit).take(2) }, label = { Text("Отдых, минут") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            if (!valid) Text("Подходы: 1–20, повторения: 1–200, вес: 0–1000 кг, отдых: 0–30 мин.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            TextButton(onClick = onDelete) { Text("Удалить упражнение", color = MaterialTheme.colorScheme.error) }
        }
    }, confirmButton = { TextButton(enabled = valid, onClick = {
        onSave(exercise.copy(sets = sets.toInt(), reps = reps.toInt(), weights = weights.trim(), restMinutes = rest.toInt()))
    }) { Text("Сохранить") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } })
}
