package net.bhardy.braintree.scala.util

import xml.Elem

/**
 */
trait ToXml {
  def toXml: Option[Elem]
}
