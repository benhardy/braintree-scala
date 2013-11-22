package com.braintreegateway.util

import scala.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.{Calendar, TimeZone}

object NodeWrapper {
  final val DATE_FORMAT: String = "yyyy-MM-dd"
  final val DATE_TIME_FORMAT: String = "yyyy-MM-dd'T'HH:mm:ss'Z'"
  final val UTC_DESCRIPTOR: String = "UTC"
}

abstract class NodeWrapper {

  import NodeWrapper._

  def findAll(expression: String): List[NodeWrapper]

  def findAllStrings(expression: String): List[String] = {
    findAll(expression).map { _.findString(".") }
  }

  @deprecated
  def findBigDecimal(expression: String): BigDecimal = {
    val value: String = findString(expression)
    if (value == null) null else BigDecimal(value)
  }

  def findBigDecimalOpt(expression: String): Option[BigDecimal] = {
    findStringOpt(expression) map {
       BigDecimal(_)
    }
  }

  @deprecated
  def findBoolean(expression: String): Boolean = {
    val value: String = findString(expression)
    java.lang.Boolean.valueOf(value)
  }

  def findBooleanOpt(expression: String): Option[Boolean] = {
    findStringOpt(expression).map {
      _.toBoolean
    }
  }

  @deprecated
  def findDate(expression: String): Calendar = {
    val dateString = findString(expression)
    if (dateString == null) {
      return null
    }
    parseDate(DATE_FORMAT)(dateString)
  }

  @deprecated
  def findDateTime(expression: String): Calendar = {
    val dateString = findString(expression)
    if (dateString == null) {
      return null
    }
    parseDate(DATE_TIME_FORMAT)(dateString)
  }

  def findDateOpt(expression: String): Option[Calendar] = {
    findStringOpt(expression) map parseDate(DATE_FORMAT)
  }

  def findDateTimeOpt(expression: String): Option[Calendar] = {
    findStringOpt(expression) map parseDate(DATE_TIME_FORMAT)
  }

  private def parseDate(format: String)(dateString: String): Calendar = {
    try {
      val dateTimeFormat = new SimpleDateFormat(format)
      dateTimeFormat.setTimeZone(TimeZone.getTimeZone(UTC_DESCRIPTOR))
      val calendar = Calendar.getInstance(TimeZone.getTimeZone(UTC_DESCRIPTOR))
      calendar.setTime(dateTimeFormat.parse(dateString))
      calendar
    }
    catch {
      case e: Exception => {
        throw new RuntimeException(e)
      }
    }
  }

  @deprecated
  def findInteger(expression: String): Integer = {
    val value: String = findString(expression)
    if (value == null) null else Integer.valueOf(value)
  }

  def findIntegerOpt(expression: String): Option[Integer] = {
    findStringOpt(expression) map (_.toInt)
  }

  @deprecated
  def findFirst(expression: String): NodeWrapper

  def findFirstOpt(expression: String): Option[NodeWrapper]

  @deprecated
  def findString(expression: String): String

  def findStringOpt(expression: String): Option[String]

  def apply(expression: String) = findStringOpt(expression)

  def getElementName: String

  def isSuccess: Boolean = {
    !((getElementName == "api-error-response"))
  }

  @deprecated
  def findMap(expression: String): Map[String, String] = {
    findMapOpt(expression)
  }

  def findMapOpt(expression: String): Map[String, String] = {
    val items = for {
      mapNode <- findAll(expression).toList
      value <- mapNode.findStringOpt(".")
      key = StringUtils.underscore(mapNode.getElementName)
    } yield (key, value)
    items.toMap
  }

  def getFormParameters: Map[String, String]
}