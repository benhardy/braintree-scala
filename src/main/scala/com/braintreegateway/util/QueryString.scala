package com.braintreegateway.util

import com.braintreegateway.Request
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.Map

object QueryString {
  def encodeParam(key: String, value: String): String = {
    val encodedKey: String = encode(key)
    val encodedValue: String = encode(value)
    return encodedKey + "=" + encodedValue
  }

  def encode(value: String): String = {
    try {
      return URLEncoder.encode(value, DEFAULT_ENCODING)
    }
    catch {
      case e: UnsupportedEncodingException => {
        throw new IllegalStateException(DEFAULT_ENCODING + " encoding should always be available")
      }
    }
  }

  var DEFAULT_ENCODING: String = "UTF-8"
}

class QueryString(content: String = "") {
  def this() = this("")

  val builder = new StringBuilder(content)

  def append(key: String, value: Int): QueryString = {
    append(key, value.toString)
  }

  def append(key: String, value: AnyRef): QueryString = {
    if (value == null) {
      this
    }
    else if (value.isInstanceOf[Request]) {
      appendRequest(key, value.asInstanceOf[Request])
    }
    else if (value.isInstanceOf[Map[_, _]]) {
      appendMap(key, value.asInstanceOf[Map[_, _]])
    } else {
      appendString(key, value.toString)
    }
  }

  def appendEncodedData(alreadyEncodedData: String): QueryString = {
    if (alreadyEncodedData != null && alreadyEncodedData.length > 0) {
      builder.append('&')
      builder.append(alreadyEncodedData)
    }
    this
  }

  override def toString: String = {
    builder.toString
  }

  protected def appendString(key: String, value: String): QueryString = {
    if (key != null && !(key == "") && value != null) {
      if (builder.length > 0) {
        builder.append("&")
      }
      builder.append(QueryString.encodeParam(key, value))
    }
    this
  }

  protected def appendRequest(parent: String, request: Request): QueryString = {
    if (request != null) {
      val requestQueryString: String = request.toQueryString(parent)
      if (requestQueryString.length > 0) {
        if (builder.length > 0) {
          builder.append("&")
        }
        builder.append(requestQueryString)
      }
    }
    this
  }

  protected def appendMap(key: String, value: Map[_, _]): QueryString = {
    import scala.collection.JavaConversions._
    for (keyString <- value.keySet) {
      appendString("%s[%s]".format(key, keyString), value.get(keyString).toString)
    }
    this
  }

}