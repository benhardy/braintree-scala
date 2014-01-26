package net.bhardy.braintree.scala


import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.FunSpec
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.scalatest.matchers.MustMatchers

@RunWith(classOf[JUnitRunner])
class TransactionRequestSpec extends FunSpec with MustMatchers {

  describe("toQueryString") {
    it("includes nested customer fields") {
      val request = new TransactionRequest().
        customer.
          firstName("Drew").
          done

      request.toQueryString must be === "transaction%5Bcustomer%5D%5Bfirst_name%5D=Drew"
    }
  }

  describe("toXmlString") {
    it("escapes custom field keys and values") {
      val request = new TransactionRequest().customField("ke&y", "va<lue")
      val xml = request.toXml.get
      xml.toString must include ("<customFields><ke&amp;y>va&lt;lue</ke&amp;y></customFields>")
    }

    it("includes security params") {
      val request = new TransactionRequest().deviceSessionId("device_session")
      val xml = request.toXml.get
      xml.toString must include ("device_session")
      (xml \ "deviceSessionId").head.text must be === "device_session"
    }

    it("includes deviceData bundle") {
      val request = new TransactionRequest().deviceData("{\"device_session_id\": \"mydsid\"}")
      val xml = request.toXml.get
      xml.toString must include ("mydsid")
      val expectedBundleText = "{\"device_session_id\": \"mydsid\"}"
      (xml \ "deviceData").head.text must be === expectedBundleText
    }
  }
}
