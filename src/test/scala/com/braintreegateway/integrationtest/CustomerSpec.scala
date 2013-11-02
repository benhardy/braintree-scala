package com.braintreegateway.integrationtest

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.MustMatchers
import com.braintreegateway._
import exceptions.{NotFoundException, ForgedQueryStringException}
import gw.{Deleted, Success, Failure}
import java.util.{Random, Calendar}
import search.CustomerSearchRequest
import test.VenmoSdk
import testhelpers.{CalendarHelper, TestHelper, GatewaySpec}
import CalendarHelper._
import com.braintreegateway.CreditCardRequest.ForCustomer


@RunWith(classOf[JUnitRunner])
class CustomerSpec extends GatewaySpec with MustMatchers {

  describe("create") {
    onGatewayIt("creates a customer") { gateway =>
      val request = new CustomerRequest().firstName("Mark").lastName("Jones").company("Jones Co.").
        email("mark.jones@example.com").fax("419-555-1234").phone("614-555-1234").website("http://example.com")
      val result = gateway.customer.create(request)
      result must be('success)  // todo probably redundant
      val customer = result match { case Success(cust) => cust }
      customer.getFirstName must be === "Mark"
      customer.getLastName must be === "Jones"
      customer.getCompany must be === "Jones Co."
      customer.getEmail must be === "mark.jones@example.com"
      customer.getFax must be === "419-555-1234"
      customer.getPhone must be === "614-555-1234"
      customer.getWebsite must be === "http://example.com"
      val thisYear = now.year
      customer.getCreatedAt.year must be === thisYear
      customer.getUpdatedAt.year must be === thisYear
    }

    onGatewayIt("populates with blanks if given nothing") { gateway =>
      val request = new CustomerRequest
      val result = gateway.customer.create(request)
      result must be('success)
      val customer = result match { case Success(cust) => cust }
      customer.getFirstName must be === null
      customer.getLastName must be === null
      customer.getCompany must be === null
      customer.getEmail must be === null
      customer.getFax must be === null
      customer.getPhone must be === null
      customer.getWebsite must be === null
    }

    onGatewayIt("it uses security params") { gateway =>
      val request = new CustomerRequest().creditCard.cardholderName("Fred Jones").number("5105105105105100").cvv("123").expirationDate("05/12").deviceSessionId("abc123").done
      val result = gateway.customer.create(request)
      result must be('success)
    }

    onGatewayIt("populates custom fields") { gateway =>
      val request = new CustomerRequest().customField("store_me", "custom value").customField("another_stored_field", "custom value2")
      val result = gateway.customer.create(request)
      result must be('success)

      val expected: java.util.Map[String, String] = new java.util.HashMap[String, String]
      expected.put("store_me", "custom value")
      expected.put("another_stored_field", "custom value2")

      result match {
        case Success(customer) => {
          customer.getCustomFields must be === expected
        }
      }
    }

    onGatewayIt("createWithCreditCard") { gateway =>
      val request = new CustomerRequest
      request.firstName("Fred").creditCard.cardholderName("Fred Jones").number("5105105105105100").cvv("123").expirationDate("05/12").done.lastName("Jones")
      val result = gateway.customer.create(request)
      result match {
        case Success(customer) => {
          customer.getFirstName must be === "Fred"
          customer.getLastName must be === "Jones"
          customer.creditCards.size must be === 1
          val creditCard = customer.creditCards(0)
          creditCard.getCardholderName must be === "Fred Jones"
          creditCard.getBin must be === "510510"
          creditCard.getLast4 must be === "5100"
          creditCard.getExpirationDate must be === "05/2012"
          creditCard.getUniqueNumberIdentifier must fullyMatch regex "\\A\\w{32}\\z"
        }
      }
      result must be('success) // todo probably redundant
    }

    onGatewayIt("createWithDuplicateCreditCard") { gateway =>
      val customerRequest = new CustomerRequest
      customerRequest.firstName("Fred").creditCard.cardholderName("John Doe").number("4012000033330026").cvv("200").expirationDate("05/12").options.failOnDuplicatePaymentMethod(true).done.done.lastName("Jones")
      gateway.customer.create(customerRequest)
      val result = gateway.customer.create(customerRequest)
      result match {
        case Failure(errors,_,_,_,_,_) => {
          val code = errors.forObject("customer").forObject("creditCard").onField("number")(0).code
          code must be === ValidationErrorCode.CREDIT_CARD_DUPLICATE_CARD_EXISTS
        }
      }
      result must not be ('success)   // todo probably redundant
    }

    onGatewayIt("createWithValidCreditCardAndVerification") { gateway =>
      val request = new CustomerRequest().firstName("Fred").lastName("Jones").
        creditCard.cardholderName("Fred Jones").number("4111111111111111").cvv("123").expirationDate("05/12").
        options.verifyCard(true).done.done
      val result = gateway.customer.create(request)
      result match {
        case Success(customer) => {
          customer.getFirstName must be === "Fred"
          customer.getLastName must be === "Jones"
          customer.creditCards.size must be === 1
          val creditCard = customer.creditCards(0)
          creditCard.getCardholderName must be === "Fred Jones"
          creditCard.getBin must be === "411111"
          creditCard.getLast4 must be === "1111"
          creditCard.getExpirationDate must be === "05/2012"
        }
      }
      result must be('success)   // probably redundant todo

    }

    onGatewayIt("createWithInvalidCreditCardAndVerification") { gateway =>
      val request = new CustomerRequest
      request.firstName("Fred").creditCard.cardholderName("Fred Jones").number("5105105105105100").cvv("123").expirationDate("05/12").options.verifyCard(true).done.done.lastName("Jones")
      val result = gateway.customer.create(request)
      result must not be ('success)
      result match {
        case Failure(_,_,_,Some(verification),_,_) => {
          verification.status must be === CreditCardVerification.Status.PROCESSOR_DECLINED
        }
      }
    }

    onGatewayIt("createWithCreditCardAndBillingAddress") { gateway =>
      val request = new CustomerRequest().firstName("Fred").creditCard.
        cardholderName("Fred Jones").number("5105105105105100").cvv("123").expirationDate("05/12").
          billingAddress.streetAddress("1 E Main St").extendedAddress("Unit 2").locality("Chicago").region("Illinois").
          postalCode("60607").countryName("United States of America").countryCodeAlpha2("US").countryCodeAlpha3("USA").
          countryCodeNumeric("840").done.
        done.lastName("Jones")

      val result = gateway.customer.create(request)
      result must be('success)     // todo possibly redundant
      result match {
        case Success(customer) => {
          customer.getFirstName must be === "Fred"
          customer.getLastName must be === "Jones"
          customer.creditCards.size must be === 1
          val creditCard = customer.creditCards(0)
          creditCard.getCardholderName must be === "Fred Jones"
          creditCard.getBin must be === "510510"
          creditCard.getLast4 must be === "5100"
          creditCard.getExpirationDate must be === "05/2012"
          val billingAddress = creditCard.getBillingAddress
          billingAddress.getStreetAddress must be === "1 E Main St"
          billingAddress.getExtendedAddress must be === "Unit 2"
          billingAddress.getLocality must be === "Chicago"
          billingAddress.getRegion must be === "Illinois"
          billingAddress.getPostalCode must be === "60607"
          billingAddress.getCountryName must be === "United States of America"
          billingAddress.getCountryCodeAlpha2 must be === "US"
          billingAddress.getCountryCodeAlpha3 must be === "USA"
          billingAddress.getCountryCodeNumeric must be === "840"
          customer.getAddresses.size must be === 1
          val address = customer.getAddresses(0)
          address.getStreetAddress must be === "1 E Main St"
          address.getExtendedAddress must be === "Unit 2"
          address.getLocality must be === "Chicago"
          address.getRegion must be === "Illinois"
          address.getPostalCode must be === "60607"
          address.getCountryName must be === "United States of America"
        }
      }
    }

    onGatewayIt("createWithCreditCardAndBillingAddressWithErrors") { gateway =>
      val request = new CustomerRequest().firstName("Fred").creditCard.cardholderName("Fred Jones").number("5105105105105100").cvv("123").expirationDate("05/12").billingAddress.countryName("United States of America").countryCodeAlpha2("MX").done.done
      val result = gateway.customer.create(request)
      result match {
        case Failure(errors,_,_,_,_,_)  => {
          val code = errors.forObject("customer").forObject("creditCard").forObject("billingAddress").onField("base")(0).code
          code must be === ValidationErrorCode.ADDRESS_INCONSISTENT_COUNTRY
        }
      }
    }
  }

  describe("create with VenmoSdk") {
    onGatewayIt("can create with PaymentMethodCode") { gateway =>
      val request = new CustomerRequest().firstName("Fred").
        creditCard.venmoSdkPaymentMethodCode(VenmoSdk.generateTestPaymentMethodCode("5105105105105100")).done
      val result = gateway.customer.create(request)
      result match {
        case Success(customer) => {
          customer.creditCards(0).getBin must be === "510510"
        }
      }
    }

    onGatewayIt("can create with session") { gateway =>
      val request = new CustomerRequest().firstName("Fred").creditCard.number("5105105105105100").cvv("123").
        expirationDate("05/12").options.venmoSdkSession(VenmoSdk.Session.Valid.value).done.done
      val result = gateway.customer.create(request)
      result match {
        case Success(customer) => {
          customer.creditCards(0) must be('venmoSdk)
        }
      }
    }
  }

  describe("create via TransparentRedirect") {
    onGatewayIt("creates a customer") { gateway =>
      val trParams = new CustomerRequest
      val request = new CustomerRequest().firstName("John").lastName("Doe")
      val trCreateUrl = gateway.transparentRedirect.url
      val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request, trCreateUrl)
      val result = gateway.transparentRedirect.confirmCustomer(queryString)
      result must be('success)     // todo probably redundant
      result match {
        case Success(customer) => {
          customer.getFirstName must be === "John"
          customer.getLastName must be === "Doe"
        }
      }
    }

    onGatewayIt("ThrowsWhenQueryStringHasBeenTamperedWith") { gateway =>
      val trCreateUrl = gateway.transparentRedirect.url
      val queryString = TestHelper.simulateFormPostForTR(gateway, new CustomerRequest, new CustomerRequest, trCreateUrl)
      intercept[ForgedQueryStringException] {
        gateway.transparentRedirect.confirmCustomer(queryString + "this make it invalid")
      }
    }

    onGatewayIt("supports nesting") { gateway =>
      val trParams = new CustomerRequest
      val request = new CustomerRequest().firstName("John").lastName("Doe").creditCard.number("4111111111111111").
        expirationDate("11/12").done
      val trCreateUrl = gateway.transparentRedirect.url
      val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request, trCreateUrl)
      val result = gateway.transparentRedirect.confirmCustomer(queryString)
      result must be('success)  // todo probably redundant
      result match {
        case Success(customer) => {
          customer.getFirstName must be === "John"
          customer.getLastName must be === "Doe"
          customer.creditCards(0).getLast4 must be === "1111"
        }
      }
    }

    onGatewayIt("can create customer") { gateway =>
      val request = new CustomerRequest().firstName("John")
      val trParams = new CustomerRequest().lastName("Fred").creditCard.cardholderName("Fred Jones").
        number("5105105105105100").cvv("123").expirationDate("05/12").
        billingAddress.countryName("United States of America").countryCodeAlpha2("US").
        countryCodeAlpha3("USA").countryCodeNumeric("840").done.done
      val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request, gateway.transparentRedirect.url)
      val result = gateway.transparentRedirect.confirmCustomer(queryString)
      result must be('success)  // todo probably redundant
      result match {
        case Success(customer) => {
          customer.getFirstName must be === "John"
          customer.getLastName must be === "Fred"
          val address = customer.creditCards(0).getBillingAddress
          address.getCountryName must be === "United States of America"
          address.getCountryCodeAlpha2 must be === "US"
          address.getCountryCodeAlpha3 must be === "USA"
          address.getCountryCodeNumeric must be === "840"
        }
      }
    }

    onGatewayIt("detected validation errors") { gateway =>
      val request = new CustomerRequest().firstName("John")
      val trParams = new CustomerRequest().lastName("Fred").creditCard.cardholderName("Fred Jones").
        number("5105105105105100").cvv("123").expirationDate("05/12").
        billingAddress.countryName("United States of America").countryCodeAlpha2("MX").done.done
      val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request, gateway.transparentRedirect.url)
      val result = gateway.transparentRedirect.confirmCustomer(queryString)
      result must not be ('success)
      result match {
        case Failure(errors,_,_,_,_,_) => {
          val code = errors.forObject("customer").forObject("creditCard").forObject("billingAddress").
            onField("base")(0).code
          code must be === ValidationErrorCode.ADDRESS_INCONSISTENT_COUNTRY
        }
      }
    }
  }

  describe("find") {
    onGatewayIt("can find a single id") { gateway =>
      val customer = gateway.customer.create(new CustomerRequest) match { case Success(c) => c }
      val foundCustomer = gateway.customer.find(customer.getId)
      foundCustomer.getId must be === customer.getId
    }

    onGatewayIt("throws NotFoundException with empty id list") { gateway =>
      intercept[NotFoundException] {
        gateway.customer.find(" ")
      }
    }

    onGatewayIt("finds duplicate cards with paymentMethodTokenWithDuplicates ") { gateway =>
      val request = new CustomerRequest().creditCard.number("4012000033330026").expirationDate("05/2010").done
      import scala.collection.JavaConversions.iterableAsScalaIterable
      val result = for {
        jim <- gateway.customer.create(request.firstName("Jim"))
        joe <- gateway.customer.create(request.firstName("Joe"))
      } yield (jim,joe)
      result match {
        case Success((jim,joe)) => {
          val searchRequest = new CustomerSearchRequest().paymentMethodTokenWithDuplicates.is(jim.creditCards(0).getToken)
          val collection = gateway.customer.search(searchRequest)
          val customerIds = collection.map { _.getId }
          customerIds must contain(jim.getId)
          customerIds must contain(joe.getId)
        }
      }
    }
  }

  describe("search") {
    onGatewayIt("can search on all text fields") { gateway =>
      val creditCardToken = new Random().nextInt.toString
      val request = new CustomerRequest().firstName("Timmy").lastName("O'Toole").company("O'Toole and Sons").
        email("timmy@example.com").website("http://example.com").fax("3145551234").phone("5551231234").
        creditCard.cardholderName("Tim Toole").number("4111111111111111").expirationDate("05/2010").token(creditCardToken).
        billingAddress.firstName("Thomas").lastName("Otool").streetAddress("1 E Main St").extendedAddress("Suite 3").
        locality("Chicago").region("Illinois").postalCode("60622").countryName("United States of America").done.done

      val customer = gateway.customer.create(request) match { case Success(c:Customer) => c }

      val searchRequest = new CustomerSearchRequest().id.is(customer.getId).firstName.is("Timmy").
        lastName.is("O'Toole").company.is("O'Toole and Sons").email.is("timmy@example.com").
        phone.is("5551231234").fax.is("3145551234").website.is("http://example.com").
        addressFirstName.is("Thomas").addressLastName.is("Otool").addressStreetAddress.is("1 E Main St").
        addressPostalCode.is("60622").addressExtendedAddress.is("Suite 3").addressLocality.is("Chicago").
        addressRegion.is("Illinois").addressCountryName.is("United States of America").
        paymentMethodToken.is(creditCardToken).cardholderName.is("Tim Toole").
        creditCardNumber.is("4111111111111111").creditCardExpirationDate.is("05/2010")

      val collection = gateway.customer.search(searchRequest)

      collection.getMaximumSize must be === 1
      collection.getFirst.getId must be === customer.getId
    }

    onGatewayIt("can search on createdAt") { gateway =>
      val request = new CustomerRequest
      val customer = gateway.customer.create(request) match { case Success(c) => c }
      val createdAt = customer.getCreatedAt
      val threeHoursEarlier = createdAt - 3.hours
      val oneHourEarlier = createdAt - 1.hours
      val oneHourLater = createdAt + 1.hours

      def searchCustomerCreatedAt = {
        new CustomerSearchRequest().id.is(customer.getId).createdAt
      }
      var searchRequest = searchCustomerCreatedAt.between(oneHourEarlier, oneHourLater)
      gateway.customer.search(searchRequest).getMaximumSize must be === 1

      searchRequest = searchCustomerCreatedAt.greaterThanOrEqualTo(oneHourEarlier)
      gateway.customer.search(searchRequest).getMaximumSize must be === 1

      searchRequest = searchCustomerCreatedAt.lessThanOrEqualTo(oneHourLater)
      gateway.customer.search(searchRequest).getMaximumSize must be === 1

      searchRequest = searchCustomerCreatedAt.between(threeHoursEarlier, oneHourEarlier)
      gateway.customer.search(searchRequest).getMaximumSize must be === 0
    }
  }

  describe("update") {
    onGatewayIt("can update fields") { gateway =>
      val createRequest = new CustomerRequest().firstName("Mark").lastName("Jones").company("Jones Co.").
        email("mark.jones@example.com").fax("419-555-1234").phone("614-555-1234").website("http://example.com")
      val updateRequest = new CustomerRequest().firstName("Drew").lastName("Olson").company("Braintree").
        email("drew.olson@example.com").fax("555-555-5555").phone("555-555-5554").website("http://getbraintree.com")

      val updateResult = for {
        customer <- gateway.customer.create(createRequest)
        updatedCustomer <- gateway.customer.update(customer.getId, updateRequest)
      } yield updatedCustomer

      updateResult must be('success)
      updateResult match {
        case Success(updatedCustomer) => {
          updatedCustomer.getFirstName must be === "Drew"
          updatedCustomer.getLastName must be === "Olson"
          updatedCustomer.getCompany must be === "Braintree"
          updatedCustomer.getEmail must be === "drew.olson@example.com"
          updatedCustomer.getFax must be === "555-555-5555"
          updatedCustomer.getPhone must be === "555-555-5554"
          updatedCustomer.getWebsite must be === "http://getbraintree.com"
        }
      }
    }

    onGatewayIt("can update with existing card and address") { gateway =>
      val request = new CustomerRequest().firstName("Mark").lastName("Jones").company("Jones Co.").
        email("mark.jones@example.com").fax("419-555-1234").phone("614-555-1234").website("http://example.com").
        creditCard.number("4111111111111111").expirationDate("12/12").billingAddress.postalCode("44444").done.done

      val updateResult = for {
        customer <- gateway.customer.create(request)

        creditCard = customer.creditCards(0)

        updateRequest = new CustomerRequest().firstName("Jane").lastName("Doe").creditCard.expirationDate("10/10").
          options.updateExistingToken(creditCard.getToken).done.
          billingAddress.postalCode("11111").countryName("Kiribati").countryCodeAlpha2("KI").countryCodeAlpha3("KIR").
          countryCodeNumeric("296").options.updateExisting(true).done.done.done

        updatedCustomer <- gateway.customer.update(customer.getId, updateRequest)

      } yield updatedCustomer

      updateResult match {
        case Success(updatedCustomer) => {
          val updatedCreditCard = updatedCustomer.creditCards(0)
          val updatedAddress = updatedCreditCard.getBillingAddress
          updatedCustomer.getFirstName must be === "Jane"
          updatedCustomer.getLastName must be === "Doe"
          updatedCreditCard.getExpirationDate must be === "10/2010"
          updatedAddress.getPostalCode must be === "11111"
          updatedAddress.getCountryName must be === "Kiribati"
          updatedAddress.getCountryCodeAlpha2 must be === "KI"
          updatedAddress.getCountryCodeAlpha3 must be === "KIR"
          updatedAddress.getCountryCodeNumeric must be === "296"
        }
      }

    }

    onGatewayIt("updates With New Credit Card And Existing Address") { gateway =>
      val result = for {
        customer <- gateway.customer.create(new CustomerRequest)

        addressRequest = new AddressRequest().firstName("John")

        address <- gateway.address.create(customer.getId, addressRequest)

        updateRequest = new CustomerRequest().creditCard.number("4111111111111111").expirationDate("12/12").
           billingAddressId(address.getId).done

        updatedCustomer <- gateway.customer.update(customer.getId, updateRequest)
      } yield (updatedCustomer, address)

      result match {
        case Success((updatedCustomer, address)) => {
          val updatedAddress = updatedCustomer.creditCards(0).getBillingAddress
          updatedAddress.getId must be === address.getId
          updatedAddress.getFirstName must be === "John"
        }
      }
    }

    onGatewayIt("rejects invalid card updates with validation errors") { gateway =>
      val creationRequest = new CustomerRequest().firstName("Mark").lastName("Jones").company("Jones Co.").
        email("mark.jones@example.com").fax("419-555-1234").phone("614-555-1234").website("http://example.com").
        creditCard.number("4111111111111111").expirationDate("12/12").billingAddress.postalCode("44444").done.done
      val updateRequest = new CustomerRequest().firstName("Janie").lastName("Dylan").
        creditCard.billingAddress.countryCodeAlpha2("KI").countryCodeAlpha3("USA").done.done

      val result = for {
        customer <- gateway.customer.create(creationRequest)
        updatedCustomer <- gateway.customer.update(customer.getId, updateRequest)
      } yield updatedCustomer

      result must not be ('success)
      result match {
        case Failure(errors,_,_,_,_,_) => {
          val code = errors.forObject("customer").forObject("creditCard").forObject("billingAddress").
            onField("base")(0).code
          code must be === ValidationErrorCode.ADDRESS_INCONSISTENT_COUNTRY
        }
      }
    }
  }

  describe("update via transparent redirect") {
    onGatewayIt("works with existing CreditCard and Address") { gateway =>
      val request = new CustomerRequest().firstName("Mark").lastName("Jones").company("Jones Co.").email("mark.jones@example.com").fax("419-555-1234").phone("614-555-1234").website("http://example.com").creditCard.number("4111111111111111").expirationDate("12/12").billingAddress.postalCode("44444").done.done
      val result = for {
        customer <- gateway.customer.create(request)
        creditCard = customer.creditCards(0)

        trParams = new CustomerRequest().customerId(customer.getId).firstName("Jane").lastName("Doe").
          creditCard.expirationDate("10/10").options.updateExistingToken(creditCard.getToken).done.
          billingAddress.postalCode("11111").options.updateExisting(true).done.done.done

        queryString = TestHelper.simulateFormPostForTR(gateway, trParams, new CustomerRequest, gateway.transparentRedirect.url)

        updatedCustomer <- gateway.transparentRedirect.confirmCustomer(queryString)
      } yield updatedCustomer

      result match {
        case Success(updatedCustomer) => {
          val updatedCreditCard = updatedCustomer.creditCards(0)
          val updatedAddress = updatedCreditCard.getBillingAddress
          updatedCustomer.getFirstName must be === "Jane"
          updatedCustomer.getLastName must be === "Doe"
          updatedCreditCard.getExpirationDate must be === "10/2010"
          updatedAddress.getPostalCode must be === "11111"
        }
      }
    }

    onGatewayIt("can add update customer fields") { gateway =>
      val createRequest = new CustomerRequest().firstName("Mark").lastName("Jones").company("Jones Co.").
        email("mark.jones@example.com").fax("419-555-1234").phone("614-555-1234").website("http://example.com")

      val updateRequest = new CustomerRequest().firstName("Drew").lastName("Olson").company("Braintree").
        email("drew.olson@example.com").fax("555-555-5555").phone("555-555-5554").website("http://getbraintree.com")

      val result = for {
        createdCustomer <- gateway.customer.create(createRequest)

        trParams = new CustomerRequest().customerId(createdCustomer.getId)
        queryString = TestHelper.simulateFormPostForTR(gateway, trParams, updateRequest, gateway.transparentRedirect.url)

        updated <- gateway.transparentRedirect.confirmCustomer(queryString)
      } yield updated

      result match {
        case Success(customer) => {
          customer.getFirstName must be === "Drew"
          customer.getLastName must be === "Olson"
          customer.getCompany must be === "Braintree"
          customer.getEmail must be === "drew.olson@example.com"
          customer.getFax must be === "555-555-5555"
          customer.getPhone must be === "555-555-5554"
          customer.getWebsite must be === "http://getbraintree.com"
        }
      }
    }

    onGatewayIt("can update customer address") { gateway =>
      val createRequest = new CustomerRequest().firstName("John").lastName("Doe").
        creditCard.
          number("4111111111111111").expirationDate("12/12").
          billingAddress.
            countryName("United States of America").countryCodeAlpha2("US").countryCodeAlpha3("USA").
            countryCodeNumeric("840").
            done.
          done

      val result = for {
        originalCustomer <- gateway.customer.create(createRequest)

        updateRequest = new CustomerRequest().firstName("Jane")
        trParams = new CustomerRequest().customerId(originalCustomer.getId).lastName("Dough").creditCard.options.
          updateExistingToken(originalCustomer.creditCards(0).getToken).done.billingAddress.countryName("Mexico").
          countryCodeAlpha2("MX").countryCodeAlpha3("MEX").countryCodeNumeric("484").options.updateExisting(true).
          done.done.done
        queryString = TestHelper.simulateFormPostForTR(gateway, trParams, updateRequest, gateway.transparentRedirect.url)

        updatedCustomer <- gateway.transparentRedirect.confirmCustomer(queryString)
      } yield(originalCustomer.getId)

      result match {
        case Success(customerId) => {
          val updatedCustomer = gateway.customer.find(customerId)
          updatedCustomer.getFirstName must be === "Jane"
          updatedCustomer.getLastName must be === "Dough"
          val address = updatedCustomer.creditCards(0).getBillingAddress
          address.getCountryName must be === "Mexico"
          address.getCountryCodeAlpha2 must be === "MX"
          address.getCountryCodeAlpha3 must be === "MEX"
          address.getCountryCodeNumeric must be === "484"
        }
      }
    }

    onGatewayIt("handles address validation errors on customer address updates") { gateway =>
      val request = new CustomerRequest().firstName("John").lastName("Doe").creditCard.number("4111111111111111").
        expirationDate("12/12").billingAddress.countryName("United States of America").countryCodeAlpha2("US").
        countryCodeAlpha3("USA").countryCodeNumeric("840").done.done

      val result = for {
        customer <- gateway.customer.create(request)

        updateRequest = new CustomerRequest().firstName("Jane")
        trParams = new CustomerRequest().customerId(customer.getId).creditCard.billingAddress.countryCodeAlpha2("MX").
          countryCodeAlpha3("USA").done.done
        queryString = TestHelper.simulateFormPostForTR(gateway, trParams, updateRequest, gateway.transparentRedirect.url)

        updated <- gateway.transparentRedirect.confirmCustomer(queryString)
      } yield updated

      result must not be ('success)
      result match {
        case Failure(errors,_,_,_,_,_) => {
          val code = errors.forObject("customer").forObject("creditCard").forObject("billingAddress").
            onField("base")(0).code
          code must be === ValidationErrorCode.ADDRESS_INCONSISTENT_COUNTRY
        }
      }
    }
  }

  describe("update misc cases") {
    onGatewayIt("can update token") { gateway =>
      val rand = new Random
      val oldId = String.valueOf(rand.nextInt)
      val request = new CustomerRequest().id(oldId)
      val result = for {
        customer <- gateway.customer.create(request)

        newId = String.valueOf(rand.nextInt)
        updateRequest = new CustomerRequest().id(newId)

        updatedCustomer <- gateway.customer.update(customer.getId, updateRequest)
      } yield(updatedCustomer, newId)

      result match {
        case Success((updatedCustomer, newId)) => {
          updatedCustomer.getId must be === newId
        }
      }
    }

    onGatewayIt("can perform selective field updates") { gateway =>
      val request = new CustomerRequest().firstName("Mark").lastName("Jones").company("Jones Co.").
        email("mark.jones@example.com").fax("419-555-1234").phone("614-555-1234").website("http://example.com")

      val result = for {
        customer <- gateway.customer.create(request)

        updateRequest = new CustomerRequest().lastName("Olson").company("Braintree")
        updated <- gateway.customer.update(customer.getId, updateRequest)
      } yield updated

      result match {
        case Success(updatedCustomer) => {
          updatedCustomer.getFirstName must be === "Mark"
          updatedCustomer.getLastName must be === "Olson"
          updatedCustomer.getCompany must be === "Braintree"
          updatedCustomer.getEmail must be === "mark.jones@example.com"
          updatedCustomer.getFax must be === "419-555-1234"
          updatedCustomer.getPhone must be === "614-555-1234"
          updatedCustomer.getWebsite must be === "http://example.com"
        }
      }
    }
  }

  describe("delete") {
    onGatewayIt("causes customer to become unfindable") { gateway =>
      val request = new CustomerRequest().firstName("Mark").lastName("Jones").company("Jones Co.").
        email("mark.jones@example.com").fax("419-555-1234").phone("614-555-1234").website("http://example.com")
      val customer = gateway.customer.create(request) match { case Success(cust) => cust }
      val result = gateway.customer.delete(customer.getId)
      result match {
        case Deleted => {
          intercept[NotFoundException] {
            gateway.customer.find(customer.getId)
          }
        }
      }

    }
  }

  describe("all") {
    onGatewayIt("finds all customers") { gateway =>
      val resourceCollection = gateway.customer.all
      resourceCollection.getMaximumSize must be > 0
      resourceCollection.getFirst must not be null
    }
  }
}