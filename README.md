# Scala Client Library for Braintree Payment gateway

The Braintree library provides integration access to the Braintree Gateway.

This is a fork and translation of Braintree's Java Client Library, and is a
work in progress. API is subject to change until version 1.0 is released.

## Important Notes

This software is not a product of Braintree Payments and as such they are not
liable for its support.

## Compatibility

This library currently has the same level of functionality as Braintree's Java
Client Library version 2.24.0.

## Design goals and their status

1. 100% compatible fully featured client library which supports all operations the Java one does.
   (Currently a few releases behind)

2. Composable gateway operations
   - use gateway operations in for-comprehensions (monadically)
   - done for create, update and delete operations
   - Result type has chainable Success, Failure and Deleted case classes
   - TODO for find. requires refactoring NotFoundException to a NotFound Result type

3. Elimination of null usage
   - much work to do here
   - XML rendering has been refactored to handle Option types
   - client library needs some awareness of which values can never legally be absent
   - TODO decide how to handle absent field for something like gateway-generated ids or timestamps:
     - cause an IllegalStateException, IllegalArgumentException, UnexpectedException?
     - cause no object to be returned (as None)? (somewhat inconvenient)
     - let absent required String fields be empty Strings? (half-assed)
     - make all fields Optional? (inconvenient)
     - let the user deal with it? (no way)
     - current behaviour carried over from java is to throw NPE

4. No external dependencies other than the language [done, carried through from original java]
   - apache.commons.codec is vendored in with this
   - side effect is using Calendar for dates
   - considering making a forked that uses more normal dependency management and Joda Time

4. Take advantage of Scala native XML support (TODO)
   - so far have only done this to a limited extent

5. Use scala collections [done, except for ResourceCollection (TODO) ]

6. Minimal usage of implicits
   - to be used mainly for eliminating ceremony or increasing obviousness of operations
   - e.g. used in CalendarUtils to make Calendar less of a bear to deal with

7. Decide whether to be compatible with Scala 2.8/2.9
   - Currently only really using String interpolation features in Scala 2.10
   - may use macros in future but for now YAGNI

8. Restore rest of integration tests

## Dependencies

* none other than Scala itself.  (currently)

## Quick Start Example

    import net.bhardy.braintree.scala._
    import net.bhardy.braintree.scala.gw._
    import scala.math.BigDecimal

    object BraintreeExample {
      def main(args: Array[String]) {
        val gateway = new BraintreeGateway(
            Environment.SANDBOX,
            "your_merchant_id",
            "your_public_key",
            "your_private_key"
        )

        val request = new TransactionRequest().
          amount(BigDecimal("1000.00")).
          creditCard.
            number("4111111111111111").
            expirationDate("05/2015").
            done

        val result = gateway.transaction.sale(request)

        // you can use pattern matching to pull information out of results easily
        // or just use result.isSuccess to check success
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

      // example error message generator, you don't have to do this

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

 * TODO, but in the meantime, the Braintree's Java docs can provide some guidance as to
   what options are available: https://www.braintreepayments.com/docs/java

At a high level, in your application, create a single instance of the BraintreeGateway
object using your supplied merchant id, public key and private key. Then use that as a starting
point for request operations. It contains factories for other gateways which can process
various kinds of Request objects.

Operations can be composed monadically and used in for-comprehensions, e.g: create a customer
then a transaction for that customer:

    val result = for {
        customer <- gateway.customer.create(
            new CustomerRequest().
                firstName("Michael").
                lastName("Angelo").
                company("Some Company")
        )
        sale <- gateway.transaction.sale(
            new TransactionRequest().
                amount(BigDecimal("1000.00")).
                customerId(customer.id).
                creditCard.
                    cardholderName("Bob the Builder").
                    number(cardNumber).
                    expirationDate("05/2015").
                    done.
                options.
                    storeInVault(true).
                    done
        )
    } yield sale

If the first operation fails, the second operation will not execute. If either fails,
a Failure state will be yielded.

All operations will return some kind of Result object, which has only three possible
implementations: Success, Failure and Deleted (so you can pattern match).
Success contains a target (a created or modified object).
Failure contains Validation errors and other useful information.
Deleted is a kind of success that yeilds nothing.
See the Result class for more details.


## SBT

  With SBT installed, this package can be built simply by running this command:

     sbt package

  The resulting jar file will be produced in the directory named "target" under a subset of the scala version in use.

     e.g.   target/scala-2.10/braintree-scala_2.10-0.1.0-SNAPSHOT.jar

### In repositories:

     not yet, see github

### In dependencies


## License

See the LICENSE file.
