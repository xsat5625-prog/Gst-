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

    val GST_STATE_CODES = mapOf(
        "01" to "Jammu & Kashmir",
        "02" to "Himachal Pradesh",
        "03" to "Punjab",
        "04" to "Chandigarh",
        "05" to "Uttarakhand",
        "06" to "Haryana",
        "07" to "Delhi",
        "08" to "Rajasthan",
        "09" to "Uttar Pradesh",
        "10" to "Bihar",
        "11" to "Sikkim",
        "12" to "Arunachal Pradesh",
        "13" to "Nagaland",
        "14" to "Manipur",
        "15" to "Mizoram",
        "16" to "Tripura",
        "17" to "Meghalaya",
        "18" to "Assam",
        "19" to "West Bengal",
        "20" to "Jharkhand",
        "21" to "Odisha",
        "22" to "Chhattisgarh",
        "23" to "Madhya Pradesh",
        "24" to "Gujarat",
        "26" to "Dadra and Nagar Haveli and Daman and Diu",
        "27" to "Maharashtra",
        "29" to "Karnataka",
        "30" to "Goa",
        "31" to "Lakshadweep",
        "32" to "Kerala",
        "33" to "Tamil Nadu",
        "34" to "Puducherry",
        "35" to "Andaman & Nicobar Islands",
        "36" to "Telangana",
        "37" to "Andhra Pradesh",
        "38" to "Ladakh"
    )

    fun getStateFromGstin(gstin: String): String? {
        val cleanGstin = gstin.trim()
        if (cleanGstin.length >= 2) {
            val code = cleanGstin.substring(0, 2)
            return GST_STATE_CODES[code]
        }
        return null
    }

    fun isValidGstinFormat(gstin: String): Boolean {
        val cleanGstin = gstin.trim().uppercase(Locale.getDefault())
        // 15-character GSTIN regex: 2 digits + 5 alpha + 4 digits + 1 alpha + 1 char (1-9/A-Z) + 'Z' + 1 char
        val gstinRegex = Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$")
        return cleanGstin.matches(gstinRegex)
    }

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
        discountPercent: Double = 0.0,
        partyName: String = "",
        partyGstin: String = ""
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
            taxType = taxType,
            partyName = partyName.trim(),
            partyGstin = partyGstin.trim().uppercase(Locale.getDefault())
        )
    }

    fun generateShareableText(breakdown: GstBreakdown, title: String = "GST Calculation"): String {
        val sb = StringBuilder()
        sb.appendLine("🧾 $title")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━")
        if (breakdown.partyName.isNotBlank()) {
            sb.appendLine("🏢 Party: ${breakdown.partyName}")
        }
        if (breakdown.partyGstin.isNotBlank()) {
            val stateName = getStateFromGstin(breakdown.partyGstin)
            val stateSuffix = if (stateName != null) " ($stateName)" else ""
            sb.appendLine("🆔 GSTIN: ${breakdown.partyGstin}$stateSuffix")
        }
        if (breakdown.partyName.isNotBlank() || breakdown.partyGstin.isNotBlank()) {
            sb.appendLine("───────────────────")
        }
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
