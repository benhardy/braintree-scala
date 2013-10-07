package com.braintreegateway.util

import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util._

object NodeWrapper {
  final val DATE_FORMAT: String = "yyyy-MM-dd"
  final val DATE_TIME_FORMAT: String = "yyyy-MM-dd'T'HH:mm:ss'Z'"
  final val UTC_DESCRIPTOR: String = "UTC"
}

abstract class NodeWrapper {
  import NodeWrapper._

  def findAll(expression: String): List[NodeWrapper]

  def findAllStrings(expression: String): List[String] = {
    val strings: List[String] = new ArrayList[String]
    import scala.collection.JavaConversions._
    for (node <- findAll(expression)) {
      strings.add(node.findString("."))
    }
    strings
  }

  def findBigDecimal(expression: String): BigDecimal = {
    val value: String = findString(expression)
    if (value == null) null else new BigDecimal(value)
  }

  def findBoolean(expression: String): Boolean = {
    val value: String = findString(expression)
    java.lang.Boolean.valueOf(value)
  }

  def findDate(expression: String): Calendar = {
    try {
      val dateString: String = findString(expression)
      if (dateString == null) {
        return null
      }
      val dateFormat: SimpleDateFormat = new SimpleDateFormat(DATE_FORMAT)
      dateFormat.setTimeZone(TimeZone.getTimeZone(UTC_DESCRIPTOR))
      val calendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone(UTC_DESCRIPTOR))
      calendar.setTime(dateFormat.parse(dateString))
      calendar
    }
    catch {
      case e: Exception => {
        throw new RuntimeException(e)
      }
    }
  }

  def findDateTime(expression: String): Calendar = {
    try {
      val dateString: String = findString(expression)
      if (dateString == null) {
        return null
      }
      val dateTimeFormat: SimpleDateFormat = new SimpleDateFormat(DATE_TIME_FORMAT)
      dateTimeFormat.setTimeZone(TimeZone.getTimeZone(UTC_DESCRIPTOR))
      val calendar: Calendar = Calendar.getInstance(TimeZone.getTimeZone(UTC_DESCRIPTOR))
      calendar.setTime(dateTimeFormat.parse(dateString))
      return calendar
    }
    catch {
      case e: Exception => {
        throw new RuntimeException(e)
      }
    }
  }

  def findInteger(expression: String): Integer = {
    val value: String = findString(expression)
    return if (value == null) null else Integer.valueOf(value)
  }

  def findFirst(expression: String): NodeWrapper

  def findString(expression: String): String

  def getElementName: String

  def isSuccess: Boolean = {
    return !((getElementName == "api-error-response"))
  }

  def findMap(expression: String): Map[String, String] = {
    val map: Map[String, String] = new HashMap[String, String]
    import scala.collection.JavaConversions._
    for (mapNode <- findAll(expression)) {
      map.put(StringUtils.underscore(mapNode.getElementName), mapNode.findString("."))
    }
    return map
  }

  def getFormParameters: Map[String, String]
}