package com.braintreegateway.util

object EnumUtils {

  def createLookupFromString[E <: java.lang.Enum[E]](values:Array[E]): String => Option[E] = {
    val map = values.toList.map { v => v.toString -> v }.toMap
    key => map get key
  }

  def findByNameOpt[T <: Enum[T]](enumType: Class[T])(nameWrapper: Option[String]): T = {
    Option(nameWrapper).flatten.map { name =>
      val underscoredName = name.toUpperCase.replaceAll(" ", "_")
      lookup(enumType, underscoredName).
        orElse(lookup(enumType, "UNRECOGNIZED")).
        getOrElse {
        throw new IllegalStateException(s"couldn't find enum ${enumType.getName} with value ${name} or UNRECOGNIZED!")
      }
    } getOrElse {
      lookup(enumType, "UNDEFINED").
        getOrElse {
        throw new IllegalStateException(s"couldn't find enum ${enumType.getName} with value UNDEFINED! " +
          "i.e. This field can be null and we don't have an UNDEFINED enum value to which we can map that. " +
          "(This is a bug)")
      }
    }
  }

  def findByName[T <: Enum[T]](enumType: Class[T])(name: String): T = {
    findByNameOpt(enumType)(Option(name))
  }

  def lookup[T <: Enum[T]](enumType: Class[T], valueName: String): Option[T] = {
    try {
      Some(Enum.valueOf(enumType, valueName))
    }
    catch {
      case e: IllegalArgumentException => {
        None
      }
    }
  }
}