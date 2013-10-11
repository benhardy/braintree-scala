package com.braintreegateway

import com.braintreegateway.util.NodeWrapper
import com.braintreegateway.util.StringUtils
import collection.mutable.ListBuffer
import scala.collection.mutable.{Map =>MutableMap}

/**
 * Represents an validation error from the gateway.
 * @param attribute - the attribute that this error references, e.g. amount or expirationDate.
 * @param code - the ValidationErrorCode for the specific validation error
 * @param message - Messages may change over time; rely on {@link #code()} for comparisons.
 */
case class ValidationError(attribute: String, code: ValidationErrorCode, message: String) {
}

/**
 * Represents a collection of (nested) validation errors. Query for validation
 * errors by object and field. For Example:
 *
 * <pre>
 * TransactionRequest request = new TransactionRequest().
 * amount(null).
 * Result<Transaction> result = gateway.transaction().sale(request);
 * Assert.assertFalse(result.isSuccess());
 * ValidationErrors errors = result.getErrors();
 * Assert.assertEquals(ValidationErrorCode.TRANSACTION_AMOUNT_IS_REQUIRED, errors.forObject("transaction").onField("amount").get(0).code());
 * </pre>
 *
 * For more detailed information on {@link ValidationErrors}, see <a
 * href="http://www.braintreepayments.com/gateway/validation-errors"
 * target="_blank">http://www.braintreepayments.com/gateway/validation-errors
 * </a>
 */
class ValidationErrors private(nodeOption: Option[NodeWrapper]) {
  def this(node:NodeWrapper) = {
    this(Some(node))
    populateErrors(node)
  }
  def this() = this(None)

  val errors = new ListBuffer[ValidationError]
  val nestedErrors = MutableMap[String, ValidationErrors]()

  /** visible for test */
  private[braintreegateway] def addError(error: ValidationError) {
    errors += error
  }

  /** visible for test */
  def addErrors(objectName: String, errors: ValidationErrors) {
    nestedErrors.put(objectName, errors)
  }

  /**
   * Returns the number of errors on this object and all nested objects.
   *
   * @see #size()
   * @return the number of errors.
   */
  def deepSize: Int = {
    errors.size + nestedErrors.values.toStream.map { _.deepSize }.sum
  }

  def forIndex(index: Int): ValidationErrors = {
    forObject("index_" + index)
  }

  /**
   * Returns a {@link ValidationErrors} representing nested errors for the
   * given objectName.
   *
   * @param objectName
     * the name of the object with nested validation errors, e.g.
   *   customer or creditCard.
   * @return a { @link ValidationErrors} object.
   */
  def forObject(objectName: String): ValidationErrors = {
    nestedErrors.
      get(StringUtils.dasherize(objectName)).
      getOrElse { new ValidationErrors() }
  }

  /**
   * Returns a List of all of the {@link ValidationError} on this object and
   * all nested objects.
   *
   * @return a List of { @link ValidationError} objects.
   */
  def getAllDeepValidationErrors: List[ValidationError] = {
    val result = ListBuffer[ValidationError]()
    def addBranchErrors(v:ValidationErrors) {
      result ++= v.errors
      for (child <- v.nestedErrors.values) {
        addBranchErrors(child)
      }
    }
    addBranchErrors(this)
    result.toList
  }

  /**
   * Returns a List of all of the {@link ValidationError} objects at the
   * current nesting level.
   *
   * @return a List of { @link ValidationError} objects.
   */
  def getAllValidationErrors: List[ValidationError] = {
    errors.toList
  }

  /**
   * Returns a List of {@link ValidationError} objects for the given field.
   *
   * @param fieldName
     * the name of the field with errors, e.g. amount or
   *   expirationDate.
   * @return a List of { @link ValidationError} objects
   */
  def onField(fieldName: String): List[ValidationError] = {
    val field = StringUtils.underscore(fieldName)
    errors.filter(_.attribute == field).toList
  }

  private def populateErrors(node: NodeWrapper) {
    import scala.collection.JavaConversions._
    val workNode = if (node.getElementName == "api-error-response") {
      node.findFirst("errors")
    } else {
      node
    }
    val errorResponses: List[NodeWrapper] = workNode.findAll("*").toList
    for (errorResponse <- errorResponses) {
      if (!(errorResponse.getElementName == "errors")) {
        nestedErrors.put(errorResponse.getElementName, new ValidationErrors(errorResponse))
      }
      else {
        populateTopLevelErrors(errorResponse.findAll("error").toList)
      }
    }
  }

  private def populateTopLevelErrors(childErrors: List[NodeWrapper]) {
    import scala.collection.JavaConversions._
    for (childError <- childErrors) {
      val code: ValidationErrorCode = ValidationErrorCode.findByCode(childError.findString("code"))
      val message: String = childError.findString("message")
      errors.add(new ValidationError(childError.findString("attribute"), code, message))
    }
  }

  /**
   * Returns the number of errors on this object at the current nesting level.
   *
   * @see #deepSize()
   * @return the number of errors.
   */
  def size: Int = {
    errors.size
  }
}