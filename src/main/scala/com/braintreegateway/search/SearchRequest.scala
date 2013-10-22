package com.braintreegateway.search

import com.braintreegateway.BaseRequest
import com.braintreegateway.RequestBuilder
import collection.mutable.{Map => MMap}
import collection.mutable.{HashMap, ListBuffer}


abstract class SearchRequest[R <: SearchRequest[R]] extends BaseRequest {

  val criteria = MMap.newBuilder[String, SearchCriteria]
  val rangeCriteria = new HashMap[String, ListBuffer[SearchCriteria]]
  val multiValueCriteria = MMap.newBuilder[String, SearchCriteria]
  val keyValueCriteria = MMap.newBuilder[String, String]

  def getThis: R

  def addCriteria(nodeName: String, searchCriteria: SearchCriteria): R = {
    criteria += (nodeName -> searchCriteria)
    getThis
  }

  def addRangeCriteria(nodeName: String, searchCriteria: SearchCriteria): R = {
    val items = if (rangeCriteria.contains(nodeName)) {
      rangeCriteria(nodeName)
    } else  {
      val m = new ListBuffer[SearchCriteria]
      rangeCriteria += nodeName -> m
      m
    }
    items += searchCriteria
    getThis
  }

  def addMultipleValueCriteria(nodeName: String, searchCriteria: SearchCriteria): R = {
    multiValueCriteria += (nodeName -> searchCriteria)
    getThis
  }

  def addKeyValueCriteria(nodeName: String, value: String): R = {
    keyValueCriteria += (nodeName -> value)
    getThis
  }

  override def toQueryString(parent: String): String = {
    null
  }

  override def toQueryString: String = {
    null
  }

  override def toXML: String = {
    val builder = new StringBuilder
    builder.append("<search>")
    for ((key, value) <- criteria.result) {
      builder.append(RequestBuilder.wrapInXMLTag(key, value.toXML))
    }
    for ((key:String, criteria:ListBuffer[SearchCriteria]) <- rangeCriteria.result) {
      builder.append("<%s>".format(RequestBuilder.xmlEscape(key)))
      for (criterium <- criteria.toList) {
        builder.append(criterium.toXML)
      }
      builder.append(String.format("</%s>", RequestBuilder.xmlEscape(key)))
    }
    for ((key, value) <- multiValueCriteria.result) {
      builder.append(RequestBuilder.wrapInXMLTag(key, value.toXML, "array"))
    }
    for ((key, value) <- keyValueCriteria.result) {
      builder.append(RequestBuilder.wrapInXMLTag(key, value))
    }
    builder.append("</search>")
    builder.toString
  }

  protected def textNode(nodeName: String) = new TextNode[R](nodeName, getThis)

  protected def isNode(nodeName: String) = new IsNode[R](nodeName, getThis)

  protected def rangeNode(nodeName: String) = new RangeNode[R](nodeName, getThis)

  protected def equalityNode(nodeName: String) = new EqualityNode[R](nodeName, getThis)

  protected def keyValueNode(nodeName: String) = new KeyValueNode[R](nodeName, getThis)

  protected def dateRangeNode(nodeName: String) = new DateRangeNode[R](nodeName, getThis)

  protected def partialMatchNode(nodeName: String) = new PartialMatchNode[R](nodeName, getThis)

  protected def multipleValueNode[T](nodeName: String) = new MultipleValueNode[R, T](nodeName, getThis)

  protected def multipleValueOrTextNode[T](nodeName: String) = new MultipleValueOrTextNode[R, T](nodeName, getThis)

}