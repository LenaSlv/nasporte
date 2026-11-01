package com.example.testmmp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.example.testmmp.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private val Ink = Color(0xFF060607)
private val Muted = Color(0xFF707075)
private val Forest = Color(0xFF0E2E16)
private val Lime = Color(0xFFB6F500)
private val Page = Color(0xFFEDEDEE)
private val Card = Color(0xFFF7F7F8)
private val Tile = Color(0xFFE9E9EA)

private data class Appointment(val time: String, val name: String, val format: String, val day: Int = 22)

private val InitialAppointments = listOf(
    Appointment("09:30", "Константин Евсеев", "Персональная .Зал"),
    Appointment("11:00", "Константин Салтыков-Щедрин", "Онлайн"),
    Appointment("19:30", "Илья Морозов", "Персональная .Зал"),
)

private data class HomeTask(val title: String, val count: Int, val details: List<String>)

private val HomeTasks = listOf(
    HomeTask("Отчёты на проверку", 3, listOf("Константин Евсеев", "Константин Салтыков-Щедрин", "Илья Морозов")),
    HomeTask("Перенос/Замена", 3, listOf("Запросы на перенос и замену тренировок")),
    HomeTask("Заявка на тренировки", 2, listOf("Новые заявки на тренировки")),
)

@Composable
fun TrainerHomeScreen() {
    var selectedDay by rememberSaveable { mutableIntStateOf(22) }
    var dayChanged by rememberSaveable { mutableStateOf(false) }
    var expandedTask by rememberSaveable { mutableIntStateOf(-1) }
    var showCalendar by rememberSaveable { mutableStateOf(false) }
    var showAdd by rememberSaveable { mutableStateOf(false) }
    var dialogTitle by rememberSaveable { mutableStateOf<String?>(null) }
    var dialogBody by rememberSaveable { mutableStateOf("") }
    // Keep additions through Activity recreation without a database or navigation stack.
    var addedRows by rememberSaveable { mutableStateOf(listOf<String>()) }
    val appointments = InitialAppointments + addedRows.map { row ->
        val parts = row.split('\t')
        Appointment(parts[0], parts[1], parts[2], parts[3].toInt())
    }
    val today = appointments.filter { it.day == selectedDay }.sortedBy { it.time }
    fun showDetails(title: String, body: String) {
        dialogTitle = title
        dialogBody = body
    }

    Box(Modifier.fillMaxSize().background(Page)) {
        Column(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxWidth().background(
                    Brush.horizontalGradient(listOf(Color(0xFF020B07), Color(0xFF0E2E16))),
                ).statusBarsPadding(),
            ) {
                HomeHeader(
                    onProfile = { showDetails("Александр", "Профиль тренера") },
                    onNotifications = {
                        showDetails("Уведомления (3)", "3 отчёта ожидают проверки.\nОткройте «Отчёты на проверку» в разделе задач.")
                    },
                )
                WeekStrip(selectedDay, appointments.map { it.day }.toSet(), {
                    selectedDay = it
                    dayChanged = true
                }, { showCalendar = true })
            }
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth().background(Forest)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)).background(Page),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 108.dp),
            ) {
                item {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Schedule(
                            title = if (dayChanged) "$selectedDay октября" else "Сегодня, 13 октября",
                            appointments = today,
                            onAdd = { showAdd = true },
                            onAppointment = { showDetails(it.name, "${it.time} · ${it.format}") },
                        )
                        TasksSection(expandedTask, { expandedTask = if (expandedTask == it) -1 else it }) {
                            showDetails("Мои задачи (15)", HomeTasks.joinToString("\n\n") { "${it.title}: ${it.count}" })
                        }
                        ClientsSection { title, body -> showDetails(title, body) }
                    }
                }
            }
        }
        HomeNavigation(
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 10.dp),
            onNavigate = { index ->
                when (index) {
                    1 -> showDetails("Мои клиенты (15)", "Требуют внимания: 3\nСтабильная динамика: 12\nНовые клиенты: 0")
                    2 -> showDetails("Программы", "Библиотека программ пока пуста.")
                    3 -> showDetails("Чат", "Раздел чатов будет доступен на следующем экране.")
                }
            },
        )
    }

    if (dialogTitle != null) {
        AlertDialog(
            onDismissRequest = { dialogTitle = null },
            title = { Text(dialogTitle.orEmpty()) },
            text = { Text(dialogBody) },
            confirmButton = { TextButton(onClick = { dialogTitle = null }) { Text("Закрыть") } },
        )
    }
    if (showCalendar) {
        CalendarDialog(selectedDay, { showCalendar = false }) {
            selectedDay = it
            dayChanged = true
            showCalendar = false
        }
    }
    if (showAdd) {
        AddAppointmentDialog(selectedDay, { showAdd = false }) { time, name, format ->
            addedRows = addedRows + "$time\t$name\t$format\t$selectedDay"
            showAdd = false
        }
    }
}

