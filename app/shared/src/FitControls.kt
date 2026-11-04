package com.example.testmmp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

/** Project controls use the geometry of FitTrack's Figma kit, independent of Material defaults. */
@Composable
internal fun FitIconButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    content: @Composable () -> Unit) {
    Box(modifier.size(48.dp).clip(CircleShape).clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center) { content() }
}

@Composable
internal fun FitTextButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    content: @Composable RowScope.() -> Unit) {
    Row(modifier.clip(RoundedCornerShape(10.dp)).clickable(enabled = enabled, role = Role.Button, onClick = onClick)
        .padding(contentPadding), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center) {
        ProvideTextStyle(TextStyle(color = if (enabled) Ink else Muted, fontSize = 14.sp,
            lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, fontFamily = MaterialTheme.typography.bodyMedium.fontFamily)) { content() }
    }
}

@Composable
internal fun FitOutlinedButton(onClick: () -> Unit, modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    shape: Shape = RoundedCornerShape(10.dp), content: @Composable RowScope.() -> Unit) {
    Row(modifier.heightIn(min = 34.dp).clip(shape).border(1.dp, Ink, shape)
        .clickable(role = Role.Button, onClick = onClick).padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, content = content)
}

@Composable
internal fun FitTextField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier,
    label: (@Composable () -> Unit)? = null, placeholder: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null, trailingIcon: (@Composable () -> Unit)? = null,
    supportingText: (@Composable () -> Unit)? = null, singleLine: Boolean = true,
    readOnly: Boolean = false, isError: Boolean = false, shape: Shape = RoundedCornerShape(12.dp),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    fontSize: TextUnit = 16.sp, fontWeight: FontWeight = FontWeight.Normal) {
    var focused by remember { mutableStateOf(false) }
    val fieldFont = fitInter()
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (label != null) ProvideTextStyle(TextStyle(fontSize = 14.sp, lineHeight = 18.sp, color = Ink,
            fontFamily = MaterialTheme.typography.bodyMedium.fontFamily)) { label() }
        BasicTextField(value, onValueChange, Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused }
            .semantics { if (isError) error("Проверьте значение") },
            singleLine = singleLine, readOnly = readOnly, keyboardOptions = keyboardOptions,
            cursorBrush = SolidColor(Purple),
            textStyle = TextStyle(fontSize = fontSize, lineHeight = 20.sp, fontWeight = fontWeight, color = Ink,
                fontFamily = fieldFont),
            decorationBox = { inner ->
                Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).clip(shape).background(Color.White)
                    .border(if (focused || isError) 1.5.dp else 1.dp,
                        if (isError) Color(0xFFB3261E) else if (focused) Purple else Color(0xFFBDBDBD), shape)
                    .padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    leadingIcon?.invoke()
                    Box(Modifier.weight(1f)) {
                        if (value.isEmpty() && placeholder != null) ProvideTextStyle(TextStyle(color = Muted,
                            fontSize = 16.sp, fontFamily = MaterialTheme.typography.bodyMedium.fontFamily)) { placeholder() }
                        inner()
                    }
                    trailingIcon?.let { Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) { it() } }
                }
            })
        if (supportingText != null) ProvideTextStyle(TextStyle(fontSize = 12.sp, lineHeight = 16.sp,
            color = if (isError) Color(0xFFB3261E) else Muted,
            fontFamily = MaterialTheme.typography.bodyMedium.fontFamily)) { supportingText() }
    }
}

@Composable
internal fun FitCheckbox(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, modifier: Modifier = Modifier) {
    Box(modifier.size(24.dp).semantics { toggleableState = if (checked)
        androidx.compose.ui.state.ToggleableState.On else androidx.compose.ui.state.ToggleableState.Off }
        .clickable(enabled = onCheckedChange != null, role = Role.Checkbox) { onCheckedChange?.invoke(!checked) },
        contentAlignment = Alignment.Center) {
        Box(Modifier.size(22.dp).clip(CircleShape).background(if (checked) Color(0xFF2B2D33) else Color.White)
            .border(1.5.dp, if (checked) Color(0xFF2B2D33) else Color(0xFFDDDDDD), CircleShape),
            contentAlignment = Alignment.Center) {
            if (checked) Text("✓", color = Color.White, fontSize = 15.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun FitSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.size(52.dp, 32.dp).clip(CircleShape).background(if (checked) Purple else Color(0xFFDDDDDD))
        .clickable(role = Role.Switch) { onCheckedChange(!checked) }.semantics {
            toggleableState = if (checked) androidx.compose.ui.state.ToggleableState.On else androidx.compose.ui.state.ToggleableState.Off
        }.padding(4.dp)) {
        Box(Modifier.align(if (checked) Alignment.CenterEnd else Alignment.CenterStart).size(24.dp).background(Color.White, CircleShape))
    }
}

@Composable
internal fun FitDialog(onDismissRequest: () -> Unit, confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier, dismissButton: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null, text: (@Composable () -> Unit)? = null) {
    Dialog(onDismissRequest, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(modifier.padding(24.dp).widthIn(max = 375.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(Color.White).imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (title != null) ProvideTextStyle(MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp,
                lineHeight = 28.sp, fontWeight = FontWeight.SemiBold, color = Ink)) { title() }
            if (text != null) ProvideTextStyle(MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp,
                lineHeight = 20.sp, color = Ink)) { text() }
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                confirmButton()
                dismissButton?.invoke()
            }
        }
    }
}

@Composable
internal fun FitDropdownMenu(expanded: Boolean, onDismissRequest: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    if (expanded) Popup(alignment = Alignment.TopEnd, onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true)) {
        Column(Modifier.widthIn(min = 200.dp, max = 300.dp).clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF7F7F7)).border(.5.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .padding(vertical = 4.dp), content = content)
    }
}

@Composable
internal fun FitDropdownMenuItem(text: @Composable () -> Unit, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick).padding(16.dp, 12.dp)) {
        ProvideTextStyle(MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp, color = Ink)) { text() }
    }
}

@Composable
internal fun FitActionFooter(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().background(Color.White).padding(top = 4.dp)) {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
            .background(Page).padding(16.dp, 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}
