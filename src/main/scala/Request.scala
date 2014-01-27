package net.bhardy.braintree.scala

import util.ToXml

/**
 * Base functionality for fluent interface request builders.
 */
trait Request extends ToXml{
  def toQueryString(parent:String): String

  def toQueryString: String

  def getKind: String = ???
}

// this class appears mostly useless TODO can we ditch it?
abstract class BaseRequest extends Request {

  def xmlName: String

  protected def buildRequest(root: String): RequestBuilder

  final def toXml = buildRequest(xmlName).toXml

  def toQueryString(parent:String): String = throw new UnsupportedOperationException()

  def toQueryString: String = throw new UnsupportedOperationException()

}

trait HasParent[P <: Request] {
  def done: P
}