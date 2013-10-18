package com.braintreegateway

import _root_.org.junit.runner.RunWith
import _root_.org.mockito.Mockito.when
import _root_.org.scalatest.FunSpec
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.scalatest.matchers.MustMatchers
import _root_.org.scalatest.mock.MockitoSugar
import testhelpers.CalendarHelper
import com.braintreegateway.util.NodeWrapper

@RunWith(classOf[JUnitRunner])
class DisbursementDetailsSpec extends FunSpec with MustMatchers with MockitoSugar {
  describe("isValid") {
    it("is true when disbursement date is present") {
      val wrapper = mock[NodeWrapper]
      val disbursementDate = CalendarHelper.date("2013-04-10")
      when(wrapper.findDateOpt("disbursement-date")).thenReturn(Some(disbursementDate))

      val detail = new DisbursementDetails(wrapper)

      detail must be('valid)
    }

    it("is false when disbursement date is absent") {
      val wrapper = mock[NodeWrapper]
      when(wrapper.findDateOpt("disbursement-date")).thenReturn(None)

      val detail = new DisbursementDetails(wrapper)

      detail must not be ('valid)
    }
  }
}