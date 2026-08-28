package com.example.util

import com.example.data.model.CalculationMode
import com.example.data.model.GstBreakdown
import com.example.data.model.GstRate
import com.example.data.model.TaxType
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.round

object GstCalculatorEngine {

    val DEFAULT_STANDARD_RATES = listOf(
        GstRate(0.0, "0%", description = "Exempt goods, fresh food, grains"),
        GstRate(3.0, "3%", description = "Gold, silver, precious jewelry"),
        GstRate(5.0, "5%", description = "Household essentials, tea, spices, sugar"),
        GstRate(12.0, "12%", description = "Processed foods, computers, medicine"),
        GstRate(18.0, "18%", description = "Most goods & services, telecom, software"),
        GstRate(28.0, "28%", description = "Automobiles, luxury goods, air conditioners")
    )

    private val currencyFormatter: DecimalFormat = DecimalFormat("₹#,##,##0.00").apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }

    private val compactFormatter: DecimalFormat = DecimalFormat("#,##,##0.##")

    fun formatCurrency(amount: Double): String {
        if (amount.isNaN() || amount.isInfinite()) return "₹0.00"
        return currencyFormatter.format(amount)
    }

    fun formatNumber(amount: Double): String {
        if (amount.isNaN() || amount.isInfinite()) return "0"
        return compactFormatter.format(amount)
    }

    fun roundToTwoDecimals(value: Double): Double {
        return round(value * 100.0) / 100.0
    }

    fun calculate(
        inputAmount: Double,
        rate: Double,
        mode: CalculationMode,
        taxType: TaxType,
        quantity: Double = 1.0,
        discountPercent: Double = 0.0
    ): GstBreakdown {
        val qty = if (quantity <= 0.0) 1.0 else quantity
        val discPercent = discountPercent.coerceIn(0.0, 100.0)

        val rawAmount = inputAmount * qty
        val discountAmount = rawAmount * (discPercent / 100.0)
        val discountedInput = (rawAmount - discountAmount).coerceAtLeast(0.0)

        val netAmount: Double
        val gstAmount: Double
        val grossAmount: Double

        if (mode == CalculationMode.EXCLUSIVE) {
            // Add GST: discountedInput is Base/Net Price
            netAmount = roundToTwoDecimals(discountedInput)
            gstAmount = roundToTwoDecimals(netAmount * (rate / 100.0))
            grossAmount = roundToTwoDecimals(netAmount + gstAmount)
        } else {
            // Remove GST: discountedInput is Gross Price
            grossAmount = roundToTwoDecimals(discountedInput)
            netAmount = if (rate > -100.0) {
                roundToTwoDecimals(grossAmount / (1.0 + (rate / 100.0)))
            } else {
                grossAmount
            }
            gstAmount = roundToTwoDecimals(grossAmount - netAmount)
        }

        val cgstRate = if (taxType == TaxType.INTRA_STATE) rate / 2.0 else 0.0
        val sgstRate = if (taxType == TaxType.INTRA_STATE) rate / 2.0 else 0.0
        val igstRate = if (taxType == TaxType.INTER_STATE) rate else 0.0

        val cgstAmount = if (taxType == TaxType.INTRA_STATE) roundToTwoDecimals(gstAmount / 2.0) else 0.0
        val sgstAmount = if (taxType == TaxType.INTRA_STATE) roundToTwoDecimals(gstAmount - cgstAmount) else 0.0
        val igstAmount = if (taxType == TaxType.INTER_STATE) gstAmount else 0.0

        return GstBreakdown(
            basePrice = inputAmount,
            quantity = qty,
            discountPercent = discPercent,
            discountAmount = roundToTwoDecimals(discountAmount),
            netAmount = netAmount,
            gstRate = rate,
            gstAmount = gstAmount,
            cgstRate = cgstRate,
            cgstAmount = cgstAmount,
            sgstRate = sgstRate,
            sgstAmount = sgstAmount,
            igstRate = igstRate,
            igstAmount = igstAmount,
            grossAmount = grossAmount,
            mode = mode,
            taxType = taxType
        )
    }

    fun generateShareableText(breakdown: GstBreakdown, title: String = "GST Calculation"): String {
        val sb = StringBuilder()
        sb.appendLine("🧾 $title")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("Type: ${if (breakdown.mode == CalculationMode.EXCLUSIVE) "GST Added (Exclusive)" else "GST Extracted (Inclusive)"}")
        sb.appendLine("Tax Mode: ${breakdown.taxType.title}")
        if (breakdown.quantity > 1.0) {
            sb.appendLine("Unit Price: ${formatCurrency(breakdown.basePrice)} × ${formatNumber(breakdown.quantity)}")
        }
        if (breakdown.discountPercent > 0.0) {
            sb.appendLine("Discount (${formatNumber(breakdown.discountPercent)}%): -${formatCurrency(breakdown.discountAmount)}")
        }
        sb.appendLine("Net (Base) Amount: ${formatCurrency(breakdown.netAmount)}")
        sb.appendLine("GST Rate: ${formatNumber(breakdown.gstRate)}%")
        
        if (breakdown.taxType == TaxType.INTRA_STATE) {
            sb.appendLine(" • CGST (${formatNumber(breakdown.cgstRate)}%): ${formatCurrency(breakdown.cgstAmount)}")
            sb.appendLine(" • SGST (${formatNumber(breakdown.sgstRate)}%): ${formatCurrency(breakdown.sgstAmount)}")
        } else {
            sb.appendLine(" • IGST (${formatNumber(breakdown.igstRate)}%): ${formatCurrency(breakdown.igstAmount)}")
        }
        sb.appendLine("Total GST: ${formatCurrency(breakdown.gstAmount)}")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("💰 Total Amount: ${formatCurrency(breakdown.grossAmount)}")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("Generated via GST Calculator")
        return sb.toString()
    }
}
