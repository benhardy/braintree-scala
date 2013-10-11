package com.braintreegateway

/**
 * Represents an validation error from the gateway.
 * @param attribute - the attribute that this error references, e.g. amount or expirationDate.
 */
case class ValidationError(attribute: String, code: ValidationErrorCode, message: String) {

  /**
   * Returns the {@link ValidationErrorCode} for the specific validation error.
   * @return a { @link ValidationErrorCode}.
   */
  def getCode: ValidationErrorCode = {
    code
  }

  /**
   * Returns the message associated with the validation error.  Messages may change over time; rely on {@link #getCode()} for comparisons.
   * @return a String for the message.
   */
  def getMessage: String = {
    message
  }
}