@Composable
private fun HomeHeader(onProfile: () -> Unit, onNotifications: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top,
    ) {
        Image(painterResource(Res.drawable.trainer_photo), "Мой профиль",
            Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onProfile), contentScale = ContentScale.Crop)
        Column(Modifier.weight(1f).clickable(onClick = onProfile).padding(top = 10.dp)) {
            Text("Привет, Александр!", fontSize = 22.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold,
                color = Color(0xFFF5F5F5), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Перейти в мой профиль", color = Color(0xFF9EA89C), fontSize = 12.sp, lineHeight = 14.sp)
        }
        Box(Modifier.size(44.dp)) {
            Box(
                Modifier.matchParentSize().clip(CircleShape).background(Color(0xFF1B201A))
                    .clickable(onClick = onNotifications),
                contentAlignment = Alignment.Center,
            ) {
                AssetIcon(Res.drawable.bell, "Уведомления, 3 новых")
            }
            CountBadge(3, Modifier.align(Alignment.TopEnd), notification = true)
        }
    }
}

@Composable
private fun WeekStrip(selected: Int, trainingDays: Set<Int>, onSelect: (Int) -> Unit, onCalendar: () -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Image(painterResource(Res.drawable.calendar), "Открыть календарь",
            Modifier.size(39.dp, 52.dp).clip(RoundedCornerShape(10.dp)).clickable(onClick = onCalendar))
        (21..31).forEach { day ->
            val active = day == selected
            Column(
                Modifier.size(39.dp, 52.dp).clip(RoundedCornerShape(10.dp))
                    .background(if (active) Lime.copy(alpha = .2f) else Color(0x33111411))
                    .border(BorderStroke(.5.dp, Color(0xFF64705E).copy(alpha = .45f)), RoundedCornerShape(10.dp))
                    .clickable(role = Role.RadioButton) { onSelect(day) }
                    .semantics { contentDescription = "$day октября"; stateDescription = if (active) "Выбрано" else "Не выбрано" }
                    .padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // The repeated weekday label follows the supplied design.
                Text("ПН", color = if (active) Lime else Color(0xFF9EA89C),
                    fontSize = 10.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(day.toString(), color = if (active) Lime else Color.White, fontSize = 16.sp,
                    lineHeight = 22.sp, fontWeight = FontWeight.Bold)
                if (day in trainingDays || day in 25..27) {
                    Image(painterResource(Res.drawable.training_dot), "Есть тренировка", Modifier.size(6.dp))
                }
            }
        }
    }
}

