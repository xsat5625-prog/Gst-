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
}
