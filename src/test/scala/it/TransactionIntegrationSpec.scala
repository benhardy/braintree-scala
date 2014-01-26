package net.bhardy.braintree.scala.it

import org.scalatest.matchers.MustMatchers
import org.scalatest.FunSpec
import net.bhardy.braintree.scala.TransactionRequest
import net.bhardy.braintree.scala.gw.Success

/**
  */
class TransactionIntegrationSpec extends FunSpec with MustMatchers with GatewayIntegrationSpec {

  describe("very basic transaction creation") {
    onGatewayIt("creates a transaction") {
      gateway =>
        val request = new TransactionRequest().
          amount(BigDecimal("401.12")).
          creditCard.
            cardholderName("Fred Jones").
            number("4111111111111111").
            cvv("312").
            expirationDate("05/2015").
            done

        val result = gateway.transaction.sale(request)

        result match {
          case Success(transaction) => {
            transaction.id.length must be > 0
          }
          case other => fail("Expected success, got: " + other)
        }

    }


  }
}
