package com.braintreegateway.util

import xml.Elem

/**
 */
trait ToXml {
  def toXml: Option[Elem]
}
