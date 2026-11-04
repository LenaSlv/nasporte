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

internal val Ink = Color(0xFF060607)
internal val Muted = Color(0xFF707075)
internal val Forest = Color(0xFF0E2E16)
internal val Lime = Color(0xFFB6F500)
internal val Page = Color(0xFFEDEDEE)
internal val Card = Color(0xFFF7F7F8)
internal val Tile = Color.White

private data class Appointment(val time: String, val name: String, val format: String, val day: Int = 22)

private val InitialAppointments = listOf(
    Appointment("09:30", "Константин Евсеев", "Персональная · Зал"),
    Appointment("11:00", "Константин Салтыков-Щедрин", "Онлайн"),
    Appointment("19:30", "Илья Морозов", "Персональная · Зал"),
)

private data class HomeTask(val title: String, val count: Int, val details: List<String>)

private val HomeTasks = listOf(
    HomeTask("Отчёты на проверку", 3, listOf("Константин Евсеев", "Константин Салтыков-Щедрин", "Илья Морозов")),
    HomeTask("Перенос/Замена", 3, listOf("Запросы на перенос и замену тренировок")),
    HomeTask("Заявка на тренировки", 2, listOf("Новые заявки на тренировки")),
)

@Composable
fun TrainerHomeScreen(onClients: () -> Unit = {}, onPrograms: () -> Unit = {}, onChats: () -> Unit = {}, programCount: Int = 7) {
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
                    FitScreenBrush,
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
                Modifier.weight(1f).fillMaxWidth().background(Color.White)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)).background(Page),
                contentPadding = PaddingValues(bottom = 108.dp),
            ) {
                item {
                    Column(Modifier.fillMaxWidth().background(Color.White), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Schedule(
                            title = if (dayChanged) "$selectedDay октября" else "Понедельник, 22 октября",
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
            programCount = programCount,
            onNavigate = { index ->
                when (index) {
                    1 -> onClients()
                    2 -> onPrograms()
                    3 -> onChats()
                }
            },
        )
    }

    if (dialogTitle != null) {
        FitDialog(
            onDismissRequest = { dialogTitle = null },
            title = { Text(dialogTitle.orEmpty()) },
            text = { Text(dialogBody) },
            confirmButton = { FitTextButton(onClick = { dialogTitle = null }) { Text("Закрыть") } },
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
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top,
    ) {
        Image(painterResource(Res.drawable.trainer_photo), "Мой профиль",
            Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onProfile), contentScale = ContentScale.Crop)
        Column(Modifier.weight(1f).clickable(onClick = onProfile)) {
            Text("Привет, Александр!", fontSize = 22.sp, lineHeight = 27.sp, fontWeight = FontWeight.SemiBold,
                color = Color(0xFFF5F5F5), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Перейти в мой профиль  ›", color = Color(0xFF9EA89C), fontSize = 12.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium)
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
                Text(ShortWeekdays[(day - 22 + 7) % 7].uppercase(), color = if (active) Lime else Color(0xFF9EA89C),
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
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Page).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontSize = 16.sp, lineHeight = 19.2.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                Text(if (appointments.size == 3) "3 клиента на тренировку" else "Тренировок: ${appointments.size}",
                    fontSize = 14.sp, lineHeight = 17.sp, color = Muted)
            }
            Text("Добавить +", Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFFE0E0E0)).clickable(onClick = onAdd).padding(horizontal = 8.dp, vertical = 4.dp),
                fontSize = 12.sp, lineHeight = 16.sp, color = Ink, fontWeight = FontWeight.SemiBold)
        }
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Card)) {
            if (appointments.isEmpty()) {
                Text("На этот день тренировок нет", Modifier.padding(20.dp), fontSize = 14.sp, color = Muted)
            }
            appointments.forEachIndexed { index, appointment ->
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).clickable { onAppointment(appointment) },
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.width(41.dp).fillMaxHeight().background(Color(0xFF0A2010)).padding(vertical = 12.dp),
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
                            fontWeight = if (appointment.format == "Онлайн") FontWeight.Normal else FontWeight.Medium,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    AssetIcon(Res.drawable.chevron_right, null, Modifier.padding(start = 8.dp, end = 16.dp))
                }
                if (index != appointments.lastIndex) HorizontalDivider(thickness = .5.dp, color = Color(0xFFEDEDEE))
            }
        }
    }
}

@Composable
private fun SectionHeading(title: String, onViewAll: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        Text("Все ›", Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFFE0E0E0)).clickable(onClick = onViewAll).padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold, color = Ink)
    }
}

@Composable
private fun TasksSection(expanded: Int, onExpand: (Int) -> Unit, onViewAll: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Page).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                if (index != HomeTasks.lastIndex) HorizontalDivider(thickness = .5.dp, color = Color(0xFFEDEDEE))
            }
        }
    }
}

