package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomKeypad(
    onKeyPress: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyRows = listOf(
        listOf("7", "8", "9", "C"),
        listOf("4", "5", "6", "DEL"),
        listOf("1", "2", "3", "00"),
        listOf("0", ".", "+/-", "SAVE")
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        keyRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    KeypadButton(
                        key = key,
                        modifier = Modifier.weight(1f),
                        onClick = { onKeyPress(key) }
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    key: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isClear = key == "C"
    val isDel = key == "DEL"
    val isAction = key == "SAVE"

    val containerColor = when {
        isClear -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
        isDel -> MaterialTheme.colorScheme.surfaceVariant
        isAction -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when {
        isClear -> MaterialTheme.colorScheme.onErrorContainer
        isDel -> MaterialTheme.colorScheme.onSurfaceVariant
        isAction -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = modifier
            .testTag("keypad_key_$key")
            .height(52.dp),
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isAction) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (isDel) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    tint = contentColor
                )
            } else {
                Text(
                    text = key,
                    color = contentColor,
                    fontSize = if (key.length > 2) 14.sp else 20.sp,
                    fontWeight = if (key.length > 1 || isClear || isAction) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}
