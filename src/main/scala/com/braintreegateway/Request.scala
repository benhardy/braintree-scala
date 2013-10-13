package com.braintreegateway

/**
 * Base functionality for fluent interface request builders.
 */
trait Request {
  def toXML:String

  def toQueryString(parent:String): String

  def toQueryString: String

  def getKind: String
}

abstract class BaseRequest extends Request {
  def toXML:String = throw new UnsupportedOperationException()

  def toQueryString(parent:String): String = throw new UnsupportedOperationException()

  def toQueryString: String = throw new UnsupportedOperationException()

  def getKind: String = null
}
