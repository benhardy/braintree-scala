package com.braintreegateway

import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.scalatest.matchers.MustMatchers
import _root_.org.scalatest.FunSpec
import xml.XML

@RunWith(classOf[JUnitRunner])
class CustomerRequestSpec extends FunSpec with MustMatchers {
  describe("toXml") {

    it("includes device bundle") {
      val deviceData = "{\"device_session_id\": \"devicesession123\"}"
      val request = new CustomerRequest().deviceData(deviceData)

      val xmlString = request.toXmlString
      xmlString must include ("devicesession123")

      val customerNode = XML.loadString(xmlString)
      (customerNode \ "deviceData").head.text must be === deviceData
    }

    it("includes Security Params") {
      val deviceSessionId = "devicesession123"
      val request = new CustomerRequest().creditCard.deviceSessionId(deviceSessionId).done

      val xmlString = request.toXmlString
      xmlString must include (deviceSessionId)

      val customerNode = XML.loadString(xmlString)
      (customerNode \ "creditCard" \ "deviceSessionId").head.text must be === deviceSessionId
    }
  }
}