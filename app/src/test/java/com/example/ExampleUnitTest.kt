package com.example

import com.example.data.model.CalculationMode
import com.example.data.model.TaxType
import com.example.util.GstCalculatorEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testGstCalculation_exclusive_intraState() {
    val breakdown = GstCalculatorEngine.calculate(
      inputAmount = 1000.0,
      rate = 18.0,
      mode = CalculationMode.EXCLUSIVE,
      taxType = TaxType.INTRA_STATE,
      quantity = 1.0,
      discountPercent = 0.0
    )

    assertEquals(1000.0, breakdown.netAmount, 0.01)
    assertEquals(180.0, breakdown.gstAmount, 0.01)
    assertEquals(90.0, breakdown.cgstAmount, 0.01)
    assertEquals(90.0, breakdown.sgstAmount, 0.01)
    assertEquals(0.0, breakdown.igstAmount, 0.01)
    assertEquals(1180.0, breakdown.grossAmount, 0.01)
  }

  @Test
  fun testGstCalculation_inclusive_interState() {
    val breakdown = GstCalculatorEngine.calculate(
      inputAmount = 1180.0,
      rate = 18.0,
      mode = CalculationMode.INCLUSIVE,
      taxType = TaxType.INTER_STATE,
      quantity = 1.0,
      discountPercent = 0.0
    )

    assertEquals(1000.0, breakdown.netAmount, 0.01)
    assertEquals(180.0, breakdown.gstAmount, 0.01)
    assertEquals(180.0, breakdown.igstAmount, 0.01)
    assertEquals(0.0, breakdown.cgstAmount, 0.01)
    assertEquals(0.0, breakdown.sgstAmount, 0.01)
    assertEquals(1180.0, breakdown.grossAmount, 0.01)
  }

  @Test
  fun testGstCalculation_withQuantityAndDiscount() {
    val breakdown = GstCalculatorEngine.calculate(
      inputAmount = 500.0,
      rate = 12.0,
      mode = CalculationMode.EXCLUSIVE,
      taxType = TaxType.INTRA_STATE,
      quantity = 2.0, // 1000
      discountPercent = 10.0 // 100 discount -> 900 net
    )

    assertEquals(900.0, breakdown.netAmount, 0.01)
    assertEquals(108.0, breakdown.gstAmount, 0.01)
    assertEquals(54.0, breakdown.cgstAmount, 0.01)
    assertEquals(54.0, breakdown.sgstAmount, 0.01)
    assertEquals(1008.0, breakdown.grossAmount, 0.01)
  }

  @Test
  fun testGstin_stateLookupAndValidation() {
    val stateMaharashtra = GstCalculatorEngine.getStateFromGstin("27AAPCA1234F1Z5")
    assertEquals("Maharashtra", stateMaharashtra)

    val stateDelhi = GstCalculatorEngine.getStateFromGstin("07AAAAA0000A1Z5")
    assertEquals("Delhi", stateDelhi)

    val stateKarnataka = GstCalculatorEngine.getStateFromGstin("29BBBBB1111B1Z2")
    assertEquals("Karnataka", stateKarnataka)

    assertEquals(true, GstCalculatorEngine.isValidGstinFormat("27AAPCA1234F1Z5"))
    assertEquals(false, GstCalculatorEngine.isValidGstinFormat("INVALID_GSTIN"))
  }

  @Test
  fun testGstCalculation_withPartyDetails() {
    val breakdown = GstCalculatorEngine.calculate(
      inputAmount = 1000.0,
      rate = 18.0,
      mode = CalculationMode.EXCLUSIVE,
      taxType = TaxType.INTRA_STATE,
      partyName = "Apex Solutions",
      partyGstin = "27AAPCA1234F1Z5"
    )

    assertEquals("Apex Solutions", breakdown.partyName)
    assertEquals("27AAPCA1234F1Z5", breakdown.partyGstin)

    val shareText = GstCalculatorEngine.generateShareableText(breakdown)
    assert(shareText.contains("Apex Solutions"))
    assert(shareText.contains("27AAPCA1234F1Z5"))
    assert(shareText.contains("Maharashtra"))
  }
}
