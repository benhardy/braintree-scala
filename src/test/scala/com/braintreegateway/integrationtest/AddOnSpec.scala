package com.braintreegateway.integrationtest

import com.braintreegateway.BraintreeGateway
import com.braintreegateway.Environment
import com.braintreegateway.util.Http
import java.math.BigDecimal
import java.util.Random
import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import scala.collection.JavaConversions._


class AddOnSpec extends FunSpec with MustMatchers {

  private def fixtures = {
    new {
      val gateway = new BraintreeGateway(
        Environment.DEVELOPMENT, "integration_merchant_id", "integration_public_key", "integration_private_key"
      )
      val http = new Http(
        gateway.getAuthorizationHeader, gateway.baseMerchantURL, Environment.DEVELOPMENT.certificateFilenames,
        BraintreeGateway.VERSION
      )
    }
  }

  describe("AddOnGateway") {
    it ("returns all AddOns") {
      val fix = fixtures
      val addOnId = "an_add_on_id" + String.valueOf(new Random().nextInt)
      val addOnRequest = new FakeModificationRequest().
          amount(new BigDecimal("100.00")).
          description("java test add-on description").
          id(addOnId).
          kind("add_on").
          name("java test add-on name").
          neverExpires(false).
          numberOfBillingCycles(12)

      fix.http.post("/modifications/create_modification_for_tests", addOnRequest)

      val addOns = fix.gateway.addOn.all

      val actualAddOn = addOns.filter(_.getId == addOnId).head

      actualAddOn.getAmount must be === new BigDecimal("100.00")
      actualAddOn.getDescription must be === "java test add-on description"
      actualAddOn.getKind must be === "add_on"
      actualAddOn.getName must be === "java test add-on name"
      actualAddOn.neverExpires must be === false
      actualAddOn.getNumberOfBillingCycles must be === new Integer("12")
    }
  }

}