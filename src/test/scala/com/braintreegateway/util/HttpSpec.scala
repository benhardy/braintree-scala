package com.braintreegateway.util

import com.braintreegateway.BraintreeGateway
import com.braintreegateway.CustomerRequest
import com.braintreegateway.Environment
import com.braintreegateway.exceptions.AuthenticationException
import com.braintreegateway.exceptions.DownForMaintenanceException
import com.braintreegateway.exceptions.UpgradeRequiredException
import com.braintreegateway.org.apache.commons.codec.binary.Base64
import com.braintreegateway.testhelpers.{GatewaySpec, TestHelper}
import java.io.File
import java.io.FileInputStream
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class HttpSpec extends GatewaySpec with FunSpec with MustMatchers {


  describe("correctly configured operations") {

    def correctlyConfiguredHttp(gateway: BraintreeGateway) = {
      new Http(gateway.authorizationHeader, gateway.baseMerchantURL,
        Environment.SANDBOX.certificateFilenames, BraintreeGateway.VERSION)
    }

    onGatewayIt("smokeTestGet") {
      gateway =>
        val http = correctlyConfiguredHttp(gateway)
        val node = http.get("/customers/131866")
        node.findString("first-name") must not be null
    }

    onGatewayIt("smokeTestPostWithRequest") {
      gateway =>
        val request = new CustomerRequest().firstName("Dan").lastName("Manges").company("Braintree")
        val http = correctlyConfiguredHttp(gateway)
        val node = http.post("/customers", request)
        node.findString("first-name") must be === "Dan"
    }

    onGatewayIt("smokeTestPut") {
      gateway =>
        val request = new CustomerRequest().firstName("NewName")
        val http = correctlyConfiguredHttp(gateway)
        val node = http.put("/customers/131866", request)
        node.findString("first-name") must be === "NewName"
    }

    onGatewayIt("smokeTestDelete") {
      gateway =>
        val http1 = correctlyConfiguredHttp(gateway)
        val node = http1.post("/customers", new CustomerRequest)
        val http2 = correctlyConfiguredHttp(gateway)
        http2.delete("/customers/" + node.findString("id"))
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

    it("authenticationExceptionRaisedWhenBadCredentialsUsingTR") {
      intercept[AuthenticationException] {
        val request = new CustomerRequest
        val trParams = new CustomerRequest
        val gateway = new BraintreeGateway(Environment.DEVELOPMENT, "integration_merchant_id", "bad_public", "bad_private")
        val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request, gateway.transparentRedirect.url)
        gateway.transparentRedirect.confirmCustomer(queryString)
      }
    }

    it("authenticationExceptionRaisedWhenBadCredentialsUsingNewTR") {
      intercept[AuthenticationException] {
        val request = new CustomerRequest
        val trParams = new CustomerRequest
        val gateway = new BraintreeGateway(Environment.DEVELOPMENT, "integration_merchant_id", "bad_public", "bad_private")
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

  describe("ssl Bad Certificate") {
    onGatewayIt("includes message about certificate problems in exception") {
      gateway =>
        try {
          startSSLServer
          intercept[Exception] {
            val http = new Http(gateway.authorizationHeader, "https://localhost:9443",
              Environment.SANDBOX.certificateFilenames, BraintreeGateway.VERSION)
            http.get("/")
          }.getMessage must include("Cert")
        }
        finally {
          stopSSLServer
        }
    }
  }

  private def startSSLServer {
    val fileName = StringUtils.getFullPathOfFile("script/httpsd.rb")
    new File(fileName).setExecutable(true)
    new ProcessBuilder(fileName, "/tmp/httpsd.pid").start.waitFor
  }

  private def stopSSLServer {
    val pid = StringUtils.inputStreamToString(new FileInputStream("/tmp/httpsd.pid"))
    new ProcessBuilder("kill", "-9", pid).start
  }
}