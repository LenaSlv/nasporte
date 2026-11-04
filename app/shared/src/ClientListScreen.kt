package com.example.testmmp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testmmp.resources.*
import org.jetbrains.compose.resources.painterResource

private data class ClientRow(val id: Int, val name: String, val program: String,
    val badge: String? = null, val attention: Boolean = false, val report: Boolean = false)

@Composable
internal fun ClientListScreen(
    firstAssigned: Boolean, secondAssigned: Boolean, first: WorkoutProgram, second: WorkoutProgram,
    onHome: () -> Unit, onPrograms: () -> Unit, programCount: Int,
    onClient: (Int) -> Unit, onAssign: (Int) -> Unit, onMessage: (String) -> Unit,
    onAdd: () -> Unit, onChats: () -> Unit,
    marinaAccepted: Boolean, marinaAssigned: Boolean, marinaProgram: WorkoutProgram,
) {
    var search by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableIntStateOf(0) }
    var reverse by rememberSaveable { mutableStateOf(false) }
    var archived by rememberSaveable { mutableStateOf(listOf<Int>()) }
    var blocked by rememberSaveable { mutableStateOf(listOf<Int>()) }
    val clients = buildList {
        add(ClientRow(0, "Андрей Синицин", if (firstAssigned) first.title else "Без программы", if (firstAssigned) null else "Новый"))
        if (marinaAccepted) add(ClientRow(2, NewClientName, if (marinaAssigned) marinaProgram.title else "Без программы", if (marinaAssigned) null else "Новый"))
        add(ClientRow(1, "Константин Салтыков-Щедрин", if (secondAssigned) second.title else "Силовая подготовка"))
        add(ClientRow(3, "Константин Салтыков-Щедрин", "Силовая подготовка", report = true, attention = true))
        add(ClientRow(4, "Владимир Морозов", "Силовая подготовка", "Требует внимания", attention = true))
    }.filter { it.id !in blocked }
    val visible = clients.filter { row ->
        row.name.contains(search, true) && if (filter == 3) row.id in archived else row.id !in archived &&
            when (filter) { 1 -> row.attention; 2 -> !row.attention && row.badge == null; else -> true }
    }.let { if (reverse) it.reversed() else it }
    Box(Modifier.fillMaxSize().background(FitScreenBrush)) {
        Column(Modifier.fillMaxSize()) {
            ClientTopBar("Мои клиенты", "${15 + if (marinaAccepted) 1 else 0} в работе") {
                FitIconButton(onClick = onAdd) { Text("+", color = Color.White, fontSize = 28.sp) }
            }
            LazyColumn(Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(Color(0xFFF0F0F0)), contentPadding = PaddingValues(12.dp, 12.dp, 12.dp, 110.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { ClientSearch(search, { search = it }) }
                item {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(38.dp, 32.dp).clip(CircleShape).background(Color(0xFFE0E0E0))
                            .clickable(role = Role.Button) { reverse = !reverse }, contentAlignment = Alignment.Center) {
                            WorkoutIcon(Res.drawable.client_sort, "Изменить порядок клиентов", size = 14.dp)
                        }
                        listOf("Все (${clients.count { it.id !in archived }})", "Требуют внимания (${clients.count { it.attention && it.id !in archived }})", "Стабильные", "Архив")
                            .forEachIndexed { index, label ->
                                Text(label, Modifier.clip(RoundedCornerShape(20.dp))
                                    .background(if (filter == index) Color(0xFF0A2010) else Color(0xFFE0E0E0))
                                    .clickable(role = Role.Tab) { filter = index }.padding(12.dp, 8.dp),
                                    color = if (filter == index) Color.White else Ink, fontSize = 12.sp,
                                    lineHeight = if (index == 3) 16.sp else 14.4.sp, fontWeight = if (index == 3) FontWeight.Normal else FontWeight.Medium)
                            }
                    }
                }
                if (visible.isEmpty()) item { Text("Клиенты не найдены", Modifier.padding(16.dp), color = Muted) }
                items(visible.size, key = { visible[it].id }) { position ->
                    val row = visible[position]
                    var menu by rememberSaveable { mutableStateOf(false) }
                    val shape = RoundedCornerShape(18.dp)
                    Column(Modifier.fillMaxWidth().clip(shape).background(Color.White).border(1.dp, Color(0xFFE0E0E0), shape)
                        .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = if (row.badge != null || row.report) 12.dp else 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(15.dp),
                            verticalAlignment = Alignment.Top) {
                            if (row.id == 2) ClientMarinaPhoto() else Image(painterResource(Res.drawable.workout_client), null,
                                Modifier.size(48.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                            Column(Modifier.weight(1f).clickable { if (row.id < 3) onClient(row.id) }, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                row.badge?.let { label ->
                                    Text(label, Modifier.clip(RoundedCornerShape(8.dp))
                                        .background(if (row.attention) Color(0xFFFF3B30) else Color(0xFFFF9500)).padding(8.dp, 2.dp),
                                        color = Color.White, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = .2.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(0.dp))
                                }
                                Text(row.name, color = Color(0xFF212121), fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold)
                                Text(row.program, color = Color(0xFF757575), fontSize = 14.sp, lineHeight = 17.sp)
                            }
                            Box {
                                FitIconButton(onClick = { menu = true }, modifier = Modifier.size(24.dp)) {
                                    Icon(painterResource(Res.drawable.client_more), "Действия с клиентом", tint = Color(0xFF757575))
                                }
                                FitDropdownMenu(menu, { menu = false }) {
                                    FitDropdownMenuItem(text = { Text("Написать клиенту") }, onClick = { menu = false; onChats() })
                                    FitDropdownMenuItem(text = { Text(if (row.id in archived) "Вернуть из архива" else "В архив") }, onClick = {
                                        menu = false; archived = if (row.id in archived) archived - row.id else archived + row.id
                                    })
                                    FitDropdownMenuItem(text = { Text("Заблокировать", color = Color(0xFFFF3547)) }, onClick = { menu = false; blocked += row.id })
                                }
                            }
                        }
                        if (row.badge == "Новый") WorkoutAction("Назначить программу", { onAssign(row.id) }, compact = true, fontWeight = FontWeight.Bold)
                        if (row.report) Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFFF0F0EF)).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("1 отчет на проверку", fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold)
                                Text("Проверьте до 10 окт.", fontSize = 12.sp, lineHeight = 18.sp, color = Muted)
                            }
                            FitOutlinedButton(onClick = { onMessage("Отчёт от тренировки") }, contentPadding = PaddingValues(12.dp, 4.dp),
                                shape = RoundedCornerShape(10.dp)) { Text("Проверить", color = Ink, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold) }
                        }
                        if (row.attention && !row.report) Text("•  2 тренировки пропущены", Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)).background(Color(0xFFF0F0EF)).padding(12.dp), fontSize = 14.sp,
                            lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, color = Muted)
                    }
                }
            }
        }
        HomeNavigation(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 10.dp), selectedTab = 1, programCount = programCount) {
            when (it) { 0 -> onHome(); 2 -> onPrograms(); 3 -> onChats() }
        }
    }
}
