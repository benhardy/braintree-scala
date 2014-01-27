package net.bhardy.braintree.scala

import net.bhardy.braintree.scala.exceptions.ForgedQueryStringException
import net.bhardy.braintree.scala.gw.Configuration
import net.bhardy.braintree.scala.util.Http
import net.bhardy.braintree.scala.util.TrUtil

class TransparentRedirectRequest(configuration: Configuration, queryString: String) extends Request {

  val paramMap = queryString.split("&").map { pair =>
    val items = pair.split("=")
    items(0) -> items(1)
  }.toMap

  val statusCode = Integer.valueOf(paramMap("http_status"))
  Http.throwExceptionIfErrorStatusCode(statusCode, paramMap.get("bt_message"))

  if (!new TrUtil(configuration).isValidTrQueryString(queryString)) {
    throw new ForgedQueryStringException
  }

  val id = paramMap("id")

  def getId = id

  override def toXml = RequestBuilder.buildXmlElement("id", id)

  override def toQueryString(parent: String) = throw new UnsupportedOperationException

  override def toQueryString: String = throw new UnsupportedOperationException
}