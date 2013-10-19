package com.braintreegateway.search

import com.braintreegateway.BaseRequest
import com.braintreegateway.RequestBuilder

class SearchCriteria private(val xml: String) extends BaseRequest {

  def this(items: List[_]) = {
    this(items.foldLeft(new StringBuilder) {
      (buf, item) => buf.append(
        RequestBuilder.buildXMLElement("item", item.toString)
      )
    }.toString)
  }

  def this(searchType: String, value: AnyRef) = {
    this(RequestBuilder.buildXMLElement(searchType, value))
  }

  override def toXML = xml

  override def toQueryString(parent: String): String = {
    throw new UnsupportedOperationException
  }

  override def toQueryString: String = {
    throw new UnsupportedOperationException
  }
}