@Composable
private fun ClientsSection(onDetails: (String, String) -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Page).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeading("Мои клиенты (15)") {
            onDetails("Мои клиенты (15)", "Требуют внимания: 3\nСтабильная динамика: 12\nНовые клиенты: 0")
        }
        Row(Modifier.fillMaxWidth().height(206.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(148f).fillMaxHeight().clip(RoundedCornerShape(16.dp)).background(Tile)
                .clickable { onDetails("Требуют внимания (3)", HomeTasks[0].details.joinToString("\n")) }) {
                Image(painterResource(Res.drawable.attention_art), null,
                    Modifier.offset(x = 40.dp, y = 79.dp).size(142.dp))
                Column(Modifier.padding(13.dp)) {
                    Text("3", fontSize = 44.sp, lineHeight = 53.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF0077F))
                    Text("Требуют\nвнимания", fontSize = 14.sp, lineHeight = 17.sp, color = Ink)
                }
            }
            Column(Modifier.weight(187f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
            Modifier.align(Alignment.TopEnd).offset(x = if (progress) 0.dp else 9.dp, y = 20.dp)
                .size(if (progress) 81.dp else 99.dp))
        Column(Modifier.padding(13.dp)) {
            Text(count, fontSize = 32.sp, lineHeight = 39.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text(label, fontSize = 14.sp, lineHeight = 17.sp, color = Ink)
        }
    }
}

@Composable
internal fun HomeNavigation(modifier: Modifier = Modifier, selectedTab: Int = 0, programCount: Int = 7, onNavigate: (Int) -> Unit) {
    val labels = listOf("Главная", "Клиенты", "Материалы", "Чат")
    val icons = listOf(Res.drawable.home, Res.drawable.clients, Res.drawable.programs, Res.drawable.chat)
    val counts = listOf(0, if (selectedTab == 0) 1 else 0, 0, 0)
    Row(modifier.padding(horizontal = 22.dp).widthIn(max = 420.dp).fillMaxWidth()
        .clip(RoundedCornerShape(22.dp)).background(Color(0xFFF1F1F0).copy(alpha = .96f))
        .border(.5.dp, Color.White, RoundedCornerShape(22.dp)).padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically) {
        labels.forEachIndexed { index, label ->
            Column(Modifier.weight(1f).height(54.dp).clip(RoundedCornerShape(18.dp))
                .background(if (index == selectedTab) Color(0xFFCDF566) else Color.Transparent)
                .clickable(role = Role.Tab) { onNavigate(index) }
                .semantics { stateDescription = if (index == selectedTab) "Выбрано" else "Не выбрано" },
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Box(Modifier.width(44.dp).height(28.dp), contentAlignment = Alignment.Center) {
                    AssetIcon(icons[index], null, size = when (index) { 0 -> 20.dp; 1 -> 20.5.dp; else -> 24.dp })
                    if (counts[index] > 0) CountBadge(counts[index], Modifier.align(Alignment.TopEnd), notification = true)
                }
                Text(label, fontSize = 10.sp, lineHeight = 12.sp, fontWeight = FontWeight.SemiBold, color = Ink, maxLines = 1)
            }
        }
    }
}

@Composable
private fun CountBadge(count: Int, modifier: Modifier = Modifier, notification: Boolean = false, accent: Boolean = false) {
    Box(modifier.size(16.dp).background(if (notification) Color(0xFFB3261E) else Color(0xFF0A2010), CircleShape),
        contentAlignment = Alignment.Center) {
        Text(count.toString(), fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium, color = if (notification) Color.White else Lime)
    }
}

@Composable
private fun AssetIcon(resource: DrawableResource, description: String?, modifier: Modifier = Modifier, size: Dp = 24.dp) {
    Image(painterResource(resource), description, modifier.size(size))
}

@Composable
private fun CalendarDialog(selected: Int, onDismiss: () -> Unit, onSelect: (Int) -> Unit) {
    FitDialog(onDismissRequest = onDismiss, title = { Text("Октябрь 2026") }, text = {
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
    }, confirmButton = { FitTextButton(onClick = onDismiss) { Text("Закрыть") } })
}

@Composable
private fun AddAppointmentDialog(day: Int, onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var time by rememberSaveable { mutableStateOf("") }
    var online by rememberSaveable { mutableStateOf(false) }
    val validTime = Regex("([01][0-9]|2[0-3]):[0-5][0-9]").matches(time)
    FitDialog(onDismissRequest = onDismiss, title = { Text("Тренировка · $day октября") }, text = {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Page).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FitTextField(name, { name = it.replace('\t', ' ').replace('\n', ' ') },
                label = { Text("Имя клиента") }, singleLine = true)
            FitTextField(time, { time = it.take(5) }, label = { Text("Время, ЧЧ:ММ") },
                singleLine = true, isError = time.isNotEmpty() && !validTime,
                supportingText = { if (time.isNotEmpty() && !validTime) Text("Укажите время от 00:00 до 23:59") })
            Row(verticalAlignment = Alignment.CenterVertically) {
                FitCheckbox(online, { online = it })
                Text("Онлайн-тренировка", fontSize = 14.sp)
            }
        }
    }, confirmButton = {
        FitTextButton(enabled = name.isNotBlank() && validTime, onClick = {
            onAdd(time, name.trim(), if (online) "Онлайн" else "Персональная · Зал")
        }) { Text("Добавить") }
    }, dismissButton = { FitTextButton(onClick = onDismiss) { Text("Отмена") } })
}
