package com.braintreegateway.integrationtest

import com.braintreegateway.BraintreeGateway
import com.braintreegateway.Environment
import com.braintreegateway.util.Http
import java.math.BigDecimal
import java.util.Random
import scala.collection.JavaConversions._
import org.scalatest.matchers.MustMatchers
import org.scalatest.FunSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class DiscountSpec extends FunSpec with MustMatchers {
  val gateway =
    new BraintreeGateway(Environment.DEVELOPMENT, "integration_merchant_id", "integration_public_key",
        "integration_private_key")

  val http = new Http(gateway.getAuthorizationHeader, gateway.baseMerchantURL,
        Environment.DEVELOPMENT.certificateFilenames, BraintreeGateway.VERSION)

  describe("discount creation") {
    it("saves all discount details retrievably") {

      val discountId = "a_discount_id" + new Random().nextInt.toString
      val discountRequest = new FakeModificationRequest().
          amount(new BigDecimal("100.00")).
          description("scala test discount description").
          id(discountId).
          kind("discount").
          name("scala test discount name").
          neverExpires(false).
          numberOfBillingCycles(12)

      http.post("/modifications/create_modification_for_tests", discountRequest)
      val discounts = gateway.discount.all

      val actualDiscount = discounts.find(_.getId == discountId).get

      actualDiscount.getAmount must be === new BigDecimal("100.00")
      actualDiscount.getDescription must be === "scala test discount description"
      actualDiscount.getKind must be === "discount"
      actualDiscount.getName must be === "scala test discount name"
      actualDiscount.neverExpires must be === false
      actualDiscount.getNumberOfBillingCycles must be === new Integer("12")
    }
  }
}