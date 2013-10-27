package com.braintreegateway

import com.braintreegateway.util.QueryString
import com.braintreegateway.util.StringUtils
import java.text.SimpleDateFormat

import xml._
import scala.collection.mutable.{Map => MMap}

import scala.collection.JavaConversions._
import StringUtils.underscore
import java.util.{TimeZone, Calendar}


object RequestBuilder {
  def buildXMLElement(element: AnyRef): String = {
    buildXMLElement("", element)
  }

  @SuppressWarnings(Array("unchecked"))
  def buildXMLElement(name: String, element: AnyRef): String = {
    element match {
      case null => ""
      case request: Request => request.toXmlString
      case calendar: Calendar => calendarElement(name, calendar).toString
      case map: java.util.Map[String, AnyRef] => {
        formatAsXML(name, map)
      }
      case scalaMap: MMap[String, AnyRef] => {
        formatAsXML(name, mapAsJavaMap(scalaMap))
      }
      case list: java.util.List[AnyRef] => {
        val xml = new StringBuilder
        for (item <- list) {
          xml.append(buildXMLElement("item", item))
        }
        wrapInXMLTag(name, xml.toString, "array")
      }
      case x => {
        String.format("<%s>%s</%s>", xmlEscape(name), if (x == null) "" else xmlEscape(x.toString), xmlEscape(name))
      }
    }
  }


  def calendarElement(name: String, calendar: Calendar): xml.Elem = {

    val dateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))

    val content = dateFormat.format(calendar.getTime)
    val attributes = new UnprefixedAttribute("type", "datetime", Null)
    Elem(null, name, attributes, TopScope, true, Text(content))
  }

  def formatAsXML(name: String, map: java.util.Map[String, AnyRef]): String = {
    if (map == null) ""
    val xml: StringBuilder = new StringBuilder
    xml.append(String.format("<%s>", name))
    for (entry <- map.entrySet) {
      xml.append(buildXMLElement(entry.getKey, entry.getValue))
    }
    xml.append(String.format("</%s>", name))
    xml.toString
  }

  def buildQueryStringElement(name: String, value: String): String = {
    if (value != null) {
      QueryString.encodeParam(name, value)
    }
    else {
      ""
    }
  }

  def parentBracketChildString(parent: String, child: String): String = {
    String.format("%s[%s]", parent, child)
  }

  def wrapInXMLTag(tagName: String, xml: String): String = {
    String.format("<%s>%s</%s>", tagName, xml, tagName)
  }

  def wrapInXMLTag(tagName: String, xml: String, `type`: String): String = {
    String.format("<%s type=\"%s\">%s</%s>", tagName, `type`, xml, tagName)
  }

  def xmlEscape(input: String): String = {
    input.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("'", "&apos;").replaceAll("\"", "&quot;")
  }
}

class RequestBuilder(parent: String) {

  val topLevelElements = MMap[String, String]()
  val elements = MMap[String, AnyRef]()

  def addTopLevelElement(name: String, value: String): RequestBuilder = {
    topLevelElements.put(name, value)
    this
  }

  def addElement(name: String, value: AnyRef): RequestBuilder = {
    elements(name) = value
    this
  }

  def addElementIf(condition: Boolean, name: String, value: AnyRef): RequestBuilder = {
    if (condition) {
      elements(name)= value
    }
    this
  }

  def addLowerCaseElementIfPresent(name: String, value: AnyRef): RequestBuilder = {
    if (value != null) {
      elements(name)= value.toString.toLowerCase
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
    for ((key:String, value:AnyRef) <- elements) {
      val bracketedKeyOffParent = RequestBuilder.parentBracketChildString(parentUnderscored, underscore(key))
      queryString.append(bracketedKeyOffParent, value)
    }
    queryString.toString
  }

  def toXmlString: String = {
    val builder = new StringBuilder
    builder.append(String.format("<%s>", parent))
    for ((key:String, value:AnyRef) <- elements) {
      builder.append(RequestBuilder.buildXMLElement(key, value))
    }
    builder.append(String.format("</%s>", parent))
    builder.toString
  }
}