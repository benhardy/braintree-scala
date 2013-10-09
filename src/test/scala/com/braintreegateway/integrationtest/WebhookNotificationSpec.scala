package com.braintreegateway.integrationtest

import com.braintreegateway.MerchantAccount
import com.braintreegateway.WebhookNotification
import com.braintreegateway.exceptions.InvalidSignatureException
import com.braintreegateway.testhelpers.{TestHelper,GatewaySpec}
import com.braintreegateway.util.NodeWrapperFactory
import com.braintreegateway.ValidationErrorCode
import java.util.Calendar
import org.scalatest.matchers.MustMatchers
import TestHelper.beSameDayAs
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.braintreegateway.testhelpers.CalendarHelper._

@RunWith(classOf[JUnitRunner])
class WebhookNotificationSpec extends GatewaySpec with MustMatchers {
  describe("create") {
    it("categorized unrecognized kinds") {
      val xml = <notification>
        <kind>bad_kind</kind> <subject></subject>
      </notification>
      val node = NodeWrapperFactory.create(xml.toString)
      val notification = new WebhookNotification(node)
      notification.getKind must be === WebhookNotification.Kind.UNRECOGNIZED
    }
  }
  describe("verify") {
    onGatewayIt("creates verification string") {
      gateway =>
        val verification = gateway.webhookNotification.verify("verification_token")
        verification must be === "integration_public_key|c9f15b74b0d98635cd182c51e2703cffa83388c3"
    }
  }

  describe("creating sample notifications") {

    onGatewayIt("createsSampleSubscriptionNotification") {
      gateway =>
        val sampleNotification = gateway.webhookTesting.sampleNotification(WebhookNotification.Kind.SUBSCRIPTION_WENT_PAST_DUE, "my_id")
        val notification = gateway.webhookNotification.parse(sampleNotification.get("signature"), sampleNotification.get("payload"))
        notification.getKind must be === WebhookNotification.Kind.SUBSCRIPTION_WENT_PAST_DUE
        notification.getSubscription.getId must be === "my_id"
        notification.getTimestamp must beSameDayAs(Calendar.getInstance)
    }

    onGatewayIt("createsSampleMerchantAccountApprovedNotification") {
      gateway =>
        val sampleNotification = gateway.webhookTesting.sampleNotification(WebhookNotification.Kind.SUB_MERCHANT_ACCOUNT_APPROVED, "my_id")
        val notification = gateway.webhookNotification.parse(sampleNotification.get("signature"), sampleNotification.get("payload"))
        notification.getKind must be === WebhookNotification.Kind.SUB_MERCHANT_ACCOUNT_APPROVED
        notification.getMerchantAccount.getId must be === "my_id"
        notification.getMerchantAccount.getStatus must be === MerchantAccount.Status.ACTIVE
        notification.getTimestamp must beSameDayAs(Calendar.getInstance)
    }

    onGatewayIt("createsSampleMerchantAccountDeclinedNotification") {
      gateway =>
        val sampleNotification = gateway.webhookTesting.sampleNotification(WebhookNotification.Kind.SUB_MERCHANT_ACCOUNT_DECLINED, "my_id")
        val notification = gateway.webhookNotification.parse(sampleNotification.get("signature"), sampleNotification.get("payload"))
        notification.getKind must be === WebhookNotification.Kind.SUB_MERCHANT_ACCOUNT_DECLINED
        notification.getMerchantAccount.getId must be === "my_id"
        notification.getMerchantAccount.getStatus must be === MerchantAccount.Status.SUSPENDED
        notification.getTimestamp must beSameDayAs(Calendar.getInstance)
    }

    onGatewayIt("createsSampleMerchantAccountDeclinedNotificationWithErrorCodes") {
      gateway =>
        val sampleNotification = gateway.webhookTesting.sampleNotification(WebhookNotification.Kind.SUB_MERCHANT_ACCOUNT_DECLINED, "my_id")
        val notification = gateway.webhookNotification.parse(sampleNotification.get("signature"), sampleNotification.get("payload"))
        notification.getKind must be === WebhookNotification.Kind.SUB_MERCHANT_ACCOUNT_DECLINED
        notification.getMerchantAccount.getId must be === "my_id"
        notification.getTimestamp must beSameDayAs(Calendar.getInstance)
        val code = notification.getErrors.forObject("merchantAccount").onField("base").get(0).getCode
        code must be === ValidationErrorCode.MERCHANT_ACCOUNT_APPLICANT_DETAILS_DECLINED_OFAC
    }

    onGatewayIt("invalidSignatureRaisesException") {
      gateway =>
        intercept[InvalidSignatureException] {
          val sampleNotification = gateway.webhookTesting.sampleNotification(WebhookNotification.Kind.SUBSCRIPTION_WENT_PAST_DUE, "my_id")
          gateway.webhookNotification.parse(sampleNotification.get("signature") + "bad_stuff", sampleNotification.get("payload"))
        }
    }

    onGatewayIt("signatureWithoutMatchingPublicKeyRaisesException") {
      gateway =>
        intercept[InvalidSignatureException] {
          val sampleNotification = gateway.webhookTesting.sampleNotification(WebhookNotification.Kind.SUBSCRIPTION_WENT_PAST_DUE, "my_id")
          gateway.webhookNotification.parse("uknown_public_key|signature", sampleNotification.get("payload"))
        }
    }

    onGatewayIt("createsSampleTransactionDisbursedNotification") {
      gateway =>
        val sampleNotification = gateway.webhookTesting.sampleNotification(WebhookNotification.Kind.TRANSACTION_DISBURSED, "my_id")
        val notification = gateway.webhookNotification.parse(sampleNotification.get("signature"), sampleNotification.get("payload"))
        notification.getKind must be === WebhookNotification.Kind.TRANSACTION_DISBURSED
        notification.getTransaction.getId must be === "my_id"
        notification.getTransaction.getDisbursementDetails.getDisbursementDate.get(Calendar.YEAR) must be === 2013
        notification.getTransaction.getDisbursementDetails.getDisbursementDate.get(Calendar.MONTH) must be === Calendar.JULY
        notification.getTransaction.getDisbursementDetails.getDisbursementDate.get(Calendar.DAY_OF_MONTH) must be === 9
    }
  }
}