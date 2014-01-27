package net.bhardy.braintree.scala.it

import org.scalatest.matchers.MustMatchers
import org.scalatest.FunSpec
import net.bhardy.braintree.scala.gw.BraintreeGateway
import net.bhardy.braintree.scala.util.Http
import net.bhardy.braintree.scala.{CustomerRequest, Environment}
import net.bhardy.braintree.scala.exceptions.{DownForMaintenanceException, UpgradeRequiredException, AuthenticationException}
import net.bhardy.braintree.scala.org.apache.commons.codec.binary.Base64
import net.bhardy.braintree.scala.testhelpers.TestHelper

/**
 */
class HttpIntegrationSpec extends FunSpec with MustMatchers with GatewayIntegrationSpec {


  describe("Sandbox CRUD operations") {

    def correctlyConfiguredHttp(gateway: BraintreeGateway) = {
      new Http(gateway.authorizationHeader, gateway.baseMerchantURL,
        Environment.SANDBOX.certificateFilenames, BraintreeGateway.VERSION)
    }
    var createdId: String = null

    onGatewayIt("smokeTestPostWithRequest") {
      gateway =>
        val request = new CustomerRequest().firstName("Dan").lastName("Manges").company("Braintree")
        val http = correctlyConfiguredHttp(gateway)
        val node = http.post("/customers", request)

        node("first-name") must be === Some("Dan")
        createdId = node("id").get
    }

    onGatewayIt("smokeTestGet") {
      gateway =>
        val http = correctlyConfiguredHttp(gateway)
        val node = http.get("/customers/" + createdId)
        node("first-name") must be ('defined)
    }

    onGatewayIt("smokeTestPut") {
      gateway =>
        val request = new CustomerRequest().firstName("NewName")
        val http = correctlyConfiguredHttp(gateway)
        val node = http.put("/customers/" + createdId, request)
        node.findString("first-name") must be === "NewName"
    }

    onGatewayIt("smokeTestDelete") {
      gateway =>
        val http2 = correctlyConfiguredHttp(gateway)
        http2.delete("/customers/" + createdId)
    }
  }

  describe("AuthenticationException") {
    onGatewayIt("bad keys") {
      gateway =>
        val authHeader = "Basic " + Base64.encodeBase64String(("bad_public_key:bad_private_key").getBytes).trim
        intercept[AuthenticationException] {
          new Http(authHeader, gateway.baseMerchantURL, Environment.SANDBOX.certificateFilenames, BraintreeGateway.VERSION).get("/")
        }
    }

    it("sslCertificateSuccessfulInSandbox") {
      intercept[AuthenticationException] {
        val http = new Http("", Environment.SANDBOX.baseURL, Environment.SANDBOX.certificateFilenames, BraintreeGateway.VERSION)
        http.get("/")
      }
    }

    it("sslCertificateSuccessfulInProduction") {
      intercept[AuthenticationException] {
        val http = new Http("", Environment.PRODUCTION.baseURL, Environment.PRODUCTION.certificateFilenames, BraintreeGateway.VERSION)
        http.get("/")
      }
    }
  }

  describe("Http error cases") {

    onGatewayIt("downForMaintenanceExceptionRaisedWhenAppInMaintenanceModeUsingServerToServer") {
      gateway =>
        intercept[DownForMaintenanceException] {
          val request = new CustomerRequest
          new Http(gateway.authorizationHeader, gateway.baseMerchantURL, Environment.SANDBOX.certificateFilenames, "1.0.0").put("/test/maintenance", request)
        }
    }

    onGatewayIt("downForMaintenanceExceptionRaisedWhenAppInMaintenanceModeUsingTR") {
      gateway =>
        intercept[DownForMaintenanceException] {
          val request = new CustomerRequest
          val trParams = new CustomerRequest
          val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request,
            gateway.configuration.baseMerchantURL + "/test/maintenance")
          gateway.transparentRedirect.confirmCustomer(queryString)
        }
    }

    onGatewayIt("downForMaintenanceExceptionRaisedWhenAppInMaintenanceModeUsingNewTR") {
      gateway =>
        intercept[DownForMaintenanceException] {
          val request = new CustomerRequest
          val trParams = new CustomerRequest
          val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request,
            gateway.configuration.baseMerchantURL + "/test/maintenance")
          gateway.transparentRedirect.confirmCustomer(queryString)
        }
    }
  }

  describe("Transparent Redirect failures") {

    ignore("authenticationExceptionRaisedWhenBadCredentialsUsingTR") {
      intercept[AuthenticationException] {
        val request = new CustomerRequest
        val trParams = new CustomerRequest
        val gateway = new BraintreeGateway(Environment.SANDBOX, "integration_merchant_id", "bad_public", "bad_private")
        val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request, gateway.transparentRedirect.url)
        gateway.transparentRedirect.confirmCustomer(queryString)
      }
    }

    ignore("authenticationExceptionRaisedWhenBadCredentialsUsingNewTR") {
      intercept[AuthenticationException] {
        val request = new CustomerRequest
        val trParams = new CustomerRequest
        val gateway = new BraintreeGateway(Environment.SANDBOX, "integration_merchant_id", "bad_public", "bad_private")
        val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request, gateway.transparentRedirect.url)
        gateway.transparentRedirect.confirmCustomer(queryString)
      }
    }
  }
  describe("Client Library too old") {
    onGatewayIt("throwUpgradeRequiredIfClientLibraryIsTooOld") {
      gateway =>
        intercept[UpgradeRequiredException] {
          val http = new Http(gateway.authorizationHeader, gateway.baseMerchantURL, Environment.SANDBOX.certificateFilenames, "1.0.0")
          http.get("/")
        }
    }
  }

}
