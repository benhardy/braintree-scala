package com.braintreegateway

/**
 * Base functionality for fluent interface request builders.
 */
trait Request {
  // TODO (big) have this actually return xml.Elem objects
  def toXML:String

  def toQueryString(parent:String): String

  def toQueryString: String

  def getKind: String
}

// this class appears mostly useless TODO can we ditch it?
abstract class BaseRequest extends Request {
  // TODO (big) have this actually return xml.Elem objects
  def toXML:String = throw new UnsupportedOperationException()

  def toQueryString(parent:String): String = throw new UnsupportedOperationException()

  def toQueryString: String = throw new UnsupportedOperationException()

  def getKind: String = null
}
