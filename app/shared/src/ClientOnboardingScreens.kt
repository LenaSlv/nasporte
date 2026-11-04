package com.example.testmmp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.testmmp.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

// Screen-only sample: no remote invitations, requests, messages, or questionnaire delivery.
internal const val ClientInviteLink = "https://fittrack.example/invite/trainer"
internal const val NewClientName = "Марина Кузнецова"

@Composable
internal fun ClientSearch(value: String, onChange: (String) -> Unit, placeholder: String = "Поиск") {
    BasicTextField(value, onChange, Modifier.fillMaxWidth(), singleLine = true,
        textStyle = TextStyle(color = Ink, fontSize = 16.sp, lineHeight = 24.sp, fontFamily = fitInter()), decorationBox = { inner ->
            Row(Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(12.dp))
                .background(Color.White).padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(painterResource(Res.drawable.workout_search), null, Modifier.size(24.dp), tint = Color(0xFFA6B0A9))
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) Text(placeholder, color = Muted, fontSize = 16.sp, lineHeight = 24.sp)
                    inner()
                }
            }
        })
}

@Composable
internal fun ClientTopBar(title: String, subtitle: String? = null, onBack: (() -> Unit)? = null,
    avatar: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {}) {
    Row(Modifier.fillMaxWidth().statusBarsPadding().heightIn(min = 64.dp).padding(horizontal = if (onBack == null) 16.dp else 4.dp),
        verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) WorkoutIconButton(Res.drawable.workout_left, "Назад", onBack, light = true)
        if (avatar) {
            Box(Modifier.size(32.dp).background(Color(0xFF6750A4), CircleShape), contentAlignment = Alignment.Center) {
                Text("А", fontSize = 16.sp, color = Color.White)
            }
            Spacer(Modifier.width(16.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold, fontFamily = fitInter(), color = Color.White)
            if (subtitle != null) Text(subtitle, fontSize = if (avatar) 14.sp else 12.sp, lineHeight = 16.sp,
                fontWeight = FontWeight.Medium, letterSpacing = .5.sp, color = if (avatar) Lime else Color(0xFFADB6AD))
        }
        actions()
    }
}

@Composable
private fun ClientButton(label: String, onClick: () -> Unit, dark: Boolean = false, outline: Boolean = false,
    icon: DrawableResource? = null) {
    val shape = RoundedCornerShape(16.dp)
    Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).clip(shape)
        .background(if (outline) Color.Transparent else if (dark) Color(0xFF0A2010) else Lime)
        .then(if (outline) Modifier.border(1.dp, Color.Black, shape) else Modifier)
        .clickable(role = Role.Button, onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            WorkoutIcon(icon, null)
            Spacer(Modifier.width(8.dp))
        }
        Text(label, color = if (dark) Color.White else if (outline) Color(0xFF123743) else Ink,
            fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClientSheet(title: String, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = null, containerColor = Color.White, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Page).padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text(title, Modifier.weight(1f), color = Color(0xFF2B2D33), fontSize = 22.sp,
                    lineHeight = 28.sp, fontWeight = FontWeight.Bold)
                FitIconButton(onClick = onDismiss, modifier = Modifier.size(44.dp)) {
                    WorkoutIcon(Res.drawable.client_close, "Закрыть")
                }
            }
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(Page).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
        }
    }
}

