package com.braintreegateway

import com.braintreegateway.util.NodeWrapper
import com.braintreegateway.util.StringUtils
import java.util._

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
 * Assert.assertEquals(ValidationErrorCode.TRANSACTION_AMOUNT_IS_REQUIRED, errors.forObject("transaction").onField("amount").get(0).getCode());
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

  val errors = new ArrayList[ValidationError]
  val nestedErrors = new HashMap[String, ValidationErrors]

  /** visible for test */
  private[braintreegateway] def addError(error: ValidationError) {
    errors.add(error)
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
    import scala.collection.JavaConversions._
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
    val errorsOnObject: ValidationErrors = nestedErrors.get(StringUtils.dasherize(objectName))
    if (errorsOnObject == null) {
      new ValidationErrors
    }
    else {
      errorsOnObject
    }
  }

  /**
   * Returns a List of all of the {@link ValidationError} on this object and
   * all nested objects.
   *
   * @return a List of { @link ValidationError} objects.
   */
  def getAllDeepValidationErrors: List[ValidationError] = {
    val result: List[ValidationError] = new ArrayList[ValidationError](errors)
    import scala.collection.JavaConversions._
    for (validationErrors <- nestedErrors.values) {
      result.addAll(validationErrors.getAllDeepValidationErrors)
    }
    result
  }

  /**
   * Returns a List of all of the {@link ValidationError} objects at the
   * current nesting level.
   *
   * @return a List of { @link ValidationError} objects.
   */
  def getAllValidationErrors: List[ValidationError] = {
    Collections.unmodifiableList(new ArrayList[ValidationError](errors))
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
    val list: List[ValidationError] = new ArrayList[ValidationError]
    import scala.collection.JavaConversions._
    for (error <- errors) {
      if (error.attribute == StringUtils.underscore(fieldName)) {
        list.add(error)
      }
    }
    list
  }

  private def populateErrors(node: NodeWrapper) {
    val workNode = if (node.getElementName == "api-error-response") {
      node.findFirst("errors")
    } else {
      node
    }
    val errorResponses: List[NodeWrapper] = workNode.findAll("*")
    import scala.collection.JavaConversions._
    for (errorResponse <- errorResponses) {
      if (!(errorResponse.getElementName == "errors")) {
        nestedErrors.put(errorResponse.getElementName, new ValidationErrors(errorResponse))
      }
      else {
        populateTopLevelErrors(errorResponse.findAll("error"))
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