package com.braintreegateway.integrationtest

import com.braintreegateway.{FakeModificationRequest, Environment}
import com.braintreegateway.util.Http
import java.math.BigDecimal
import java.util.Random
import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import scala.collection.JavaConversions._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.braintreegateway.gw.BraintreeGateway

@RunWith(classOf[JUnitRunner])
class AddOnSpec extends FunSpec with MustMatchers {

  private def fixtures = {
    new {
      val gateway = new BraintreeGateway(
        Environment.DEVELOPMENT, "integration_merchant_id", "integration_public_key", "integration_private_key"
      )
      val http = new Http(
        gateway.authorizationHeader, gateway.baseMerchantURL, Environment.DEVELOPMENT.certificateFilenames,
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

      val actualAddOn = addOns.filter(_.id == addOnId).head

      actualAddOn.amount must be === new BigDecimal("100.00")
      actualAddOn.description must be === "java test add-on description"
      actualAddOn.kind must be === "add_on"
      actualAddOn.name must be === "java test add-on name"
      actualAddOn.neverExpires must be === false
      actualAddOn.numberOfBillingCycles must be === new Integer("12")
    }
  }

}