package com.braintreegateway

import _root_.org.scalatest.{FunSpec, Inside}
import _root_.org.scalatest.matchers.MustMatchers
import xml.XML

/**
 */
class SubscriptionOptionsRequestSpec extends FunSpec with MustMatchers with Inside {
  describe("toXmlString") {
    it("populates options when present") {
      val request = new SubscriptionOptionsRequest(new SubscriptionRequest).
        doNotInheritAddOnsOrDiscounts(true).
        prorateCharges(false).
        replaceAllAddOnsAndDiscounts(true).
        revertSubscriptionOnProrationFailure(false).
        startImmediately(true)

      val xmlString = request.toXml.get.toString

      val options = XML.loadString(xmlString)
      options.label must be === "options"
      (options \\ "doNotInheritAddOnsOrDiscounts" text) must be === "true"
      (options \\ "prorateCharges" text) must be === "false"
      (options \\ "replaceAllAddOnsAndDiscounts" text) must be === "true"
      (options \\ "revertSubscriptionOnProrationFailure" text) must be === "false"
      (options \\ "startImmediately" text) must be === "true"
    }

    it("avoids options when absent") {
      val request = new SubscriptionOptionsRequest(new SubscriptionRequest).
        startImmediately(true)

      val options = request.toXml.get

      options.label must be === "options"
      (options \\ "doNotInheritAddOnsOrDiscounts") must be ('empty)
      (options \\ "prorateCharges") must be ('empty)
      (options \\ "replaceAllAddOnsAndDiscounts") must be ('empty)
      (options \\ "revertSubscriptionOnProrationFailure") must be ('empty)
      (options \\ "startImmediately" text) must be === "true"
    }
  }

}
