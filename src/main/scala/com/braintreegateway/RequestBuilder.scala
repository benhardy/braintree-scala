package com.braintreegateway

import com.braintreegateway.util.QueryString
import com.braintreegateway.util.StringUtils
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util._
import java.util
import collection.immutable

import QueryString.encode
import QueryString.encodeParam

object RequestBuilder {
  def buildXMLElement(element: AnyRef): String = {
    buildXMLElement("", element)
  }

  @SuppressWarnings(Array("unchecked"))
  def buildXMLElement(name: String, element: AnyRef): String = {
    element match {
      case null => ""
      case request: Request => request.toXML
      case calendar: Calendar => {
        val dateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
        String.format("<%s type=\"datetime\">%s</%s>", name, dateFormat.format(calendar.getTime), name)
      }
      case map: java.util.Map[String, AnyRef] => {
        formatAsXML(name, map)
      }
      case list: java.util.List[AnyRef] => {
        val xml: StringBuilder = new StringBuilder
        import scala.collection.JavaConversions._
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

  def formatAsXML(name: String, map: Map[String, AnyRef]): String = {
    if (map == null) ""
    val xml: StringBuilder = new StringBuilder
    xml.append(String.format("<%s>", name))
    import scala.collection.JavaConversions._
    for (entry <- map.entrySet) {
      xml.append(buildXMLElement(entry.getKey, entry.getValue))
    }
    xml.append(String.format("</%s>", name))
    xml.toString
  }

  def buildQueryStringElement(name: String, value: String): AnyRef = {
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

  val topLevelElements: Map[String, String] = new HashMap[String, String]()
  val elements: Map[String, AnyRef] = new HashMap[String, AnyRef]

  def addTopLevelElement(name: String, value: String): RequestBuilder = {
    topLevelElements.put(name, value)
    this
  }

  def addElement(name: String, value: AnyRef): RequestBuilder = {
    elements.put(name, value)
    this
  }

  def addElementIf(condition: Boolean, name: String, value: AnyRef): RequestBuilder = {
    if (condition) {
      elements.put(name, value)
    }
    this
  }
  def addLowerCaseElementIfPresent(name: String, value: AnyRef): RequestBuilder = {
    if (value != null) {
      elements.put(name, value.toString.toLowerCase)
    }
    this
  }

  def toQueryString: String = {
    val queryString: QueryString = new QueryString
    import scala.collection.JavaConversions._
    for (entry <- topLevelElements.entrySet) {
      queryString.append(StringUtils.underscore(entry.getKey), entry.getValue)
    }
    import scala.collection.JavaConversions._
    for (entry <- elements.entrySet) {
      queryString.append(RequestBuilder.parentBracketChildString(StringUtils.underscore(parent), StringUtils.underscore(entry.getKey)), entry.getValue)
    }
    queryString.toString
  }

  def toXML: String = {
    val builder: StringBuilder = new StringBuilder
    builder.append(String.format("<%s>", parent))
    import scala.collection.JavaConversions._
    for (entry <- elements.entrySet) {
      builder.append(RequestBuilder.buildXMLElement(entry.getKey, entry.getValue))
    }
    builder.append(String.format("</%s>", parent))
    builder.toString
  }
}