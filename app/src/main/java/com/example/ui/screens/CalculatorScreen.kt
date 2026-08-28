package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CalculationMode
import com.example.data.model.TaxType
import com.example.ui.components.AddCustomRateDialog
import com.example.ui.components.BreakdownCard
import com.example.ui.components.CustomKeypad
import com.example.ui.components.SaveCalculationDialog
import com.example.ui.components.TaxRateChipRow
import com.example.ui.theme.PolishInputContainer
import com.example.ui.viewmodel.GstViewModel
import com.example.util.GstCalculatorEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: GstViewModel,
    onNavigateToInvoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val calcState by viewModel.calcState.collectAsStateWithLifecycle()
    val allRates by viewModel.allRates.collectAsStateWithLifecycle()
    val breakdown by viewModel.currentBreakdown.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Mode Selector Tab (Add GST vs Remove GST) in Pill Shape
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val isExclusive = calcState.mode == CalculationMode.EXCLUSIVE

                val exclusiveBg by animateColorAsState(
                    targetValue = if (isExclusive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    label = "exBg"
                )
                val exclusiveText by animateColorAsState(
                    targetValue = if (isExclusive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "exText"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(exclusiveBg)
                        .clickable { viewModel.setCalculationMode(CalculationMode.EXCLUSIVE) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Add GST (+)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isExclusive) FontWeight.Bold else FontWeight.Medium,
                        color = exclusiveText
                    )
                }

                val inclusiveBg by animateColorAsState(
                    targetValue = if (!isExclusive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    label = "incBg"
                )
                val inclusiveText by animateColorAsState(
                    targetValue = if (!isExclusive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "incText"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(inclusiveBg)
                        .clickable { viewModel.setCalculationMode(CalculationMode.INCLUSIVE) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Remove GST (-)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (!isExclusive) FontWeight.Bold else FontWeight.Medium,
                        color = inclusiveText
                    )
                }
            }
        }

        // Amount Display Component matching the Professional Polish M3 Theme
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (calcState.mode == CalculationMode.EXCLUSIVE) "Amount (Excl. Tax)" else "Amount (Incl. Tax)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Intra vs Inter state toggle pill
                Surface(
                    onClick = {
                        val nextType = if (calcState.taxType == TaxType.INTRA_STATE) TaxType.INTER_STATE else TaxType.INTRA_STATE
                        viewModel.setTaxType(nextType)
                    },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.testTag("toggle_tax_type_button")
                ) {
                    Text(
                        text = if (calcState.taxType == TaxType.INTRA_STATE) "Intra-State (CGST+SGST)" else "Inter-State (IGST)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Input Container with M3 filled styling and vibrant accent border
            val amountDouble = calcState.rawInput.toDoubleOrNull() ?: 0.0
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
                    )
                    .testTag("amount_display_card")
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "₹",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = if (calcState.rawInput == "0" || calcState.rawInput.isEmpty()) "0.00" else GstCalculatorEngine.formatNumber(amountDouble),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("formatted_amount_text")
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Amount",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Accent bottom border
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }

        // Tax Rate Selector Strip
        TaxRateChipRow(
            rates = allRates,
            selectedRate = calcState.selectedRate,
            onRateSelected = { viewModel.selectRate(it) },
            onAddCustomRateClick = { viewModel.showCustomRateDialog(true) }
        )

        // Expandable Quantity & Discount Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = { viewModel.toggleQuantityDiscountRow() },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier.testTag("toggle_qty_discount_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Quantity & Discount",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (calcState.quantity > 1.0 || calcState.discountPercent > 0.0) 
                            "Qty: ${GstCalculatorEngine.formatNumber(calcState.quantity)} | Disc: ${GstCalculatorEngine.formatNumber(calcState.discountPercent)}%" 
                        else 
                            "+ Qty & Discount",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (calcState.showQuantityDiscountRow) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Optional Quantity & Discount Controls
        AnimatedVisibility(
            visible = calcState.showQuantityDiscountRow,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quantity control
                    Column {
                        Text(
                            text = "Quantity",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                onClick = { viewModel.setQuantity((calcState.quantity - 1.0).coerceAtLeast(1.0)) },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                }
                            }
                            Text(
                                text = GstCalculatorEngine.formatNumber(calcState.quantity),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(36.dp),
                                textAlign = TextAlign.Center
                            )
                            Surface(
                                onClick = { viewModel.setQuantity(calcState.quantity + 1.0) },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // Discount Control
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Discount %",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(0.0, 5.0, 10.0, 20.0).forEach { disc ->
                                val isSelected = calcState.discountPercent == disc
                                Surface(
                                    onClick = { viewModel.setDiscountPercent(disc) },
                                    shape = CircleShape,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Text(
                                        text = "${disc.toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Breakdown Card
        BreakdownCard(
            breakdown = breakdown,
            onCopyClick = {
                val text = GstCalculatorEngine.generateShareableText(breakdown)
                viewModel.copyToClipboard(text)
            },
            onShareClick = {
                viewModel.shareCalculation(breakdown)
            },
            onSaveClick = {
                viewModel.showSaveDialog(true)
            },
            onAddToInvoiceClick = {
                viewModel.addCurrentCalcToInvoice()
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Keypad for direct numerical entry
        CustomKeypad(
            onKeyPress = { key ->
                if (key == "SAVE") {
                    viewModel.showSaveDialog(true)
                } else {
                    viewModel.onKeypadPress(key)
                }
            }
        )
    }

    // Dialogs
    if (calcState.isCustomRateDialogOpen) {
        AddCustomRateDialog(
            onDismiss = { viewModel.showCustomRateDialog(false) },
            onConfirm = { rate, label, desc ->
                viewModel.addCustomRate(rate, label, desc)
            }
        )
    }

    if (calcState.isSaveDialogOpen) {
        SaveCalculationDialog(
            initialNote = calcState.note,
            onDismiss = { viewModel.showSaveDialog(false) },
            onConfirm = { note ->
                viewModel.saveCurrentCalculation(note)
            }
        )
    }
}
