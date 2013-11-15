# Braintree Scala Client Library

The Braintree library provides integration access to the Braintree Gateway.

## Dependencies

* none

## Quick Start Example

    import com.braintreegateway._

    object BraintreeExample {
      def main(args: Array[String]) {
        val gateway = new BraintreeGateway(
            Environment.SANDBOX,
            "the_merchant_id",
            "the_public_key",
            "the_private_key"
        )

        val request = new TransactionRequest().
          amount(BigDecimal("1000.00")).
          creditCard.
            number("4111111111111111").
            expirationDate("05/2009").
            done

        val result = gateway.transaction.sale(request)

        result match {
          case Success(transaction) => {
            println("Success!: created transaction with id ${transaction.id}")
          }
          case Failure(errors, parameters, message, verification, transaction, subscription) => {
            val errorMessage = transaction.map(errorMessageFromTransaction).
                                getOrElse(errorMessageFromValidation(errors))
            println(errorMessage) 
          }
        }
      }

      def errorMessageFromValidation(errors: ValidationErrors): String = {
        errors.getAllDeepValidationErrors.map { error =>
           "Attribute: ${error.attribute}\n  Code: ${error.code}\n  Message: ${error.message}"
        }.mkString("\n")
      }

      def errorMessageFromTransaction(transaction:Transaction): String = {
        "Error processing transaction:\n" +
        "  Status: ${t.status}\n" +
        "  Code: ${t.processorResponseCode}" +
        "  Text: ${t.processorResponseText}"
      }
    }


## Documentation

 * TODO

## SBT

  With SBT installed, this package can be built simply by running this command:

     mvn package

  The resulting jar file will be produced in the directory named "target" under a subset of the scala version in use.

     e.g.   target/scala-2.10/braintree-scala_2.10-0.1.0-SNAPSHOT.jar

### In repositories:

     not yet, see github

### In dependencies


## License

See the LICENSE file.