@Composable
private fun Schedule(title: String, appointments: List<Appointment>, onAdd: () -> Unit, onAppointment: (Appointment) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold, color = Ink)
                Text(if (appointments.size == 3) "3 клиента на тренировку" else "Тренировок: ${appointments.size}",
                    fontSize = 14.sp, lineHeight = 17.sp, color = Muted)
            }
            Text("+ Добавить", Modifier.clip(RoundedCornerShape(4.dp)).clickable(onClick = onAdd).padding(vertical = 2.dp),
                fontSize = 14.sp, lineHeight = 16.sp, color = Forest, fontWeight = FontWeight.SemiBold)
        }
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Card)) {
            if (appointments.isEmpty()) {
                Text("На этот день тренировок нет", Modifier.padding(20.dp), fontSize = 14.sp, color = Muted)
            }
            appointments.forEachIndexed { index, appointment ->
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).clickable { onAppointment(appointment) },
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.width(41.dp).fillMaxHeight().background(Color(0xFF1C1C1F)).padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Text(appointment.time.substringBefore(':'), color = Color(0xFFC1FF28), fontSize = 14.sp,
                            lineHeight = 18.sp, fontWeight = FontWeight.Bold)
                        Image(painterResource(Res.drawable.time_divider), null, Modifier.size(21.dp, 1.dp))
                        Text(appointment.time.substringAfter(':'), color = Color(0xFFC1FF28), fontSize = 14.sp,
                            lineHeight = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Image(painterResource(Res.drawable.client_photo), null,
                        Modifier.padding(start = 8.dp).size(40.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                    Column(Modifier.weight(1f).padding(start = 15.dp, top = 12.dp, bottom = 12.dp)) {
                        Text(appointment.name, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold,
                            color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(appointment.format, fontSize = 12.sp, lineHeight = 18.sp, color = Muted,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    AssetIcon(Res.drawable.chevron_right, null, Modifier.padding(start = 8.dp, end = 16.dp))
                }
                if (index != appointments.lastIndex) HorizontalDivider(thickness = .5.dp, color = Color(0xFFB5B5B8))
            }
        }
    }
}

@Composable
private fun SectionHeading(title: String, onViewAll: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold, color = Ink)
        Text("Смотреть все", Modifier.clip(RoundedCornerShape(4.dp)).clickable(onClick = onViewAll),
            fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold, color = Forest)
    }
}

@Composable
private fun TasksSection(expanded: Int, onExpand: (Int) -> Unit, onViewAll: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeading("Мои задачи (15)", onViewAll)
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Card)) {
            HomeTasks.forEachIndexed { index, task ->
                Row(Modifier.fillMaxWidth().clickable { onExpand(index) }.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CountBadge(task.count)
                    Text(task.title, Modifier.weight(1f), fontSize = 14.sp, lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium, color = Ink)
                    AssetIcon(Res.drawable.chevron_down, if (expanded == index) "Свернуть" else "Развернуть",
                        Modifier.rotate(if (expanded == index) 180f else 0f))
                }
                AnimatedVisibility(expanded == index) {
                    Column(Modifier.padding(start = 36.dp, end = 12.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        task.details.forEach { Text(it, color = Muted, fontSize = 13.sp, lineHeight = 18.sp) }
                    }
                }
                if (index != HomeTasks.lastIndex) HorizontalDivider(thickness = .5.dp, color = Color(0xFFD3D3D5))
            }
        }
    }
}

@Composable
private fun ClientsSection(onDetails: (String, String) -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeading("Мои клиенты (15)") {
            onDetails("Мои клиенты (15)", "Требуют внимания: 3\nСтабильная динамика: 12\nНовые клиенты: 0")
        }
        Row(Modifier.fillMaxWidth().height(202.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(16.dp)).background(Tile)
                .clickable { onDetails("Требуют внимания (3)", HomeTasks[0].details.joinToString("\n")) }) {
                Image(painterResource(Res.drawable.attention_art), null,
                    Modifier.offset(x = 32.dp, y = 98.dp).size(142.dp))
                Column(Modifier.padding(13.dp)) {
                    Text("3", fontSize = 44.sp, lineHeight = 52.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF0077F))
                    Text("Требуют\nвнимания", fontSize = 14.sp, lineHeight = 17.sp, color = Ink)
                }
            }
            Column(Modifier.weight(1.1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallClientTile("12", "Стабильная\nдинамика", Res.drawable.progress_art, Modifier.weight(1f), true) {
                    onDetails("Стабильная динамика", "12 клиентов занимаются по плану.")
                }
                SmallClientTile("0", "Новые\nклиенты", Res.drawable.new_clients_art, Modifier.weight(1f), false) {
                    onDetails("Новые клиенты", "Новых клиентов пока нет.")
                }
            }
        }
    }
}

@Composable
private fun SmallClientTile(count: String, label: String, art: DrawableResource, modifier: Modifier, progress: Boolean, onClick: () -> Unit) {
    Box(modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Tile).clickable(onClick = onClick)) {
        Image(painterResource(art), null,
            Modifier.align(Alignment.TopEnd).offset(x = if (progress) 0.dp else 9.dp, y = if (progress) 33.dp else 20.dp)
                .size(if (progress) 81.dp else 99.dp))
        Column(Modifier.padding(13.dp)) {
            Text(count, fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text(label, fontSize = 14.sp, lineHeight = 17.sp, color = Ink)
        }
    }
}

