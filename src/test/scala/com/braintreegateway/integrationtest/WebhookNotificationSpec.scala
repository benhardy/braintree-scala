package com.braintreegateway.integrationtest

import com.braintreegateway.{WebhookNotifications, MerchantAccount, WebhookNotification, ValidationErrorCode}
import com.braintreegateway.exceptions.InvalidSignatureException
import com.braintreegateway.testhelpers.{CalendarHelper, TestHelper, GatewaySpec}
import com.braintreegateway.util.NodeWrapperFactory
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
      notification.kind must be === WebhookNotifications.Kind.UNRECOGNIZED
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
        val sampleNotification = gateway.webhookTesting.sampleNotification(WebhookNotifications.Kind.SUBSCRIPTION_WENT_PAST_DUE, "my_id")
        val notification = gateway.webhookNotification.parse(sampleNotification.get("signature"), sampleNotification.get("payload"))
        notification.kind must be === WebhookNotifications.Kind.SUBSCRIPTION_WENT_PAST_DUE
        notification.subscription.get.id must be === "my_id"
        notification.timestamp must beSameDayAs(Calendar.getInstance)
    }

    onGatewayIt("createsSampleMerchantAccountApprovedNotification") {
      gateway =>
        val sampleNotification = gateway.webhookTesting.sampleNotification(WebhookNotifications.Kind.SUB_MERCHANT_ACCOUNT_APPROVED, "my_id")
        val notification = gateway.webhookNotification.parse(sampleNotification.get("signature"), sampleNotification.get("payload"))
        notification.kind must be === WebhookNotifications.Kind.SUB_MERCHANT_ACCOUNT_APPROVED
        notification.merchantAccount.get.getId must be === "my_id"
        notification.merchantAccount.get.getStatus must be === MerchantAccount.Status.ACTIVE
        notification.timestamp must beSameDayAs(Calendar.getInstance)
    }

    onGatewayIt("createsSampleMerchantAccountDeclinedNotification") {
      gateway =>
        val sampleNotification = gateway.webhookTesting.sampleNotification(WebhookNotifications.Kind.SUB_MERCHANT_ACCOUNT_DECLINED, "my_id")
        val notification = gateway.webhookNotification.parse(sampleNotification.get("signature"), sampleNotification.get("payload"))
        notification.kind must be === WebhookNotifications.Kind.SUB_MERCHANT_ACCOUNT_DECLINED
        notification.merchantAccount.get.getId must be === "my_id"
        notification.merchantAccount.get.getStatus must be === MerchantAccount.Status.SUSPENDED
        notification.timestamp must beSameDayAs(Calendar.getInstance)
    }

    onGatewayIt("createsSampleMerchantAccountDeclinedNotificationWithErrorCodes") {
      gateway =>
        val sampleNotification = gateway.webhookTesting.sampleNotification(WebhookNotifications.Kind.SUB_MERCHANT_ACCOUNT_DECLINED, "my_id")
        val notification = gateway.webhookNotification.parse(sampleNotification.get("signature"), sampleNotification.get("payload"))
        notification.kind must be === WebhookNotifications.Kind.SUB_MERCHANT_ACCOUNT_DECLINED
        notification.merchantAccount.get.getId must be === "my_id"
        notification.timestamp must beSameDayAs(Calendar.getInstance)
        val code = notification.errors.forObject("merchantAccount").onField("base")(0).code
        code must be === ValidationErrorCode.MERCHANT_ACCOUNT_APPLICANT_DETAILS_DECLINED_OFAC
    }

    onGatewayIt("invalidSignatureRaisesException") {
      gateway =>
        intercept[InvalidSignatureException] {
          val sampleNotification = gateway.webhookTesting.sampleNotification(WebhookNotifications.Kind.SUBSCRIPTION_WENT_PAST_DUE, "my_id")
          gateway.webhookNotification.parse(sampleNotification.get("signature") + "bad_stuff", sampleNotification.get("payload"))
        }
    }

    onGatewayIt("signatureWithoutMatchingPublicKeyRaisesException") {
      gateway =>
        intercept[InvalidSignatureException] {
          val sampleNotification = gateway.webhookTesting.sampleNotification(WebhookNotifications.Kind.SUBSCRIPTION_WENT_PAST_DUE, "my_id")
          gateway.webhookNotification.parse("uknown_public_key|signature", sampleNotification.get("payload"))
        }
    }

    onGatewayIt("createsSampleTransactionDisbursedNotification") {
      gateway =>
        val sampleNotification = gateway.webhookTesting.sampleNotification(WebhookNotifications.Kind.TRANSACTION_DISBURSED, "my_id")
        val notification = gateway.webhookNotification.parse(sampleNotification.get("signature"), sampleNotification.get("payload"))
        notification.kind must be === WebhookNotifications.Kind.TRANSACTION_DISBURSED
        notification.transaction.get.getId must be === "my_id"
        val actualDate = notification.transaction.get.getDisbursementDetails.disbursementDate.get
        val expected = CalendarHelper.date(2013, Calendar.JULY, 9).in(actualDate.getTimeZone)
        actualDate must beSameDayAs(expected)

    }
  }
}