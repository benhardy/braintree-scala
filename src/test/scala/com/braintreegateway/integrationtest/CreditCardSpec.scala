package com.braintreegateway.integrationtest

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.MustMatchers
import com.braintreegateway._
import exceptions.{NotFoundException, ForgedQueryStringException}
import test.{CreditCardDefaults, CreditCardNumbers, VenmoSdk}
import testhelpers.{MerchantAccountTestConstants, TestHelper}
import java.util.{Calendar, Random}
import java.math.BigDecimal
import scala.collection.JavaConversions._
import MerchantAccountTestConstants._

@RunWith(classOf[JUnitRunner])
class CreditCardSpec extends FunSpec with MustMatchers {

  def createGateway = {
    new BraintreeGateway(Environment.DEVELOPMENT, "integration_merchant_id", "integration_public_key", "integration_private_key")
  }

  def createProcessingRulesGateway = {
    new BraintreeGateway(Environment.DEVELOPMENT, "processing_rules_merchant_id", "processing_rules_public_key", "processing_rules_private_key")
  }

  describe("transparentRedirect") {
    it("has correct URL For Create") {
      val gateway = createGateway

      val url = gateway.creditCard.transparentRedirectURLForCreate
      url must be === gateway.baseMerchantURL + "/payment_methods/all/create_via_transparent_redirect_request"
    }
    it("has correct URL For Update") {
      val gateway = createGateway

      val url = gateway.creditCard.transparentRedirectURLForUpdate
      url must be === gateway.baseMerchantURL + "/payment_methods/all/update_via_transparent_redirect_request"
    }
    it("trData") {
      val gateway = createGateway

      val trData = gateway.trData(new CreditCardRequest, "http://example.com")
      TestHelper.assertValidTrData(gateway.getConfiguration, trData)
    }
  }

