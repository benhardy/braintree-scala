package com.braintreegateway.testhelpers

import xml.{XML, Elem}
import java.io.StringWriter

/**
 */
object XmlHelper {

  def xmlAsStringWithHeader(response: Elem): String = {
    val buf = new StringWriter()
    XML.write(buf, response, "utf-8", true, null)
    buf.flush()
    buf.toString
  }

}
