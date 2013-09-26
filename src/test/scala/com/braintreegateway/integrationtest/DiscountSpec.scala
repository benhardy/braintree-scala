package com.braintreegateway.integrationtest

import com.braintreegateway.{FakeModificationRequest, BraintreeGateway, Environment}
import com.braintreegateway.util.Http
import java.math.BigDecimal
import java.util.Random
import scala.collection.JavaConversions._
import org.scalatest.matchers.MustMatchers
import org.scalatest.FunSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.braintreegateway.testhelpers.GatewaySpec

@RunWith(classOf[JUnitRunner])
class DiscountSpec extends FunSpec with MustMatchers with GatewaySpec {

  describe("discount creation") {
    onGatewayIt("saves all discount details retrievably") { gateway =>
      val discountId = "a_discount_id" + new Random().nextInt.toString
      val discountRequest = new FakeModificationRequest().
          amount(new BigDecimal("100.00")).
          description("scala test discount description").
          id(discountId).
          kind("discount").
          name("scala test discount name").
          neverExpires(false).
          numberOfBillingCycles(12)

      val http = gatewayHttp(gateway)
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

  def gatewayHttp(gateway: BraintreeGateway): Http = {
    new Http(gateway.authorizationHeader, gateway.baseMerchantURL,
      Environment.DEVELOPMENT.certificateFilenames, BraintreeGateway.VERSION)
  }
}