package com.braintreegateway

import _root_.org.mockito.Mockito.when
import _root_.org.scalatest.FunSpec
import _root_.org.scalatest.matchers.MustMatchers
import _root_.org.scalatest.mock.MockitoSugar
import com.braintreegateway.testhelpers.CalendarTestUtils
import com.braintreegateway.util.NodeWrapper

class DisbursementDetailsSpec extends FunSpec with MustMatchers with MockitoSugar {
  describe("isValid") {
    it("is true when disbursement date is present") {
      val wrapper = mock[NodeWrapper]
      val disbursementDate = CalendarTestUtils.date("2013-04-10")
      when(wrapper.findDate("disbursement-date")).thenReturn(disbursementDate)

      val detail = new DisbursementDetails(wrapper)

      detail must be('valid)
    }

    it("is false when disbursement date is absent") {
      val wrapper = mock[NodeWrapper]
      when(wrapper.findDate("disbursement-date")).thenReturn(null)

      val detail = new DisbursementDetails(wrapper)

      detail must not be ('valid)
    }
  }
}