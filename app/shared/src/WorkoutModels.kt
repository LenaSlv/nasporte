package com.example.testmmp

import androidx.compose.runtime.saveable.listSaver

internal data class WorkoutDate(val year: Int, val month: Int, val day: Int) {
    val daysInMonth: Int get() = when (month) {
        2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }
    // Monday = 0. Gregorian calendar arithmetic works on every KMP target.
    val weekday: Int get() {
        val y = year - if (month < 3) 1 else 0
        val offsets = intArrayOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4)
        return (y + y / 4 - y / 100 + y / 400 + offsets[month - 1] + day + 6) % 7
    }
    val display: String get() = "${day.toString().padStart(2, '0')}.${month.toString().padStart(2, '0')}.$year"
    val key: String get() = "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
    fun plusDays(count: Int): WorkoutDate {
        var result = this
        repeat(count.coerceAtLeast(0)) {
            result = if (result.day < result.daysInMonth) result.copy(day = result.day + 1)
            else result.plusMonths(1).copy(day = 1)
        }
        return result
    }
    fun plusMonths(count: Int): WorkoutDate {
        val index = year * 12 + month - 1 + count
        val first = WorkoutDate(index / 12, index % 12 + 1, 1)
        return first.copy(day = day.coerceAtMost(first.daysInMonth))
    }
    companion object {
        fun parse(key: String): WorkoutDate {
            val parts = key.split('-').map(String::toInt)
            return WorkoutDate(parts[0], parts[1], parts[2])
        }
    }
}

internal val MonthNames = listOf("Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь")
internal val WeekdayNames = listOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье")
internal val ShortWeekdays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
internal val ExerciseNames = listOf("Выпады назад", "Ягодичный мостик", "Румынская тяга", "Планка", "Отведение ноги в кроссовере",
    "Жим штанги лёжа", "Приседания со штангой на спине", "Становая тяга")

internal data class TrainingExercise(
    val id: Int,
    val catalogIndex: Int,
    val sets: Int = 3,
    val reps: Int = 12,
    val weights: String = "10,12,15",
    val restMinutes: Int = 2,
    val seconds: Int = 0,
    val sameDuration: Boolean = true,
    val setDurations: List<Int> = emptyList(),
    val restEnabled: Boolean = true,
    val perSetWeights: Boolean = true,
    val maxWeightSets: List<Int> = emptyList(),
    val supersetId: Int = 0,
)
internal data class TrainingDay(val id: Int, val title: String, val exercises: List<TrainingExercise> = emptyList())
internal data class TrainingWeek(val id: Int, val days: List<TrainingDay>)
internal data class ScheduledWorkout(val date: WorkoutDate, val time: String, val day: TrainingDay)

internal data class WorkoutProgram(
    val title: String = "Новая программа",
    val weeks: List<TrainingWeek> = listOf(TrainingWeek(1, listOf(TrainingDay(1, "День 1")))),
    val start: WorkoutDate = WorkoutDate(2026, 10, 9),
    val weekdays: List<Int> = emptyList(),
    val times: List<String> = List(7) { "19:00" },
    val repeatWeekly: Boolean = true,
    val scheduleApplied: Boolean = false,
    val additionalWorkouts: List<ScheduledWorkout> = emptyList(),
) {
    val exerciseCount: Int get() = weeks.sumOf { week -> week.days.sumOf { it.exercises.size } }
    fun updateDay(id: Int, transform: (TrainingDay) -> TrainingDay): WorkoutProgram = copy(
        weeks = weeks.map { week -> week.copy(days = week.days.map { if (it.id == id) transform(it) else it }) },
    )
    fun day(id: Int): TrainingDay? = weeks.flatMap { it.days }.find { it.id == id }
    fun schedule(): List<ScheduledWorkout> {
        if (!scheduleApplied || weekdays.isEmpty()) return additionalWorkouts
        val result = mutableListOf<ScheduledWorkout>()
        val orderedDays = weekdays.sorted()
        weeks.forEachIndexed { index, week ->
            val days = week.days.filter { it.exercises.isNotEmpty() }
            if (days.isEmpty() || (!repeatWeekly && index > 0)) return@forEachIndexed
            // A training week starts on the chosen start date, including partial calendar weeks.
            val dates = (0..6).map { start.plusDays(index * 7 + it) }.filter { it.weekday in orderedDays }
            dates.forEachIndexed { dayIndex, date ->
                result += ScheduledWorkout(date, times[date.weekday], days[dayIndex % days.size])
            }
        }
        return (result + additionalWorkouts).sortedWith(compareBy({ it.date.key }, { it.time }))
    }
}

