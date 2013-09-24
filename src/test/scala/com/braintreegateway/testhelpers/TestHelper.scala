package com.braintreegateway.testhelpers

import _root_.org.scalatest.matchers.{MatchResult, Matcher}
import com.braintreegateway._
import com.braintreegateway.exceptions.UnexpectedException
import com.braintreegateway.util.Crypto
import com.braintreegateway.util.Http
import com.braintreegateway.util.NodeWrapper
import _root_.org.junit.Ignore
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Calendar
import scala.collection.JavaConversions._


@Ignore("Testing utility class")
object TestHelper {
  import CalendarHelper._

  // TODO one day let us not have to deal with nulls
  def beSameDayAs(right: Calendar) = Matcher {
    (left: Calendar) => {
      def render(day: Calendar) = {
        if (day == null) {
          "null"
        } else {
          "%04d-%02d-%02d".format(day.year, day.month, day.day)
        }
      }

      val same = (left, right) match {
        case (null, null) => true
        case (null, _) => false
        case (_, null) => false
        case (a, b) => {
          a.day == b.day && a.month == b.month && a.year == b.year
        }
      }
      val prefix = s"days ${render(left)} and ${render(right)} are "
      MatchResult(same, prefix + "not same", prefix + "same")
    }
  }

  def beValidTrData(configuration: Configuration) = Matcher {
    (trData: String) => {
      val dataSections = trData.split("\\|")
      val trHash = dataSections(0)
      val trContent = dataSections(1)
      val hash = new Crypto().hmacHash(configuration.privateKey, trContent)
      MatchResult(trHash == hash, "TrData is not valid", "TrData is valid")
    }
  }

  def includeSubscription(item: Subscription) = Matcher {
    (collection: ResourceCollection[Subscription]) => {
      val subIds = "Subscription list " + collection.map(_.getId).toList.toString
      val id = item.getId
      MatchResult(collection.exists(_.getId == item.getId),
        subIds + "does not contain " + id,
        subIds + "contains " + id)
    }
  }

  def includeStatus(status: Transaction.Status) = Matcher {
    (collection: ResourceCollection[Transaction]) => {
      val tInfos = collection.map(t => s"${t.getId}:${t.getStatus}").toList
      val transactions = "Transaction list " + tInfos.toString

      MatchResult(collection.exists(_.getStatus == status),
        transactions + "does not contain status" + status,
        transactions + "contains " + status)
    }
  }

  def settle(gateway: BraintreeGateway) = Matcher {
    (transaction: Transaction) => {
      val response: NodeWrapper = new Http(gateway.getAuthorizationHeader, gateway.baseMerchantURL,
        Environment.DEVELOPMENT.certificateFilenames,
        BraintreeGateway.VERSION).put(s"/transactions/${transaction.getId}/settle")
      MatchResult(response.isSuccess,
        s"transaction ${transaction.getId} did not settle",
        s"transaction ${transaction.getId} did unexpectedly settle")
    }
  }

  def escrow(gateway: BraintreeGateway) = Matcher {
    (transaction: Transaction) => {
      val response: NodeWrapper = new Http(gateway.getAuthorizationHeader, gateway.baseMerchantURL,
        Environment.DEVELOPMENT.certificateFilenames,
        BraintreeGateway.VERSION).put(s"/transactions/${transaction.getId}/escrow")
      MatchResult(response.isSuccess,
        s"transaction ${transaction.getId} did not escrow",
        s"transaction ${transaction.getId} did unexpectedly escrow")
    }
  }


  def simulateFormPostForTR(gateway: BraintreeGateway, trParams: Request, request: Request, postUrl: String): String = {
    try {
      val trData = gateway.transparentRedirect.trData(trParams, "http://example.com")
      val postData = new StringBuilder("tr_data=").
        append(URLEncoder.encode(trData, "UTF-8")).
        append("&").
        append(request.toQueryString)
      val url = new URL(postUrl)
      val connection = url.openConnection.asInstanceOf[HttpURLConnection]
      connection.setInstanceFollowRedirects(false)
      connection.setDoOutput(true)
      connection.setRequestMethod("POST")
      connection.addRequestProperty("Accept", "application/xml")
      connection.addRequestProperty("Content-Type", "application/x-www-form-urlencoded")
      connection.getOutputStream.write(postData.toString.getBytes("UTF-8"))
      connection.getOutputStream.close
      if (connection.getResponseCode == 422) {
        connection.getErrorStream
      }
      else {
        connection.getInputStream
      }
      new URL(connection.getHeaderField("Location")).getQuery
    }
    catch {
      case e: IOException =>
        throw new UnexpectedException(e.getMessage)
    }
  }
}