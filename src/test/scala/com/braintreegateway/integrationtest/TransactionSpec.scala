package com.braintreegateway.integrationtest

import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.scalatest.matchers.MustMatchers
import com.braintreegateway._
import com.braintreegateway.SandboxValues.CreditCardNumber
import com.braintreegateway.SandboxValues.TransactionAmount
import com.braintreegateway.exceptions.ForgedQueryStringException
import com.braintreegateway.exceptions.NotFoundException
import com.braintreegateway.test.CreditCardNumbers
import com.braintreegateway.test.VenmoSdk
import gw.BraintreeGateway
import testhelpers._
import com.braintreegateway.util.NodeWrapperFactory
import java.math.BigDecimal

import MerchantAccountTestConstants._
import scala.collection.JavaConversions._
import java.util.Random
import CalendarHelper._
import TestHelper._

@RunWith(classOf[JUnitRunner])
class TransactionSpec extends GatewaySpec with MustMatchers {
  val DISBURSEMENT_TRANSACTION_ID = "deposittransaction"

  describe("transparent redirect") {
    onGatewayIt("transparentRedirectURLForCreate") { gateway =>
      val actualUrl = gateway.transaction.transparentRedirectURLForCreate
      val expectedUrl = gateway.baseMerchantURL + "/transactions/all/create_via_transparent_redirect_request"
      actualUrl must be === expectedUrl
    }

    onGatewayIt("trData") { gateway =>
      val trData = gateway.trData(new TransactionRequest, "http://example.com")
      trData must beValidTrData(gateway.configuration)
    }

    onGatewayIt("saleTrData") { gateway =>
      val trData = gateway.transaction.saleTrData(new TransactionRequest, "http://example.com")
      trData must beValidTrData(gateway.configuration)
      trData.contains("sale") must be === true
    }

    onGatewayIt("creditTrData") { gateway =>
      val trData = gateway.transaction.creditTrData(new TransactionRequest, "http://example.com")
      trData must beValidTrData(gateway.configuration)
      trData.contains("credit") must be === true
    }

    onGatewayIt("createViaTransparentRedirect") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done.options.storeInVault(true).done
      val trParams = new TransactionRequest().`type`(Transaction.Type.SALE)
      val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request, gateway.transaction.transparentRedirectURLForCreate)
      val result = gateway.transaction.confirmTransparentRedirect(queryString)
      result must be ('success)
    }

