package com.braintreegateway.util

import com.braintreegateway.Configuration
import com.braintreegateway.CreditCardRequest
import com.braintreegateway.TransactionRequest
import com.braintreegateway.testhelpers.TestHelper
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers

@RunWith(classOf[JUnitRunner])
class TrUtilSpec extends FunSpec with MustMatchers {

  val configuration = new Configuration("baseMerchantURL", "integration_public_key", "integration_private_key")

  describe("buildTrData") {
    it("builds valid tr data") {
      val request = new CreditCardRequest().customerId("123")
      val trData = new TrUtil(configuration).buildTrData(request, "http://example.com")
      TestHelper.assertValidTrData(configuration, trData)
    }

    it("apiVersionIsCorrectInTrData") {
      val util = new TrUtil(configuration)
      val data = util.buildTrData(new TransactionRequest, "http://google.com")
      data must include ("api_version=3")
    }
  }

  describe("isValidTrQueryString") {
    it("is true For Valid String") {
      val queryString = "http_status=200&id=6kdj469tw7yck32j&hash=99c9ff20cd7910a1c1e793ff9e3b2d15586dc6b9"
      val trUtil = new TrUtil(configuration)
      trUtil.isValidTrQueryString(queryString) must be === true
    }
    it("isn't true For Invalid String") {
      val queryString = "http_status=200&id=6kdj469tw7yck32j&hash=99c9ff20cd7910a1c1e793ff9e3b2d15586dc6b8"
      (new TrUtil(configuration).isValidTrQueryString(queryString)) must be === false
    }
  }

  describe("url") {
    it("includes base tr url part") {
      val trUtil = new TrUtil(configuration)
      val url = trUtil.url
      url must include ("baseMerchantURL/transparent_redirect_requests")
    }
  }
}