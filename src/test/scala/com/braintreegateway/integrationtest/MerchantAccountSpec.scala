package com.braintreegateway.integrationtest

import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.scalatest.matchers.MustMatchers
import com.braintreegateway._
import com.braintreegateway.testhelpers.GatewaySpec
import gw.{Success, Failure}
import java.util.Random

@RunWith(classOf[JUnitRunner])
class MerchantAccountSpec extends GatewaySpec with MustMatchers {

  describe("create") {
    onGatewayIt("requires no id") {
      gateway =>
        gateway.merchantAccount.create(creationRequest) match {
          case Success(ma) => {
            ma.getStatus must be === MerchantAccount.Status.PENDING
            ma.getMasterMerchantAccount.getId must be === "sandbox_master_merchant_account"
            ma must be('subMerchant)
            ma.getMasterMerchantAccount must not be ('subMerchant)
          }
          case _ => fail("expected success")
        }
    }

    onGatewayIt("will use id if passed") {
      gateway =>
        val randomNumber = new Random().nextInt % 10000
        val subMerchantAccountId = "sub_merchant_account_id_%d".format(randomNumber)
        val request = creationRequest.id(subMerchantAccountId)
        gateway.merchantAccount.create(request) match {
          case Success(ma) => {
            ma.getStatus must be === MerchantAccount.Status.PENDING
            subMerchantAccountId must be === ma.getId
            ma.getMasterMerchantAccount.getId must be === "sandbox_master_merchant_account"
            ma must be('subMerchant)
            ma.getMasterMerchantAccount must not be ('subMerchant)
          }
          case _ => fail("expected success")
        }
    }

    onGatewayIt("handles unsuccessful results") {
      gateway =>
        val result = gateway.merchantAccount.create(new MerchantAccountRequest)
        result match {
          case Failure(allErrors, _, _, _, _, _) => {
            val errors = allErrors.forObject("merchant-account").onField("master_merchant_account_id")
            errors.size must be === 1
            val code = errors.get(0).code
            code must be === ValidationErrorCode.MERCHANT_ACCOUNT_MASTER_MERCHANT_ACCOUNT_ID_IS_REQUIRED
          }
          case x => fail("expected Failure got " + x)
        }
    }
  }

  private def creationRequest = {
    new MerchantAccountRequest().
      applicantDetails.firstName("Joe").lastName("Bloggs").email("joe@bloggs.com").phone("555-555-5555").
      address.streetAddress("123 Credibility St.").postalCode("60606").locality("Chicago").region("IL").done.
      dateOfBirth("10/9/1980").ssn("123-456-7890").routingNumber("122100024").accountNumber("98479798798").
      taxId("123456789").companyName("Calculon's Drama School").done.tosAccepted(true).
      masterMerchantAccountId("sandbox_master_merchant_account")
  }

}