@Suppress("DEPRECATION")
@Composable
internal fun ClientInvitationSheet(onDismiss: () -> Unit, onShare: ((String) -> Unit)?) {
    val clipboard = LocalClipboardManager.current
    var copied by rememberSaveable { mutableStateOf(false) }
    var sharePreview by rememberSaveable { mutableStateOf(false) }
    ClientSheet("Персональная ссылка", onDismiss) {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color(0xFFE4E6EA))
            .padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(Res.drawable.client_invite_qr), "QR-код персональной ссылки",
                Modifier.size(245.dp).clip(RoundedCornerShape(24.dp)))
            Spacer(Modifier.height(24.dp))
            Text("Константин Салтыков-Щедрин", Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                fontSize = 20.sp, lineHeight = 24.sp, letterSpacing = .2.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, color = Ink)
            Spacer(Modifier.height(8.dp))
            Text("Персональный фитнес-тренер", fontSize = 16.sp, lineHeight = 20.sp, letterSpacing = .2.sp, color = Color(0xFF707F8D), textAlign = TextAlign.Center)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ClientButton(if (copied) "Ссылка скопирована" else "Копировать ссылку", {
                clipboard.setText(AnnotatedString(ClientInviteLink)); copied = true
            }, dark = true, icon = Res.drawable.client_copy)
            ClientButton("Поделиться", { if (onShare != null) onShare(ClientInviteLink) else sharePreview = true })
        }
    }
    if (sharePreview) FitDialog(onDismissRequest = { sharePreview = false }, title = { Text("Персональная ссылка") },
        text = { Text(ClientInviteLink) }, confirmButton = { FitTextButton(onClick = {
            clipboard.setText(AnnotatedString(ClientInviteLink)); copied = true; sharePreview = false
        }) { Text("Копировать") } }, dismissButton = { FitTextButton(onClick = { sharePreview = false }) { Text("Закрыть") } })
}

@Composable
internal fun NewClientRequestDialog(onDismiss: () -> Unit, onOpen: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
        Column(Modifier.widthIn(max = 313.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White)
            .verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                FitIconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    WorkoutIcon(Res.drawable.client_close, "Закрыть")
                }
            }
            // Preserve the overflow and crop of the illustration's Figma slot.
            Box(Modifier.size(175.dp, 160.dp), contentAlignment = Alignment.TopStart) {
                Image(painterResource(Res.drawable.client_request_art), null,
                    Modifier.wrapContentSize(Alignment.TopStart, unbounded = true).offset(x = (-6).dp, y = (-25).dp).size(194.dp, 191.dp),
                    contentScale = ContentScale.FillBounds)
            }
            Text("У вас новый запрос!", color = Color(0xFF1D1B20), fontSize = 24.sp, lineHeight = 32.sp,
                fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            Text("$NewClientName хочет начать тренировки с вами", color = Color(0xFF49454F),
                fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = .25.sp, textAlign = TextAlign.Center)
            Column(Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ClientButton("Посмотреть позже", onDismiss, outline = true)
                ClientButton("Перейти к запросу", onOpen)
            }
        }
        }
    }
}

@Composable
internal fun ClientAcceptedSheet(onDismiss: () -> Unit, onAssign: () -> Unit) {
    ClientSheet("Заявка принята", onDismiss) {
        Text("$NewClientName добавлена в список ваших клиентов. Теперь вы можете назначить ей программу тренировок",
            color = Color(0xFF172D37), fontSize = 16.sp, lineHeight = 20.sp)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ClientButton("Позже", onDismiss, outline = true)
            ClientButton("Назначить программу", onAssign)
        }
    }
}