  describe("create") {
    it("populates expeted fields") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").cvv("123").number("5105105105105100").expirationDate("05/12")

      val result = gateway.creditCard.create(request)

      result must be('success)
      val card = result.getTarget
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
      val thisYear = Calendar.getInstance.get(Calendar.YEAR)
      card.getCreatedAt.get(Calendar.YEAR) must be === thisYear
      card.getUpdatedAt.get(Calendar.YEAR) must be === thisYear
      card.getUniqueNumberIdentifier.matches("\\A\\w{32}\\z") must be === true
      card must not be ('venmoSdk)
      card.getImageUrl.matches(".*png.*") must be === true
    }

    it("sets card expiration dates correctly") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").
        cvv("123").number("5105105105105100").expirationMonth("06").expirationYear("13")
      val result = gateway.creditCard.create(request)
      result must be('success)
      val card = result.getTarget
      card.getCustomerId must be === customer.getId
      card.getExpirationMonth must be === "06"
      card.getExpirationYear must be === "2013"
      card.getExpirationDate must be === "06/2013"
      val thisYear = Calendar.getInstance.get(Calendar.YEAR)
      card.getCreatedAt.get(Calendar.YEAR) must be === thisYear
      card.getUpdatedAt.get(Calendar.YEAR) must be === thisYear
    }

    it("reproduces XML chars in cardholder name") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new CreditCardRequest().customerId(customer.getId).cardholderName("Special Chars <>&\"'").
        number("5105105105105100").expirationDate("05/12")

      val result = gateway.creditCard.create(request)

      result must be('success)
      val card = result.getTarget
      card.getCardholderName must be === "Special Chars <>&\"'"
    }

    it("processes security parameters") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new CreditCardRequest().customerId(customer.getId).cardholderName("Special Chars").
        number("5105105105105100").expirationDate("05/12").deviceSessionId("abc123")

      val result = gateway.creditCard.create(request)

      result must be('success)
    }

    describe("Address usage") {
      it("can add address on card create") {
        val gateway = createGateway

        val customer = gateway.customer.create(new CustomerRequest).getTarget
        val request = new CreditCardRequest().
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

        val result = gateway.creditCard.create(request)

        result must be('success)
        val card = result.getTarget
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

      it("can create a new card with a previously existing address") {
        val gateway = createGateway

        val customer = gateway.customer.create(new CustomerRequest).getTarget
        val address = gateway.address.create(customer.getId, new AddressRequest().postalCode("11111")).getTarget
        val request = new CreditCardRequest().customerId(customer.getId).billingAddressId(address.getId).
          cardholderName("John Doe").cvv("123").number("5105105105105100").expirationDate("05/12")

        val result = gateway.creditCard.create(request)

        result must be('success)
        val card = result.getTarget
        val billingAddress = card.getBillingAddress
        billingAddress.getId must be === address.getId
        billingAddress.getPostalCode must be === "11111"
      }
    }
  }

  describe("default option") {
    it("sets one card to be default") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request1 = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").
        number("5105105105105100").expirationDate("05/12")
      val request2 = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").
        number("5105105105105100").expirationDate("05/12").
        options.makeDefault(true).done

      val card1 = gateway.creditCard.create(request1).getTarget
      val card2 = gateway.creditCard.create(request2).getTarget

      gateway.creditCard.find(card1.getToken) must not be ('default)
      gateway.creditCard.find(card2.getToken) must be('default)
    }
  }

  describe("Transparent redirect") {
    it("can perform a basic card create") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val trParams = new CreditCardRequest().customerId(customer.getId)
      val request = new CreditCardRequest().cardholderName("John Doe").number("5105105105105100").expirationDate("05/12")
      val trCreateUrl = gateway.creditCard.transparentRedirectURLForCreate
      val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request, trCreateUrl)

      val result = gateway.creditCard.confirmTransparentRedirect(queryString)

      result must be('success)
      val card = result.getTarget
      card.getCardholderName must be === "John Doe"
      card.getBin must be === "510510"
      card.getExpirationMonth must be === "05"
      card.getExpirationYear must be === "2012"
      card.getExpirationDate must be === "05/2012"
      card.getLast4 must be === "5100"
      card.getToken must not be null
    }

    it("can create card with country") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new CreditCardRequest
      val trParams = new CreditCardRequest().customerId(customer.getId).number("4111111111111111").expirationDate("10/10").billingAddress.countryName("Aruba").countryCodeAlpha2("AW").countryCodeAlpha3("ABW").countryCodeNumeric("533").done
      val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request, gateway.transparentRedirect.url)
      val result = gateway.transparentRedirect.confirmCreditCard(queryString)
      result must be('success)
      result.getTarget.getBin must be === "411111"
      result.getTarget.getLast4 must be === "1111"
      result.getTarget.getExpirationDate must be === "10/2010"
      result.getTarget.getBillingAddress.getCountryName must be === "Aruba"
      result.getTarget.getBillingAddress.getCountryCodeAlpha2 must be === "AW"
      result.getTarget.getBillingAddress.getCountryCodeAlpha3 must be === "ABW"
      result.getTarget.getBillingAddress.getCountryCodeNumeric must be === "533"
    }

    it("can set an existing card to be default") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request1 = new CreditCardRequest().customerId(customer.getId).number("5105105105105100").expirationDate("05/12")
      gateway.creditCard.create(request1)
      val trParams = new CreditCardRequest().customerId(customer.getId).options.makeDefault(true).done
      val request2 = new CreditCardRequest().number("5105105105105100").expirationDate("05/12")
      val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request2, gateway.creditCard.transparentRedirectURLForCreate)
      val card = gateway.creditCard.confirmTransparentRedirect(queryString).getTarget
      card must be('default)
    }

    it("can make a card default as it's created") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request1 = new CreditCardRequest().customerId(customer.getId).number("5105105105105100").expirationDate("05/12")
      gateway.creditCard.create(request1)
      val trParams = new CreditCardRequest().customerId(customer.getId)
      val request2 = new CreditCardRequest().number("5105105105105100").expirationDate("05/12").options.makeDefault(true).done
      val trCreateUrl = gateway.creditCard.transparentRedirectURLForCreate
      val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request2, trCreateUrl)
      val card = gateway.creditCard.confirmTransparentRedirect(queryString).getTarget
      card must be('default)
    }

    it("rejects inconsistent country data") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new CreditCardRequest
      val trParams = new CreditCardRequest().customerId(customer.getId).number("4111111111111111").expirationDate("10/10").billingAddress.countryName("Aruba").countryCodeAlpha2("US").done
      val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request, gateway.transparentRedirect.url)
      val result = gateway.transparentRedirect.confirmCreditCard(queryString)
      result must not be ('success)
      val code = result.getErrors.forObject("creditCard").forObject("billingAddress").onField("base").get(0).getCode
      code must be === ValidationErrorCode.ADDRESS_INCONSISTENT_COUNTRY
    }

    it("throws ForgedQueryStringException when query string has been tampered with") {
      intercept[ForgedQueryStringException] {
        val gateway = createGateway
        val customer = gateway.customer.create(new CustomerRequest).getTarget
        val trParams = new CreditCardRequest().customerId(customer.getId)
        val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, new CreditCardRequest, gateway.creditCard.transparentRedirectURLForCreate)
        gateway.creditCard.confirmTransparentRedirect(queryString + "this makes it invalid")
      }
    }
  }

  describe("venmoSdkPaymentMethodCode") {
    it("creates with valid code") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new CreditCardRequest().customerId(customer.getId).venmoSdkPaymentMethodCode(VenmoSdk.PaymentMethodCode.Visa.code)
      val result = gateway.creditCard.create(request)
      result must be('success)
      val card = result.getTarget
      card.getBin must be === "411111"
      card must be('venmoSdk)
    }

    it("fails on invalid code") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new CreditCardRequest().customerId(customer.getId).venmoSdkPaymentMethodCode(VenmoSdk.PaymentMethodCode.Invalid.code)
      val result = gateway.creditCard.create(request)
      val errorCode = result.getErrors.forObject("creditCard").onField("venmoSdkPaymentMethodCode").get(0).getCode
      result must not be ('success)
      result.getMessage must be === "Invalid VenmoSDK payment method code"
      errorCode must be === ValidationErrorCode.CREDIT_CARD_INVALID_VENMO_SDK_PAYMENT_METHOD_CODE
    }
  }

  describe("venmoSdkSession") {
    it("creates with valid session") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").
        number("5105105105105100").expirationDate("05/12").
        options.venmoSdkSession(VenmoSdk.Session.Valid.value).done
      val result = gateway.creditCard.create(request)
      result must be('success)
      val card = result.getTarget
      card.getBin must be === "510510"
      card must be('venmoSdk)
    }

    it("fails with invalid session") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").
        number("5105105105105100").expirationDate("05/12").
        options.venmoSdkSession(VenmoSdk.Session.Invalid.value).done
      val result = gateway.creditCard.create(request)
      result must be('success)
      val card = result.getTarget
      card.getBin must be === "510510"
      card must not be ('venmoSdk)
    }
  }

  describe("update") {
    it("updates expeted card fields") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").cvv("123").
        number("5105105105105100").expirationDate("05/12")
      val result = gateway.creditCard.create(request)
      result must be('success)
      val card = result.getTarget
      val updateRequest = new CreditCardRequest().customerId(customer.getId).cardholderName("Jane Jones").
        cvv("321").number("4111111111111111").expirationDate("12/05").
        billingAddress.
          countryName("Italy"). countryCodeAlpha2("IT").countryCodeAlpha3("ITA").countryCodeNumeric("380").done

      val updateResult = gateway.creditCard.update(card.getToken, updateRequest)

      updateResult must be('success)
      val updatedCard = updateResult.getTarget
      updatedCard.getCardholderName must be === "Jane Jones"
      updatedCard.getBin must be === "411111"
      updatedCard.getExpirationMonth must be === "12"
      updatedCard.getExpirationYear must be === "2005"
      updatedCard.getExpirationDate must be === "12/2005"
      updatedCard.getLast4 must be === "1111"
      updatedCard.getToken must not be theSameInstanceAs(card.getToken)
      updatedCard.getBillingAddress.getCountryName must be === "Italy"
      updatedCard.getBillingAddress.getCountryCodeAlpha2 must be === "IT"
      updatedCard.getBillingAddress.getCountryCodeAlpha3 must be === "ITA"
      updatedCard.getBillingAddress.getCountryCodeNumeric must be === "380"
    }

    it("can set a card to be the default") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new CreditCardRequest().customerId(customer.getId).number("5105105105105100").expirationDate("05/12")
      val card1 = gateway.creditCard.create(request).getTarget
      val card2 = gateway.creditCard.create(request).getTarget
      card1 must be('default)
      card2 must not be ('default)
      gateway.creditCard.update(card2.getToken, new CreditCardRequest().options.makeDefault(true).done)
      gateway.creditCard.find(card1.getToken) must not be ('default)
      gateway.creditCard.find(card2.getToken) must be('default)
      gateway.creditCard.update(card1.getToken, new CreditCardRequest().options.makeDefault(true).done)
      gateway.creditCard.find(card1.getToken) must be('default)
      gateway.creditCard.find(card2.getToken) must not be ('default)
    }
  }

  describe("update via TransparentRedirect") {
    it("updates basic fields") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val createRequest = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").
        cvv("123").number("5105105105105100").expirationDate("05/12")
      val card = gateway.creditCard.create(createRequest).getTarget
      val trParams = new CreditCardRequest().paymentMethodToken(card.getToken)
      val request = new CreditCardRequest().cardholderName("joe cool")
      val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request,
        gateway.creditCard.transparentRedirectURLForUpdate)

      val result = gateway.creditCard.confirmTransparentRedirect(queryString)

      result must be('success)
      val updatedCard = result.getTarget
      updatedCard.getCardholderName must be === "joe cool"
    }

    it("updates country") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new CreditCardRequest().customerId(customer.getId).
        number("5105105105105100").expirationDate("05/12")
      val card = gateway.creditCard.create(request).getTarget
      val updateRequest = new CreditCardRequest
      val trParams = new CreditCardRequest().paymentMethodToken(card.getToken).number("4111111111111111").expirationDate("10/10").billingAddress.countryName("Jersey").countryCodeAlpha2("JE").countryCodeAlpha3("JEY").countryCodeNumeric("832").done
      val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, updateRequest, gateway.transparentRedirect.url)

      val result = gateway.transparentRedirect.confirmCreditCard(queryString)

      result must be('success)
      val updatedCreditCard = gateway.creditCard.find(card.getToken)
      updatedCreditCard.getBin must be === "411111"
      updatedCreditCard.getLast4 must be === "1111"
      updatedCreditCard.getExpirationDate must be === "10/2010"
      updatedCreditCard.getBillingAddress.getCountryName must be === "Jersey"
      updatedCreditCard.getBillingAddress.getCountryCodeAlpha2 must be === "JE"
      updatedCreditCard.getBillingAddress.getCountryCodeAlpha3 must be === "JEY"
      updatedCreditCard.getBillingAddress.getCountryCodeNumeric must be === "832"
    }

    it("rejects invalid country updates") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new CreditCardRequest().customerId(customer.getId).number("5105105105105100").expirationDate("05/12")
      val card = gateway.creditCard.create(request).getTarget
      val updateRequest = new CreditCardRequest
      val trParams = new CreditCardRequest().paymentMethodToken(card.getToken).number("4111111111111111").
        expirationDate("10/10").billingAddress.countryCodeAlpha2("zz").done
      val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, updateRequest,
        gateway.transparentRedirect.url)
      val result = gateway.transparentRedirect.confirmCreditCard(queryString)
      result must not be ('success)
      val code = result.getErrors.forObject("creditCard").forObject("billingAddress").onField("countryCodeAlpha2").get(0).getCode
      code must be === ValidationErrorCode.ADDRESS_COUNTRY_CODE_ALPHA2_IS_NOT_ACCEPTED
    }

    it("updates card tokens") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").cvv("123").
        number("5105105105105100").expirationDate("05/12")
      val result = gateway.creditCard.create(request)
      result must be('success)
      val card = result.getTarget
      val newToken = String.valueOf(new Random().nextInt)
      val updateRequest = new CreditCardRequest().customerId(customer.getId).token(newToken)

      val updateResult = gateway.creditCard.update(card.getToken, updateRequest)

      updateResult must be('success)
      val updatedCard = updateResult.getTarget
      updatedCard.getToken must be === newToken
    }

    it("can update a small number of attributes") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").cvv("123").
        number("5105105105105100").expirationDate("05/12")
      val result = gateway.creditCard.create(request)
      result must be('success)
      val card = result.getTarget
      val updateRequest = new CreditCardRequest().cardholderName("Jane Jones")

      val updateResult = gateway.creditCard.update(card.getToken, updateRequest)

      updateResult must be('success)
      val updatedCard = updateResult.getTarget
      updatedCard.getCardholderName must be === "Jane Jones"
      updatedCard.getBin must be === "510510"
      updatedCard.getExpirationMonth must be === "05"
      updatedCard.getExpirationYear must be === "2012"
      updatedCard.getExpirationDate must be === "05/2012"
      updatedCard.getLast4 must be === "5100"
    }
  }

  describe("update with billing address") {
    it("creates new address by default") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new CreditCardRequest().customerId(customer.getId).number("5105105105105100").
        expirationDate("05/12").billingAddress.firstName("John").done
      val creditCard = gateway.creditCard.create(request).getTarget
      val updateRequest = new CreditCardRequest().billingAddress.lastName("Jones").done

      val updatedCreditCard = gateway.creditCard.update(creditCard.getToken, updateRequest).getTarget

      updatedCreditCard.getBillingAddress.getFirstName must be === null
      updatedCreditCard.getBillingAddress.getLastName must be === "Jones"
      creditCard.getBillingAddress.getId must not be ===(updatedCreditCard.getBillingAddress.getId)
    }

    it("updates existing address if updateExisting option is used") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new CreditCardRequest().customerId(customer.getId).number("5105105105105100").
        expirationDate("05/12").billingAddress.firstName("John").done
      val creditCard = gateway.creditCard.create(request).getTarget
      val updateRequest = new CreditCardRequest().billingAddress.lastName("Jones").
        options.updateExisting(true).done.done

      val updatedCreditCard = gateway.creditCard.update(creditCard.getToken, updateRequest).getTarget

      updatedCreditCard.getBillingAddress.getFirstName must be === "John"
      updatedCreditCard.getBillingAddress.getLastName must be === "Jones"
      updatedCreditCard.getBillingAddress.getId must be === creditCard.getBillingAddress.getId
    }

    it("updates existing address if updateExisting option is used with Transparent Redirect too") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new CreditCardRequest().customerId(customer.getId).number("5105105105105100").
        expirationDate("05/12").
        billingAddress.firstName("John").done
      val creditCard = gateway.creditCard.create(request).getTarget
      val trParams = new CreditCardRequest().paymentMethodToken(creditCard.getToken).
        billingAddress.options.updateExisting(true).done.done
      val updateRequest = new CreditCardRequest().billingAddress.lastName("Jones").done
      val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, updateRequest,
        gateway.creditCard.transparentRedirectURLForUpdate)

      val updatedCard = gateway.creditCard.confirmTransparentRedirect(queryString).getTarget

      updatedCard.getBillingAddress.getFirstName must be === "John"
      updatedCard.getBillingAddress.getLastName must be === "Jones"
      updatedCard.getBillingAddress.getId must be === creditCard.getBillingAddress.getId
    }
  }

  describe("find") {
    it("can find card by token") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").cvv("123").
        number("5105105105105100").expirationDate("05/12")
      val result = gateway.creditCard.create(request)
      result must be('success)
      val card = result.getTarget

      val found = gateway.creditCard.find(card.getToken)

      found.getCardholderName must be === "John Doe"
      found.getBin must be === "510510"
      found.getExpirationMonth must be === "05"
      found.getExpirationYear must be === "2012"
      found.getExpirationDate must be === "05/2012"
      found.getLast4 must be === "5100"
    }

    it("returns associated subscriptions") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val cardRequest = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").cvv("123").
        number("5105105105105100").expirationDate("05/12")
      val card = gateway.creditCard.create(cardRequest).getTarget
      val id = "subscription-id-" + new Random().nextInt
      val subscriptionRequest = new SubscriptionRequest().id(id).planId("integration_trialless_plan").
        paymentMethodToken(card.getToken).
        price(new BigDecimal("1.00"))
      val subscription = gateway.subscription.create(subscriptionRequest).getTarget

      val foundCard = gateway.creditCard.find(card.getToken)

      foundCard.getSubscriptions.get(0).getId must be === subscription.getId
      foundCard.getSubscriptions.get(0).getPrice must be === new BigDecimal("1.00")
      foundCard.getSubscriptions.get(0).getPlanId must be === "integration_trialless_plan"
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
    it("causes a card to become unfindable") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe")
        .cvv("123").number("5105105105105100").expirationDate("05/12")
      val result = gateway.creditCard.create(request)
      result must be('success)
      val card = result.getTarget
      val deleteResult = gateway.creditCard.delete(card.getToken)
      deleteResult must be('success)
      intercept[NotFoundException] {
        gateway.creditCard.find(card.getToken)
      }
    }
  }

  describe("failOnDuplicatePaymentMethod option") {
    it("fails on if duplicate it detected") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").
        cvv("123").number("4012000033330026").expirationDate("05/12").
        options.failOnDuplicatePaymentMethod(true).done
      gateway.creditCard.create(request)
      val result = gateway.creditCard.create(request)
      result must not be ('success)
      val code = result.getErrors.forObject("creditCard").onField("number").get(0).getCode
      code must be === ValidationErrorCode.CREDIT_CARD_DUPLICATE_CARD_EXISTS
    }
  }

  describe("verifyCard option") {
    it("verifies valid Credit Card") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").cvv("123").
        number("4111111111111111").expirationDate("05/12").
        options.
        verifyCard(true).
        done

      val result = gateway.creditCard.create(request)

      result must be ('success)
    }

    it("verifies Credit Card against specific Merchant Account") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").cvv("123").
        number("5105105105105100").expirationDate("05/12").
        options.
          verifyCard(true).
          verificationMerchantAccountId(NON_DEFAULT_MERCHANT_ACCOUNT_ID).
          done

      val result = gateway.creditCard.create(request)

      result must not be ('success)
      result.getCreditCardVerification.getMerchantAccountId must be === NON_DEFAULT_MERCHANT_ACCOUNT_ID
    }

    it("verifies invalid Credit Card") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").cvv("123").
        number("5105105105105100").expirationDate("05/12").
        options.verifyCard(true).done

      val result = gateway.creditCard.create(request)

      result must not be ('success)
      val verification = result.getCreditCardVerification
      verification.getStatus must be === CreditCardVerification.Status.PROCESSOR_DECLINED
      result.getMessage must be === "Do Not Honor"
      verification.getGatewayRejectionReason must be === null
    }

    it("exposes gateway rejection reason") {
      val processingRulesGateway = createProcessingRulesGateway
      val customer = processingRulesGateway.customer.create(new CustomerRequest).getTarget
      val request = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").cvv("200").
        number("4111111111111111").expirationDate("05/12").
        options.verifyCard(true).done

      val result = processingRulesGateway.creditCard.create(request)

      result must not be ('success)
      val verification = result.getCreditCardVerification
      verification.getGatewayRejectionReason must be === Transaction.GatewayRejectionReason.CVV
    }
  }

  describe("expired") {
    it("finds all expired cards") {
      val gateway = createGateway

      val expiredCards = gateway.creditCard.expired
      (expiredCards.getMaximumSize) must be > 0

      for (card <- expiredCards) {
        card must be('expired)
      }
      val uniqueTokens = expiredCards.map { _.getToken }.toSet
      uniqueTokens.size must be === expiredCards.getMaximumSize
    }
  }

  describe("expiringBetween") {
    it("finds cards within expiry range") {
      val gateway = createGateway

      val start = Calendar.getInstance
      start.set(2010, 0, 1)
      val end = Calendar.getInstance
      end.set(2010, 11, 30)
      val expiredCards = gateway.creditCard.expiringBetween(start, end)
      expiredCards.getMaximumSize must be > 0

      for (card <- expiredCards) {
        card.getExpirationYear must be === "2010"
      }
      val uniqueTokens = expiredCards.map { _.getToken }.toSet
      uniqueTokens.size must be === expiredCards.getMaximumSize
    }
  }

  describe("card type indicators") {
    describe("commercialCard") {
      it("is populated") {
        val processingRulesGateway = createProcessingRulesGateway
        val customer = processingRulesGateway.customer.create(new CustomerRequest).getTarget
        val request = new CreditCardRequest().customerId(customer.getId).
          number(CreditCardNumbers.CardTypeIndicators.Commercial.getValue).expirationDate("05/12").
          options.verifyCard(true).done

        val result = processingRulesGateway.creditCard.create(request)

        val card = result.getTarget
        card.getCommercial must be === CreditCard.Commercial.YES
      }
    }

    describe("durbinRegulatedCard") {
      it("is populated") {
        val processingRulesGateway = createProcessingRulesGateway
        val customer = processingRulesGateway.customer.create(new CustomerRequest).getTarget
        val request = new CreditCardRequest().customerId(customer.getId).
          number(CreditCardNumbers.CardTypeIndicators.DurbinRegulated.getValue).expirationDate("05/12").
          options.verifyCard(true).done

        val result = processingRulesGateway.creditCard.create(request)

        val card = result.getTarget
        card.getDurbinRegulated must be === CreditCard.DurbinRegulated.YES
      }
    }

    describe("debitCard") {
      it("is populated") {
        val processingRulesGateway = createProcessingRulesGateway
        val customer = processingRulesGateway.customer.create(new CustomerRequest).getTarget
        val request = new CreditCardRequest().customerId(customer.getId).
          number(CreditCardNumbers.CardTypeIndicators.Debit.getValue).expirationDate("05/12").
          options.verifyCard(true).done
        val result = processingRulesGateway.creditCard.create(request)
        val card = result.getTarget
        card.getDebit must be === CreditCard.Debit.YES
      }
    }

    describe("healthcareCard") {
      it("is populated") {
        val processingRulesGateway = createProcessingRulesGateway
        val customer = processingRulesGateway.customer.create(new CustomerRequest).getTarget
        val request = new CreditCardRequest().customerId(customer.getId).
          number(CreditCardNumbers.CardTypeIndicators.Healthcare.getValue).expirationDate("05/12").
          options.verifyCard(true).done

        val result = processingRulesGateway.creditCard.create(request)

        val card = result.getTarget
        card.getHealthcare must be === CreditCard.Healthcare.YES
      }
    }

    describe("payrollCard") {
      it("is populated") {
        val processingRulesGateway = createProcessingRulesGateway
        val customer = processingRulesGateway.customer.create(new CustomerRequest).getTarget
        val request = new CreditCardRequest().customerId(customer.getId).
          number(CreditCardNumbers.CardTypeIndicators.Payroll.getValue).expirationDate("05/12").
          options.verifyCard(true).done

        val result = processingRulesGateway.creditCard.create(request)

        val card = result.getTarget
        card.getPayroll must be === CreditCard.Payroll.YES
      }
    }

    describe("prepaidCard") {
      it("is populated") {
        val processingRulesGateway = createProcessingRulesGateway
        val customer = processingRulesGateway.customer.create(new CustomerRequest).getTarget
        val request = new CreditCardRequest().customerId(customer.getId).
          number(CreditCardNumbers.CardTypeIndicators.Prepaid.getValue).expirationDate("05/12").
          options.verifyCard(true).done

        val result = processingRulesGateway.creditCard.create(request)

        val card = result.getTarget
        card.getPrepaid must be === CreditCard.Prepaid.YES
      }
    }

    describe("issuingBank") {
      it("is populated") {
        val processingRulesGateway = createProcessingRulesGateway
        val customer = processingRulesGateway.customer.create(new CustomerRequest).getTarget
        val request = new CreditCardRequest().customerId(customer.getId).
          number(CreditCardNumbers.CardTypeIndicators.IssuingBank.getValue).expirationDate("05/12").
          options.verifyCard(true).done

        val result = processingRulesGateway.creditCard.create(request)

        val card = result.getTarget
        card.getIssuingBank must be === CreditCardDefaults.IssuingBank.getValue
      }
    }

    describe("countryOfIssuance") {
      it("is populated") {
        val processingRulesGateway = createProcessingRulesGateway
        val customer = processingRulesGateway.customer.create(new CustomerRequest).getTarget
        val request = new CreditCardRequest().customerId(customer.getId).
          number(CreditCardNumbers.CardTypeIndicators.CountryOfIssuance.getValue).expirationDate("05/12").
          options.verifyCard(true).done

        val result = processingRulesGateway.creditCard.create(request)

        val card = result.getTarget
        card.getCountryOfIssuance must be === CreditCardDefaults.CountryOfIssuance.getValue
      }
    }

    describe("Card Type indicators negative cases") {
      it("sets all indicators to NO") {
        val processingRulesGateway = createProcessingRulesGateway
        val customer = processingRulesGateway.customer.create(new CustomerRequest).getTarget
        val request = new CreditCardRequest().customerId(customer.getId).
          number(CreditCardNumbers.CardTypeIndicators.No.getValue).expirationDate("05/12").
          options.verifyCard(true).done

        val result = processingRulesGateway.creditCard.create(request)

        val card = result.getTarget
        card.getCommercial must be === CreditCard.Commercial.NO
        card.getDebit must be === CreditCard.Debit.NO
        card.getDurbinRegulated must be === CreditCard.DurbinRegulated.NO
        card.getHealthcare must be === CreditCard.Healthcare.NO
        card.getPayroll must be === CreditCard.Payroll.NO
        card.getPrepaid must be === CreditCard.Prepaid.NO
      }

      it("type absence sets flag to unknown") {
        val processingRulesGateway = createProcessingRulesGateway
        val customer = processingRulesGateway.customer.create(new CustomerRequest).getTarget
        val request = new CreditCardRequest().customerId(customer.getId).number("5555555555554444").
          expirationDate("05/12").options.verifyCard(true).done

        val result = processingRulesGateway.creditCard.create(request)

        val card = result.getTarget
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
