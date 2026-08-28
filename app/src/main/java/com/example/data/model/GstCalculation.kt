package com.example.data.model

data class GstRate(
    val rate: Double,
    val label: String,
    val isCustom: Boolean = false,
    val description: String = ""
)

enum class CalculationMode(val title: String, val subtitle: String) {
    EXCLUSIVE("Add GST (+)", "Tax added on top of base amount"),
    INCLUSIVE("Remove GST (-)", "Tax extracted from total amount")
}

enum class TaxType(val title: String, val description: String) {
    INTRA_STATE("Intra-State", "CGST + SGST (Same State)"),
    INTER_STATE("Inter-State", "IGST (Different State)")
}

data class GstBreakdown(
    val basePrice: Double = 0.0,
    val quantity: Double = 1.0,
    val discountPercent: Double = 0.0,
    val discountAmount: Double = 0.0,
    val netAmount: Double = 0.0,
    val gstRate: Double = 18.0,
    val gstAmount: Double = 0.0,
    val cgstRate: Double = 9.0,
    val cgstAmount: Double = 0.0,
    val sgstRate: Double = 9.0,
    val sgstAmount: Double = 0.0,
    val igstRate: Double = 18.0,
    val igstAmount: Double = 0.0,
    val grossAmount: Double = 0.0,
    val mode: CalculationMode = CalculationMode.EXCLUSIVE,
    val taxType: TaxType = TaxType.INTRA_STATE
)

data class InvoiceItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val unitPrice: Double,
    val quantity: Double = 1.0,
    val unit: String = "pcs",
    val gstRate: Double,
    val isInclusive: Boolean = false,
    val breakdown: GstBreakdown
)

data class GstRateGuideItem(
    val rate: Double,
    val slab: String,
    val colorHex: Long,
    val commonCategories: List<String>,
    val examples: List<String>
)
