package com.braintreegateway.integrationtest

import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.scalatest.matchers.MustMatchers
import com.braintreegateway._
import com.braintreegateway.SandboxValues.CreditCardNumber
import com.braintreegateway.SandboxValues.TransactionAmount
import testhelpers.{GatewaySpec, MerchantAccountTestConstants, TestHelper}
import java.math.BigDecimal

import MerchantAccountTestConstants._

@RunWith(classOf[JUnitRunner])
class TransparentRedirectSpec extends GatewaySpec with MustMatchers {

  describe("creating transaction") {
    onGatewayIt("basically works") {
      gateway =>
        val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done.options.storeInVault(true).done
        val trParams = new TransactionRequest().`type`(Transaction.Type.SALE)
        val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request, gateway.transparentRedirect.url)
        val result = gateway.transparentRedirect.confirmTransaction(queryString)
        result must be('success)
        result.getTarget.getCreditCard.getBin must be === CreditCardNumber.VISA.number.substring(0, 6)
        result.getTarget.getAmount must be === TransactionAmount.AUTHORIZE.amount
    }

    onGatewayIt("can specify merchant id") {
      gateway =>
        val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done
        val trParams = new TransactionRequest().`type`(Transaction.Type.SALE).merchantAccountId(NON_DEFAULT_MERCHANT_ACCOUNT_ID)
        val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request, gateway.transparentRedirect.url)
        val result = gateway.transparentRedirect.confirmTransaction(queryString)
        result must be('success)
        result.getTarget.getMerchantAccountId must be === NON_DEFAULT_MERCHANT_ACCOUNT_ID
    }

    onGatewayIt("can specify descriptor") {
      gateway =>
        val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).
          creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done
        val trParams = new TransactionRequest().`type`(Transaction.Type.SALE).
          descriptor.name("123*123456789012345678").phone("3334445555").done
        val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request, gateway.transparentRedirect.url)
        val result = gateway.transparentRedirect.confirmTransaction(queryString)
        result must be('success)
        val transaction = result.getTarget
        transaction.getDescriptor.getName must be === "123*123456789012345678"
        transaction.getDescriptor.getPhone must be === "3334445555"
    }

    onGatewayIt("can specify level 2 attribtues") {
      gateway =>
        val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).
          creditCard.number(CreditCardNumber.VISA.number).expirationDate("05/2009").done
        val trParams = new TransactionRequest().`type`(Transaction.Type.SALE).
          taxAmount(new BigDecimal("10.00")).taxExempt(true).purchaseOrderNumber("12345")
        val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request, gateway.transparentRedirect.url)
        val result = gateway.transparentRedirect.confirmTransaction(queryString)
        result must be('success)
        val transaction = result.getTarget
        transaction.getTaxAmount must be === new BigDecimal("10.00")
        transaction.isTaxExempt must be === true
        transaction.getPurchaseOrderNumber must be === "12345"
    }
  }

  describe("customer operations") {
    onGatewayIt("creates via TR") {
      gateway =>
        val request = new CustomerRequest().firstName("John")
        val trParams = new CustomerRequest().lastName("Doe")
        val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request, gateway.transparentRedirect.url)
        val result = gateway.transparentRedirect.confirmCustomer(queryString)
        result must be('success)
        result.getTarget.getFirstName must be === "John"
        result.getTarget.getLastName must be === "Doe"
    }

    onGatewayIt("updates via TR") {
      gateway =>
        val request = new CustomerRequest().firstName("John").lastName("Doe")
        val customer = gateway.customer.create(request).getTarget
        val updateRequest = new CustomerRequest().firstName("Jane")
        val trParams = new CustomerRequest().customerId(customer.getId).lastName("Dough")
        val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, updateRequest, gateway.transparentRedirect.url)
        val result = gateway.transparentRedirect.confirmCustomer(queryString)
        result must be('success)
        val updatedCustomer = gateway.customer.find(customer.getId)
        updatedCustomer.getFirstName must be === "Jane"
        updatedCustomer.getLastName must be === "Dough"
    }
  }

  describe("credit card operations") {
    onGatewayIt("can create") {
      gateway =>
        val customer = gateway.customer.create(new CustomerRequest).getTarget
        val request = new CreditCardRequest
        val trParams = new CreditCardRequest().customerId(customer.getId).number("4111111111111111").expirationDate("10/10")
        val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request, gateway.transparentRedirect.url)
        val result = gateway.transparentRedirect.confirmCreditCard(queryString)
        result must be('success)
        result.getTarget.getBin must be === "411111"
        result.getTarget.getLast4 must be === "1111"
        result.getTarget.getExpirationDate must be === "10/2010"
    }

    onGatewayIt("can update") {
      gateway =>
        val customer = gateway.customer.create(new CustomerRequest).getTarget
        val request = new CreditCardRequest().customerId(customer.getId).number("5105105105105100").expirationDate("05/12")
        val card = gateway.creditCard.create(request).getTarget
        val updateRequest = new CreditCardRequest
        val trParams = new CreditCardRequest().paymentMethodToken(card.getToken).number("4111111111111111").expirationDate("10/10")
        val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, updateRequest, gateway.transparentRedirect.url)
        val result = gateway.transparentRedirect.confirmCreditCard(queryString)
        result must be('success)
        val updatedCreditCard = gateway.creditCard.find(card.getToken)
        updatedCreditCard.getBin must be === "411111"
        updatedCreditCard.getLast4 must be === "1111"
        updatedCreditCard.getExpirationDate must be === "10/2010"
    }
  }

  describe("error handling") {
    onGatewayIt("throws exception when confirming incorrect resource") {
      gateway =>
        val request = new CustomerRequest().firstName("John")
        val trParams = new CustomerRequest().lastName("Doe")
        val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, request, gateway.transparentRedirect.url)
        intercept[Exception] {
          gateway.transparentRedirect.confirmTransaction(queryString)
        }.getMessage must be === "You attemped to confirm a transaction, but received a customer."
    }

    onGatewayIt("doesn't raise error when receiving API error response") {
      gateway =>
        val invalidRequest = new TransactionRequest
        val trParams = new TransactionRequest().`type`(Transaction.Type.SALE)
        val queryString = TestHelper.simulateFormPostForTR(gateway, trParams, invalidRequest, gateway.transparentRedirect.url)
        val result = gateway.transparentRedirect.confirmTransaction(queryString)
        result must not be ('success)
        result.getErrors.deepSize must be > 0
    }
  }
}