internal data class ProgramTemplate(val title: String, val weeks: Int, val days: Int, val fromClient: Boolean)
internal val ProgramTemplates = listOf(
    ProgramTemplate("Фулбоди для начинающих", 4, 2, false),
    ProgramTemplate("Силовая база", 6, 3, false),
    ProgramTemplate("Похудение и тонус для Ивана", 8, 3, true),
    ProgramTemplate("Сила и рельеф", 4, 4, true),
)

internal fun programFromTemplate(template: ProgramTemplate): WorkoutProgram {
    var nextId = 1
    return WorkoutProgram(title = template.title, weeks = List(template.weeks) { week ->
        TrainingWeek(week + 1, List(template.days) { day ->
            TrainingDay(nextId++, "День ${day + 1} · ${if (day % 2 == 0) "Фулбоди" else "Ноги"}",
                (0..4).map { TrainingExercise(nextId++, it) })
        })
    })
}

// Length-prefixed records keep arbitrary user-entered text intact without a serialization dependency.
private fun pack(fields: List<String>): String = fields.joinToString("") { "${it.length}:$it" }
private fun unpack(record: String): List<String> {
    val fields = mutableListOf<String>()
    var position = 0
    while (position < record.length) {
        val separator = record.indexOf(':', position)
        val length = record.substring(position, separator).toInt()
        position = separator + 1
        fields += record.substring(position, position + length)
        position += length
    }
    return fields
}

internal fun encodeExercise(e: TrainingExercise): String = pack(listOf(e.id.toString(), e.catalogIndex.toString(),
    e.sets.toString(), e.reps.toString(), e.weights, e.restMinutes.toString(), e.seconds.toString(),
    e.sameDuration.toString(), e.setDurations.joinToString(","), e.restEnabled.toString(),
    e.perSetWeights.toString(), e.maxWeightSets.joinToString(","), e.supersetId.toString()))
internal fun decodeExercise(record: String): TrainingExercise {
    val e = unpack(record)
    fun integers(index: Int) = e.getOrNull(index).orEmpty().split(',').mapNotNull(String::toIntOrNull)
    return TrainingExercise(e[0].toInt(), e[1].toInt(), e[2].toInt(), e[3].toInt(), e[4], e[5].toInt(),
        e.getOrNull(6)?.toIntOrNull() ?: 0, e.getOrNull(7)?.toBooleanStrictOrNull() ?: true,
        integers(8), e.getOrNull(9)?.toBooleanStrictOrNull() ?: true,
        e.getOrNull(10)?.toBooleanStrictOrNull() ?: true, integers(11), e.getOrNull(12)?.toIntOrNull() ?: 0)
}
private fun encodeDay(day: TrainingDay): String = pack(listOf(day.id.toString(), day.title) + day.exercises.map(::encodeExercise))
private fun decodeDay(record: String): TrainingDay {
    val day = unpack(record)
    return TrainingDay(day[0].toInt(), day[1], day.drop(2).map(::decodeExercise))
}

private fun programFields(program: WorkoutProgram): List<String> =
        listOf(program.title, program.start.key, program.weekdays.joinToString(","),
            pack(program.times), program.repeatWeekly.toString(), program.scheduleApplied.toString(),
            pack(program.additionalWorkouts.map { pack(listOf(it.date.key, it.time, encodeDay(it.day))) })) +
            program.weeks.map { week ->
                pack(listOf(week.id.toString()) + week.days.map(::encodeDay))
            }

private fun programFromFields(fields: List<String>): WorkoutProgram =
        WorkoutProgram(fields[0], fields.drop(7).map { record ->
            val week = unpack(record)
            TrainingWeek(week[0].toInt(), week.drop(1).map(::decodeDay))
        }, WorkoutDate.parse(fields[1]), fields[2].split(',').filter(String::isNotEmpty).map(String::toInt),
            unpack(fields[3]), fields[4].toBoolean(), fields[5].toBoolean(), unpack(fields[6]).map { record ->
                val workout = unpack(record)
                ScheduledWorkout(WorkoutDate.parse(workout[0]), workout[1], decodeDay(workout[2]))
            })
internal val WorkoutProgramSaver = listSaver<WorkoutProgram, String>(
    save = { programFields(it) },
    restore = ::programFromFields,
)

internal data class LibraryProgram(val id: Int, val program: WorkoutProgram, val isTemplate: Boolean = false)

internal fun initialProgramLibrary(): List<LibraryProgram> = ProgramTemplates.mapIndexed { index, template ->
    LibraryProgram(index + 1, programFromTemplate(template), isTemplate = !template.fromClient)
}

internal val WorkoutLibrarySaver = listSaver<List<LibraryProgram>, String>(
    save = { entries -> entries.map { pack(listOf(it.id.toString(), it.isTemplate.toString()) + programFields(it.program)) } },
    restore = { records -> records.map { record ->
        val fields = unpack(record)
        LibraryProgram(fields[0].toInt(), programFromFields(fields.drop(2)), fields[1].toBoolean())
    } },
)