@Composable
internal fun ClientChatsScreen(programCount: Int, requestVisible: Boolean, onNavigate: (Int) -> Unit, onRequest: () -> Unit,
    onOtherChat: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var requestsOnly by rememberSaveable { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().background(FitScreenBrush)) {
        Column(Modifier.fillMaxSize()) {
            ClientTopBar("Чаты")
            LazyColumn(Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(Page), contentPadding = PaddingValues(12.dp, 12.dp, 12.dp, 110.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f)) { ClientSearch(query, { query = it }) }
                        FitIconButton({ requestsOnly = !requestsOnly }, Modifier.padding(start = 8.dp).size(44.dp)
                            .clip(RoundedCornerShape(12.dp)).background(Color.White)) {
                            Icon(painterResource(Res.drawable.workout_filter), "Только заявки", tint = if (requestsOnly) Purple else Ink)
                        }
                    }
                }
                val names = if (requestsOnly) listOf(NewClientName) else listOf("Константин Ильин", "Константин Ильин", NewClientName)
                val visible = names.withIndex().filter { it.value.contains(query, true) && (it.value != NewClientName || requestVisible) }
                if (visible.isEmpty()) item { Text("Чаты не найдены", Modifier.padding(16.dp), color = Muted) }
                visible.forEach { (index, name) -> item(key = "$name$index") {
                    val marina = name == NewClientName
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White)
                        .border(.5.dp, Color(0xFFE0E0E0), RoundedCornerShape(18.dp))
                        .clickable(onClick = if (marina) onRequest else onOtherChat)
                        .padding(horizontal = 12.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                        if (marina) ClientMarinaPhoto() else Image(painterResource(Res.drawable.client_chat_photo), null,
                            Modifier.size(40.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold)
                            Text(if (marina) "Здравствуйте! Я хотела бы начать…" else if (index == 0) "Отчёт от тренировки" else "Запрос о переносе тренировки",
                                fontSize = 14.sp, lineHeight = 18.sp, color = Color(0xFF152820), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("14:40", fontSize = 14.sp, lineHeight = 18.sp, color = Color(0xFF5A6E85))
                            Badge(containerColor = Color(0xFFB3261E), contentColor = Color.White) { Text("1") }
                        }
                    }
                } }
            }
        }
        HomeNavigation(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 10.dp),
            selectedTab = 3, programCount = programCount, onNavigate = onNavigate)
    }
}

@Composable
internal fun ClientMarinaPhoto(modifier: Modifier = Modifier) {
    Box(modifier.size(44.dp).clip(CircleShape).background(Color(0xFFEDA0A8))) {
        Image(painterResource(Res.drawable.client_marina_photo), null,
            Modifier.wrapContentSize(Alignment.TopStart, unbounded = true).offset(x = (-13.84).dp, y = .2.dp)
                .size(68.53.dp, 45.7.dp), contentScale = ContentScale.FillBounds)
    }
}