    onGatewayIt("createViaTransparentRedirectThrowsWhenQueryStringHasBeenTamperedWith") { gateway =>
      intercept[ForgedQueryStringException] {
        val queryString = TestHelper.simulateFormPostForTR(gateway, new TransactionRequest, new TransactionRequest, gateway.transaction.transparentRedirectURLForCreate)
        gateway.transaction.confirmTransparentRedirect(queryString + "this make it invalid")
      }
    }
  }

  describe("cloneTransaction") {
    onGatewayIt("cloneTransaction") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).orderId("123").creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done.customer.firstName("Dan").done.billingAddress.firstName("Carl").done.shippingAddress.firstName("Andrew").done
      val result = gateway.transaction.sale(request)
      result must be ('success)
      val transaction = result.getTarget
      val cloneRequest = new TransactionCloneRequest().amount(new BigDecimal("123.45")).channel("MyShoppingCartProvider").options.submitForSettlement(false).done
      val cloneResult = gateway.transaction.cloneTransaction(transaction.getId, cloneRequest)
      cloneResult must be ('success)
      val cloneTransaction = cloneResult.getTarget
      cloneTransaction.getAmount must be === new BigDecimal("123.45")
      cloneTransaction.getChannel must be === "MyShoppingCartProvider"
      cloneTransaction.getOrderId must be === "123"
      cloneTransaction.getCreditCard.getMaskedNumber must be === "411111******1111"
      cloneTransaction.getCustomer.getFirstName must be === "Dan"
      cloneTransaction.getBillingAddress.getFirstName must be === "Carl"
      cloneTransaction.getShippingAddress.getFirstName must be === "Andrew"
    }

    onGatewayIt("cloneTransactionAndSubmitForSettlement") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).orderId("123").creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done
      val result = gateway.transaction.sale(request)
      result must be ('success)
      val transaction = result.getTarget
      val cloneRequest = new TransactionCloneRequest().amount(new BigDecimal("123.45")).options.submitForSettlement(true).done
      val cloneResult = gateway.transaction.cloneTransaction(transaction.getId, cloneRequest)
      cloneResult must be ('success)
      val cloneTransaction = cloneResult.getTarget
      cloneTransaction.getStatus must be === Transaction.Status.SUBMITTED_FOR_SETTLEMENT
    }

    onGatewayIt("cloneTransactionWithValidationErrors") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done
      val result = gateway.transaction.credit(request)
      result must be ('success)
      val transaction = result.getTarget
      val cloneRequest = new TransactionCloneRequest().amount(new BigDecimal("123.45"))
      val cloneResult = gateway.transaction.cloneTransaction(transaction.getId, cloneRequest)
      cloneResult must not be ('success)
      cloneResult.getErrors.forObject("transaction").onField("base").get(0).getCode must be === ValidationErrorCode.TRANSACTION_CANNOT_CLONE_CREDIT
    }
  }

  describe("sale") {
    onGatewayIt("sale") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done
      val result = gateway.transaction.sale(request)
      result must be ('success)
      val transaction = result.getTarget
      transaction.getAmount must be === new BigDecimal("1000.00")
      transaction.getCurrencyIsoCode must be === "USD"
      transaction.getProcessorAuthorizationCode must not be === (null)
      transaction.getType must be === Transaction.Type.SALE
      transaction.getStatus must be === Transaction.Status.AUTHORIZED
      val thisYear = now.year
      transaction.getCreatedAt.year must be === thisYear
      transaction.getUpdatedAt.year must be === thisYear
      val creditCard = transaction.getCreditCard
      creditCard.getBin must be === "411111"
      creditCard.getLast4 must be === "1111"
      creditCard.getExpirationMonth must be === "05"
      creditCard.getExpirationYear must be === "2009"
      creditCard.getExpirationDate must be === "05/2009"
    }

    onGatewayIt("saleWithCardTypeIndicators") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumbers.CardTypeIndicators.Prepaid.getValue).expirationDate("05/2012").done
      val result = gateway.transaction.sale(request)
      result must be ('success)
      val card = result.getTarget.getCreditCard
      card.getPrepaid must be === CreditCard.Prepaid.YES
      card.getHealthcare must be === CreditCard.Healthcare.UNKNOWN
      card.getPayroll must be === CreditCard.Payroll.UNKNOWN
      card.getDebit must be === CreditCard.Debit.UNKNOWN
      card.getDurbinRegulated must be === CreditCard.DurbinRegulated.UNKNOWN
      card.getCommercial must be === CreditCard.Commercial.UNKNOWN
      card.getCountryOfIssuance must be === "Unknown"
      card.getIssuingBank must be === "Unknown"
    }

    onGatewayIt("saleWithAllAttributes") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).channel("MyShoppingCartProvider").orderId("123").creditCard.cardholderName("The Cardholder").number(CreditCardNumber.VISA.number).cvv("321").expirationDate("05/2009").done.customer.firstName("Dan").lastName("Smith").company("Braintree Payment Solutions").email("dan@example.com").phone("419-555-1234").fax("419-555-1235").website("http://braintreepayments.com").done.billingAddress.firstName("Carl").lastName("Jones").company("Braintree").streetAddress("123 E Main St").extendedAddress("Suite 403").locality("Chicago").region("IL").postalCode("60622").countryName("United States of America").countryCodeAlpha2("US").countryCodeAlpha3("USA").countryCodeNumeric("840").done.shippingAddress.firstName("Andrew").lastName("Mason").company("Braintree Shipping").streetAddress("456 W Main St").extendedAddress("Apt 2F").locality("Bartlett").region("MA").postalCode("60103").countryName("Mexico").countryCodeAlpha2("MX").countryCodeAlpha3("MEX").countryCodeNumeric("484").done
      val result = gateway.transaction.sale(request)
      result must be ('success)
      val transaction = result.getTarget
      transaction.getAmount must be === new BigDecimal("1000.00")
      transaction.getStatus must be === Transaction.Status.AUTHORIZED
      transaction.getChannel must be === "MyShoppingCartProvider"
      transaction.getOrderId must be === "123"
      transaction.getVaultCreditCard(gateway) must be === (null)
      transaction.getVaultCustomer(gateway) must be === (null)
      transaction.getAvsErrorResponseCode must be === (null)
      transaction.getAvsPostalCodeResponseCode must be === "M"
      transaction.getAvsStreetAddressResponseCode must be === "M"
      transaction.getCvvResponseCode must be === "M"
      transaction.isTaxExempt must be === java.lang.Boolean.FALSE
      transaction.getVaultCreditCard(gateway) must be === (null)
      val creditCard = transaction.getCreditCard
      creditCard.getBin must be === "411111"
      creditCard.getLast4 must be === "1111"
      creditCard.getExpirationMonth must be === "05"
      creditCard.getExpirationYear must be === "2009"
      creditCard.getExpirationDate must be === "05/2009"
      creditCard.getCardholderName must be === "The Cardholder"
      transaction.getVaultCustomer(gateway) must be === (null)
      val customer = transaction.getCustomer
      customer.getFirstName must be === "Dan"
      customer.getLastName must be === "Smith"
      customer.getCompany must be === "Braintree Payment Solutions"
      customer.getEmail must be === "dan@example.com"
      customer.getPhone must be === "419-555-1234"
      customer.getFax must be === "419-555-1235"
      customer.getWebsite must be === "http://braintreepayments.com"
      transaction.getVaultBillingAddress(gateway) must be === (null)
      val billing = transaction.getBillingAddress
      billing.getFirstName must be === "Carl"
      billing.getLastName must be === "Jones"
      billing.getCompany must be === "Braintree"
      billing.getStreetAddress must be === "123 E Main St"
      billing.getExtendedAddress must be === "Suite 403"
      billing.getLocality must be === "Chicago"
      billing.getRegion must be === "IL"
      billing.getPostalCode must be === "60622"
      billing.getCountryName must be === "United States of America"
      billing.getCountryCodeAlpha2 must be === "US"
      billing.getCountryCodeAlpha3 must be === "USA"
      billing.getCountryCodeNumeric must be === "840"
      transaction.getVaultShippingAddress(gateway) must be === (null)
      val shipping = transaction.getShippingAddress
      shipping.getFirstName must be === "Andrew"
      shipping.getLastName must be === "Mason"
      shipping.getCompany must be === "Braintree Shipping"
      shipping.getStreetAddress must be === "456 W Main St"
      shipping.getExtendedAddress must be === "Apt 2F"
      shipping.getLocality must be === "Bartlett"
      shipping.getRegion must be === "MA"
      shipping.getPostalCode must be === "60103"
      shipping.getCountryName must be === "Mexico"
      shipping.getCountryCodeAlpha2 must be === "MX"
      shipping.getCountryCodeAlpha3 must be === "MEX"
      shipping.getCountryCodeNumeric must be === "484"
    }

    onGatewayIt("saleWithSpecifyingMerchantAccountId") { gateway =>
      val request = new TransactionRequest().merchantAccountId(NON_DEFAULT_MERCHANT_ACCOUNT_ID).amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done
      val result = gateway.transaction.sale(request)
      result must be ('success)
      val transaction = result.getTarget
      transaction.getMerchantAccountId must be === NON_DEFAULT_MERCHANT_ACCOUNT_ID
    }

    onGatewayIt("saleWithoutSpecifyingMerchantAccountIdFallsBackToDefault") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done
      val result = gateway.transaction.sale(request)
      result must be ('success)
      val transaction = result.getTarget
      transaction.getMerchantAccountId must be === DEFAULT_MERCHANT_ACCOUNT_ID
    }

    onGatewayIt("saleWithStoreInVaultAndSpecifyingToken") { gateway =>
      val customerId = String.valueOf(new Random().nextInt)
      val paymentToken = String.valueOf(new Random().nextInt)
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.token(paymentToken).number(CreditCardNumber.VISA.number).expirationDate("05/2009").done.customer.id(customerId).firstName("Jane").done.options.storeInVault(true).done
      val result = gateway.transaction.sale(request)
      result must be ('success)
      val transaction = result.getTarget
      val creditCard = transaction.getCreditCard
      creditCard.getToken must be === paymentToken
      transaction.getVaultCreditCard(gateway).getExpirationDate must be === "05/2009"
      val customer = transaction.getCustomer
      customer.getId must be === customerId
      transaction.getVaultCustomer(gateway).getFirstName must be === "Jane"
    }

    onGatewayIt("saleWithStoreInVaultWithoutSpecifyingToken") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done.customer.firstName("Jane").done.options.storeInVault(true).done
      val result = gateway.transaction.sale(request)
      result must be ('success)
      val transaction = result.getTarget
      val creditCard = transaction.getCreditCard
      creditCard.getToken must not be === (null)
      transaction.getVaultCreditCard(gateway).getExpirationDate must be === "05/2009"
      val customer = transaction.getCustomer
      customer.getId must not be === (null)
      transaction.getVaultCustomer(gateway).getFirstName must be === "Jane"
    }

    onGatewayIt("saleWithStoreInVaultOnSuccessWhenTransactionSucceeds") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).
        creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done.customer.firstName("Jane").done.
        options.storeInVaultOnSuccess(true).done
      val result = gateway.transaction.sale(request)
      result must be ('success)
      val transaction = result.getTarget
      val creditCard = transaction.getCreditCard
      creditCard.getToken must not be === (null)
      transaction.getVaultCreditCard(gateway).getExpirationDate must be === "05/2009"
      val customer = transaction.getCustomer
      customer.getId must not be === (null)
      transaction.getVaultCustomer(gateway).getFirstName must be === "Jane"
    }

    onGatewayIt("saleWithStoreInVaultOnSuccessWhenTransactionFails") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.DECLINE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done.customer.firstName("Jane").done.options.storeInVaultOnSuccess(true).done
      val result = gateway.transaction.sale(request)
      result must not be ('success)
      val transaction = result.getTransaction
      val creditCard = transaction.getCreditCard
      creditCard.getToken must be === (null)
      transaction.getVaultCreditCard(gateway) must be === (null)
      val customer = transaction.getCustomer
      customer.getId must be === (null)
      transaction.getVaultCustomer(gateway) must be === (null)
    }

    onGatewayIt("saleWithStoreInVaultForBillingAndShipping") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).
        creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done.billingAddress.firstName("Carl").done.
        shippingAddress.firstName("Andrew").done.options.storeInVault(true).addBillingAddressToPaymentMethod(true).
        storeShippingAddressInVault(true).done
      val result = gateway.transaction.sale(request)
      result must be ('success)
      val transaction = result.getTarget
      val creditCard = transaction.getVaultCreditCard(gateway)
      creditCard.getBillingAddress.getFirstName must be === "Carl"
      transaction.getVaultBillingAddress(gateway).getFirstName must be === "Carl"
       transaction.getVaultShippingAddress(gateway).getFirstName must be === "Andrew"
      val customer = transaction.getVaultCustomer(gateway)
      customer.getAddresses.size must be === 2
      val addresses = customer.getAddresses.sortWith((a,b) => a.getFirstName < b.getFirstName)

      addresses.get(0).getFirstName must be === "Andrew"
      addresses.get(1).getFirstName must be === "Carl"
      transaction.getBillingAddress.getId must not be === (null)
      transaction.getShippingAddress.getId must not be === (null)
    }

    onGatewayIt("saleWithVaultCustomerAndNewCreditCard") { gateway =>
      val customer = gateway.customer.create(new CustomerRequest().firstName("Michael").lastName("Angelo").company("Some Company")).getTarget
      val request = new TransactionRequest().amount(SandboxValues.TransactionAmount.AUTHORIZE.amount).customerId(customer.getId).creditCard.cardholderName("Bob the Builder").number(SandboxValues.CreditCardNumber.VISA.number).expirationDate("05/2009").done
      val result = gateway.transaction.sale(request)
      result must be ('success)
      val transaction = result.getTarget
      transaction.getCreditCard.getCardholderName must be === "Bob the Builder"
      transaction.getVaultCreditCard(gateway) must be === (null)
    }

    onGatewayIt("saleWithVaultCustomerAndNewCreditCardStoresInVault") { gateway =>
      val customer = gateway.customer.create(new CustomerRequest().firstName("Michael").lastName("Angelo").company("Some Company")).getTarget
      val request = new TransactionRequest().amount(SandboxValues.TransactionAmount.AUTHORIZE.amount).customerId(customer.getId).creditCard.cardholderName("Bob the Builder").number(SandboxValues.CreditCardNumber.VISA.number).expirationDate("05/2009").done.options.storeInVault(true).done
      val result = gateway.transaction.sale(request)
      result must be ('success)
      val transaction = result.getTarget
      transaction.getCreditCard.getCardholderName must be === "Bob the Builder"
      transaction.getVaultCreditCard(gateway).getCardholderName must be === "Bob the Builder"
    }

    onGatewayIt("saleDeclined") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.DECLINE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done
      val result = gateway.transaction.sale(request)
      result must not be ('success)
      val transaction = result.getTransaction
      transaction.getAmount must be === new BigDecimal("2000.00")
      transaction.getStatus must be === Transaction.Status.PROCESSOR_DECLINED
      transaction.getProcessorResponseCode must be === "2000"
      transaction.getProcessorResponseText must not be === (null)
      val creditCard = transaction.getCreditCard
      creditCard.getBin must be === "411111"
      creditCard.getLast4 must be === "1111"
      creditCard.getExpirationMonth must be === "05"
      creditCard.getExpirationYear must be === "2009"
      creditCard.getExpirationDate must be === "05/2009"
    }

    onGatewayIt("saleWithSecuirtyParams") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).deviceSessionId("abc123").creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done
      val result = gateway.transaction.sale(request)
      result must be ('success)
    }

    onGatewayIt("saleWithCustomFields") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).customField("storeMe", "custom value").customField("another_stored_field", "custom value2").creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done
      val result = gateway.transaction.sale(request)
      result must be ('success)
      val transaction = result.getTarget
      val expected = Map(
        "store_me" -> "custom value",
        "another_stored_field" -> "custom value2"
      )
      transaction.getCustomFields.toMap must be === expected
    }

    onGatewayIt("saleWithRecurringFlag") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).recurring(true).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done
      val result = gateway.transaction.sale(request)
      result must be ('success)
      val transaction = result.getTarget
      transaction.getRecurring must be === true
    }

    onGatewayIt("saleWithValidationErrorsOnAddress") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.DECLINE.amount).customField("unkown_custom_field", "custom value").creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done.billingAddress.countryName("No such country").countryCodeAlpha2("zz").countryCodeAlpha3("zzz").countryCodeNumeric("000").done
      val result = gateway.transaction.sale(request)
      result must not be ('success)
      val billingValidationErrors = result.getErrors.forObject("transaction").forObject("billing")
      billingValidationErrors.onField("countryName").get(0).getCode must be === ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED
      billingValidationErrors.onField("countryCodeAlpha2").get(0).getCode must be === ValidationErrorCode.ADDRESS_COUNTRY_CODE_ALPHA2_IS_NOT_ACCEPTED
      billingValidationErrors.onField("countryCodeAlpha3").get(0).getCode must be === ValidationErrorCode.ADDRESS_COUNTRY_CODE_ALPHA3_IS_NOT_ACCEPTED
      billingValidationErrors.onField("countryCodeNumeric").get(0).getCode must be === ValidationErrorCode.ADDRESS_COUNTRY_CODE_NUMERIC_IS_NOT_ACCEPTED
    }

    onGatewayIt("saleWithUnregisteredCustomField") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.DECLINE.amount).customField("unkown_custom_field", "custom value").creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done
      val result = gateway.transaction.sale(request)
      result must not be ('success)
      result.getErrors.forObject("transaction").onField("customFields").get(0).getCode must be === ValidationErrorCode.TRANSACTION_CUSTOM_FIELD_IS_INVALID
    }

    onGatewayIt("saleWithMultipleValidationErrorsOnSameField") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).paymentMethodToken("foo").customerId("5").creditCard.number(CreditCardNumber.VISA.number).cvv("321").expirationDate("04/2009").done
      val result = gateway.transaction.sale(request)
      result must not be ('success)
      val errros = result.getErrors.forObject("transaction").onField("base")
      result.getTransaction must be === (null)
      result.getCreditCardVerification must be === (null)
      errros.size must be === 2
      val validationErrorCodes = errros.map {_.getCode}

      validationErrorCodes must contain (ValidationErrorCode.TRANSACTION_PAYMENT_METHOD_CONFLICT_WITH_VENMO_SDK)
      validationErrorCodes must contain (ValidationErrorCode.TRANSACTION_PAYMENT_METHOD_DOES_NOT_BELONG_TO_CUSTOMER)
    }

    onGatewayIt("saleWithCustomerId") { gateway =>
      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val creditCardRequest = new CreditCardRequest().customerId(customer.getId).cvv("123").number("5105105105105100").expirationDate("05/12")
      val creditCard = gateway.creditCard.create(creditCardRequest).getTarget
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).paymentMethodToken(creditCard.getToken)
      val result = gateway.transaction.sale(request)
      result must be ('success)
      val transaction = result.getTarget
      transaction.getCreditCard.getToken must be === creditCard.getToken
      transaction.getCreditCard.getBin must be === "510510"
      transaction.getCreditCard.getExpirationDate must be === "05/2012"
    }

    onGatewayIt("saleWithPaymentMethodTokenAndCvv") { gateway =>
      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val creditCardRequest = new CreditCardRequest().customerId(customer.getId).number("5105105105105100").expirationDate("05/12")
      val creditCard = gateway.creditCard.create(creditCardRequest).getTarget
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).paymentMethodToken(creditCard.getToken).creditCard.cvv("301").done
      val result = gateway.transaction.sale(request)
      result must be ('success)
      val transaction = result.getTarget
      transaction.getCreditCard.getToken must be === creditCard.getToken
      transaction.getCreditCard.getBin must be === "510510"
      transaction.getCreditCard.getExpirationDate must be === "05/2012"
      transaction.getCvvResponseCode must be === "S"
    }

    onGatewayIt("saleUsesShippingAddressFromVault") { gateway =>
      val customer = gateway.customer.create(new CustomerRequest).getTarget
      gateway.creditCard.create(new CreditCardRequest().customerId(customer.getId).cvv("123").number("5105105105105100").expirationDate("05/12")).getTarget
      val shippingAddress = gateway.address.create(customer.getId, new AddressRequest().firstName("Carl")).getTarget
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).customerId(customer.getId).shippingAddressId(shippingAddress.getId)
      val result = gateway.transaction.sale(request)
      result must be ('success)
      val transaction = result.getTarget
      transaction.getShippingAddress.getId must be === shippingAddress.getId
      transaction.getShippingAddress.getFirstName must be === "Carl"
    }

    onGatewayIt("saleWithValidationError") { gateway =>
      val request = new TransactionRequest().amount(null).creditCard.expirationMonth("05").expirationYear("2010").done
      val result = gateway.transaction.sale(request)
      result must not be ('success)
      result.getTarget must be === (null)
      result.getErrors.forObject("transaction").onField("amount").get(0).getCode must be === ValidationErrorCode.TRANSACTION_AMOUNT_IS_REQUIRED
      val parameters = result.getParameters
      parameters.get("transaction[amount]") must be === (null)
      parameters.get("transaction[credit_card][expiration_month]") must be === "05"
      parameters.get("transaction[credit_card][expiration_year]")  must be === "2010"
    }

    onGatewayIt("saleWithSubmitForSettlement") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done.options.submitForSettlement(true).done
      val result = gateway.transaction.sale(request)
      result must be ('success)
      val transaction = result.getTarget
      transaction.getStatus must be === Transaction.Status.SUBMITTED_FOR_SETTLEMENT
    }

    onGatewayIt("saleWithDescriptor") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done.descriptor.name("123*123456789012345678").phone("3334445555").done
      val result = gateway.transaction.sale(request)
      result must be ('success)
      val transaction = result.getTarget
      transaction.getDescriptor.getName must be === "123*123456789012345678"
      transaction.getDescriptor.getPhone must be === "3334445555"
    }

    onGatewayIt("saleWithDescriptorValidation") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done.descriptor.name("badcompanyname12*badproduct12").phone("%bad4445555").done
      val result = gateway.transaction.sale(request)
      result must not be ('success)
      val expected1 = result.getErrors.forObject("transaction").forObject("descriptor").onField("name").get(0).getCode
      expected1 must be === ValidationErrorCode.DESCRIPTOR_NAME_FORMAT_IS_INVALID
      val expected2 = result.getErrors.forObject("transaction").forObject("descriptor").onField("phone").get(0).getCode
      expected2 must be === ValidationErrorCode.DESCRIPTOR_PHONE_FORMAT_IS_INVALID
    }

    onGatewayIt("saleWithLevel2") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done.taxAmount(new BigDecimal("10.00")).taxExempt(true).purchaseOrderNumber("12345")
      val result = gateway.transaction.sale(request)
      result must be ('success)
      val transaction = result.getTarget
      transaction.getTaxAmount must be === new BigDecimal("10.00")
      transaction.isTaxExempt must be === true
      transaction.getPurchaseOrderNumber must be === "12345"
    }

    onGatewayIt("saleWithTooLongPurchaseOrderNumber") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done.purchaseOrderNumber("aaaaaaaaaaaaaaaaaa")
      val result = gateway.transaction.sale(request)
      result must not be ('success)
      result.getErrors.forObject("transaction").onField("purchaseOrderNumber").get(0).getCode must be === ValidationErrorCode.TRANSACTION_PURCHASE_ORDER_NUMBER_IS_TOO_LONG
    }

    onGatewayIt("saleWithInvalidPurchaseOrderNumber") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done.purchaseOrderNumber("\u00c3\u009f\u00c3\u00a5\u00e2\u0088\u0082")
      val result = gateway.transaction.sale(request)
      result must not be ('success)
      result.getErrors.forObject("transaction").onField("purchaseOrderNumber").get(0).getCode must be === ValidationErrorCode.TRANSACTION_PURCHASE_ORDER_NUMBER_IS_INVALID
    }

    onGatewayIt("saleWithVenmoSdkPaymentMethodCode") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).venmoSdkPaymentMethodCode(VenmoSdk.PaymentMethodCode.Visa.code)
      val result = gateway.transaction.sale(request)
      result must be ('success)
      result.getTarget.getCreditCard.getBin must be === "411111"
    }

    onGatewayIt("saleWithVenmoSdkSession") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done.options.venmoSdkSession(VenmoSdk.Session.Valid.value).done
      val result = gateway.transaction.sale(request)
      result must be ('success)
      result.getTarget.getCreditCard.isVenmoSdk must be === true
    }
  }

  describe("creating transaction with Transparent Redirect") {
    onGatewayIt("populates billing address") { gateway =>
      val request = new TransactionRequest
      val trParams = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).`type`(Transaction.Type.SALE).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done.billingAddress.countryName("United States of America").countryCodeAlpha2("US").countryCodeAlpha3("USA").countryCodeNumeric("840").done
      val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request, gateway.transparentRedirect.url)
      val result = gateway.transparentRedirect.confirmTransaction(queryString)
      result must be ('success)
      val transaction = result.getTarget
      transaction.getBillingAddress.getCountryName must be === "United States of America"
      transaction.getBillingAddress.getCountryCodeAlpha2 must be === "US"
      transaction.getBillingAddress.getCountryCodeAlpha3 must be === "USA"
      transaction.getBillingAddress.getCountryCodeNumeric must be === "840"
    }

    onGatewayIt("rejects invalid billing addresses") { gateway =>
      val request = new TransactionRequest
      val trParams = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).`type`(Transaction.Type.SALE).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done.billingAddress.countryName("Foo bar!").countryCodeAlpha2("zz").countryCodeAlpha3("zzz").countryCodeNumeric("000").done
      val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request, gateway.transparentRedirect.url)
      val result = gateway.transparentRedirect.confirmTransaction(queryString)
      result must not be ('success)
      val billingValidationErrors = result.getErrors.forObject("transaction").forObject("billing")
      val code1 = billingValidationErrors.onField("countryName").get(0).getCode
      code1 must be === ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED
      val code2 = billingValidationErrors.onField("countryCodeAlpha2").get(0).getCode
      code2 must be === ValidationErrorCode.ADDRESS_COUNTRY_CODE_ALPHA2_IS_NOT_ACCEPTED
      val code3 = billingValidationErrors.onField("countryCodeAlpha3").get(0).getCode
      code3 must be === ValidationErrorCode.ADDRESS_COUNTRY_CODE_ALPHA3_IS_NOT_ACCEPTED
      val code4 = billingValidationErrors.onField("countryCodeNumeric").get(0).getCode
      code4 must be === ValidationErrorCode.ADDRESS_COUNTRY_CODE_NUMERIC_IS_NOT_ACCEPTED
    }
  }

  describe("credit") {
    onGatewayIt("credit") { gateway =>
      val request = new TransactionRequest().amount(SandboxValues.TransactionAmount.AUTHORIZE.amount).creditCard.number(SandboxValues.CreditCardNumber.VISA.number).expirationDate("05/2009").done
      val result = gateway.transaction.credit(request)
      result must be ('success)
      val transaction = result.getTarget
      transaction.getAmount must be === new BigDecimal("1000.00")
      transaction.getType must be === Transaction.Type.CREDIT
      transaction.getStatus must be === Transaction.Status.SUBMITTED_FOR_SETTLEMENT
      val creditCard = transaction.getCreditCard
      creditCard.getBin must be === "411111"
      creditCard.getLast4 must be === "1111"
      creditCard.getExpirationMonth must be === "05"
      creditCard.getExpirationYear must be === "2009"
      creditCard.getExpirationDate must be === "05/2009"
    }

    onGatewayIt("creditWithSpecifyingMerchantAccountId") { gateway =>
      val request = new TransactionRequest().amount(SandboxValues.TransactionAmount.AUTHORIZE.amount).merchantAccountId(NON_DEFAULT_MERCHANT_ACCOUNT_ID).creditCard.number(SandboxValues.CreditCardNumber.VISA.number).expirationDate("05/2009").done
      val result = gateway.transaction.credit(request)
      result must be ('success)
      val transaction = result.getTarget
      transaction.getMerchantAccountId must be === NON_DEFAULT_MERCHANT_ACCOUNT_ID
    }

    onGatewayIt("creditWithoutSpecifyingMerchantAccountIdFallsBackToDefault") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done
      val result = gateway.transaction.credit(request)
      result must be ('success)
      val transaction = result.getTarget
      transaction.getMerchantAccountId must be === DEFAULT_MERCHANT_ACCOUNT_ID
    }

    onGatewayIt("creditWithCustomFields") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).customField("store_me", "custom value").customField("another_stored_field", "custom value2").creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done
      val result = gateway.transaction.credit(request)
      result must be ('success)
      val transaction = result.getTarget
      val expected: java.util.Map[String, String] = Map(
        "store_me" -> "custom value",
        "another_stored_field" -> "custom value2"
      )
      transaction.getCustomFields must be === expected
    }

    onGatewayIt("creditWithValidationError") { gateway =>
      val request = new TransactionRequest().amount(null).creditCard.expirationMonth("05").expirationYear("2010").done
      val result = gateway.transaction.credit(request)
      result must not be ('success)
      result.getTarget must be === (null)
      val code = result.getErrors.forObject("transaction").onField("amount").get(0).getCode
      code must be === ValidationErrorCode.TRANSACTION_AMOUNT_IS_REQUIRED
      val parameters: java.util.Map[String, String] = result.getParameters
      parameters.get("transaction[amount]") must be === (null)
      parameters.get("transaction[credit_card][expiration_month]") must be === "05"
      parameters.get("transaction[credit_card][expiration_year]") must be === "2010"
    }
  }

  describe("find") {
    onGatewayIt("finds") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2008").done
      val transaction = gateway.transaction.sale(request).getTarget
      val foundTransaction = gateway.transaction.find(transaction.getId)
      foundTransaction.getId must be === transaction.getId
      foundTransaction.getStatus must be === Transaction.Status.AUTHORIZED
      foundTransaction.getCreditCard.getExpirationDate must be === "05/2008"
    }

    onGatewayIt("throws exception with bad id") { gateway =>
      intercept[NotFoundException] {
        gateway.transaction.find("badId")
      }
    }

    onGatewayIt("throws exception with whitespace id") { gateway =>
      intercept[NotFoundException] {
        gateway.transaction.find(" ")
      }
    }

    onGatewayIt("findWithDisbursementDetails") { gateway =>
      val disbursementDate = CalendarHelper.date("2013-04-10")
      val foundTransaction = gateway.transaction.find(DISBURSEMENT_TRANSACTION_ID)
      val disbursementDetails = foundTransaction.getDisbursementDetails
      foundTransaction.isDisbursed must be === true
      disbursementDetails.getDisbursementDate must be === disbursementDate
      disbursementDetails.getSettlementCurrencyIsoCode must be === "USD"
      disbursementDetails.isFundsHeld must be === false
      disbursementDetails.getSettlementCurrencyExchangeRate must be === new BigDecimal("1")
      disbursementDetails.getSettlementAmount must be === new BigDecimal("100.00")
    }
  }

  describe("void") {
    onGatewayIt("voidVoidsTheTransaction") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2008").done
      val transaction = gateway.transaction.sale(request).getTarget
      val result = gateway.transaction.voidTransaction(transaction.getId)
      result must be ('success)
      result.getTarget.getId must be === transaction.getId
      result.getTarget.getStatus must be === Transaction.Status.VOIDED
    }

    onGatewayIt("voidWithBadId") { gateway =>
      intercept[NotFoundException] {
        gateway.transaction.voidTransaction("badId")
      }
    }

    onGatewayIt("voidWithBadStatus") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).
        creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2008").done
      val transaction = gateway.transaction.sale(request).getTarget
      gateway.transaction.voidTransaction(transaction.getId)

      val result = gateway.transaction.voidTransaction(transaction.getId)

      result must not be ('success)
      val code = result.getErrors.forObject("transaction").onField("base").get(0).getCode
      code must be === ValidationErrorCode.TRANSACTION_CANNOT_BE_VOIDED
    }
  }

  describe("statusHistory") {
    onGatewayIt("ReturnsCorrectStatusEvents") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2008").done
      val transaction = gateway.transaction.sale(request).getTarget
      val settledTransaction = gateway.transaction.submitForSettlement(transaction.getId).getTarget
      settledTransaction.getStatusHistory.size must be === 2
      settledTransaction.getStatusHistory.get(0).getStatus must be === Transaction.Status.AUTHORIZED
      settledTransaction.getStatusHistory.get(1).getStatus must be === Transaction.Status.SUBMITTED_FOR_SETTLEMENT
    }
  }

  describe("submitForSettlement") {
    onGatewayIt("submitForSettlementWithoutAmount") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2008").done
      val transaction = gateway.transaction.sale(request).getTarget
      val result = gateway.transaction.submitForSettlement(transaction.getId)
      result must be ('success)
      result.getTarget.getStatus must be === Transaction.Status.SUBMITTED_FOR_SETTLEMENT
      result.getTarget.getAmount must be === TransactionAmount.AUTHORIZE.amount
    }

    onGatewayIt("submitForSettlementWithAmount") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2008").done
      val transaction = gateway.transaction.sale(request).getTarget
      val result = gateway.transaction.submitForSettlement(transaction.getId, new BigDecimal("50.00"))
      result must be ('success)
      result.getTarget.getStatus must be === Transaction.Status.SUBMITTED_FOR_SETTLEMENT
      result.getTarget.getAmount must be === new BigDecimal("50.00")
    }

    onGatewayIt("submitForSettlementWithBadStatus") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2008").done
      val transaction = gateway.transaction.sale(request).getTarget
      gateway.transaction.submitForSettlement(transaction.getId)
      val result = gateway.transaction.submitForSettlement(transaction.getId)
      result must not be ('success)
      val code = result.getErrors.forObject("transaction").onField("base").get(0).getCode
      code must be === ValidationErrorCode.TRANSACTION_CANNOT_SUBMIT_FOR_SETTLEMENT
    }

    onGatewayIt("submitForSettlementWithBadId") { gateway =>
      intercept[NotFoundException] {
        gateway.transaction.submitForSettlement("badId")
      }
    }
  }

  describe("search") {
    onGatewayIt("searchOnAllTextFields") { gateway =>
      val creditCardToken = String.valueOf(new Random().nextInt)
      val firstName = String.valueOf(new Random().nextInt)
      val request = new TransactionRequest().amount(new BigDecimal("1000")).creditCard.number("4111111111111111").expirationDate("05/2012").cardholderName("Tom Smith").token(creditCardToken).done.billingAddress.company("Braintree").countryName("United States of America").extendedAddress("Suite 123").firstName(firstName).lastName("Smith").locality("Chicago").postalCode("12345").region("IL").streetAddress("123 Main St").done.customer.company("Braintree").email("smith@example.com").fax("5551231234").firstName("Tom").lastName("Smith").phone("5551231234").website("http://example.com").done.options.storeInVault(true).submitForSettlement(true).done.orderId("myorder").shippingAddress.company("Braintree P.S.").countryName("Mexico").extendedAddress("Apt 456").firstName("Thomas").lastName("Smithy").locality("Braintree").postalCode("54321").region("MA").streetAddress("456 Road").done
      val transaction1 = gateway.transaction.sale(request).getTarget
      transaction1 must settle(gateway)
      val transaction2 = gateway.transaction.find(transaction1.getId)
      val searchRequest = new TransactionSearchRequest().id.is(transaction1.getId).billingCompany.is("Braintree").
        billingCountryName.is("United States of America").billingExtendedAddress.is("Suite 123").
        billingFirstName.is(firstName).billingLastName.is("Smith").billingLocality.is("Chicago").
        billingPostalCode.is("12345").billingRegion.is("IL").billingStreetAddress.is("123 Main St").
        creditCardCardholderName.is("Tom Smith").creditCardExpirationDate.is("05/2012").
        creditCardNumber.is(CreditCardNumber.VISA.number).currency.is("USD").customerCompany.is("Braintree").
        customerEmail.is("smith@example.com").
        customerFax.is("5551231234").customerFirstName.is("Tom").customerId.is(transaction2.getCustomer.getId).
        customerLastName.is("Smith").customerPhone.is("5551231234").customerWebsite.is("http://example.com").
        orderId.is("myorder").paymentMethodToken.is(creditCardToken).
        processorAuthorizationCode.is(transaction2.getProcessorAuthorizationCode).
        settlementBatchId.is(transaction2.getSettlementBatchId).shippingCompany.is("Braintree P.S.").
        shippingCountryName.is("Mexico").shippingExtendedAddress.is("Apt 456").shippingFirstName.is("Thomas").
        shippingLastName.is("Smithy").shippingLocality.is("Braintree").shippingPostalCode.is("54321").
        shippingRegion.is("MA").shippingStreetAddress.is("456 Road")
      val collection = gateway.transaction.search(searchRequest)
      collection.getMaximumSize must be === 1
      collection.getFirst.getId must be === transaction1.getId
    }

    onGatewayIt("searchOnTextNodeOperators") { gateway =>
      val request = new TransactionRequest().amount(new BigDecimal("1000")).creditCard.number("4111111111111111").expirationDate("05/2012").cardholderName("Tom Smith").done
      val transaction = gateway.transaction.sale(request).getTarget
      var searchRequest= new TransactionSearchRequest().id.is(transaction.getId).creditCardCardholderName.startsWith("Tom")
      var collection = gateway.transaction.search(searchRequest)
      collection.getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).creditCardCardholderName.endsWith("Smith")
      collection = gateway.transaction.search(searchRequest)
      collection.getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).creditCardCardholderName.contains("m Sm")
      collection = gateway.transaction.search(searchRequest)
      collection.getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).creditCardCardholderName.isNot("Tom Smith")
      collection = gateway.transaction.search(searchRequest)
      collection.getMaximumSize must be === 0
    }

    onGatewayIt("searchOnNullValue") { gateway =>
      val request = new TransactionRequest().amount(new BigDecimal("1000")).creditCard.number("4111111111111111").expirationDate("05/2012").cardholderName("Tom Smith").done
      val transaction = gateway.transaction.sale(request).getTarget
      val searchRequest = new TransactionSearchRequest().id.is(transaction.getId).creditCardCardholderName.is(null)
      val collection = gateway.transaction.search(searchRequest)
      collection.getMaximumSize must be === 1
    }

    onGatewayIt("searchOnCreatedUsing") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2010").done
      val transaction = gateway.transaction.sale(request).getTarget
      var searchRequest= new TransactionSearchRequest().id.is(transaction.getId).createdUsing.is(Transaction.CreatedUsing.FULL_INFORMATION)
      var collection= gateway.transaction.search(searchRequest)
      collection.getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).createdUsing.in(Transaction.CreatedUsing.FULL_INFORMATION, Transaction.CreatedUsing.TOKEN)
      collection = gateway.transaction.search(searchRequest)
      collection.getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).createdUsing.is(Transaction.CreatedUsing.TOKEN)
      collection = gateway.transaction.search(searchRequest)
      collection.getMaximumSize must be === 0
    }

    onGatewayIt("searchOnCreditCardCustomerLocation") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2010").done
      val transaction = gateway.transaction.sale(request).getTarget
      var searchRequest= new TransactionSearchRequest().id.is(transaction.getId).creditCardCustomerLocation.is(CreditCard.CustomerLocation.US)
      var collection= gateway.transaction.search(searchRequest)
      collection.getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).creditCardCustomerLocation.in(CreditCard.CustomerLocation.US, CreditCard.CustomerLocation.INTERNATIONAL)
      collection = gateway.transaction.search(searchRequest)
      collection.getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).creditCardCustomerLocation.is(CreditCard.CustomerLocation.INTERNATIONAL)
      collection = gateway.transaction.search(searchRequest)
      collection.getMaximumSize must be === 0
    }

    onGatewayIt("searchOnMerchantAccountId") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2010").done
      val transaction = gateway.transaction.sale(request).getTarget
      var searchRequest= new TransactionSearchRequest().id.is(transaction.getId).merchantAccountId.is(transaction.getMerchantAccountId)
      var collection= gateway.transaction.search(searchRequest)
      collection.getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).merchantAccountId.in(transaction.getMerchantAccountId, "badmerchantaccountid")
      collection = gateway.transaction.search(searchRequest)
      collection.getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).merchantAccountId.is("badmerchantaccountid")
      collection = gateway.transaction.search(searchRequest)
      collection.getMaximumSize must be === 0
    }

    onGatewayIt("searchOnCreditCardType") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2010").done
      val transaction = gateway.transaction.sale(request).getTarget
      var searchRequest= new TransactionSearchRequest().id.is(transaction.getId).creditCardCardType.is(CreditCard.CardType.VISA)
      var collection= gateway.transaction.search(searchRequest)
      collection.getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).creditCardCardType.in(CreditCard.CardType.VISA, CreditCard.CardType.MASTER_CARD)
      collection = gateway.transaction.search(searchRequest)
      collection.getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).creditCardCardType.is(CreditCard.CardType.MASTER_CARD)
      collection = gateway.transaction.search(searchRequest)
      collection.getMaximumSize must be === 0
    }

    onGatewayIt("searchOnStatus") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2010").done
      val transaction = gateway.transaction.sale(request).getTarget
      var searchRequest= new TransactionSearchRequest().id.is(transaction.getId).status.is(Transaction.Status.AUTHORIZED)
      var collection= gateway.transaction.search(searchRequest)
      collection.getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).status.in(Transaction.Status.AUTHORIZED, Transaction.Status.GATEWAY_REJECTED)
      collection = gateway.transaction.search(searchRequest)
      collection.getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).status.is(Transaction.Status.GATEWAY_REJECTED)
      collection = gateway.transaction.search(searchRequest)
      collection.getMaximumSize must be === 0
    }

    onGatewayIt("searchOnAuthorizationExpiredStatus") { gateway =>
      val searchRequest = new TransactionSearchRequest().status.is(Transaction.Status.AUTHORIZATION_EXPIRED)
      val collection = gateway.transaction.search(searchRequest)
      collection.getMaximumSize must be > 0
      collection.getFirst.getStatus must be === Transaction.Status.AUTHORIZATION_EXPIRED
    }

    onGatewayIt("searchOnSource") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2010").done
      val transaction = gateway.transaction.sale(request).getTarget
      var searchRequest= new TransactionSearchRequest().id.is(transaction.getId).source.is(Transaction.Source.API)
      var collection= gateway.transaction.search(searchRequest)
      collection.getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).source.in(Transaction.Source.API, Transaction.Source.CONTROL_PANEL)
      collection = gateway.transaction.search(searchRequest)
      collection.getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).source.is(Transaction.Source.CONTROL_PANEL)
      collection = gateway.transaction.search(searchRequest)
      collection.getMaximumSize must be === 0
    }

    onGatewayIt("searchOnType") { gateway =>
      val name = String.valueOf(new Random().nextInt)
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2010").cardholderName(name).done.options.submitForSettlement(true).done
      val creditTransaction = gateway.transaction.credit(request).getTarget
      val saleTransaction = gateway.transaction.sale(request).getTarget
      saleTransaction must settle(gateway)
      val refundTransaction = gateway.transaction.refund(saleTransaction.getId).getTarget
      var searchRequest= new TransactionSearchRequest().creditCardCardholderName.is(name).`type`.is(Transaction.Type.CREDIT)
      var collection= gateway.transaction.search(searchRequest)
      collection.getMaximumSize must be === 2
      searchRequest = new TransactionSearchRequest().creditCardCardholderName.is(name).`type`.is(Transaction.Type.SALE)
      collection = gateway.transaction.search(searchRequest)
      collection.getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().creditCardCardholderName.is(name).`type`.is(Transaction.Type.CREDIT).refund.is(true)
      collection = gateway.transaction.search(searchRequest)
      collection.getMaximumSize must be === 1
      collection.getFirst.getId must be === refundTransaction.getId
      searchRequest = new TransactionSearchRequest().creditCardCardholderName.is(name).`type`.is(Transaction.Type.CREDIT).refund.is(false)
      collection = gateway.transaction.search(searchRequest)
      collection.getMaximumSize must be === 1
      collection.getFirst.getId must be === creditTransaction.getId
    }

    onGatewayIt("searchOnAmount") { gateway =>
      val request = new TransactionRequest().amount(new BigDecimal("1000")).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2010").done
      val transaction = gateway.transaction.sale(request).getTarget
      var searchRequest= new TransactionSearchRequest().id.is(transaction.getId).amount.between(new BigDecimal("500"), new BigDecimal("1500"))
      gateway.transaction.search(searchRequest).getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).amount.greaterThanOrEqualTo(new BigDecimal("500"))
      gateway.transaction.search(searchRequest).getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).amount.lessThanOrEqualTo(new BigDecimal("1500"))
      gateway.transaction.search(searchRequest).getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).amount.between(new BigDecimal("1300"), new BigDecimal("1500"))
      gateway.transaction.search(searchRequest).getMaximumSize must be === 0
    }

    onGatewayIt("searchOnDisbursementDate") { gateway =>
      val disbursementTime = CalendarHelper.dateTime("2013-04-10T00:00:00Z")
      val threeDaysEarlier = 3.days before disbursementTime
      val oneDayEarlier = 1.days before disbursementTime
      val oneDayLater = 1.days after disbursementTime
      var searchRequest= new TransactionSearchRequest().id.is(DISBURSEMENT_TRANSACTION_ID).disbursementDate.between(oneDayEarlier, oneDayLater)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(DISBURSEMENT_TRANSACTION_ID).disbursementDate.greaterThanOrEqualTo(oneDayEarlier)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(DISBURSEMENT_TRANSACTION_ID).disbursementDate.lessThanOrEqualTo(oneDayLater)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(DISBURSEMENT_TRANSACTION_ID).disbursementDate.between(threeDaysEarlier, oneDayEarlier)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 0
    }

    onGatewayIt("searchOnDisbursementDateUsingLocalTime") { gateway =>
      val oneDayEarlier = CalendarHelper.dateTime("2013-04-09T00:00:00Z", "CST")
      val oneDayLater = CalendarHelper.dateTime("2013-04-11T00:00:00Z", "CST")
      val searchRequest = new TransactionSearchRequest().id.is(DISBURSEMENT_TRANSACTION_ID).disbursementDate.between(oneDayEarlier, oneDayLater)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 1
    }

    onGatewayIt("searchOnCreatedAt") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2010").done
      val transaction = gateway.transaction.sale(request).getTarget
      val createdAt = transaction.getCreatedAt
      val threeDaysEarlier = createdAt -3.days
      val oneDayEarlier = createdAt -1.days
      val oneDayLater = 1.days after createdAt
      var searchRequest= new TransactionSearchRequest().id.is(transaction.getId).createdAt.between(oneDayEarlier, oneDayLater)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).createdAt.greaterThanOrEqualTo(oneDayEarlier)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).createdAt.lessThanOrEqualTo(oneDayLater)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).createdAt.between(threeDaysEarlier, oneDayEarlier)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 0
    }

    onGatewayIt("searchOnCreatedAtUsingLocalTime") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2010").done
      val transaction = gateway.transaction.sale(request).getTarget
      val rightNow = now
      val oneDayEarlier = 1.days before rightNow
      val oneDayLater = 1.days after rightNow
      val searchRequest = new TransactionSearchRequest().id.is(transaction.getId).createdAt.between(oneDayEarlier, oneDayLater)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 1
    }

    // TODO this test is broken in that it'll fail if it's been too long since dbdo has been run, needs fixing
    onGatewayIt("searchOnAuthorizationExpiredAt") { gateway =>
      val rightNow = now
      val threeDaysEarlier = 3.days before rightNow
      val oneDayEarlier = 1.days before rightNow
      val oneDayLater = 1.days after rightNow
      var searchRequest= new TransactionSearchRequest().authorizationExpiredAt.between(oneDayEarlier, oneDayLater)
      val results = gateway.transaction.search(searchRequest)
      results.getMaximumSize must be > 0
      results.getFirst.getStatus must be === Transaction.Status.AUTHORIZATION_EXPIRED
      searchRequest = new TransactionSearchRequest().authorizationExpiredAt.between(threeDaysEarlier, oneDayEarlier)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 0
    }

    onGatewayIt("searchOnAuthorizedAt") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2010").done
      val transaction = gateway.transaction.sale(request).getTarget
      val rightNow = now
      val threeDaysEarlier = 3.days before rightNow
      val oneDayEarlier = 1.days before rightNow
      val oneDayLater = 1.days after rightNow
      var searchRequest= new TransactionSearchRequest().id.is(transaction.getId).authorizedAt.between(oneDayEarlier, oneDayLater)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).authorizedAt.greaterThanOrEqualTo(oneDayEarlier)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).authorizedAt.lessThanOrEqualTo(oneDayLater)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).authorizedAt.between(threeDaysEarlier, oneDayEarlier)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 0
    }

    onGatewayIt("searchOnFailedAt") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.FAILED.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2010").done
      val transaction = gateway.transaction.sale(request).getTransaction
      val rightNow = now
      val threeDaysEarlier = 3.days before rightNow
      val oneDayEarlier = 1.days before rightNow
      val oneDayLater = 1.days after rightNow
      var searchRequest= new TransactionSearchRequest().id.is(transaction.getId).failedAt.between(oneDayEarlier, oneDayLater)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).failedAt.greaterThanOrEqualTo(oneDayEarlier)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).failedAt.lessThanOrEqualTo(oneDayLater)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).failedAt.between(threeDaysEarlier, oneDayEarlier)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 0
    }

    onGatewayIt("searchOnGatewayRejectedAt") { gateway =>
      val processingRulesGateway = new BraintreeGateway(Environment.DEVELOPMENT, "processing_rules_merchant_id", "processing_rules_public_key", "processing_rules_private_key")
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").cvv("200").done
      val transaction = processingRulesGateway.transaction.sale(request).getTransaction
      val rightNow = now
      val threeDaysEarlier = 3.days before rightNow
      val oneDayEarlier = 1.days before rightNow
      val oneDayLater =  1.days after rightNow
      var searchRequest= new TransactionSearchRequest().id.is(transaction.getId).gatewayRejectedAt.between(oneDayEarlier, oneDayLater)
      processingRulesGateway.transaction.search(searchRequest).getMaximumSize must be === 1

      searchRequest = new TransactionSearchRequest().
        id().is(transaction.getId()).
        gatewayRejectedAt().greaterThanOrEqualTo(oneDayEarlier)

      processingRulesGateway.transaction.search(searchRequest).getMaximumSize must be === 1

      searchRequest = new TransactionSearchRequest().
        id().is(transaction.getId()).
        gatewayRejectedAt().lessThanOrEqualTo(oneDayLater)

      processingRulesGateway.transaction.search(searchRequest).getMaximumSize must be === 1

      searchRequest = new TransactionSearchRequest().
        id().is(transaction.getId()).
        gatewayRejectedAt().between(threeDaysEarlier, oneDayEarlier)

      processingRulesGateway.transaction.search(searchRequest).getMaximumSize must be === 0
    }

    onGatewayIt("searchOnProcessorDeclinedAt") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.DECLINE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2010").done
      val transaction = gateway.transaction.sale(request).getTransaction
      val rightNow = now
      val threeDaysEarlier = 3.days before rightNow
      val oneDayEarlier = 1.days before rightNow
      val oneDayLater = 1.days after rightNow
      var searchRequest= new TransactionSearchRequest().id.is(transaction.getId).processorDeclinedAt.between(oneDayEarlier, oneDayLater)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).processorDeclinedAt.greaterThanOrEqualTo(oneDayEarlier)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).processorDeclinedAt.lessThanOrEqualTo(oneDayLater)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).processorDeclinedAt.between(threeDaysEarlier, oneDayEarlier)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 0
    }

    onGatewayIt("searchOnSettledAt") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2010").done.options.submitForSettlement(true).done
      var transaction= gateway.transaction.sale(request).getTarget
      transaction must settle(gateway)
      transaction = gateway.transaction.find(transaction.getId)
      val rightNow = now
      val threeDaysEarlier = 3.days before rightNow
      val oneDayEarlier = 1.days before rightNow
      val oneDayLater = 1.days after rightNow
      var searchRequest= new TransactionSearchRequest().id.is(transaction.getId).settledAt.between(oneDayEarlier, oneDayLater)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).settledAt.greaterThanOrEqualTo(oneDayEarlier)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).settledAt.lessThanOrEqualTo(oneDayLater)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).settledAt.between(threeDaysEarlier, oneDayEarlier)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 0
    }

    onGatewayIt("searchOnSubmittedForSettlementAt") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2010").done.options.submitForSettlement(true).done
      val transaction = gateway.transaction.sale(request).getTarget
      val rightNow = now
      val threeDaysEarlier = 3.days before rightNow
      val oneDayEarlier = 1.days before rightNow
      val oneDayLater = 1.days after rightNow
      var searchRequest= new TransactionSearchRequest().id.is(transaction.getId).submittedForSettlementAt.between(oneDayEarlier, oneDayLater)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).submittedForSettlementAt.greaterThanOrEqualTo(oneDayEarlier)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).submittedForSettlementAt.lessThanOrEqualTo(oneDayLater)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).submittedForSettlementAt.between(threeDaysEarlier, oneDayEarlier)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 0
    }

    onGatewayIt("searchOnVoidedAt") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2010").done
      var transaction= gateway.transaction.sale(request).getTarget
      transaction = gateway.transaction.voidTransaction(transaction.getId).getTarget
      val rightNow = now
      val threeDaysEarlier = 3.days before rightNow
      val oneDayEarlier = 1.days before rightNow
      val oneDayLater = 1.days after rightNow
      var searchRequest= new TransactionSearchRequest().id.is(transaction.getId).voidedAt.between(oneDayEarlier, oneDayLater)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).voidedAt.greaterThanOrEqualTo(oneDayEarlier)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).voidedAt.lessThanOrEqualTo(oneDayLater)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).voidedAt.between(threeDaysEarlier, oneDayEarlier)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 0
    }

    onGatewayIt("searchOnMultipleStatusAtFields") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2010").done.options.submitForSettlement(true).done
      val transaction = gateway.transaction.sale(request).getTarget
      val rightNow = now
      val threeDaysEarlier = 3.days before rightNow
      val oneDayEarlier = 1.days before rightNow
      val oneDayLater = 1.days after rightNow
      var searchRequest= new TransactionSearchRequest().id.is(transaction.getId).authorizedAt.between(oneDayEarlier, oneDayLater).submittedForSettlementAt.between(oneDayEarlier, oneDayLater)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 1
      searchRequest = new TransactionSearchRequest().id.is(transaction.getId).authorizedAt.between(threeDaysEarlier, oneDayEarlier).submittedForSettlementAt.between(threeDaysEarlier, oneDayEarlier)
      gateway.transaction.search(searchRequest).getMaximumSize must be === 0
    }
  }

  describe("refund") {
    onGatewayIt("refund transaction") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2008").done.options.submitForSettlement(true).done
      val sale = gateway.transaction.sale(request).getTarget
      sale must settle(gateway)
      val result = gateway.transaction.refund(sale.getId)
      result must be ('success)
      val refund = result.getTarget
      val originalTransaction = gateway.transaction.find(sale.getId)
      refund.getType must be === Transaction.Type.CREDIT
      refund.getAmount must be === originalTransaction.getAmount
      originalTransaction.getRefundId must be === refund.getId
      refund.getRefundedTransactionId must be === originalTransaction.getId
    }

    onGatewayIt("refundTransactionWithPartialAmount") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2008").done.options.submitForSettlement(true).done
      val transaction = gateway.transaction.sale(request).getTarget
      transaction must settle(gateway)
      val result = gateway.transaction.refund(transaction.getId, TransactionAmount.AUTHORIZE.amount.divide(new BigDecimal(2)))
      result must be ('success)
      result.getTarget.getType must be === Transaction.Type.CREDIT
      result.getTarget.getAmount must be === TransactionAmount.AUTHORIZE.amount.divide(new BigDecimal(2))
    }

    onGatewayIt("refundMultipleTransactionsWithPartialAmounts") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2008").done.options.submitForSettlement(true).done
      var transaction= gateway.transaction.sale(request).getTarget
      transaction must settle(gateway)
      val refund1 = gateway.transaction.refund(transaction.getId, TransactionAmount.AUTHORIZE.amount.divide(new BigDecimal(2))).getTarget
      refund1.getType must be === Transaction.Type.CREDIT
      refund1.getAmount must be === TransactionAmount.AUTHORIZE.amount.divide(new BigDecimal(2))
      val refund2 = gateway.transaction.refund(transaction.getId, TransactionAmount.AUTHORIZE.amount.divide(new BigDecimal(2))).getTarget
      refund2.getType must be === Transaction.Type.CREDIT
      refund2.getAmount must be === TransactionAmount.AUTHORIZE.amount.divide(new BigDecimal(2))
      transaction = gateway.transaction.find(transaction.getId)
      transaction.getRefundIds must contain (refund1.getId)
      transaction.getRefundIds must contain (refund1.getId)
    }

    onGatewayIt("refundFailsWithNonSettledTransaction") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2008").done
      val transaction = gateway.transaction.sale(request).getTarget
      transaction.getStatus must be === Transaction.Status.AUTHORIZED
      val result = gateway.transaction.refund(transaction.getId)
      result must not be ('success)
      result.getErrors.forObject("transaction").onField("base").get(0).getCode must be === ValidationErrorCode.TRANSACTION_CANNOT_REFUND_UNLESS_SETTLED
    }
  }

  describe("Transaction constructor") {
    onGatewayIt("unrecognizedStatus") { gateway =>
      val xml = <transaction><status>foobar</status><billing/><credit-card/><customer/><descriptor/><shipping/><subscription/><service-fee></service-fee><disbursement-details/><type>sale</type></transaction>
      val transaction = new Transaction(NodeWrapperFactory.instance.create(xml.toString))
      transaction.getStatus must be === Transaction.Status.UNRECOGNIZED
    }

    onGatewayIt("unrecognizedType") { gateway =>
      val xml = <transaction><type>foobar</type><billing/><credit-card/><customer/><descriptor/><shipping/><subscription/><service-fee></service-fee><disbursement-details/><type>sale</type></transaction>
      val transaction = new Transaction(NodeWrapperFactory.instance.create(xml.toString))
      transaction.getType must be === Transaction.Type.UNRECOGNIZED
    }
  }

  describe("gateway rejections") {
    onGatewayIt("gatewayRejectedOnCvv") { gateway =>
      val processingRulesGateway = new BraintreeGateway(Environment.DEVELOPMENT, "processing_rules_merchant_id", "processing_rules_public_key", "processing_rules_private_key")
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").cvv("200").done
      val result = processingRulesGateway.transaction.sale(request)
      result must not be ('success)
      val transaction = result.getTransaction
      transaction.getGatewayRejectionReason must be === Transaction.GatewayRejectionReason.CVV
    }

    onGatewayIt("gatewayRejectedOnAvs") { gateway =>
      val processingRulesGateway = new BraintreeGateway(Environment.DEVELOPMENT, "processing_rules_merchant_id", "processing_rules_public_key", "processing_rules_private_key")
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).billingAddress.postalCode("20001").done.creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done
      val result = processingRulesGateway.transaction.sale(request)
      result must not be ('success)
      val transaction = result.getTransaction
      transaction.getGatewayRejectionReason must be === Transaction.GatewayRejectionReason.AVS
    }

    onGatewayIt("gatewayRejectedOnAvsAndCvv") { gateway =>
      val processingRulesGateway = new BraintreeGateway(Environment.DEVELOPMENT, "processing_rules_merchant_id", "processing_rules_public_key", "processing_rules_private_key")
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).billingAddress.postalCode("20001").done.creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").cvv("200").done
      val result = processingRulesGateway.transaction.sale(request)
      result must not be ('success)
      val transaction = result.getTransaction
      transaction.getGatewayRejectionReason must be === Transaction.GatewayRejectionReason.AVS_AND_CVV
    }
  }

  describe("addOns") {
    onGatewayIt("snapshotPlanIdAddOnsAndDiscountsFromSubscription") { gateway =>
      val customerRequest = new CustomerRequest().creditCard.number("5105105105105100").expirationDate("05/12").done
      val creditCard = gateway.customer.create(customerRequest).getTarget.getCreditCards.get(0)
      val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(PlanFixture.PLAN_WITHOUT_TRIAL.getId).addOns.add.amount(new BigDecimal("11.00")).inheritedFromId("increase_10").numberOfBillingCycles(5).quantity(2).done.add.amount(new BigDecimal("21.00")).inheritedFromId("increase_20").numberOfBillingCycles(6).quantity(3).done.done.discounts.add.amount(new BigDecimal("7.50")).inheritedFromId("discount_7").neverExpires(true).quantity(2).done.done
      val transaction = gateway.subscription.create(request).getTarget.getTransactions.get(0)
      transaction.getPlanId must be === PlanFixture.PLAN_WITHOUT_TRIAL.getId
      val addOns = transaction.getAddOns.sortWith((a,b) => a.getId < b.getId)

      addOns.size must be === 2
      addOns.get(0).getId must be === "increase_10"
      addOns.get(0).getAmount must be === new BigDecimal("11.00")
      addOns.get(0).getNumberOfBillingCycles must be === new Integer(5)
      addOns.get(0).getQuantity must be === new Integer(2)
      addOns.get(0).neverExpires must be === false
      addOns.get(1).getId must be === "increase_20"
      addOns.get(1).getAmount must be === new BigDecimal("21.00")
      addOns.get(1).getNumberOfBillingCycles must be === new Integer(6)
      addOns.get(1).getQuantity must be === new Integer(3)
      addOns.get(1).neverExpires must be === false
      val discounts = transaction.getDiscounts
      discounts.size must be === 1
      discounts.get(0).getId must be === "discount_7"
      discounts.get(0).getAmount must be === new BigDecimal("7.50")
      discounts.get(0).getNumberOfBillingCycles must be === (null)
      discounts.get(0).getQuantity must be === new Integer(2)
      discounts.get(0).neverExpires must be === true
    }
  }

  describe("service fees") {
    onGatewayIt("sales with serviceFee") { gateway =>
      val request = new TransactionRequest().merchantAccountId(NON_DEFAULT_SUB_MERCHANT_ACCOUNT_ID).amount(new BigDecimal("100.00")).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done.serviceFeeAmount(new BigDecimal("1.00"))
      val result = gateway.transaction.sale(request)
      result must be ('success)
      val transaction = result.getTarget
      transaction.getServiceFeeAmount must be === new BigDecimal("1.00")
    }

    onGatewayIt("serviceFeeNotAllowedForMasterMerchant") { gateway =>
      val request = new TransactionRequest().merchantAccountId(NON_DEFAULT_MERCHANT_ACCOUNT_ID).amount(new BigDecimal("100.00")).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2017").done.serviceFeeAmount(new BigDecimal("1.00"))
      val result = gateway.transaction.sale(request)
      result must not be ('success)
      result.getErrors.forObject("transaction").onField("service_fee_amount").get(0).getCode must be === ValidationErrorCode.TRANSACTION_SERVICE_FEE_AMOUNT_NOT_ALLOWED_ON_MASTER_MERCHANT_ACCOUNT
    }

    onGatewayIt("serviceFeeRequiredWhenUsingSubmerchant") { gateway =>
      val request = new TransactionRequest().merchantAccountId(NON_DEFAULT_SUB_MERCHANT_ACCOUNT_ID).amount(new BigDecimal("100.00")).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done
      val result = gateway.transaction.sale(request)
      result must not be ('success)
      result.getErrors.forObject("transaction").onField("merchant_account_id").get(0).getCode must be === ValidationErrorCode.TRANSACTION_SUB_MERCHANT_ACCOUNT_REQUIRES_SERVICE_FEE_AMOUNT
    }

    onGatewayIt("negativeServiceFee") { gateway =>
      val request = new TransactionRequest().merchantAccountId(NON_DEFAULT_SUB_MERCHANT_ACCOUNT_ID).amount(new BigDecimal("100.00")).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done.serviceFeeAmount(new BigDecimal("-1.00"))
      val result = gateway.transaction.sale(request)
      result must not be ('success)
      result.getErrors.forObject("transaction").onField("service_fee_amount").get(0).getCode must be === ValidationErrorCode.TRANSACTION_SERVICE_FEE_AMOUNT_CANNOT_BE_NEGATIVE
    }
  }

  describe("holdInEscrow") {
    onGatewayIt("holdInEscrowOnCreate") { gateway =>
      val request = new TransactionRequest().merchantAccountId(NON_DEFAULT_SUB_MERCHANT_ACCOUNT_ID).amount(new BigDecimal("100.00")).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done.serviceFeeAmount(new BigDecimal("1.00")).options.holdInEscrow(true).done
      val result = gateway.transaction.sale(request)
      result must be ('success)
      result.getTarget.getEscrowStatus must be === Transaction.EscrowStatus.HOLD_PENDING
    }

    onGatewayIt("holdInEscrowOnSaleForMasterMerchantAccount") { gateway =>
      val request = new TransactionRequest().merchantAccountId(NON_DEFAULT_MERCHANT_ACCOUNT_ID).amount(new BigDecimal("100.00")).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done.serviceFeeAmount(new BigDecimal("1.00")).options.holdInEscrow(true).done
      val result = gateway.transaction.sale(request)
      result must not be ('success)
      result.getErrors.forObject("transaction").onField("base").get(0).getCode must be === ValidationErrorCode.TRANSACTION_CANNOT_HOLD_IN_ESCROW
    }

    onGatewayIt("holdInEscrowAfterSale") { gateway =>
      val request = new TransactionRequest().merchantAccountId(NON_DEFAULT_SUB_MERCHANT_ACCOUNT_ID).amount(new BigDecimal("100.00")).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2012").done.serviceFeeAmount(new BigDecimal("1.00"))
      val sale = gateway.transaction.sale(request)
      sale must be ('success)
      val transactionID = sale.getTarget.getId
      val holdInEscrow = gateway.transaction.holdInEscrow(transactionID)
      holdInEscrow must be ('success)
      holdInEscrow.getTarget.getEscrowStatus must be === Transaction.EscrowStatus.HOLD_PENDING
    }

    onGatewayIt("holdInEscrowAfterSaleFailsForMasterMerchants") { gateway =>
      val request = new TransactionRequest().merchantAccountId(NON_DEFAULT_MERCHANT_ACCOUNT_ID).amount(new BigDecimal("100.00")).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2012").done
      val sale = gateway.transaction.sale(request)
      sale must be ('success)
      val holdInEscrow = gateway.transaction.holdInEscrow(sale.getTarget.getId)
      holdInEscrow must not be ('success)
      holdInEscrow.getErrors.forObject("transaction").onField("base").get(0).getCode must be === ValidationErrorCode.TRANSACTION_CANNOT_HOLD_IN_ESCROW
    }
  }

  describe("release from escrow") {
    onGatewayIt("releaseFromEscrow") { gateway =>
      val request = new TransactionRequest().merchantAccountId(NON_DEFAULT_SUB_MERCHANT_ACCOUNT_ID).amount(new BigDecimal("100.00")).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2012").done.serviceFeeAmount(new BigDecimal("1.00"))
      val saleResult = gateway.transaction.sale(request)
      saleResult must be ('success)
      saleResult.getTarget must escrow(gateway)
      val releaseResult = gateway.transaction.releaseFromEscrow(saleResult.getTarget.getId)
      releaseResult must be ('success)
      releaseResult.getTarget.getEscrowStatus must be === Transaction.EscrowStatus.RELEASE_PENDING
    }

    onGatewayIt("releaseFromEscrowFailsWhenTransactionIsNotEscrowed") { gateway =>
      val request = new TransactionRequest().merchantAccountId(NON_DEFAULT_SUB_MERCHANT_ACCOUNT_ID).amount(new BigDecimal("100.00")).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2012").done.serviceFeeAmount(new BigDecimal("1.00"))
      val saleResult = gateway.transaction.sale(request)
      saleResult must be ('success)
      val releaseResult = gateway.transaction.releaseFromEscrow(saleResult.getTarget.getId)
      releaseResult must not be ('success)
      releaseResult.getErrors.forObject("transaction").onField("base").get(0).getCode must be === ValidationErrorCode.TRANSACTION_CANNOT_RELEASE_FROM_ESCROW
    }

    onGatewayIt("cancelReleaseSucceeds") { gateway =>
      val request = new TransactionRequest().merchantAccountId(NON_DEFAULT_SUB_MERCHANT_ACCOUNT_ID).amount(new BigDecimal("100.00")).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2012").done.serviceFeeAmount(new BigDecimal("1.00"))
      val saleResult = gateway.transaction.sale(request)
      saleResult must be ('success)
      saleResult.getTarget must escrow(gateway)
      val releaseResult = gateway.transaction.releaseFromEscrow(saleResult.getTarget.getId)
      val cancelResult = gateway.transaction.cancelRelease(saleResult.getTarget.getId)
      cancelResult must be ('success)
      cancelResult.getTarget.getEscrowStatus must be === Transaction.EscrowStatus.HELD
    }

    onGatewayIt("cancelReleaseFailsReleasingNonPendingTransactions") { gateway =>
      val request = new TransactionRequest().merchantAccountId(NON_DEFAULT_SUB_MERCHANT_ACCOUNT_ID).amount(new BigDecimal("100.00")).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2012").done.serviceFeeAmount(new BigDecimal("1.00"))
      val saleResult = gateway.transaction.sale(request)
      saleResult must be ('success)
      saleResult.getTarget must escrow(gateway)
      val cancelResult = gateway.transaction.cancelRelease(saleResult.getTarget.getId)
      cancelResult must not be ('success)
      cancelResult.getErrors.forObject("transaction").onField("base").get(0).getCode must be === ValidationErrorCode.TRANSACTION_CANNOT_CANCEL_RELEASE
    }
  }
}
