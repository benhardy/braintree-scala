package com.braintreegateway

import util.{ToXml, QueryString, StringUtils}
import java.text.SimpleDateFormat

import xml.Elem
import scala.collection.mutable.{Map => MMap}

import StringUtils.underscore
import java.util.{TimeZone, Calendar}

import com.braintreegateway.util.XmlUtil._

object RequestBuilder {

  def buildXmlElement(tagName:String, data:Any): Option[Elem] = {
    data match {
      case null => None
      case None => None
      case Some(item) => buildXmlElement(tagName, item) // unpack
      case xmlAble: ToXml => xmlAble.toXml
      case calendar: Calendar => Some(calendarElement(tagName, calendar))
      case scalaMutableMap: MMap[String, Any] => {
        Some(formatAsXml(tagName, scalaMutableMap.toMap))
      }
      case scalaMap: Map[String, AnyRef] => {
        Some(formatAsXml(tagName, scalaMap))
      }
      case list: List[Any] => Some(listToXml(tagName, list))
      case other => Some(tag(tagName).content(other.toString))
    }
  }

  def listToXml(tagName:String, list: List[Any]) = {
    val children = for {
      item <- list
      elem <- buildXmlElement("item", item)
    } yield elem

    tag(tagName).withType("array").content(children)
  }

  def calendarElement(name: String, calendar: Calendar) = {

    val dateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
    val content = dateFormat.format(calendar.getTime)

    tag(name).withType("datetime").content(content)
  }

  def formatAsXml(tagName: String, map: Map[String, Any]): Elem = {
    val children = for {
      (key, value) <- map
      elem <- buildXmlElement(key, value)
    } yield elem

    tag(tagName).content(children)
  }

  def buildQueryStringElement(name: String, value: String): String = {
    Option(value) map {
      QueryString.encodeParam(name, _)
    } getOrElse {
      ""
    }
  }

  def parentBracketChildString(parent: String, child: String): String = {
    String.format("%s[%s]", parent, child)
  }
}

class RequestBuilder(parent: String) extends ToXml {

  val topLevelElements = MMap[String, String]()
  val elements = MMap[String, Any]()

  def addTopLevelElement(name: String, value: String): RequestBuilder = {
    topLevelElements.put(name, value)
    this
  }

  def addTopLevelElement(name: String, value: Option[String]): RequestBuilder = {
    if (value.isDefined) {
      topLevelElements.put(name, value.get)
    }
    this
  }

  def addElement(name: String, value: Any): RequestBuilder = {
    elements(name) = value
    this
  }

  def addElementIf(condition: Boolean, name: String, value: => Any): RequestBuilder = {
    if (condition) {
      elements(name)= value
    }
    this
  }

  def toQueryString: String = {
    val parentUnderscored = underscore(parent)
    val queryString = new QueryString
    for ((key:String, value:String) <- topLevelElements) {
      val underscoredKey = StringUtils.underscore(key)
      queryString.append(underscoredKey, value)
    }
    for ((key:String, value:Any) <- elements) {
      val bracketedKeyOffParent = RequestBuilder.parentBracketChildString(parentUnderscored, underscore(key))
      queryString.append(bracketedKeyOffParent, optionize(value))
    }
    queryString.toString
  }

  // TODO make this unnecessary
  def optionize(value: Any): Option[_] = {
    val maybe: Option[_] = value match {
      case optionAlready: Option[_] => optionAlready
      case other => Option(other)
    }
    maybe
  }

  def toXml = {
    val children = for {
      (key: String, value: AnyRef) <- elements if value != null
      elem <- RequestBuilder.buildXmlElement(key, value)
    } yield elem

    val elem = tag(parent).content(children)
    Some(elem)
  }
}