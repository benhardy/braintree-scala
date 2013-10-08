package com.braintreegateway.util

import com.braintreegateway.exceptions.UnexpectedException
import java.io.IOException
import java.util.Properties

object ClientLibraryProperties {
  val BRAINTREE_PROPERTY_FILE = "braintree.properties"
  val VERSION_PROPERTY_NAME = "braintree.gateway.version"
}

class ClientLibraryProperties {
  import ClientLibraryProperties._

  def version = {
    properties.getProperty(VERSION_PROPERTY_NAME)
  }

  private def loadProperties(propertyFile: String): Properties = {
    try {
      val is = getClass.getClassLoader.getResourceAsStream(propertyFile)
      val p = new Properties
      p.load(is)
      p
    }
    catch {
      case e: IOException => {
        throw new UnexpectedException("Couldn't load " + BRAINTREE_PROPERTY_FILE + " can't continue", e)
      }
    }
  }

  private final val properties = loadProperties(BRAINTREE_PROPERTY_FILE)
}