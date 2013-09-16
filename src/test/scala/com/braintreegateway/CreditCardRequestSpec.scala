package com.braintreegateway

import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.FunSpec
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.scalatest.matchers.MustMatchers

@RunWith(classOf[JUnitRunner])
class CreditCardRequestSpec extends FunSpec with MustMatchers {
  describe("toXml") {
    it("escapes xml chars") {
      val request = new CreditCardRequest().cardholderName("Special Xml Chars <>&\"'")
      request.toXML must be === "<creditCard><cardholderName>Special Xml Chars &lt;&gt;&amp;&quot;&apos;</cardholderName></creditCard>"
    }

    it("includes bundle") {
      val request = new CreditCardRequest().deviceData("{\"deviceSessionId\": \"dsid_abc123\"")
      request.toXML must include ("dsid_abc123")
    }

    it("includes security params") {
      val request = new CreditCardRequest().deviceSessionId("dsid_abc123")
      request.toXML must include ("dsid_abc123")
    }
  }

  describe("toQueryString") {
    it("converts CC requests to query strings") {
      val request = new CreditCardRequest().cardholderName("Drew").billingAddress.region("Chicago").done
      val expected = "credit_card%5Bcardholder_name%5D=Drew&credit_card%5Bbilling_address%5D%5Bregion%5D=Chicago"
      request.toQueryString must be === expected
    }

    it("works with parent") {
      val request = new CreditCardRequest().cardholderName("Drew").billingAddress.region("Chicago").done
      val expected = "customer%5Bcredit_card%5D%5Bcardholder_name%5D=Drew&customer%5Bcredit_card%5D%5Bbilling_address%5D%5Bregion%5D=Chicago"
      request.toQueryString("customer[credit_card]") must be === expected
    }
  }
}