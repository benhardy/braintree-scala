package com.braintreegateway.integrationtest

import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.scalatest.matchers.MustMatchers
import com.braintreegateway._
import com.braintreegateway.SandboxValues.CreditCardNumber
import com.braintreegateway.SandboxValues.TransactionAmount
import gw.Success
import testhelpers.{CalendarHelper, TestHelper, GatewaySpec}
import java.util.Calendar
import java.util.TimeZone
import TestHelper._
import CalendarHelper._

@RunWith(classOf[JUnitRunner])
class SettlementBatchSummarySpec extends GatewaySpec with MustMatchers {

  val eastern_timezone = TimeZone.getTimeZone("America/New_York")

  describe("formatDateString") {
    onGatewayIt("formats") { gateway =>
      val time = Calendar.getInstance
      time.clear
      time.set(2011, 7, 31)
      SettlementBatchSummaryRequest.dateString(time) must be === "2011-08-31"
    }

    onGatewayIt("formats on Boundary") { gateway =>
      val tz = TimeZone.getTimeZone("America/New_York")
      val time = Calendar.getInstance(tz)
      time.clear
      time.set(2011, 7, 31, 23, 00)
      SettlementBatchSummaryRequest.dateString(time) must be === "2011-08-31"
    }
  }

  describe("generate") {
    onGatewayIt("returns Empty Collection If There Is No Data") { gateway =>
      val settlementDate = Calendar.getInstance
      settlementDate.add(Calendar.YEAR, -5)

      val result = gateway.settlementBatchSummary.generate(settlementDate)

      result must be ('success)
    }

    onGatewayIt("returns Data For The Given Settlement Date") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).
        creditCard.number(CreditCardNumber.VISA.number).cvv("321").expirationDate("05/2009").done.
        options.submitForSettlement(true).done
      val transaction = gateway.transaction.sale(request) match { case Success(t) => t }
      transaction must settle(gateway)
      val estTime = now in eastern_timezone
      val summaryResult = gateway.settlementBatchSummary.generate(estTime)

      summaryResult match {
        case Success(summary) => {
          summary.getRecords.size must be > 0
          val first = summary.getRecords.get(0)
          first.containsKey("kind") must be === true
          first.containsKey("count") must be === true
          first.containsKey("amount_settled") must be === true
          first.containsKey("merchant_account_id") must be === true
        }
      }
    }

    onGatewayIt("returns Data Grouped By The Given Custom Field") { gateway =>
      val request = new TransactionRequest().amount(TransactionAmount.AUTHORIZE.amount).
        creditCard.number(CreditCardNumber.VISA.number).cvv("321").expirationDate("05/2009").done.
        customField("store_me", "1").options.submitForSettlement(true).done

      val transaction = gateway.transaction.sale(request)  match { case Success(t) => t }
      transaction must settle(gateway)

      val timestamp = now in eastern_timezone
      val summaryResult = gateway.settlementBatchSummary.generate(timestamp, "store_me")

      summaryResult match {
        case Success(summary) => {
          summary.getRecords.size must be > 0
          val first = summary.getRecords.get(0)
          first.containsKey("store_me") must be === true
        }
      }
    }
  }
}