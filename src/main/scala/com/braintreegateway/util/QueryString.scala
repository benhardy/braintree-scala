package com.braintreegateway.util

import com.braintreegateway.Request
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.{Map => JMap}

object QueryString {
  def encodeParam(key: String, value: String): String = {
    val encodedKey = encode(key)
    val encodedValue = encode(value)
    encodedKey + "=" + encodedValue
  }

  def encode(value: String): String = {
    try {
      URLEncoder.encode(value, DEFAULT_ENCODING)
    }
    catch {
      case e: UnsupportedEncodingException => {
        throw new IllegalStateException(DEFAULT_ENCODING + " encoding should always be available")
      }
    }
  }

  val DEFAULT_ENCODING: String = "UTF-8"
}

final class QueryString(content: String = "") {

  private val builder = new StringBuilder(content)

  def append(key: String, value: Int): QueryString = {
    append(key, value.toString)
  }

  def append(key: String, value: AnyRef): QueryString = {
    import scala.collection.JavaConversions._
    value match {
      case null => this
      case request: Request => appendRequest(key, request)
      case jMap: JMap[_,_] => appendMap(key, jMap)
      case sMap: Map[_,_] => appendMap(key, sMap)
      case other => appendString(key, other.toString)
    }
  }

  def appendEncodedData(alreadyEncodedData: String): QueryString = {
    if (alreadyEncodedData != null && alreadyEncodedData.length > 0) {
      addItem(alreadyEncodedData)
    }
    this
  }

  override def toString: String = {
    builder.toString
  }

  private def appendString(key: String, value: String): QueryString = {
    if (key != null && !key.isEmpty && value != null) {
      addItem(QueryString.encodeParam(key, value))
    }
    this
  }

  private def addItem(item: String): QueryString = {
    builder append separator append item
    this
  }

  private def separator = {
    if (builder.length > 0) "&" else ""
  }

  private def appendRequest(parent: String, request: Request): QueryString = {
    if (request != null) {
      val requestQueryString = request.toQueryString(parent)
      if (requestQueryString.length > 0) {
        addItem(requestQueryString)
      }
    }
    this
  }

  private def appendMap(key: String, value: JMap[_, _]): QueryString = {
    import scala.collection.JavaConversions._
    for (keyString <- value.keySet) {
      appendString("%s[%s]".format(key, keyString), value.get(keyString).toString)
    }
    this
  }

}