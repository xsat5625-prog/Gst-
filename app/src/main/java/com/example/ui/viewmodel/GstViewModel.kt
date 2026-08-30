package com.example.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CustomRateEntity
import com.example.data.local.entity.HistoryEntity
import com.example.data.model.CalculationMode
import com.example.data.model.GstBreakdown
import com.example.data.model.GstRate
import com.example.data.model.GstRateGuideItem
import com.example.data.model.InvoiceItem
import com.example.data.model.TaxType
import com.example.data.repository.GstRepository
import com.example.util.GstCalculatorEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CalculatorUiState(
    val rawInput: String = "1000",
    val selectedRate: Double = 18.0,
    val mode: CalculationMode = CalculationMode.EXCLUSIVE,
    val taxType: TaxType = TaxType.INTRA_STATE,
    val quantity: Double = 1.0,
    val discountPercent: Double = 0.0,
    val note: String = "",
    val partyName: String = "",
    val partyGstin: String = "",
    val showPartySection: Boolean = true,
    val showQuantityDiscountRow: Boolean = false,
    val isCustomRateDialogOpen: Boolean = false,
    val isSaveDialogOpen: Boolean = false
)

data class InvoiceUiState(
    val items: List<InvoiceItem> = emptyList(),
    val customerName: String = "",
    val partyGstin: String = "",
    val invoiceNumber: String = "INV-001",
    val taxType: TaxType = TaxType.INTRA_STATE
)

class GstViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = GstRepository(database.historyDao(), database.customRateDao())

    private val _calcState = MutableStateFlow(CalculatorUiState())
    val calcState: StateFlow<CalculatorUiState> = _calcState.asStateFlow()

    private val _invoiceState = MutableStateFlow(InvoiceUiState())
    val invoiceState: StateFlow<InvoiceUiState> = _invoiceState.asStateFlow()

    val customRates: StateFlow<List<CustomRateEntity>> = repository.allCustomRates
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val historyList: StateFlow<List<HistoryEntity>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Derived full list of rates (Standard + Custom)
    val allRates: StateFlow<List<GstRate>> = combine(
        _calcState,
        customRates
    ) { _, customEntities ->
        val customList = customEntities.map {
            GstRate(
                rate = it.rate,
                label = it.label.ifBlank { "${GstCalculatorEngine.formatNumber(it.rate)}%" },
                isCustom = true,
                description = it.description
            )
        }
        GstCalculatorEngine.DEFAULT_STANDARD_RATES + customList
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GstCalculatorEngine.DEFAULT_STANDARD_RATES
    )

    // Realtime Breakdown
    val currentBreakdown: StateFlow<GstBreakdown> = _calcState.combine(allRates) { state, _ ->
        val amount = state.rawInput.toDoubleOrNull() ?: 0.0
        GstCalculatorEngine.calculate(
            inputAmount = amount,
            rate = state.selectedRate,
            mode = state.mode,
            taxType = state.taxType,
            quantity = state.quantity,
            discountPercent = state.discountPercent,
            partyName = state.partyName,
            partyGstin = state.partyGstin
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GstCalculatorEngine.calculate(1000.0, 18.0, CalculationMode.EXCLUSIVE, TaxType.INTRA_STATE)
    )

    // Rate Guide data
    val rateGuideList: List<GstRateGuideItem> = listOf(
        GstRateGuideItem(
            rate = 0.0,
            slab = "0% (Exempt)",
            colorHex = 0xFF10B981,
            commonCategories = listOf("Fresh Produce", "Grains & Cereals", "Education", "Healthcare"),
            examples = listOf("Fresh fruits & vegetables", "Unbranded milk & eggs", "Curd, Lassi", "Bread, Salt", "School education & healthcare services")
        ),
        GstRateGuideItem(
            rate = 3.0,
            slab = "3% (Special)",
            colorHex = 0xFFF59E0B,
            commonCategories = listOf("Precious Metals", "Jewelry"),
            examples = listOf("Gold & Silver jewelry", "Diamonds and precious stones", "Platinum items")
        ),
        GstRateGuideItem(
            rate = 5.0,
            slab = "5% (Essentials)",
            colorHex = 0xFF06B6D4,
            commonCategories = listOf("Household Goods", "Economy Transport", "Apparel <= ₹1000"),
            examples = listOf("Tea, Coffee, Spices", "Edible oil, Sugar", "Apparel & footwear under ₹1,000", "Life-saving medicines", "Economy railway travel & EV charging")
        ),
        GstRateGuideItem(
            rate = 12.0,
            slab = "12% (Standard I)",
            colorHex = 0xFF3B82F6,
            commonCategories = listOf("Processed Foods", "Computers", "Business Goods"),
            examples = listOf("Butter, Cheese, Ghee", "Frozen meat & processed fruits", "Computers, diagnostic kits", "Apparel & footwear above ₹1,000", "Ayurvedic medicines")
        ),
        GstRateGuideItem(
            rate = 18.0,
            slab = "18% (Standard II - Most Common)",
            colorHex = 0xFF6366F1,
            commonCategories = listOf("Services", "IT & Telecom", "Consumer Electronics", "Restaurants"),
            examples = listOf("Software, IT & Consulting services", "Telecom & broadband", "AC restaurants & hotels", "Hair oil, toothpaste, soap", "Capital goods & industrial intermediaries")
        ),
        GstRateGuideItem(
            rate = 28.0,
            slab = "28% (Luxury & Sin Goods)",
            colorHex = 0xFFEC4899,
            commonCategories = listOf("Automobiles", "Luxury Goods", "High-end Electronics"),
            examples = listOf("Motor vehicles & motorcycles", "Air conditioners, refrigerators", "Tobacco & aerated drinks", "Betting & gaming (where applicable)")
        )
    )

    // Keypad and Input Handling
    fun onKeypadPress(key: String) {
        _calcState.update { current ->
            val cur = current.rawInput
            val updated = when (key) {
                "C" -> "0"
                "DEL" -> if (cur.length > 1) cur.dropLast(1) else "0"
                "." -> if (!cur.contains(".")) "$cur." else cur
                "00" -> if (cur == "0") "0" else if (cur.length < 10) "${cur}00" else cur
                "+/-" -> {
                    val num = cur.toDoubleOrNull() ?: 0.0
                    if (num == 0.0) "0" else (-num).toString()
                }
                else -> {
                    // Digits 0-9
                    if (cur == "0") key else if (cur.length < 11) cur + key else cur
                }
            }
            current.copy(rawInput = updated)
        }
    }

    fun setDirectAmount(amountStr: String) {
        _calcState.update { it.copy(rawInput = amountStr.filter { ch -> ch.isDigit() || ch == '.' }) }
    }

    fun selectRate(rate: Double) {
        _calcState.update { it.copy(selectedRate = rate) }
    }

    fun setCalculationMode(mode: CalculationMode) {
        _calcState.update { it.copy(mode = mode) }
    }

    fun setTaxType(taxType: TaxType) {
        _calcState.update { it.copy(taxType = taxType) }
    }

    fun setQuantity(qty: Double) {
        _calcState.update { it.copy(quantity = qty.coerceAtLeast(1.0)) }
    }

    fun setDiscountPercent(discount: Double) {
        _calcState.update { it.copy(discountPercent = discount.coerceIn(0.0, 100.0)) }
    }

    fun toggleQuantityDiscountRow() {
        _calcState.update { it.copy(showQuantityDiscountRow = !it.showQuantityDiscountRow) }
    }

    fun showCustomRateDialog(show: Boolean) {
        _calcState.update { it.copy(isCustomRateDialogOpen = show) }
    }

    fun showSaveDialog(show: Boolean) {
        _calcState.update { it.copy(isSaveDialogOpen = show) }
    }

    fun setPartyName(name: String) {
        _calcState.update { it.copy(partyName = name) }
    }

    fun setPartyGstin(gstin: String) {
        _calcState.update { current ->
            val formatted = gstin.uppercase(java.util.Locale.getDefault()).take(15)
            current.copy(partyGstin = formatted)
        }
    }

    fun togglePartySection() {
        _calcState.update { it.copy(showPartySection = !it.showPartySection) }
    }

    fun setNote(note: String) {
        _calcState.update { it.copy(note = note) }
    }

    fun addCustomRate(rate: Double, label: String, description: String = "") {
        viewModelScope.launch {
            repository.addCustomRate(rate, label, description)
            selectRate(rate)
            showCustomRateDialog(false)
            Toast.makeText(getApplication(), "Added custom rate: $rate%", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteCustomRate(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomRate(id)
            Toast.makeText(getApplication(), "Custom rate deleted", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveCurrentCalculation(note: String = "", partyName: String = "", partyGstin: String = "") {
        viewModelScope.launch {
            val breakdown = currentBreakdown.value
            val effectivePartyName = if (partyName.isNotBlank()) partyName else _calcState.value.partyName
            val effectivePartyGstin = if (partyGstin.isNotBlank()) partyGstin else _calcState.value.partyGstin
            
            val defaultTitle = if (effectivePartyName.isNotBlank()) {
                "Calc for $effectivePartyName (${GstCalculatorEngine.formatNumber(breakdown.gstRate)}%)"
            } else if (note.isNotBlank()) {
                note
            } else {
                "GST @ ${GstCalculatorEngine.formatNumber(breakdown.gstRate)}%"
            }

            val entity = HistoryEntity(
                title = defaultTitle,
                calculationType = "SINGLE_CALC",
                baseAmount = breakdown.netAmount,
                gstRate = breakdown.gstRate,
                gstAmount = breakdown.gstAmount,
                cgstAmount = breakdown.cgstAmount,
                sgstAmount = breakdown.sgstAmount,
                igstAmount = breakdown.igstAmount,
                totalAmount = breakdown.grossAmount,
                isInclusive = breakdown.mode == CalculationMode.INCLUSIVE,
                isInterState = breakdown.taxType == TaxType.INTER_STATE,
                quantity = breakdown.quantity,
                discountPercent = breakdown.discountPercent,
                notes = note,
                partyName = effectivePartyName,
                partyGstin = effectivePartyGstin
            )
            repository.saveHistory(entity)
            showSaveDialog(false)
            Toast.makeText(getApplication(), "Saved to History", Toast.LENGTH_SHORT).show()
        }
    }

    fun loadFromHistory(history: HistoryEntity) {
        _calcState.update {
            it.copy(
                rawInput = if (history.isInclusive) history.totalAmount.toString() else history.baseAmount.toString(),
                selectedRate = history.gstRate,
                mode = if (history.isInclusive) CalculationMode.INCLUSIVE else CalculationMode.EXCLUSIVE,
                taxType = if (history.isInterState) TaxType.INTER_STATE else TaxType.INTRA_STATE,
                quantity = history.quantity,
                discountPercent = history.discountPercent,
                note = history.notes,
                partyName = history.partyName,
                partyGstin = history.partyGstin,
                showPartySection = history.partyName.isNotBlank() || history.partyGstin.isNotBlank()
            )
        }
        Toast.makeText(getApplication(), "Loaded ${history.title}", Toast.LENGTH_SHORT).show()
    }

    fun deleteHistory(id: Long) {
        viewModelScope.launch {
            repository.deleteHistory(id)
            Toast.makeText(getApplication(), "Record deleted", Toast.LENGTH_SHORT).show()
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            Toast.makeText(getApplication(), "History cleared", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyToClipboard(text: String, label: String = "GST Breakdown") {
        val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(getApplication(), "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    fun shareCalculation(breakdown: GstBreakdown, title: String = "GST Calculation") {
        val customTitle = if (breakdown.partyName.isNotBlank()) "GST Quote for ${breakdown.partyName}" else title
        val text = GstCalculatorEngine.generateShareableText(breakdown, customTitle)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            this.type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, customTitle)
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooserIntent = Intent.createChooser(sendIntent, "Share GST Breakdown").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        getApplication<Application>().startActivity(chooserIntent)
    }

    // --- Multi-Item Invoice Management ---
    fun addItemToInvoice(
        name: String,
        unitPrice: Double,
        quantity: Double = 1.0,
        unit: String = "pcs",
        rate: Double,
        isInclusive: Boolean = false
    ) {
        val mode = if (isInclusive) CalculationMode.INCLUSIVE else CalculationMode.EXCLUSIVE
        val taxType = _invoiceState.value.taxType
        val breakdown = GstCalculatorEngine.calculate(
            inputAmount = unitPrice,
            rate = rate,
            mode = mode,
            taxType = taxType,
            quantity = quantity,
            discountPercent = 0.0
        )
        val newItem = InvoiceItem(
            name = name.ifBlank { "Item ${_invoiceState.value.items.size + 1}" },
            unitPrice = unitPrice,
            quantity = quantity,
            unit = unit,
            gstRate = rate,
            isInclusive = isInclusive,
            breakdown = breakdown
        )
        _invoiceState.update { it.copy(items = it.items + newItem) }
        Toast.makeText(getApplication(), "Added item to invoice", Toast.LENGTH_SHORT).show()
    }

    fun addCurrentCalcToInvoice(itemName: String = "Item") {
        val breakdown = currentBreakdown.value
        val item = InvoiceItem(
            name = if (itemName.isNotBlank()) itemName else "Item ${_invoiceState.value.items.size + 1} (${GstCalculatorEngine.formatNumber(breakdown.gstRate)}%)",
            unitPrice = breakdown.basePrice,
            quantity = breakdown.quantity,
            gstRate = breakdown.gstRate,
            isInclusive = breakdown.mode == CalculationMode.INCLUSIVE,
            breakdown = breakdown
        )
        _invoiceState.update { it.copy(items = it.items + item) }
        Toast.makeText(getApplication(), "Added to Quick Bill", Toast.LENGTH_SHORT).show()
    }

    fun removeInvoiceItem(id: String) {
        _invoiceState.update { it.copy(items = it.items.filterNot { item -> item.id == id }) }
    }

    fun clearInvoice() {
        _invoiceState.update { it.copy(items = emptyList()) }
        Toast.makeText(getApplication(), "Invoice cleared", Toast.LENGTH_SHORT).show()
    }

    fun setInvoiceTaxType(taxType: TaxType) {
        _invoiceState.update { current ->
            val updatedItems = current.items.map { item ->
                val newBreakdown = GstCalculatorEngine.calculate(
                    inputAmount = item.unitPrice,
                    rate = item.gstRate,
                    mode = if (item.isInclusive) CalculationMode.INCLUSIVE else CalculationMode.EXCLUSIVE,
                    taxType = taxType,
                    quantity = item.quantity
                )
                item.copy(breakdown = newBreakdown)
            }
            current.copy(taxType = taxType, items = updatedItems)
        }
    }

    fun setCustomerName(name: String) {
        _invoiceState.update { it.copy(customerName = name) }
    }

    fun setInvoicePartyGstin(gstin: String) {
        _invoiceState.update { current ->
            val formatted = gstin.uppercase(java.util.Locale.getDefault()).take(15)
            current.copy(partyGstin = formatted)
        }
    }

    fun setInvoiceNumber(number: String) {
        _invoiceState.update { it.copy(invoiceNumber = number) }
    }

    fun saveInvoiceToHistory() {
        val state = _invoiceState.value
        if (state.items.isEmpty()) return

        val totalNet = state.items.sumOf { it.breakdown.netAmount }
        val totalGst = state.items.sumOf { it.breakdown.gstAmount }
        val totalCgst = state.items.sumOf { it.breakdown.cgstAmount }
        val totalSgst = state.items.sumOf { it.breakdown.sgstAmount }
        val totalIgst = state.items.sumOf { it.breakdown.igstAmount }
        val totalGross = state.items.sumOf { it.breakdown.grossAmount }

        val summary = state.items.joinToString("; ") { "${it.name} x${GstCalculatorEngine.formatNumber(it.quantity)} (${it.gstRate}%): ${GstCalculatorEngine.formatCurrency(it.breakdown.grossAmount)}" }

        viewModelScope.launch {
            val entity = HistoryEntity(
                title = if (state.customerName.isNotBlank()) "Bill for ${state.customerName}" else "Quick Bill (${state.items.size} items)",
                calculationType = "INVOICE",
                baseAmount = totalNet,
                gstRate = 0.0, // Multi-rate
                gstAmount = totalGst,
                cgstAmount = totalCgst,
                sgstAmount = totalSgst,
                igstAmount = totalIgst,
                totalAmount = totalGross,
                isInclusive = false,
                isInterState = state.taxType == TaxType.INTER_STATE,
                notes = "Invoice #${state.invoiceNumber}",
                itemsSummary = summary,
                partyName = state.customerName,
                partyGstin = state.partyGstin
            )
            repository.saveHistory(entity)
            Toast.makeText(getApplication(), "Invoice saved to History", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareInvoice() {
        val state = _invoiceState.value
        if (state.items.isEmpty()) return

        val totalNet = state.items.sumOf { it.breakdown.netAmount }
        val totalGst = state.items.sumOf { it.breakdown.gstAmount }
        val totalGross = state.items.sumOf { it.breakdown.grossAmount }

        val sb = StringBuilder()
        sb.appendLine("🧾 INVOICE / BILL SUMMARY")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━")
        if (state.invoiceNumber.isNotBlank()) sb.appendLine("Invoice No: ${state.invoiceNumber}")
        if (state.customerName.isNotBlank()) sb.appendLine("Party / Customer: ${state.customerName}")
        if (state.partyGstin.isNotBlank()) {
            val stateName = GstCalculatorEngine.getStateFromGstin(state.partyGstin)
            val stateSuffix = if (stateName != null) " ($stateName)" else ""
            sb.appendLine("Party GSTIN: ${state.partyGstin}$stateSuffix")
        }
        sb.appendLine("Tax Mode: ${state.taxType.title}")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("ITEMS:")
        state.items.forEachIndexed { idx, item ->
            sb.appendLine("${idx + 1}. ${item.name}")
            sb.appendLine("   Qty: ${GstCalculatorEngine.formatNumber(item.quantity)} ${item.unit} @ ${GstCalculatorEngine.formatCurrency(item.unitPrice)}")
            sb.appendLine("   GST: ${GstCalculatorEngine.formatNumber(item.gstRate)}% (${GstCalculatorEngine.formatCurrency(item.breakdown.gstAmount)}) | Subtotal: ${GstCalculatorEngine.formatCurrency(item.breakdown.grossAmount)}")
        }
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("Total Net (Taxable): ${GstCalculatorEngine.formatCurrency(totalNet)}")
        sb.appendLine("Total GST: ${GstCalculatorEngine.formatCurrency(totalGst)}")
        if (state.taxType == TaxType.INTRA_STATE) {
            val totalCgst = state.items.sumOf { it.breakdown.cgstAmount }
            val totalSgst = state.items.sumOf { it.breakdown.sgstAmount }
            sb.appendLine(" • Total CGST: ${GstCalculatorEngine.formatCurrency(totalCgst)}")
            sb.appendLine(" • Total SGST: ${GstCalculatorEngine.formatCurrency(totalSgst)}")
        } else {
            val totalIgst = state.items.sumOf { it.breakdown.igstAmount }
            sb.appendLine(" • Total IGST: ${GstCalculatorEngine.formatCurrency(totalIgst)}")
        }
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("💰 GRAND TOTAL: ${GstCalculatorEngine.formatCurrency(totalGross)}")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("Thank you for your business!")

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            this.type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Invoice ${state.invoiceNumber}")
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooserIntent = Intent.createChooser(sendIntent, "Share Invoice").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        getApplication<Application>().startActivity(chooserIntent)
    }
}
