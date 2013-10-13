package com.braintreegateway

import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.FunSpec
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.scalatest.matchers.MustMatchers
import com.braintreegateway.exceptions._
import gw.Configuration
import java.net.URLEncoder
import util.QueryString

@RunWith(classOf[JUnitRunner])
class TransparentRedirectRequestSpec extends FunSpec with MustMatchers {
  val configuration = new Configuration("baseMerchantURL", "integration_public_key", "integration_private_key")

  describe("constructor") {
    it("can construct without errors with correct queryString") {
      val queryString = "http_status=200&id=6kdj469tw7yck32j&hash=99c9ff20cd7910a1c1e793ff9e3b2d15586dc6b9"
      new TransparentRedirectRequest(configuration, queryString)
    }

    it("raises ForgedQueryStringException if Given Invalid Query String") {
      val queryString = "http_status=200&id=6kdj469tw7yck32j&hash=99c9ff20cd7910a1c1e793ff9e3b2d15586dc6b9" + "this makes it invalid"
      intercept[ForgedQueryStringException] {
        new TransparentRedirectRequest(configuration, queryString)
      }
    }

    it("raises ServerException if Http Status Is 500") {
      val queryString = "http_status=500&id=6kdj469tw7yck32j&hash=a839a44ca69d59a3d6f639c294794989676632dc"
      intercept[ServerException] {
        new TransparentRedirectRequest(configuration, queryString)
      }
    }

    it("raises AuthenticationException if Http Status Is 401") {
      val queryString = "http_status=401&id=6kdj469tw7yck32j&hash=5a26e3cde5ebedb0ec1ba8d35724360334fbf419"
      intercept[AuthenticationException] {
        new TransparentRedirectRequest(configuration, queryString)
      }
    }

    it("raises AuthorizatonException If Http Status Is 403") {
      val queryString = "http_status=403&id=6kdj469tw7yck32j&hash=126d5130b71a4907e460fad23876ed70dd41dcd2"
      intercept[AuthorizationException] {
        new TransparentRedirectRequest(configuration, queryString)
      }
    }

    it("raises AuthorizatonExecption With Message If Http Status Is 403 And Message Is In Query String") {
      val message = "Invalid params: transaction[bad]"
      val encodedMessage = QueryString.encode(message)
      val queryString = s"bt_message=${encodedMessage}&http_status=403&id=6kdj469tw7yck32j&hash=126d5130b71a4907e460fad23876ed70dd41dcd2"
      intercept[AuthorizationException] {
        new TransparentRedirectRequest(configuration, queryString)
      }.getMessage must be === message
    }

    it("raises DownForMaintenanceException if Http Status Is 503") {
      val queryString = "http_status=503&id=6kdj469tw7yck32j&hash=1b3d29199a282e63074a7823b76bccacdf732da6"
      intercept[DownForMaintenanceException] {
        new TransparentRedirectRequest(configuration, queryString)
      }
    }

    it("raises NotFoundException if Http Status Is 404") {
      val queryString = "http_status=404&id=6kdj469tw7yck32j&hash=0d3724a45cf1cda5524aa68f1f28899d34d2ff3a"
      intercept[NotFoundException] {
        new TransparentRedirectRequest(configuration, queryString)
      }
    }

    it("raises UnexpectedException if Http Status Is Unexpected") {
      val queryString = "http_status=600&id=6kdj469tw7yck32j&hash=740633356f93384167d887de0c1d9745e3de8fb6"
      intercept[UnexpectedException] {
        new TransparentRedirectRequest(configuration, queryString)
      }
    }
  }
}