package com.braintreegateway.integrationtest

import com.braintreegateway.{FakeModificationRequest, Environment}
import com.braintreegateway.util.Http
import scala.math.BigDecimal
import java.util.Random
import org.scalatest.matchers.MustMatchers
import org.scalatest.FunSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.braintreegateway.testhelpers.GatewaySpec
import com.braintreegateway.gw.BraintreeGateway

@RunWith(classOf[JUnitRunner])
class DiscountSpec extends FunSpec with MustMatchers with GatewaySpec {

  describe("discount creation") {
    onGatewayIt("saves all discount details retrievably") { gateway =>
      val discountId = "a_discount_id" + new Random().nextInt.toString
      val discountRequest = new FakeModificationRequest().
          amount(BigDecimal("100.00")).
          description("scala test discount description").
          id(discountId).
          kind("discount").
          name("scala test discount name").
          neverExpires(false).
          numberOfBillingCycles(12)

      val http = gatewayHttp(gateway)
      http.post("/modifications/create_modification_for_tests", discountRequest)
      val discounts = gateway.discount.all

      val actualDiscount = discounts.find(_.id == discountId).get

      actualDiscount.amount must be === BigDecimal("100.00")
      actualDiscount.description must be === "scala test discount description"
      actualDiscount.kind must be === "discount"
      actualDiscount.name must be === "scala test discount name"
      actualDiscount.neverExpires must be === false
      actualDiscount.numberOfBillingCycles must be === new Integer("12")
    }
  }

  def gatewayHttp(gateway: BraintreeGateway): Http = {
    new Http(gateway.authorizationHeader, gateway.baseMerchantURL,
      Environment.DEVELOPMENT.certificateFilenames, BraintreeGateway.VERSION)
  }
}