@Composable
private fun HomeNavigation(modifier: Modifier = Modifier, onNavigate: (Int) -> Unit) {
    val labels = listOf("Главная", "Клиенты", "Программы", "Чат")
    val icons = listOf(Res.drawable.home, Res.drawable.clients, Res.drawable.programs, Res.drawable.chat)
    val counts = listOf(0, 1, 7, 4)
    Row(modifier.padding(horizontal = 26.dp).widthIn(max = 420.dp).fillMaxWidth()
        .clip(RoundedCornerShape(22.dp)).background(Color(0xFFCBCBC9)).padding(4.dp),
        verticalAlignment = Alignment.CenterVertically) {
        labels.forEachIndexed { index, label ->
            Column(Modifier.weight(1f).height(54.dp).clip(RoundedCornerShape(18.dp))
                .background(if (index == 0) Lime.copy(alpha = .65f) else Color.Transparent)
                .clickable(role = Role.Tab) { onNavigate(index) }
                .semantics { stateDescription = if (index == 0) "Выбрано" else "Не выбрано" },
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Box(Modifier.width(44.dp).height(28.dp), contentAlignment = Alignment.Center) {
                    AssetIcon(icons[index], null, size = when (index) { 0 -> 20.dp; 1 -> 20.5.dp; else -> 24.dp })
                    if (counts[index] > 0) CountBadge(counts[index], Modifier.align(Alignment.TopEnd), accent = true)
                }
                Text(label, fontSize = 10.sp, lineHeight = 12.sp, fontWeight = FontWeight.SemiBold, color = Ink, maxLines = 1)
            }
        }
    }
}

@Composable
private fun CountBadge(count: Int, modifier: Modifier = Modifier, notification: Boolean = false, accent: Boolean = false) {
    Badge(modifier = modifier, containerColor = if (notification) Color(0xFFB3261E) else Color(0xFF1A201C),
        contentColor = if (notification) Color.White else if (accent) Lime else Color(0xFFFAFFE5)) {
        Text(count.toString(), fontSize = 11.sp, lineHeight = 14.sp)
    }
}

@Composable
private fun AssetIcon(resource: DrawableResource, description: String?, modifier: Modifier = Modifier, size: Dp = 24.dp) {
    Image(painterResource(resource), description, modifier.size(size))
}

@Composable
private fun CalendarDialog(selected: Int, onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Октябрь 2026") }, text = {
        Column {
            Row(Modifier.fillMaxWidth()) {
                listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС").forEach {
                    Text(it, Modifier.weight(1f), fontSize = 12.sp, color = Muted)
                }
            }
            LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.height(240.dp)) {
                items(3) { Spacer(Modifier.size(36.dp)) }
                items(31) { index ->
                    val day = index + 1
                    Box(Modifier.padding(2.dp).size(36.dp).clip(CircleShape)
                        .background(if (day == selected) Lime else Color.Transparent).clickable { onSelect(day) },
                        contentAlignment = Alignment.Center) { Text(day.toString(), color = Ink, fontSize = 14.sp) }
                }
            }
        }
    }, confirmButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } })
}

@Composable
private fun AddAppointmentDialog(day: Int, onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var time by rememberSaveable { mutableStateOf("") }
    var online by rememberSaveable { mutableStateOf(false) }
    val validTime = Regex("([01][0-9]|2[0-3]):[0-5][0-9]").matches(time)
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Тренировка · $day октября") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(name, { name = it.replace('\t', ' ').replace('\n', ' ') },
                label = { Text("Имя клиента") }, singleLine = true)
            OutlinedTextField(time, { time = it.take(5) }, label = { Text("Время, ЧЧ:ММ") },
                singleLine = true, isError = time.isNotEmpty() && !validTime,
                supportingText = { if (time.isNotEmpty() && !validTime) Text("Укажите время от 00:00 до 23:59") })
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(online, { online = it })
                Text("Онлайн-тренировка", fontSize = 14.sp)
            }
        }
    }, confirmButton = {
        TextButton(enabled = name.isNotBlank() && validTime, onClick = {
            onAdd(time, name.trim(), if (online) "Онлайн" else "Персональная .Зал")
        }) { Text("Добавить") }
    }, dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } })
}
