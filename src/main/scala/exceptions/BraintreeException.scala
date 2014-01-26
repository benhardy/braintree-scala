package net.bhardy.braintree.scala.exceptions

object BraintreeException {
  private final val serialVersionUID: Long = 1L
}

class BraintreeException(message: String, cause: Throwable) extends RuntimeException(message, cause) {
  def this(cause:Throwable) = this(null, cause)

  def this(message: String) = this(message, null)

  def this() = this(null, null)
}

class AuthenticationException extends BraintreeException

object AuthenticationException {
  private final val serialVersionUID: Long = 1L
}

class AuthorizationException(message:String) extends BraintreeException(message)

object AuthorizationException {
  private final val serialVersionUID: Long = 1L
}

class DownForMaintenanceException extends BraintreeException

object DownForMaintenanceException {
  private final val serialVersionUID: Long = 1L
}

class ForgedQueryStringException extends BraintreeException

object ForgedQueryStringException {
  private final val serialVersionUID: Long = 1L
}

class InvalidSignatureException extends BraintreeException

object InvalidSignatureException {
  private final val serialVersionUID: Long = 1L
}

class NotFoundException extends BraintreeException

object NotFoundException {
  private final val serialVersionUID: Long = 1L
}

class ServerException extends BraintreeException

object ServerException {
  private final val serialVersionUID: Long = 1L
}

class UpgradeRequiredException extends BraintreeException

object UpgradeRequiredException {
  private final val serialVersionUID: Long = 1L
}

class UnexpectedException(message:String, cause:Throwable) extends BraintreeException(message, cause) {
  def this(message:String) = this(message, null)
}

object UnexpectedException {
  private final val serialVersionUID: Long = 1L
}
