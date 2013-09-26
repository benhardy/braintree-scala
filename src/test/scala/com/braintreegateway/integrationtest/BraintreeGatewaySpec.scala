package com.braintreegateway.integrationtest

import com.braintreegateway.BraintreeGateway
import com.braintreegateway.Environment
import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class BraintreeGatewaySpec extends FunSpec with MustMatchers {
  describe("base merchant urls") {
    it("matches for development") {
      val config = new BraintreeGateway(Environment.DEVELOPMENT, "integration_merchant_id", "publicKey", "privateKey")
      val port = Option(System.getenv.get("GATEWAY_PORT")).getOrElse(3000)
      config.baseMerchantURL must be === "http://localhost:" + port + "/merchants/integration_merchant_id"
    }
    it("matches for sandbox") {
      val config = new BraintreeGateway(Environment.SANDBOX, "sandbox_merchant_id", "publicKey", "privateKey")
      config.baseMerchantURL must be === "https://sandbox.braintreegateway.com:443/merchants/sandbox_merchant_id"
    }
    it("matches for production") {
      val config = new BraintreeGateway(Environment.PRODUCTION, "production_merchant_id", "publicKey", "privateKey")
      config.baseMerchantURL must be === "https://www.braintreegateway.com:443/merchants/production_merchant_id"
    }
  }
  describe("getAuthorizationHeader") {
    it("returns a valid header in dev") {
      val config = new BraintreeGateway(Environment.DEVELOPMENT, "development_merchant_id",
        "integration_public_key", "integration_private_key")
      config.authorizationHeader must be === "Basic aW50ZWdyYXRpb25fcHVibGljX2tleTppbnRlZ3JhdGlvbl9wcml2YXRlX2tleQ=="
    }
  }
}