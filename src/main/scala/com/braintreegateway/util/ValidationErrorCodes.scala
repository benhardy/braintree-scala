package com.braintreegateway.util

import com.braintreegateway.ValidationErrorCode

/**
 */
object ValidationErrorCodes {

  private val reverseLookup: Map[String, ValidationErrorCode] = {
    ValidationErrorCode.values.map {
      errorCode =>
        errorCode.code -> errorCode
    }.toMap
  }

  def fromString(code:String): ValidationErrorCode = {
    reverseLookup.get(code).getOrElse(ValidationErrorCode.UNKNOWN_VALIDATION_ERROR)
  }
}
