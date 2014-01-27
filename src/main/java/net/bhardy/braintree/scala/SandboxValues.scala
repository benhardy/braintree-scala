package net.bhardy.braintree.scala

import scala.math.BigDecimal

/**
 * Values for testing in the {@link Environment#SANDBOX SANDBOX} environment.
 */
object SandboxValues {

  sealed abstract class CreditCardNumber(val number: String) {}

  object CreditCardNumber {
    case object VISA extends CreditCardNumber("4111111111111111")
  }

  object TransactionAmount {
    val AUTHORIZE = BigDecimal("1000.00")
    val DECLINE = BigDecimal("2000.00")
    val FAILED = BigDecimal("3000.00")
  }
}