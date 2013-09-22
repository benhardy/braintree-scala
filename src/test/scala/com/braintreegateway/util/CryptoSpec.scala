package com.braintreegateway.util

import com.braintreegateway.org.apache.commons.codec.binary.Hex
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers

@RunWith(classOf[JUnitRunner])
class CryptoSpec extends FunSpec with MustMatchers {
  describe("hmacHash") {
    it("creates correct hmac hashes") {
      val actual = new Crypto().hmacHash("secretKey", "hello world")
      actual must be === "d503d7a1a6adba1e6474e9ff2c4167f9dfdf4247"
    }
  }

  describe("sha1Bytes") {
    it("doen't throw exception when encoding sha1") {
      val sha1Bytes = new Crypto().sha1Bytes("hello world")
      val hexBytes= new Hex().encode(sha1Bytes)
      val actual = new String(hexBytes, "ISO-8859-1")
      actual must be === "2aae6c35c94fcfb415dbe95f408b9ce91ee846ed"
    }
  }
}