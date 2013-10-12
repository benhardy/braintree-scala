package com.braintreegateway

import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.FunSpec
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.scalatest.matchers.MustMatchers

@RunWith(classOf[JUnitRunner])
class ValidationErrorCodeSpec extends FunSpec with MustMatchers {
  describe("findByCode") {
    it("happy case") {
      val code = ValidationErrorCode.findByCode("81801")
      code must be === ValidationErrorCode.ADDRESS_CANNOT_BE_BLANK
    }

    it("falls back when not found") {
      val code = ValidationErrorCode.findByCode("-9999")
      code must be === ValidationErrorCode.UNKNOWN_VALIDATION_ERROR
    }
  }
}