package com.braintreegateway.integrationtest

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.MustMatchers
import com.braintreegateway._
import exceptions.{NotFoundException, ForgedQueryStringException}
import gw.{Deleted, Failure, BraintreeGateway, Success}
import test.{CreditCardDefaults, CreditCardNumbers, VenmoSdk}
import com.braintreegateway.testhelpers.{GatewaySpec, MerchantAccountTestConstants, TestHelper}
import java.util.Random
import java.math.BigDecimal
import scala.collection.JavaConversions._
import MerchantAccountTestConstants._
import TestHelper._
import com.braintreegateway.testhelpers.CalendarHelper._

@RunWith(classOf[JUnitRunner])
class CreditCardSpec extends FunSpec with MustMatchers with GatewaySpec {

  def createProcessingRulesGateway = {
    new BraintreeGateway(Environment.DEVELOPMENT, "processing_rules_merchant_id", "processing_rules_public_key", "processing_rules_private_key")
  }

  describe("transparentRedirect") {
    onGatewayIt("trData") {
      gateway =>
        val trData = gateway.trData(new CreditCardRequest, "http://example.com")
        trData must beValidTrData(gateway.configuration)
    }
  }

  describe("create") {
    onGatewayIt("populates expeted fields") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").cvv("123").
            number("5105105105105100").expirationDate("05/12");

          card <- gateway.creditCard.create(request)

        } yield (customer, card)

        result match {
          case Success((customer, card)) => {
            card.getCardholderName must be === "John Doe"
            card.getCardType must be === "MasterCard"
            card.getCustomerId must be === customer.getId
            card.getCustomerLocation must be === "US"
            card.getBin must be === "510510"
            card.getExpirationMonth must be === "05"
            card.getExpirationYear must be === "2012"
            card.getExpirationDate must be === "05/2012"
            card.getLast4 must be === "5100"
            card.getMaskedNumber must be === "510510******5100"
            card.getToken must not be null
            val thisYear = now.year
            card.getCreatedAt.year must be === thisYear
            card.getUpdatedAt.year must be === thisYear
            card.getUniqueNumberIdentifier.matches("\\A\\w{32}\\z") must be === true
            card must not be ('venmoSdk)
            card.getImageUrl.matches(".*png.*") must be === true
          }
        }
    }

