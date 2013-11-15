package com.braintreegateway.integrationtest

import org.junit.runner.RunWith
import _root_.org.scalatest.{Inside, FunSpec}
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.MustMatchers
import com.braintreegateway._
import exceptions.{NotFoundException, ForgedQueryStringException}
import com.braintreegateway.gw.{Deleted, Failure, BraintreeGateway, Success}
import gw.Failure
import gw.Success
import test.{CreditCardDefaults, CreditCardNumbers, VenmoSdk}
import com.braintreegateway.testhelpers.{GatewaySpec, MerchantAccountTestConstants, TestHelper}
import java.util.Random
import java.math.BigDecimal
import scala.collection.JavaConversions._
import MerchantAccountTestConstants._
import TestHelper._
import com.braintreegateway.testhelpers.CalendarHelper._
import com.braintreegateway.Transactions.GatewayRejectionReason

@RunWith(classOf[JUnitRunner])
class CreditCardSpec extends FunSpec with MustMatchers with GatewaySpec with Inside {

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

          request = new CreditCardRequest().customerId(customer.id).cardholderName("John Doe").cvv("123").
            number("5105105105105100").expirationDate("05/12");

          card <- gateway.creditCard.create(request)

        } yield (customer, card)

        inside(result) {
          case Success((customer, card)) => {
            card.cardholderName must be === "John Doe"
            card.cardType must be === CreditCards.CardType.MASTER_CARD
            card.customerId must be === customer.id
            card.customerLocation must be === "US"
            card.bin must be === "510510"
            card.expirationMonth must be === "05"
            card.expirationYear must be === "2012"
            card.expirationDate must be === "05/2012"
            card.last4 must be === "5100"
            card.maskedNumber must be === "510510******5100"
            card.token must not be null
            val thisYear = now.year
            card.createdAt.year must be === thisYear
            card.updatedAt.year must be === thisYear
            card.uniqueNumberIdentifier.matches("\\A\\w{32}\\z") must be === true
            card must not be ('venmoSdk)
            card.imageUrl.matches(".*png.*") must be === true
          }
        }
    }

    onGatewayIt("sets card expiration dates correctly") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.id).cardholderName("John Doe").
            cvv("123").number("5105105105105100").expirationMonth("06").expirationYear("13")
          card <- gateway.creditCard.create(request)
        } yield (customer, card)

        inside(result) {
          case Success((customer, card)) => {
            card.customerId must be === customer.id
            card.expirationMonth must be === "06"
            card.expirationYear must be === "2013"
            card.expirationDate must be === "06/2013"
            val thisYear = now.year
            card.createdAt.year must be === thisYear
            card.updatedAt.year must be === thisYear
          }
        }
    }

    onGatewayIt("reproduces XML chars in cardholder name") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.id).cardholderName("Special Chars <>&\"'").
            number("5105105105105100").expirationDate("05/12")

          card <- gateway.creditCard.create(request)
        } yield card

        inside(result) {
          case Success(card) => {
            card.cardholderName must be === "Special Chars <>&\"'"
          }
        }
    }

    onGatewayIt("processes security parameters") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.id).cardholderName("Special Chars").
            number("5105105105105100").expirationDate("05/12").deviceSessionId("abc123")

          card <- gateway.creditCard.create(request)
        } yield card

        inside(result) {
          case Success(card) =>
        }
    }

    describe("Address usage") {
      onGatewayIt("can add address on card create") {
        gateway =>
          val result = for {
            customer <- gateway.customer.create(new CustomerRequest)
            request = new CreditCardRequest().
              customerId(customer.id).
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

          inside(result) {
            case Success((customer, card)) => {
              card.customerId must be === customer.id
              inside(card.billingAddress) { case Some(billingAddress) =>
                billingAddress.streetAddress must be === "1 E Main St"
                billingAddress.extendedAddress must be === "Unit 2"
                billingAddress.locality must be === "Chicago"
                billingAddress.region must be === "Illinois"
                billingAddress.postalCode must be === "60607"
                billingAddress.countryName must be === "United States of America"
                billingAddress.countryCodeAlpha2 must be === "US"
                billingAddress.countryCodeAlpha3 must be === "USA"
                billingAddress.countryCodeNumeric must be === "840"
              }
            }
          }
      }

      onGatewayIt("can create a new card with a previously existing address") {
        gateway =>
          val result = for {
            customer <- gateway.customer.create(new CustomerRequest)

            request = new CreditCardRequest().customerId(customer.id).
              cardholderName("John Doe").cvv("123").number("5105105105105100").expirationDate("05/12")

            address <- gateway.address.create(customer.id, new AddressRequest().postalCode("11111"))
            card <- gateway.creditCard.create(request.billingAddressId(address.id))
          } yield (address, card)

          inside(result) { case Success((address, card)) =>
            inside(card.billingAddress) { case Some(billingAddress) =>
              billingAddress.id must be === address.id
              billingAddress.postalCode must be === "11111"
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

          request1 = new CreditCardRequest().customerId(customer.id).cardholderName("John Doe").
            number("5105105105105100").expirationDate("05/12")
          request2 = new CreditCardRequest().customerId(customer.id).cardholderName("John Doe").
            number("5105105105105100").expirationDate("05/12").
            options.makeDefault(true).done

          card1 <- gateway.creditCard.create(request1)
          card2 <- gateway.creditCard.create(request2)
        } yield (card1, card2)

        inside(result) {
          case Success((card1, card2)) => {
            gateway.creditCard.find(card1.token) must not be ('default)
            gateway.creditCard.find(card2.token) must be('default)
          }
        }
    }
  }

  describe("Transparent redirect") {
    onGatewayIt("can perform a basic card create") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          trParams = new CreditCardRequest().customerId(customer.id)
          request = new CreditCardRequest().cardholderName("John Doe").number("5105105105105100").expirationDate("05/12")
          trCreateUrl = gateway.transparentRedirect.url
          queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request, trCreateUrl)

          card <- gateway.transparentRedirect.confirmCreditCard(queryString)
        } yield card

        inside(result) {
          case Success(card) => {
            card.cardholderName must be === "John Doe"
            card.bin must be === "510510"
            card.expirationMonth must be === "05"
            card.expirationYear must be === "2012"
            card.expirationDate must be === "05/2012"
            card.last4 must be === "5100"
            card.token must not be null
          }
        }
    }

    onGatewayIt("can create card with country") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest
          trParams = new CreditCardRequest().customerId(customer.id).number("4111111111111111").expirationDate("10/10").billingAddress.countryName("Aruba").countryCodeAlpha2("AW").countryCodeAlpha3("ABW").countryCodeNumeric("533").done
          queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request, gateway.transparentRedirect.url)
          card <- gateway.transparentRedirect.confirmCreditCard(queryString)
        } yield card

        inside(result) { case Success(card) =>
          card.bin must be === "411111"
          card.last4 must be === "1111"
          card.expirationDate must be === "10/2010"
          inside(card.billingAddress) { case Some(address) =>
            address.countryName must be === "Aruba"
            address.countryCodeAlpha2 must be === "AW"
            address.countryCodeAlpha3 must be === "ABW"
            address.countryCodeNumeric must be === "533"
          }
        }
    }

    onGatewayIt("can set an existing card to be default") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request1 = new CreditCardRequest().customerId(customer.id).number("5105105105105100").expirationDate("05/12")
          card1 <- gateway.creditCard.create(request1)
          trParams = new CreditCardRequest().customerId(customer.id).options.makeDefault(true).done
          request2 = new CreditCardRequest().number("5105105105105100").expirationDate("05/12")
          queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request2, gateway.transparentRedirect.url)
          card2 <- gateway.transparentRedirect.confirmCreditCard(queryString)
        } yield card2

        inside(result) {
          case Success(card2) => {
            card2 must be('default)
          }
        }
    }

    onGatewayIt("can make a card default as it's created") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request1 = new CreditCardRequest().customerId(customer.id).number("5105105105105100").expirationDate("05/12")
          card1 <- gateway.creditCard.create(request1)
          trParams = new CreditCardRequest().customerId(customer.id)
          request2 = new CreditCardRequest().number("5105105105105100").expirationDate("05/12").options.makeDefault(true).done
          trCreateUrl = gateway.transparentRedirect.url
          queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request2, trCreateUrl)
          card2 <- gateway.transparentRedirect.confirmCreditCard(queryString)
        } yield card2

        inside(result) {
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
          trParams = new CreditCardRequest().customerId(customer.id).number("4111111111111111").expirationDate("10/10").billingAddress.countryName("Aruba").countryCodeAlpha2("US").done
          queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request, gateway.transparentRedirect.url)
          card <- gateway.transparentRedirect.confirmCreditCard(queryString)
        } yield card

        inside(result) {
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
            trParams = new CreditCardRequest().customerId(customer.id)
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

          request = new CreditCardRequest().customerId(customer.id).venmoSdkPaymentMethodCode(VenmoSdk.PaymentMethodCode.Visa.code)
          card <- gateway.creditCard.create(request)
        } yield card

        inside(result) {
          case Success(card) => {
            card.bin must be === "411111"
            card must be('venmoSdk)
          }
        }
    }

    onGatewayIt("fails on invalid code") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.id).venmoSdkPaymentMethodCode(VenmoSdk.PaymentMethodCode.Invalid.code)
          card <- gateway.creditCard.create(request)
        } yield card

        inside(result) {
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
          request = new CreditCardRequest().customerId(customer.id).cardholderName("John Doe").
            number("5105105105105100").expirationDate("05/12").
            options.venmoSdkSession(VenmoSdk.Session.Valid.value).done
          card <- gateway.creditCard.create(request)
        } yield card

        inside(result) {
          case Success(card) => {
            card.bin must be === "510510"
            card must be('venmoSdk)
          }
        }
    }

    onGatewayIt("fails with invalid session") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)
          request = new CreditCardRequest().customerId(customer.id).cardholderName("John Doe").
            number("5105105105105100").expirationDate("05/12").
            options.venmoSdkSession(VenmoSdk.Session.Invalid.value).done
          card <- gateway.creditCard.create(request)
        } yield card

        inside(result) {
          case Success(card) => {
            card.bin must be === "510510"
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
          request = new CreditCardRequest().customerId(customer.id).cardholderName("John Doe").cvv("123").
            number("5105105105105100").expirationDate("05/12")
          original <- gateway.creditCard.create(request)
          updateRequest = new CreditCardRequest().customerId(customer.id).cardholderName("Jane Jones").
            cvv("321").number("4111111111111111").expirationDate("12/05").
            billingAddress.countryName("Italy").countryCodeAlpha2("IT").countryCodeAlpha3("ITA").countryCodeNumeric("380").done
          updated <- gateway.creditCard.update(original.token, updateRequest)
        } yield (original, updated)

        inside(updateResult) { case Success((original, updatedCard)) => 
          updatedCard.cardholderName must be === "Jane Jones"
          updatedCard.bin must be === "411111"
          updatedCard.expirationMonth must be === "12"
          updatedCard.expirationYear must be === "2005"
          updatedCard.expirationDate must be === "12/2005"
          updatedCard.last4 must be === "1111"
          updatedCard.token must not be theSameInstanceAs(original.token)
          
          inside(updatedCard.billingAddress) { case Some(address) =>
            address.countryName must be === "Italy"
            address.countryCodeAlpha2 must be === "IT"
            address.countryCodeAlpha3 must be === "ITA"
            address.countryCodeNumeric must be === "380"
          }
        }
    }

    onGatewayIt("can set a card to be the default") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)
          request = new CreditCardRequest().customerId(customer.id).number("5105105105105100").expirationDate("05/12")
          card1 <- gateway.creditCard.create(request)
          card2 <- gateway.creditCard.create(request)
        } yield (card1, card2)

        inside(result) {
          case Success((card1, card2)) => {
            (card1 must be('default))
            (card2 must not be ('default))

            gateway.creditCard.update(card2.token, new CreditCardRequest().options.makeDefault(true).done)
            gateway.creditCard.find(card1.token) must not be ('default)
            gateway.creditCard.find(card2.token) must be('default)

            gateway.creditCard.update(card1.token, new CreditCardRequest().options.makeDefault(true).done)
            gateway.creditCard.find(card1.token) must be('default)
            gateway.creditCard.find(card2.token) must not be ('default)
          }
        }
    }
  }

  describe("update via TransparentRedirect") {
    onGatewayIt("updates basic fields") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)
          createRequest = new CreditCardRequest().customerId(customer.id).cardholderName("John Doe").
            cvv("123").number("5105105105105100").expirationDate("05/12")
          card <- gateway.creditCard.create(createRequest)
          trParams = new CreditCardRequest().paymentMethodToken(card.token)
          request = new CreditCardRequest().cardholderName("joe cool")
          queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request,
            gateway.transparentRedirect.url)

          updated <- gateway.transparentRedirect.confirmCreditCard(queryString)
        } yield (card, updated)

        inside(result) {
          case Success((card, updated)) => {
            updated.cardholderName must be === "joe cool"
          }
        }
    }

    onGatewayIt("updates country") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.id).
            number("5105105105105100").expirationDate("05/12")
          original <- gateway.creditCard.create(request)
          updateRequest = new CreditCardRequest
          trParams = new CreditCardRequest().paymentMethodToken(original.token).number("4111111111111111").
            expirationDate("10/10").billingAddress.countryName("Jersey").countryCodeAlpha2("JE").
            countryCodeAlpha3("JEY").countryCodeNumeric("832").done
          queryString = TestHelper.simulateFormPostForTR(gateway, trParams, updateRequest, gateway.transparentRedirect.url)

          updated <- gateway.transparentRedirect.confirmCreditCard(queryString)
        } yield (original)

        inside(result) { case Success(original) => 
          val updatedCreditCard = gateway.creditCard.find(original.token)
          updatedCreditCard.bin must be === "411111"
          updatedCreditCard.last4 must be === "1111"
          updatedCreditCard.expirationDate must be === "10/2010"
          
          inside(updatedCreditCard.billingAddress) { case Some(address) =>
            address.countryName must be === "Jersey"
            address.countryCodeAlpha2 must be === "JE"
            address.countryCodeAlpha3 must be === "JEY"
            address.countryCodeNumeric must be === "832"
          }
        }
        result must be('success)
    }

    onGatewayIt("rejects invalid country updates") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)
          request = new CreditCardRequest().customerId(customer.id).number("5105105105105100").expirationDate("05/12")
          card <- gateway.creditCard.create(request)
          updateRequest = new CreditCardRequest
          trParams = new CreditCardRequest().paymentMethodToken(card.token).number("4111111111111111").
            expirationDate("10/10").billingAddress.countryCodeAlpha2("zz").done
          queryString = TestHelper.simulateFormPostForTR(gateway, trParams, updateRequest, gateway.transparentRedirect.url)
          confirmedCard <- gateway.transparentRedirect.confirmCreditCard(queryString)
        } yield confirmedCard

        inside(result) {
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
          request = new CreditCardRequest().customerId(customer.id).cardholderName("John Doe").cvv("123").
            number("5105105105105100").expirationDate("05/12")
          card <- gateway.creditCard.create(request)
          newToken = String.valueOf(new Random().nextInt)
          updateRequest = new CreditCardRequest().customerId(customer.id).token(newToken)
          updatedCard <- gateway.creditCard.update(card.token, updateRequest)

        } yield (updatedCard, newToken)

        inside(result) { case Success((updatedCard, newToken)) => 
          updatedCard.token must be === newToken       
        }

    }

    onGatewayIt("can update a small number of attributes") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.id).cardholderName("John Doe").cvv("123").
            number("5105105105105100").expirationDate("05/12")

          card <- gateway.creditCard.create(request)

          updateRequest = new CreditCardRequest().cardholderName("Jane Jones")

          updateResult <- gateway.creditCard.update(card.token, updateRequest)
        } yield (updateResult)

        inside(result) {
          case Success(updatedCard) => {
            updatedCard.cardholderName must be === "Jane Jones"
            updatedCard.bin must be === "510510"
            updatedCard.expirationMonth must be === "05"
            updatedCard.expirationYear must be === "2012"
            updatedCard.expirationDate must be === "05/2012"
            updatedCard.last4 must be === "5100"
          }
        }
    }
  }

  describe("update with billing address") {
    onGatewayIt("creates new address by default") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.id).number("5105105105105100").
            expirationDate("05/12").billingAddress.firstName("John").done

          original <- gateway.creditCard.create(request)

          updateRequest = new CreditCardRequest().billingAddress.lastName("Jones").done

          updated <- gateway.creditCard.update(original.token, updateRequest)
        } yield (original, updated)

        inside(result) { case Success((original, updatedCreditCard)) => 
          inside(updatedCreditCard.billingAddress) { case Some(address) =>
            address.firstName must be === null
            address.lastName must be === "Jones"
            address.id must not be === (original.billingAddress.get.id)
          }
        }
    }

    onGatewayIt("updates existing address if updateExisting option is used") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.id).number("5105105105105100").
            expirationDate("05/12").billingAddress.firstName("John").done
          original <- gateway.creditCard.create(request)
          updateRequest = new CreditCardRequest().billingAddress.lastName("Jones").
            options.updateExisting(true).done.done

          updatedCreditCard <- gateway.creditCard.update(original.token, updateRequest)
        } yield (original, updatedCreditCard)

        inside(result) { case Success((original, updatedCreditCard)) =>
          inside(updatedCreditCard.billingAddress) { case Some(address) =>
            address.firstName must be === "John"
            address.lastName must be === "Jones"
            address.id must be === original.billingAddress.get.id
          }
        }
    }

    onGatewayIt("updates existing address if updateExisting option is used with Transparent Redirect too") {
      gateway =>
        val request = new CreditCardRequest().number("5105105105105100").
          expirationDate("05/12").
          billingAddress.firstName("John").done

        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          original <- gateway.creditCard.create(request.customerId(customer.id))
          trParams = new CreditCardRequest().paymentMethodToken(original.token).
            billingAddress.options.updateExisting(true).done.done
          updateRequest = new CreditCardRequest().billingAddress.lastName("Jones").done
          queryString = TestHelper.simulateFormPostForTR(gateway, trParams, updateRequest,
            gateway.transparentRedirect.url)

          updatedCard <- gateway.transparentRedirect.confirmCreditCard(queryString)
        } yield ((original, updatedCard))

        inside(result) { case Success((original, updatedCreditCard)) =>
          inside(updatedCreditCard.billingAddress) { case Some(address) =>
            address.firstName must be === "John"
            address.lastName must be === "Jones"
            address.id must be === original.billingAddress.get.id
          }
        }
    }
  }

  describe("find") {
    onGatewayIt("can find card by token") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.id).cardholderName("John Doe").cvv("123").
            number("5105105105105100").expirationDate("05/12")
          card <- gateway.creditCard.create(request)
        } yield card

        inside(result) {
          case Success(card) => {
            val found = gateway.creditCard.find(card.token)

            found.cardholderName must be === "John Doe"
            found.bin must be === "510510"
            found.expirationMonth must be === "05"
            found.expirationYear must be === "2012"
            found.expirationDate must be === "05/2012"
            found.last4 must be === "5100"
          }
        }
    }

    onGatewayIt("returns associated subscriptions") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          cardRequest = new CreditCardRequest().customerId(customer.id).cardholderName("John Doe").cvv("123").
            number("5105105105105100").expirationDate("05/12")

          card <- gateway.creditCard.create(cardRequest)

          id = "subscription-id-" + new Random().nextInt
          subscriptionRequest = new SubscriptionRequest().id(id).planId("integration_trialless_plan").
            paymentMethodToken(card.token).
            price(new BigDecimal("1.00"))

          subscription <- gateway.subscription.create(subscriptionRequest)
        } yield (card, subscription)

        inside(result) {
          case Success((card, subscription)) => {
            val foundCard = gateway.creditCard.find(card.token)
            inside(foundCard.subscriptions.headOption) { case Some(foundSub) =>
              foundSub.id must be === subscription.id
              foundSub.price must be === new BigDecimal("1.00")
              foundSub.planId must be === "integration_trialless_plan"
            }
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

          request = new CreditCardRequest().customerId(customer.id).cardholderName("John Doe")
            .cvv("123").number("5105105105105100").expirationDate("05/12")

          card <- gateway.creditCard.create(request)
        } yield card

        inside(result) {
          case Success(card) => {
            gateway.creditCard.delete(card.token) must be === Deleted
            intercept[NotFoundException] {
              gateway.creditCard.find(card.token)
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


          request = new CreditCardRequest().customerId(customer.id).cardholderName("John Doe").
            cvv("123").number("4012000033330026").expirationDate("05/12").
            options.failOnDuplicatePaymentMethod(true).done

          card1 <- gateway.creditCard.create(request)
          card2 <- gateway.creditCard.create(request)
        } yield (card1, card2)

        inside(result) {
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

          request = new CreditCardRequest().customerId(customer.id).cardholderName("John Doe").cvv("123").
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


          request = new CreditCardRequest().customerId(customer.id).cardholderName("John Doe").cvv("123").
            number("5105105105105100").expirationDate("05/12").
            options.
            verifyCard(true).
            verificationMerchantAccountId(NON_DEFAULT_MERCHANT_ACCOUNT_ID).
            done

          card <- gateway.creditCard.create(request)
        } yield card

        inside(result) {
          case r: Failure => {
            r.creditCardVerification.get.merchantAccountId must be === NON_DEFAULT_MERCHANT_ACCOUNT_ID
          }
        }
    }

    onGatewayIt("verifies invalid Credit Card") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.id).cardholderName("John Doe").cvv("123").
            number("5105105105105100").expirationDate("05/12").
            options.verifyCard(true).done

          card <- gateway.creditCard.create(request)
        } yield card

        inside(result) {
          case r: Failure => {
            val verification = r.creditCardVerification.get
            verification.gatewayRejectionReason must be === Transactions.GatewayRejectionReason.UNDEFINED
            verification.status must be === CreditCardVerification.Status.PROCESSOR_DECLINED
            r.message must be === "Do Not Honor"
          }
        }
    }

    it("exposes gateway rejection reason") {
      val processingRulesGateway = createProcessingRulesGateway
      val result = for {
        customer <- processingRulesGateway.customer.create(new CustomerRequest)

        request = new CreditCardRequest().customerId(customer.id).cardholderName("John Doe").cvv("200").
          number("4111111111111111").expirationDate("05/12").
          options.verifyCard(true).done

        card <- processingRulesGateway.creditCard.create(request)
      } yield card

      inside(result) {
        case r: Failure => {
          val verification = r.creditCardVerification.get
          verification.gatewayRejectionReason must be === Transactions.GatewayRejectionReason.CVV
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
        val uniqueTokens = expiredCards.map { _.token }.toSet
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
          card.expirationYear must be === "2010"
        }
        val uniqueTokens = expiredCards.map { _.token }.toSet
        uniqueTokens.size must be === expiredCards.getMaximumSize
    }
  }

  describe("card type indicators") {
    describe("commercialCard") {
      it("is populated") {
        val processingRulesGateway = createProcessingRulesGateway
        val result = for {
          customer <- processingRulesGateway.customer.create(new CustomerRequest)
          request = new CreditCardRequest().customerId(customer.id).
            number(CreditCardNumbers.CardTypeIndicators.Commercial.getValue).expirationDate("05/12").
            options.verifyCard(true).done

          card <- processingRulesGateway.creditCard.create(request)
        } yield card

        inside(result) {
          case Success(card) => {
            card.commercial must be === CreditCard.KindIndicator.YES
          }
        }
      }
    }

    describe("durbinRegulatedCard") {
      it("is populated") {
        val processingRulesGateway = createProcessingRulesGateway
        val result = for {
          customer <- processingRulesGateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.id).
            number(CreditCardNumbers.CardTypeIndicators.DurbinRegulated.getValue).expirationDate("05/12").
            options.verifyCard(true).done

          card <- processingRulesGateway.creditCard.create(request)
        } yield card

        inside(result) {
          case Success(card) => {
            card.durbinRegulated must be === CreditCard.KindIndicator.YES
          }
        }
      }
    }

    describe("debitCard") {
      it("is populated") {
        val processingRulesGateway = createProcessingRulesGateway
        val result = for {
          customer <- processingRulesGateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.id).
            number(CreditCardNumbers.CardTypeIndicators.Debit.getValue).expirationDate("05/12").
            options.verifyCard(true).done

          card <- processingRulesGateway.creditCard.create(request)
        } yield card

        inside(result) {
          case Success(card) => {
            card.debit must be === CreditCard.KindIndicator.YES
          }
        }
      }
    }

    describe("healthcareCard") {
      it("is populated") {
        val processingRulesGateway = createProcessingRulesGateway
        val result = for {
          customer <- processingRulesGateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.id).
            number(CreditCardNumbers.CardTypeIndicators.Healthcare.getValue).expirationDate("05/12").
            options.verifyCard(true).done

          card <- processingRulesGateway.creditCard.create(request)
        } yield card

        inside(result) {
          case Success(card) => {
            card.healthcare must be === CreditCard.KindIndicator.YES
          }
        }
      }
    }

    describe("payrollCard") {
      it("is populated") {
        val processingRulesGateway = createProcessingRulesGateway
        val result = for {
          customer <- processingRulesGateway.customer.create(new CustomerRequest)
          request = new CreditCardRequest().customerId(customer.id).
            number(CreditCardNumbers.CardTypeIndicators.Payroll.getValue).expirationDate("05/12").
            options.verifyCard(true).done

          card <- processingRulesGateway.creditCard.create(request)
        } yield card

        inside(result) {
          case Success(card) => {
            card.payroll must be === CreditCard.KindIndicator.YES
          }
        }
      }
    }

    describe("prepaidCard") {
      it("is populated") {
        val processingRulesGateway = createProcessingRulesGateway
        val result = for {
          customer <- processingRulesGateway.customer.create(new CustomerRequest)
          request = new CreditCardRequest().customerId(customer.id).
            number(CreditCardNumbers.CardTypeIndicators.Prepaid.getValue).expirationDate("05/12").
            options.verifyCard(true).done

          card <- processingRulesGateway.creditCard.create(request)
        } yield card

        inside(result) {
          case Success(card) => {
            card.prepaid must be === CreditCard.KindIndicator.YES
          }
        }
      }
    }

    describe("issuingBank") {
      it("is populated") {
        val processingRulesGateway = createProcessingRulesGateway
        val result = for {
          customer <- processingRulesGateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.id).
            number(CreditCardNumbers.CardTypeIndicators.IssuingBank.getValue).expirationDate("05/12").
            options.verifyCard(true).done

          card <- processingRulesGateway.creditCard.create(request)
        } yield card

        inside(result) {
          case Success(card) => {
            card.issuingBank must be === CreditCardDefaults.IssuingBank.getValue
          }
        }
      }
    }

    describe("countryOfIssuance") {
      it("is populated") {
        val processingRulesGateway = createProcessingRulesGateway
        val result = for {
          customer <- processingRulesGateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.id).
            number(CreditCardNumbers.CardTypeIndicators.CountryOfIssuance.getValue).expirationDate("05/12").
            options.verifyCard(true).done

          card <- processingRulesGateway.creditCard.create(request)
        } yield card

        inside(result) {
          case Success(card) => {
            card.countryOfIssuance must be === CreditCardDefaults.CountryOfIssuance.getValue
          }
        }
      }
    }

    describe("Card Type indicators negative cases") {
      it("sets all indicators to NO") {
        val processingRulesGateway = createProcessingRulesGateway
        val result = for {
          customer <- processingRulesGateway.customer.create(new CustomerRequest)
          request = new CreditCardRequest().customerId(customer.id).
            number(CreditCardNumbers.CardTypeIndicators.No.getValue).expirationDate("05/12").
            options.verifyCard(true).done

          card <- processingRulesGateway.creditCard.create(request)
        } yield card

        inside(result) {
          case Success(card) => {
            card.commercial must be === CreditCard.KindIndicator.NO
            card.debit must be === CreditCard.KindIndicator.NO
            card.durbinRegulated must be === CreditCard.KindIndicator.NO
            card.healthcare must be === CreditCard.KindIndicator.NO
            card.payroll must be === CreditCard.KindIndicator.NO
            card.prepaid must be === CreditCard.KindIndicator.NO
          }
        }
      }

      it("type absence sets flag to unknown") {
        val processingRulesGateway = createProcessingRulesGateway
        val result = for {
          customer <- processingRulesGateway.customer.create(new CustomerRequest)

          request = new CreditCardRequest().customerId(customer.id).number("5555555555554444").
            expirationDate("05/12").options.verifyCard(true).done

          card <- processingRulesGateway.creditCard.create(request)

        } yield card
        import CreditCard.KindIndicator.UNKNOWN
        inside(result) {
          case Success(card) => {
            card.commercial must be === UNKNOWN
            card.debit must be === UNKNOWN
            card.durbinRegulated must be === UNKNOWN
            card.healthcare must be === UNKNOWN
            card.payroll must be === UNKNOWN
            card.prepaid must be === UNKNOWN
            card.countryOfIssuance must be === "Unknown"
            card.issuingBank must be === "Unknown"
          }
        }
      }
    }
  }
}
