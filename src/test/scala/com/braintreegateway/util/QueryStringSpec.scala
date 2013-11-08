package com.braintreegateway.util

import com.braintreegateway.CreditCardRequest
import java.math.BigDecimal
import org.scalatest.matchers.MustMatchers
import org.scalatest.FunSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class QueryStringSpec extends FunSpec with MustMatchers {
  describe("append") {
    it("append") {
      val actual = new QueryString().append("foo", "f").append("bar", "b").toString
      actual must be === "foo=f&bar=b"
    }

    it("appendEmptyStringOrNulls") {
      val nullString: String = null
      val actual = new QueryString().append("foo", "f").append("", "b").append("bar", "").append("boom", nullString).append("", "c").toString
      actual must be === "foo=f&bar="
    }

    it("appendOtherObjectsWithCanBeConvertedToStrings") {
      val actual = new QueryString().append("foo", 10).append("bar", new BigDecimal("20.00")).toString
      actual must be === "foo=10&bar=20.00"
    }

    it("appendWithRequest") {
      val request = new CreditCardRequest().cvv("123").cardholderName("Drew")
      val actual = new QueryString().append("[credit_card]", request).toString
      actual must include("%5Bcredit_card%5D%5Bcardholder_name%5D=Drew")
      actual must include("%5Bcredit_card%5D%5Bcvv%5D=123")
    }

    it("appends with Scala Map") {
      val map = Map("name" -> "john", "age" -> "15")
      val actual = new QueryString().append("transaction[custom_fields]", map).toString
      actual must include("transaction%5Bcustom_fields%5D%5Bage%5D=15")
      actual must include("transaction%5Bcustom_fields%5D%5Bname%5D=john")
    }

    it("appendWithNestedRequest") {
      val request = new CreditCardRequest().cvv("123").cardholderName("Drew").billingAddress.company("Braintree").done.
        options.makeDefault(true).verifyCard(true).done
      val actual = new QueryString().append("[credit_card]", request).toString
      actual must include("%5Bcredit_card%5D%5Bcardholder_name%5D=Drew")
      actual must include("%5Bcredit_card%5D%5Bcvv%5D=123")
      actual must include("%5Bcredit_card%5D%5Bbilling_address%5D%5Bcompany%5D=Braintree")
      actual must include("%5Bcredit_card%5D%5Boptions%5D%5Bmake_default%5D=true")
      actual must include("%5Bcredit_card%5D%5Boptions%5D%5Bverify_card%5D=true")
    }
  }
}