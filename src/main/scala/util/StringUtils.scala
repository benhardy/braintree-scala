package net.bhardy.braintree.scala.util

import java.io.InputStream
import java.io.InputStreamReader

object StringUtils {
  def classToXMLName(klass: Class[_]): String = {
    dasherize(klass.getSimpleName).toLowerCase
  }

  def dasherize(str: String): String = {
    if (str == null) null else str.replaceAll("([a-z])([A-Z])", "$1-$2").replaceAll("_", "-").toLowerCase
  }

  def getFullPathOfFile(filename: String): String = {
    getClassLoader.getResource(filename).getFile
  }

  private def getClassLoader: ClassLoader = {
    Thread.currentThread.getContextClassLoader
  }

  def inputStreamToString(inputStream: InputStream): String = {
    val inputReader: InputStreamReader = new InputStreamReader(inputStream)
    val builder = new java.lang.StringBuilder
    val buffer = new Array[Char](0x1000)
    var bytesRead = inputReader.read(buffer, 0, buffer.length)
    while (bytesRead >= 0) {
      builder.append(buffer, 0, bytesRead)
      bytesRead = inputReader.read(buffer, 0, buffer.length)
    }
    builder.toString
  }

  def nullIfEmpty(str: String): String = {
    if (str == null || str.length == 0) null else str
  }

  def underscore(str: String): String = {
    if (str == null) null else str.replaceAll("([a-z])([A-Z])", "$1_$2").replaceAll("-", "_").toLowerCase
  }

  def join(delimiter: String, tokens: AnyRef*): String = {
    tokens.mkString(delimiter).trim
  }
}