package com.braintreegateway.util

object EnumUtils {

  def createLookupFromString[E <: java.lang.Enum[E]](values:Array[E]): String => Option[E] = {
    val map = values.toList.map { v => v.toString -> v }.toMap
    key => map get key
  }

  def findByName[T <: Enum[T]](enumType: Class[T], name: String): T = {
    if (name == null) {
      lookup(enumType, "UNDEFINED").
        getOrElse {
          throw new IllegalStateException(s"couldn't find enum ${enumType.getName} with value UNDEFINED! " +
            "i.e. This field can be null and we don't have an UNDEFINED enum value to which we can map that. " +
            "(This is a bug)")
        }
    } else {
      val underscoredName = name.toUpperCase.replaceAll(" ", "_")
      lookup(enumType, underscoredName).
        orElse(lookup(enumType, "UNRECOGNIZED")).
        getOrElse {
          throw new IllegalStateException(s"couldn't find enum ${enumType.getName} with value ${name} or UNRECOGNIZED!")
        }
    }
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