@Composable
internal fun ClientRequestChatScreen(accepted: Boolean, onBack: () -> Unit, onAccept: () -> Unit,
    onAssign: () -> Unit, onCancel: () -> Unit, onBlock: () -> Unit) {
    var menu by rememberSaveable { mutableStateOf(false) }
    var input by rememberSaveable { mutableStateOf("") }
    var messages by rememberSaveable { mutableStateOf(listOf<String>()) }
    var questionnaire by rememberSaveable { mutableStateOf(false) }
    var notice by rememberSaveable { mutableStateOf<String?>(null) }
    var confirm by rememberSaveable { mutableStateOf<String?>(null) }
    val list = rememberLazyListState()
    LaunchedEffect(messages.size, questionnaire) {
        if (messages.isNotEmpty() || questionnaire) list.animateScrollToItem(3 + messages.size + if (questionnaire) 1 else 0)
    }
    Column(Modifier.fillMaxSize().background(FitScreenBrush).imePadding()) {
        ClientTopBar(NewClientName, "Онлайн", onBack, avatar = true) {
            Box {
                WorkoutIconButton(Res.drawable.client_more, "Действия с запросом", { menu = true }, light = true)
                FitDropdownMenu(menu, { menu = false }) {
                    FitDropdownMenuItem(text = { Text("Отправить анкету") }, onClick = { menu = false; questionnaire = true })
                    if (!accepted) FitDropdownMenuItem(text = { Text("Принять запрос") }, onClick = { menu = false; onAccept() })
                    if (!accepted) FitDropdownMenuItem(text = { Text("Отменить запрос") }, onClick = { menu = false; confirm = "Отменить запрос" })
                    FitDropdownMenuItem(text = { Text("Заблокировать", color = MaterialTheme.colorScheme.error) },
                        onClick = { menu = false; confirm = "Заблокировать пользователя" })
                }
            }
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)).background(Color(0xFFF0F0F0)),
            state = list, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { ClientChatBubble("Здравствуйте!\nЯ хотела бы начать заниматься и немного похудеть. Подскажите, как вы работаете?", "10:30") }
            item { ClientChatBubble("Здравствуйте, Марина!\nРаботаю онлайн: программы тренировок, питание, поддержка в чате. Сначала уточню ваши цели и текущую форму, чтобы подобрать оптимальный план.", "10:32", true) }
            item { ClientChatBubble("Отлично! Моя цель — снизить вес на 5–7 кг, подтянуть фигуру и чувствовать себя энергичнее.", "10:34") }
            item { ClientChatBubble("Отличная цель!\nДавайте я пришлю анкету, чтобы лучше понять ваши привычки и составить план?", "10:35", true, Color(0xFFE5F7D9)) }
            if (questionnaire) item { ClientChatBubble("Анкета клиента\nЦели, опыт тренировок, привычки и ограничения", "Сейчас", true) }
            items(messages.size) { ClientChatBubble(messages[it], "Сейчас", true) }
        }
        Column(Modifier.fillMaxWidth().background(Color.White).navigationBarsPadding()) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF5FFD6)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (accepted) "Марина в списке клиентов" else "Марина ещё не в списке клиентов",
                    fontSize = 14.sp, lineHeight = 17.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                Text(if (accepted) "Теперь вы можете назначить программу тренировок" else "Добавьте пользователя, чтобы начать сопровождение и назначить программу",
                    fontSize = 12.sp, lineHeight = 15.sp, color = Color(0xFF1C1C1F))
                WorkoutAction(if (accepted) "Назначить программу" else "Добавить в клиенты", if (accepted) onAssign else onAccept,
                    outlined = true, compact = true)
            }
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FitIconButton(onClick = { notice = "Прикрепление файлов будет доступно после подключения чата." },
                    modifier = Modifier.size(48.dp).background(Color(0xFFF0F0EF), CircleShape)) {
                    WorkoutIcon(Res.drawable.client_attachment, "Прикрепить файл")
                }
                BasicTextField(input, { input = it.take(2000) }, Modifier.weight(1f).heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(16.dp)).background(Color(0xFFF2F2F2)).padding(12.dp),
                    textStyle = TextStyle(color = Ink, fontSize = 15.sp, lineHeight = 22.sp, fontFamily = fitInter()), maxLines = 4,
                    decorationBox = { inner -> if (input.isEmpty()) Text("Сообщение", color = Muted, fontSize = 15.sp); inner() })
                FitIconButton(onClick = { if (input.isNotBlank()) { messages = messages + input.trim(); input = "" } },
                    enabled = input.isNotBlank(), modifier = Modifier.size(48.dp).background(Color(0xFFC6FF33), CircleShape)) {
                    WorkoutIcon(Res.drawable.client_send, "Отправить сообщение")
                }
            }
        }
    }
    if (notice != null) FitDialog(onDismissRequest = { notice = null }, text = { Text(notice.orEmpty()) },
        confirmButton = { FitTextButton(onClick = { notice = null }) { Text("Понятно") } })
    if (confirm != null) FitDialog(onDismissRequest = { confirm = null }, title = { Text("${confirm}?") },
        text = { Text(if (confirm == "Отменить запрос") "Заявка будет убрана из списка." else "Переписка с Мариной будет скрыта.") },
        confirmButton = { FitTextButton(onClick = { val cancel = confirm == "Отменить запрос"; confirm = null; if (cancel) onCancel() else onBlock() }) { Text("Подтвердить") } },
        dismissButton = { FitTextButton(onClick = { confirm = null }) { Text("Назад") } })
}

@Composable
private fun ClientChatBubble(text: String, time: String, outgoing: Boolean = false, color: Color = Color(0xFFE8DAFB)) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = if (outgoing) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text, Modifier.fillMaxWidth(.82f).clip(RoundedCornerShape(16.dp))
            .background(if (outgoing) color else Color.White).padding(12.dp),
            color = Color(0xFF152820), fontSize = 14.sp, lineHeight = 20.sp)
        Text(time, color = Color(0xFF66716B), fontSize = 10.sp, lineHeight = 12.sp)
    }
}
