package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AddCustomRateDialog(
    onDismiss: () -> Unit,
    onConfirm: (rate: Double, label: String, description: String) -> Unit
) {
    var rateInput by remember { mutableStateOf("") }
    var labelInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Custom Tax Rate",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Enter a custom GST rate percentage for your specific product or business category (e.g. 0.25%, 1.5%, 6%, etc.).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = rateInput,
                    onValueChange = {
                        rateInput = it
                        if (errorText != null) errorText = null
                    },
                    label = { Text("Tax Rate (%)") },
                    placeholder = { Text("e.g. 1.5 or 6") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Percent, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = errorText != null,
                    supportingText = errorText?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_rate_input")
                )

                OutlinedTextField(
                    value = labelInput,
                    onValueChange = { labelInput = it },
                    label = { Text("Label (Optional)") },
                    placeholder = { Text("e.g. Rough Diamonds, Solar") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descInput,
                    onValueChange = { descInput = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("e.g. Special concessional rate") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rate = rateInput.toDoubleOrNull()
                    if (rate == null || rate < 0.0 || rate > 100.0) {
                        errorText = "Please enter a valid rate between 0 and 100"
                    } else {
                        val finalLabel = if (labelInput.isNotBlank()) labelInput else "$rate%"
                        onConfirm(rate, finalLabel, descInput)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_custom_rate_button")
            ) {
                Text("Add Rate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun SaveCalculationDialog(
    initialNote: String,
    initialPartyName: String = "",
    initialPartyGstin: String = "",
    onDismiss: () -> Unit,
    onConfirm: (note: String, partyName: String, partyGstin: String) -> Unit
) {
    var note by remember { mutableStateOf(initialNote) }
    var partyName by remember { mutableStateOf(initialPartyName) }
    var partyGstin by remember { mutableStateOf(initialPartyGstin) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Save Calculation",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Save this calculation with party info or note for quick reference later.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = partyName,
                    onValueChange = { partyName = it },
                    label = { Text("Party Name / Client (Optional)") },
                    placeholder = { Text("e.g. Apex Enterprises") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_party_name_input")
                )

                OutlinedTextField(
                    value = partyGstin,
                    onValueChange = { partyGstin = it.uppercase(java.util.Locale.getDefault()).take(15) },
                    label = { Text("Party GSTIN / GST No. (Optional)") },
                    placeholder = { Text("e.g. 27AAPCA1234F1Z5") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_party_gstin_input")
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Description") },
                    placeholder = { Text("e.g. AC Repair quote") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_note_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(note, partyName, partyGstin) },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirm_save_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