    onGatewayIt("sets card expiration dates correctly") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").
            cvv("123").number("5105105105105100").expirationMonth("06").expirationYear("13")
          card <- gateway.creditCard.create(request)
        } yield (customer, card)

        result match {
          case Success((customer, card)) => {
            card.getCustomerId must be === customer.getId
            card.getExpirationMonth must be === "06"
            card.getExpirationYear must be === "2013"
            card.getExpirationDate must be === "06/2013"
            val thisYear = now.year
            card.getCreatedAt.year must be === thisYear
            card.getUpdatedAt.year must be === thisYear
          }
        }
    }

    onGatewayIt("reproduces XML chars in cardholder name") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.getId).cardholderName("Special Chars <>&\"'").
            number("5105105105105100").expirationDate("05/12")

          card <- gateway.creditCard.create(request)
        } yield card

        result match {
          case Success(card) => {
            card.getCardholderName must be === "Special Chars <>&\"'"
          }
        }
    }

    onGatewayIt("processes security parameters") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.getId).cardholderName("Special Chars").
            number("5105105105105100").expirationDate("05/12").deviceSessionId("abc123")

          card <- gateway.creditCard.create(request)
        } yield card

        result match {
          case Success(card) =>
        }
    }

    describe("Address usage") {
      onGatewayIt("can add address on card create") {
        gateway =>
          val result = for {
            customer <- gateway.customer.create(new CustomerRequest)
            request = new CreditCardRequest().
              customerId(customer.getId).
              billingAddress.
              streetAddress("1 E Main St").
              extendedAddress("Unit 2").
              locality("Chicago").
              region("Illinois").
              postalCode("60607").
              countryName("United States of America").
              countryCodeAlpha2("US").
              countryCodeAlpha3("USA").
              countryCodeNumeric("840").
              done.
              cardholderName("John Doe").
              cvv("123").
              number("5105105105105100").
              expirationDate("05/12")
            card <- gateway.creditCard.create(request)
          } yield (customer, card)

          result match {
            case Success((customer, card)) => {
              card.getCustomerId must be === customer.getId
              val billingAddress = card.getBillingAddress
              billingAddress.getStreetAddress must be === "1 E Main St"
              billingAddress.getExtendedAddress must be === "Unit 2"
              billingAddress.getLocality must be === "Chicago"
              billingAddress.getRegion must be === "Illinois"
              billingAddress.getPostalCode must be === "60607"
              billingAddress.getCountryName must be === "United States of America"
              billingAddress.getCountryCodeAlpha2 must be === "US"
              billingAddress.getCountryCodeAlpha3 must be === "USA"
              billingAddress.getCountryCodeNumeric must be === "840"
            }
          }
      }

      onGatewayIt("can create a new card with a previously existing address") {
        gateway =>
          val result = for {
            customer <- gateway.customer.create(new CustomerRequest)

            request = new CreditCardRequest().customerId(customer.getId).
              cardholderName("John Doe").cvv("123").number("5105105105105100").expirationDate("05/12")

            address <- gateway.address.create(customer.getId, new AddressRequest().postalCode("11111"))
            card <- gateway.creditCard.create(request.billingAddressId(address.getId))
          } yield (address, card)

          result match {
            case Success((address, card)) => {
              val billingAddress = card.getBillingAddress
              billingAddress.getId must be === address.getId
              billingAddress.getPostalCode must be === "11111"
            }
          }
      }
    }
  }

  describe("default option") {
    onGatewayIt("sets one card to be default") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request1 = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").
            number("5105105105105100").expirationDate("05/12")
          request2 = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").
            number("5105105105105100").expirationDate("05/12").
            options.makeDefault(true).done

          card1 <- gateway.creditCard.create(request1)
          card2 <- gateway.creditCard.create(request2)
        } yield (card1, card2)

        result match {
          case Success((card1, card2)) => {
            gateway.creditCard.find(card1.getToken) must not be ('default)
            gateway.creditCard.find(card2.getToken) must be('default)
          }
        }
    }
  }

  describe("Transparent redirect") {
    onGatewayIt("can perform a basic card create") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          trParams = new CreditCardRequest().customerId(customer.getId)
          request = new CreditCardRequest().cardholderName("John Doe").number("5105105105105100").expirationDate("05/12")
          trCreateUrl = gateway.transparentRedirect.url
          queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request, trCreateUrl)

          card <- gateway.transparentRedirect.confirmCreditCard(queryString)
        } yield card

        result match {
          case Success(card) => {
            card.getCardholderName must be === "John Doe"
            card.getBin must be === "510510"
            card.getExpirationMonth must be === "05"
            card.getExpirationYear must be === "2012"
            card.getExpirationDate must be === "05/2012"
            card.getLast4 must be === "5100"
            card.getToken must not be null
          }
        }
    }

    onGatewayIt("can create card with country") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest
          trParams = new CreditCardRequest().customerId(customer.getId).number("4111111111111111").expirationDate("10/10").billingAddress.countryName("Aruba").countryCodeAlpha2("AW").countryCodeAlpha3("ABW").countryCodeNumeric("533").done
          queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request, gateway.transparentRedirect.url)
          card <- gateway.transparentRedirect.confirmCreditCard(queryString)
        } yield card

        result match {
          case Success(card) => {
            card.getBin must be === "411111"
            card.getLast4 must be === "1111"
            card.getExpirationDate must be === "10/2010"
            card.getBillingAddress.getCountryName must be === "Aruba"
            card.getBillingAddress.getCountryCodeAlpha2 must be === "AW"
            card.getBillingAddress.getCountryCodeAlpha3 must be === "ABW"
            card.getBillingAddress.getCountryCodeNumeric must be === "533"
          }
        }
    }

    onGatewayIt("can set an existing card to be default") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request1 = new CreditCardRequest().customerId(customer.getId).number("5105105105105100").expirationDate("05/12")
          card1 <- gateway.creditCard.create(request1)
          trParams = new CreditCardRequest().customerId(customer.getId).options.makeDefault(true).done
          request2 = new CreditCardRequest().number("5105105105105100").expirationDate("05/12")
          queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request2, gateway.transparentRedirect.url)
          card2 <- gateway.transparentRedirect.confirmCreditCard(queryString)
        } yield card2

        result match {
          case Success(card2) => {
            card2 must be('default)
          }
        }
    }

    onGatewayIt("can make a card default as it's created") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request1 = new CreditCardRequest().customerId(customer.getId).number("5105105105105100").expirationDate("05/12")
          card1 <- gateway.creditCard.create(request1)
          trParams = new CreditCardRequest().customerId(customer.getId)
          request2 = new CreditCardRequest().number("5105105105105100").expirationDate("05/12").options.makeDefault(true).done
          trCreateUrl = gateway.transparentRedirect.url
          queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request2, trCreateUrl)
          card2 <- gateway.transparentRedirect.confirmCreditCard(queryString)
        } yield card2

        result match {
          case Success(card2) => {
            card2.isDefault must be === true
          }
        }
    }

    onGatewayIt("rejects inconsistent country data") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest
          trParams = new CreditCardRequest().customerId(customer.getId).number("4111111111111111").expirationDate("10/10").billingAddress.countryName("Aruba").countryCodeAlpha2("US").done
          queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request, gateway.transparentRedirect.url)
          card <- gateway.transparentRedirect.confirmCreditCard(queryString)
        } yield card

        result match {
          case Failure(errors,_,_,_,_,_) => {
            val code = errors.forObject("creditCard").forObject("billingAddress").onField("base").get(0).code
            code must be === ValidationErrorCode.ADDRESS_INCONSISTENT_COUNTRY
          }
        }
    }

    onGatewayIt("throws ForgedQueryStringException when query string has been tampered with") {
      gateway =>
        intercept[ForgedQueryStringException] {
          for {
            customer <- gateway.customer.create(new CustomerRequest)
            trParams = new CreditCardRequest().customerId(customer.getId)
            queryString = TestHelper.simulateFormPostForTR(gateway, trParams, new CreditCardRequest, gateway.transparentRedirect.url)
            card <- gateway.transparentRedirect.confirmCreditCard(queryString + "this makes it invalid")
          } yield card
        }
    }
  }

  describe("venmoSdkPaymentMethodCode") {
    onGatewayIt("creates with valid code") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.getId).venmoSdkPaymentMethodCode(VenmoSdk.PaymentMethodCode.Visa.code)
          card <- gateway.creditCard.create(request)
        } yield card

        result match {
          case Success(card) => {
            card.getBin must be === "411111"
            card must be('venmoSdk)
          }
        }
    }

    onGatewayIt("fails on invalid code") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.getId).venmoSdkPaymentMethodCode(VenmoSdk.PaymentMethodCode.Invalid.code)
          card <- gateway.creditCard.create(request)
        } yield card

        result match {
          case r: Failure => {
            val errorCode = r.errors.forObject("creditCard").onField("venmoSdkPaymentMethodCode").get(0).code
            errorCode must be === ValidationErrorCode.CREDIT_CARD_INVALID_VENMO_SDK_PAYMENT_METHOD_CODE
            r.message must be === "Invalid VenmoSDK payment method code"
          }
        }
    }
  }

  describe("venmoSdkSession") {
    onGatewayIt("creates with valid session") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)
          request = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").
            number("5105105105105100").expirationDate("05/12").
            options.venmoSdkSession(VenmoSdk.Session.Valid.value).done
          card <- gateway.creditCard.create(request)
        } yield card

        result match {
          case Success(card) => {
            card.getBin must be === "510510"
            card must be('venmoSdk)
          }
        }
    }

    onGatewayIt("fails with invalid session") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)
          request = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").
            number("5105105105105100").expirationDate("05/12").
            options.venmoSdkSession(VenmoSdk.Session.Invalid.value).done
          card <- gateway.creditCard.create(request)
        } yield card

        result match {
          case Success(card) => {
            card.getBin must be === "510510"
            card must not be ('venmoSdk)
          }
        }
    }
  }

  describe("update") {
    onGatewayIt("updates expeted card fields") {
      gateway =>
        val updateResult = for {
          customer <- gateway.customer.create(new CustomerRequest)
          request = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").cvv("123").
            number("5105105105105100").expirationDate("05/12")
          original <- gateway.creditCard.create(request)
          updateRequest = new CreditCardRequest().customerId(customer.getId).cardholderName("Jane Jones").
            cvv("321").number("4111111111111111").expirationDate("12/05").
            billingAddress.countryName("Italy").countryCodeAlpha2("IT").countryCodeAlpha3("ITA").countryCodeNumeric("380").done
          updated <- gateway.creditCard.update(original.getToken, updateRequest)
        } yield (original, updated)

        updateResult match {
          case Success((original, updatedCard)) => {
            updatedCard.getCardholderName must be === "Jane Jones"
            updatedCard.getBin must be === "411111"
            updatedCard.getExpirationMonth must be === "12"
            updatedCard.getExpirationYear must be === "2005"
            updatedCard.getExpirationDate must be === "12/2005"
            updatedCard.getLast4 must be === "1111"
            updatedCard.getToken must not be theSameInstanceAs(original.getToken)
            updatedCard.getBillingAddress.getCountryName must be === "Italy"
            updatedCard.getBillingAddress.getCountryCodeAlpha2 must be === "IT"
            updatedCard.getBillingAddress.getCountryCodeAlpha3 must be === "ITA"
            updatedCard.getBillingAddress.getCountryCodeNumeric must be === "380"
          }
        }
    }

    onGatewayIt("can set a card to be the default") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)
          request = new CreditCardRequest().customerId(customer.getId).number("5105105105105100").expirationDate("05/12")
          card1 <- gateway.creditCard.create(request)
          card2 <- gateway.creditCard.create(request)
        } yield (card1, card2)

        result match {
          case Success((card1, card2)) => {
            (card1 must be('default))
            (card2 must not be ('default))

            gateway.creditCard.update(card2.getToken, new CreditCardRequest().options.makeDefault(true).done)
            gateway.creditCard.find(card1.getToken) must not be ('default)
            gateway.creditCard.find(card2.getToken) must be('default)

            gateway.creditCard.update(card1.getToken, new CreditCardRequest().options.makeDefault(true).done)
            gateway.creditCard.find(card1.getToken) must be('default)
            gateway.creditCard.find(card2.getToken) must not be ('default)
          }
        }
    }
  }

  describe("update via TransparentRedirect") {
    onGatewayIt("updates basic fields") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)
          createRequest = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").
            cvv("123").number("5105105105105100").expirationDate("05/12")
          card <- gateway.creditCard.create(createRequest)
          trParams = new CreditCardRequest().paymentMethodToken(card.getToken)
          request = new CreditCardRequest().cardholderName("joe cool")
          queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request,
            gateway.transparentRedirect.url)

          updated <- gateway.transparentRedirect.confirmCreditCard(queryString)
        } yield (card, updated)

        result match {
          case Success((card, updated)) => {
            updated.getCardholderName must be === "joe cool"
          }
        }
    }

    onGatewayIt("updates country") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.getId).
            number("5105105105105100").expirationDate("05/12")
          original <- gateway.creditCard.create(request)
          updateRequest = new CreditCardRequest
          trParams = new CreditCardRequest().paymentMethodToken(original.getToken).number("4111111111111111").
            expirationDate("10/10").billingAddress.countryName("Jersey").countryCodeAlpha2("JE").
            countryCodeAlpha3("JEY").countryCodeNumeric("832").done
          queryString = TestHelper.simulateFormPostForTR(gateway, trParams, updateRequest, gateway.transparentRedirect.url)

          updated <- gateway.transparentRedirect.confirmCreditCard(queryString)
        } yield (original)

        result match {
          case Success(original) => {
            val updatedCreditCard = gateway.creditCard.find(original.getToken)
            updatedCreditCard.getBin must be === "411111"
            updatedCreditCard.getLast4 must be === "1111"
            updatedCreditCard.getExpirationDate must be === "10/2010"
            updatedCreditCard.getBillingAddress.getCountryName must be === "Jersey"
            updatedCreditCard.getBillingAddress.getCountryCodeAlpha2 must be === "JE"
            updatedCreditCard.getBillingAddress.getCountryCodeAlpha3 must be === "JEY"
            updatedCreditCard.getBillingAddress.getCountryCodeNumeric must be === "832"
          }
        }
        result must be('success)
    }

    onGatewayIt("rejects invalid country updates") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)
          request = new CreditCardRequest().customerId(customer.getId).number("5105105105105100").expirationDate("05/12")
          card <- gateway.creditCard.create(request)
          updateRequest = new CreditCardRequest
          trParams = new CreditCardRequest().paymentMethodToken(card.getToken).number("4111111111111111").
            expirationDate("10/10").billingAddress.countryCodeAlpha2("zz").done
          queryString = TestHelper.simulateFormPostForTR(gateway, trParams, updateRequest, gateway.transparentRedirect.url)
          confirmedCard <- gateway.transparentRedirect.confirmCreditCard(queryString)
        } yield confirmedCard

        result match {
          case r: Failure => {
            val code = r.errors.forObject("creditCard").forObject("billingAddress").onField("countryCodeAlpha2").get(0).code
            code must be === ValidationErrorCode.ADDRESS_COUNTRY_CODE_ALPHA2_IS_NOT_ACCEPTED
          }
        }
    }

    onGatewayIt("updates card tokens") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)
          request = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").cvv("123").
            number("5105105105105100").expirationDate("05/12")
          card <- gateway.creditCard.create(request)
          newToken = String.valueOf(new Random().nextInt)
          updateRequest = new CreditCardRequest().customerId(customer.getId).token(newToken)
          updatedCard <- gateway.creditCard.update(card.getToken, updateRequest)

        } yield (updatedCard, newToken)

        result match {
          case Success((updatedCard, newToken)) => {
            updatedCard.getToken must be === newToken
          }
        }

    }

    onGatewayIt("can update a small number of attributes") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").cvv("123").
            number("5105105105105100").expirationDate("05/12")

          card <- gateway.creditCard.create(request)

          updateRequest = new CreditCardRequest().cardholderName("Jane Jones")

          updateResult <- gateway.creditCard.update(card.getToken, updateRequest)
        } yield (updateResult)

        result match {
          case Success(updatedCard) => {
            updatedCard.getCardholderName must be === "Jane Jones"
            updatedCard.getBin must be === "510510"
            updatedCard.getExpirationMonth must be === "05"
            updatedCard.getExpirationYear must be === "2012"
            updatedCard.getExpirationDate must be === "05/2012"
            updatedCard.getLast4 must be === "5100"
          }
        }
    }
  }

  describe("update with billing address") {
    onGatewayIt("creates new address by default") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.getId).number("5105105105105100").
            expirationDate("05/12").billingAddress.firstName("John").done

          original <- gateway.creditCard.create(request)

          updateRequest = new CreditCardRequest().billingAddress.lastName("Jones").done

          updated <- gateway.creditCard.update(original.getToken, updateRequest)
        } yield (original, updated)

        result match {
          case Success((original, updatedCreditCard)) => {
            updatedCreditCard.getBillingAddress.getFirstName must be === null
            updatedCreditCard.getBillingAddress.getLastName must be === "Jones"
            updatedCreditCard.getBillingAddress.getId must not be ===(original.getBillingAddress.getId)
          }
        }
    }

    onGatewayIt("updates existing address if updateExisting option is used") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.getId).number("5105105105105100").
            expirationDate("05/12").billingAddress.firstName("John").done
          original <- gateway.creditCard.create(request)
          updateRequest = new CreditCardRequest().billingAddress.lastName("Jones").
            options.updateExisting(true).done.done

          updatedCreditCard <- gateway.creditCard.update(original.getToken, updateRequest)
        } yield (original, updatedCreditCard)

        result match {
          case Success((original, updatedCreditCard)) => {
            updatedCreditCard.getBillingAddress.getFirstName must be === "John"
            updatedCreditCard.getBillingAddress.getLastName must be === "Jones"
            updatedCreditCard.getBillingAddress.getId must be === original.getBillingAddress.getId
          }
        }
    }

    onGatewayIt("updates existing address if updateExisting option is used with Transparent Redirect too") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.getId).number("5105105105105100").
            expirationDate("05/12").
            billingAddress.firstName("John").done
          original <- gateway.creditCard.create(request)
          trParams = new CreditCardRequest().paymentMethodToken(original.getToken).
            billingAddress.options.updateExisting(true).done.done
          updateRequest = new CreditCardRequest().billingAddress.lastName("Jones").done
          queryString = TestHelper.simulateFormPostForTR(gateway, trParams, updateRequest,
            gateway.transparentRedirect.url)

          updatedCard <- gateway.transparentRedirect.confirmCreditCard(queryString)
        } yield ((original, updatedCard))

        result match {
          case Success((original, updatedCard)) => {
            updatedCard.getBillingAddress.getFirstName must be === "John"
            updatedCard.getBillingAddress.getLastName must be === "Jones"
            updatedCard.getBillingAddress.getId must be === original.getBillingAddress.getId
          }
        }
    }
  }

  describe("find") {
    onGatewayIt("can find card by token") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").cvv("123").
            number("5105105105105100").expirationDate("05/12")
          card <- gateway.creditCard.create(request)
        } yield card

        result match {
          case Success(card) => {
            val found = gateway.creditCard.find(card.getToken)

            found.getCardholderName must be === "John Doe"
            found.getBin must be === "510510"
            found.getExpirationMonth must be === "05"
            found.getExpirationYear must be === "2012"
            found.getExpirationDate must be === "05/2012"
            found.getLast4 must be === "5100"
          }
        }
    }

    onGatewayIt("returns associated subscriptions") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          cardRequest = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").cvv("123").
            number("5105105105105100").expirationDate("05/12")

          card <- gateway.creditCard.create(cardRequest)

          id = "subscription-id-" + new Random().nextInt
          subscriptionRequest = new SubscriptionRequest().id(id).planId("integration_trialless_plan").
            paymentMethodToken(card.getToken).
            price(new BigDecimal("1.00"))

          subscription <- gateway.subscription.create(subscriptionRequest)
        } yield (card, subscription)

        result match {
          case Success((card, subscription)) => {
            val foundCard = gateway.creditCard.find(card.getToken)

            foundCard.getSubscriptions.get(0).getId must be === subscription.getId
            foundCard.getSubscriptions.get(0).getPrice must be === new BigDecimal("1.00")
            foundCard.getSubscriptions.get(0).getPlanId must be === "integration_trialless_plan"
          }
        }
    }

    it("throws NotFoundExceptions for unknown tokens") {
      intercept[NotFoundException] {
        createGateway.creditCard.find("badToken")
      }
    }

    it("throws NotFoundExceptions for empty id lists") {
      intercept[NotFoundException] {
        createGateway.creditCard.find(" ")
      }
    }
  }

  describe("delete") {
    onGatewayIt("causes a card to become unfindable") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe")
            .cvv("123").number("5105105105105100").expirationDate("05/12")

          card <- gateway.creditCard.create(request)
        } yield card

        result match {
          case Success(card) => {
            gateway.creditCard.delete(card.getToken) must be === Deleted
            intercept[NotFoundException] {
              gateway.creditCard.find(card.getToken)
            }
          }
        }
    }
  }

  describe("failOnDuplicatePaymentMethod option") {
    onGatewayIt("fails on if duplicate it detected") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)


          request = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").
            cvv("123").number("4012000033330026").expirationDate("05/12").
            options.failOnDuplicatePaymentMethod(true).done

          card1 <- gateway.creditCard.create(request)
          card2 <- gateway.creditCard.create(request)
        } yield (card1, card2)

        result match {
          case r: Failure => {
            val code = r.errors.forObject("creditCard").onField("number").get(0).code
            code must be === ValidationErrorCode.CREDIT_CARD_DUPLICATE_CARD_EXISTS
          }
        }
    }
  }

  describe("verifyCard option") {
    onGatewayIt("verifies valid Credit Card") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").cvv("123").
            number("4111111111111111").expirationDate("05/12").
            options.
            verifyCard(true).
            done

          card <- gateway.creditCard.create(request)
        } yield card

        result must be ('success)
    }

    onGatewayIt("verifies Credit Card against specific Merchant Account") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)


          request = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").cvv("123").
            number("5105105105105100").expirationDate("05/12").
            options.
            verifyCard(true).
            verificationMerchantAccountId(NON_DEFAULT_MERCHANT_ACCOUNT_ID).
            done

          card <- gateway.creditCard.create(request)
        } yield card

        result match {
          case r: Failure => {
            r.creditCardVerification.get.getMerchantAccountId must be === NON_DEFAULT_MERCHANT_ACCOUNT_ID
          }
        }
    }

    onGatewayIt("verifies invalid Credit Card") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").cvv("123").
            number("5105105105105100").expirationDate("05/12").
            options.verifyCard(true).done

          card <- gateway.creditCard.create(request)
        } yield card

        result match {
          case r: Failure => {
            val verification = r.creditCardVerification.get
            verification.getGatewayRejectionReason must be === null
            verification.getStatus must be === CreditCardVerification.Status.PROCESSOR_DECLINED
            r.message must be === "Do Not Honor"
          }
        }
    }

    it("exposes gateway rejection reason") {
      val processingRulesGateway = createProcessingRulesGateway
      val result = for {
        customer <- processingRulesGateway.customer.create(new CustomerRequest)

        request = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").cvv("200").
          number("4111111111111111").expirationDate("05/12").
          options.verifyCard(true).done

        card <- processingRulesGateway.creditCard.create(request)
      } yield card

      result match {
        case r: Failure => {
          val verification = r.creditCardVerification.get
          verification.getGatewayRejectionReason must be === Transaction.GatewayRejectionReason.CVV
        }
      }
    }
  }

  describe("expired") {
    onGatewayIt("finds all expired cards") {
      gateway =>
        val expiredCards = gateway.creditCard.expired
        (expiredCards.getMaximumSize) must be > 0

        expiredCards.count(!_.isExpired) must be === 0
        val uniqueTokens = expiredCards.map {
          _.getToken
        }.toSet
        uniqueTokens.size must be === expiredCards.getMaximumSize
    }
  }

  describe("expiringBetween") {
    onGatewayIt("finds cards within expiry range") {
      gateway =>
        val start = now
        start.set(2010, 0, 1)
        val end = now
        end.set(2010, 11, 30)
        val expiredCards = gateway.creditCard.expiringBetween(start, end)
        expiredCards.getMaximumSize must be > 0

        for (card <- expiredCards) {
          card.getExpirationYear must be === "2010"
        }
        val uniqueTokens = expiredCards.map {
          _.getToken
        }.toSet
        uniqueTokens.size must be === expiredCards.getMaximumSize
    }
  }

  describe("card type indicators") {
    describe("commercialCard") {
      it("is populated") {
        val processingRulesGateway = createProcessingRulesGateway
        val result = for {
          customer <- processingRulesGateway.customer.create(new CustomerRequest)
          request = new CreditCardRequest().customerId(customer.getId).
            number(CreditCardNumbers.CardTypeIndicators.Commercial.getValue).expirationDate("05/12").
            options.verifyCard(true).done

          card <- processingRulesGateway.creditCard.create(request)
        } yield card

        result match {
          case Success(card) => {
            card.getCommercial must be === CreditCard.Commercial.YES
          }
        }
      }
    }

    describe("durbinRegulatedCard") {
      it("is populated") {
        val processingRulesGateway = createProcessingRulesGateway
        val result = for {
          customer <- processingRulesGateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.getId).
            number(CreditCardNumbers.CardTypeIndicators.DurbinRegulated.getValue).expirationDate("05/12").
            options.verifyCard(true).done

          card <- processingRulesGateway.creditCard.create(request)
        } yield card

        result match {
          case Success(card) => {
            card.getDurbinRegulated must be === CreditCard.DurbinRegulated.YES
          }
        }
      }
    }

    describe("debitCard") {
      it("is populated") {
        val processingRulesGateway = createProcessingRulesGateway
        val result = for {
          customer <- processingRulesGateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.getId).
            number(CreditCardNumbers.CardTypeIndicators.Debit.getValue).expirationDate("05/12").
            options.verifyCard(true).done

          card <- processingRulesGateway.creditCard.create(request)
        } yield card

        result match {
          case Success(card) => {
            card.getDebit must be === CreditCard.Debit.YES
          }
        }
      }
    }

    describe("healthcareCard") {
      it("is populated") {
        val processingRulesGateway = createProcessingRulesGateway
        val result = for {
          customer <- processingRulesGateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.getId).
            number(CreditCardNumbers.CardTypeIndicators.Healthcare.getValue).expirationDate("05/12").
            options.verifyCard(true).done

          card <- processingRulesGateway.creditCard.create(request)
        } yield card

        result match {
          case Success(card) => {
            card.getHealthcare must be === CreditCard.Healthcare.YES
          }
        }
      }
    }

    describe("payrollCard") {
      it("is populated") {
        val processingRulesGateway = createProcessingRulesGateway
        val result = for {
          customer <- processingRulesGateway.customer.create(new CustomerRequest)
          request = new CreditCardRequest().customerId(customer.getId).
            number(CreditCardNumbers.CardTypeIndicators.Payroll.getValue).expirationDate("05/12").
            options.verifyCard(true).done

          card <- processingRulesGateway.creditCard.create(request)
        } yield card

        result match {
          case Success(card) => {
            card.getPayroll must be === CreditCard.Payroll.YES
          }
        }
      }
    }

    describe("prepaidCard") {
      it("is populated") {
        val processingRulesGateway = createProcessingRulesGateway
        val result = for {
          customer <- processingRulesGateway.customer.create(new CustomerRequest)
          request = new CreditCardRequest().customerId(customer.getId).
            number(CreditCardNumbers.CardTypeIndicators.Prepaid.getValue).expirationDate("05/12").
            options.verifyCard(true).done

          card <- processingRulesGateway.creditCard.create(request)
        } yield card

        result match {
          case Success(card) => {
            card.getPrepaid must be === CreditCard.Prepaid.YES
          }
        }
      }
    }

    describe("issuingBank") {
      it("is populated") {
        val processingRulesGateway = createProcessingRulesGateway
        val result = for {
          customer <- processingRulesGateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.getId).
            number(CreditCardNumbers.CardTypeIndicators.IssuingBank.getValue).expirationDate("05/12").
            options.verifyCard(true).done

          card <- processingRulesGateway.creditCard.create(request)
        } yield card

        result match {
          case Success(card) => {
            card.getIssuingBank must be === CreditCardDefaults.IssuingBank.getValue
          }
        }
      }
    }

    describe("countryOfIssuance") {
      it("is populated") {
        val processingRulesGateway = createProcessingRulesGateway
        val result = for {
          customer <- processingRulesGateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.getId).
            number(CreditCardNumbers.CardTypeIndicators.CountryOfIssuance.getValue).expirationDate("05/12").
            options.verifyCard(true).done

          card <- processingRulesGateway.creditCard.create(request)
        } yield card

        result match {
          case Success(card) => {
            card.getCountryOfIssuance must be === CreditCardDefaults.CountryOfIssuance.getValue
          }
        }
      }
    }

    describe("Card Type indicators negative cases") {
      it("sets all indicators to NO") {
        val processingRulesGateway = createProcessingRulesGateway
        val result = for {
          customer <- processingRulesGateway.customer.create(new CustomerRequest)
          request = new CreditCardRequest().customerId(customer.getId).
            number(CreditCardNumbers.CardTypeIndicators.No.getValue).expirationDate("05/12").
            options.verifyCard(true).done

          card <- processingRulesGateway.creditCard.create(request)
        } yield card

        result match {
          case Success(card) => {
            card.getCommercial must be === CreditCard.Commercial.NO
            card.getDebit must be === CreditCard.Debit.NO
            card.getDurbinRegulated must be === CreditCard.DurbinRegulated.NO
            card.getHealthcare must be === CreditCard.Healthcare.NO
            card.getPayroll must be === CreditCard.Payroll.NO
            card.getPrepaid must be === CreditCard.Prepaid.NO
          }
        }
      }

      it("type absence sets flag to unknown") {
        val processingRulesGateway = createProcessingRulesGateway
        val result = for {
          customer <- processingRulesGateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.getId).number("5555555555554444").
            expirationDate("05/12").options.verifyCard(true).done

          card <- processingRulesGateway.creditCard.create(request)

        } yield card

        result match {
          case Success(card) => {
            card.getCommercial must be === CreditCard.Commercial.UNKNOWN
            card.getDebit must be === CreditCard.Debit.UNKNOWN
            card.getDurbinRegulated must be === CreditCard.DurbinRegulated.UNKNOWN
            card.getHealthcare must be === CreditCard.Healthcare.UNKNOWN
            card.getPayroll must be === CreditCard.Payroll.UNKNOWN
            card.getPrepaid must be === CreditCard.Prepaid.UNKNOWN
            card.getCountryOfIssuance must be === "Unknown"
            card.getIssuingBank must be === "Unknown"
          }
        }
      }
    }
  }
}
