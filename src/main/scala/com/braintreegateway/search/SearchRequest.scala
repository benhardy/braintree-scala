package com.braintreegateway.search

import com.braintreegateway.Request
import com.braintreegateway.util.XmlUtil._

import collection.mutable.{Map => MMap}
import collection.mutable.{HashMap, ListBuffer}
import xml.Elem

abstract class SearchRequest[R <: SearchRequest[R]] extends Request {

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

  override def toQueryString(parent: String) = ??? // not used here

  override def toQueryString = ???  // not used here

  override def toXml: Option[Elem] = {
    val children = (criteriaAsXml ++ rangeCriteriaAsXml ++
        multiValueCriteriaAsXml ++ keyValueCriteriaAsXml).toSeq

    val elem = tag("search").content(children)
    Some(elem)
  }

  def criteriaAsXml = {
    for {
      (key, value) <- criteria.result
      xml <- value.toXml
    } yield tag(key).content(xml)
  }

  def keyValueCriteriaAsXml = {
    for {
      (key, value) <- keyValueCriteria.result
    } yield tag(key).content(value)
  }

  def rangeCriteriaAsXml = {
    for {
      (key, criteria) <- rangeCriteria.result
    } yield tag(key).content(criteriaXml(criteria.toList))
  }

  def criteriaXml(criteria:List[SearchCriteria]) = criteria flatMap (_.toXml)

  def multiValueCriteriaAsXml = {
    for {
      (key, value) <- multiValueCriteria.result
    } yield {
      tag(key).withType("array").content(value.toXml.get \ "item")
    }
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