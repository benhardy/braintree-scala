package com.braintreegateway.util

import com.braintreegateway.gw.Configuration
import com.braintreegateway.Request
import java.util.Calendar
import java.util.TimeZone
import com.braintreegateway.util.QueryString.encodeParam

class TrUtil(configuration: Configuration) {

  def buildTrData(request: Request, redirectURL: String): String = {
    val now = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    val dateString = String.format("%1$tY%1$tm%1$td%1$tH%1$tM%1$tS", now)
    val trContent = new QueryString().append("api_version", Configuration.apiVersion).
      append("public_key", configuration.publicKey).append("redirect_url", redirectURL).
      append("time", dateString).append("kind", request.getKind).appendEncodedData(request.toQueryString).
      toString
    val trHash = new Crypto().hmacHash(configuration.privateKey, trContent)
    trHash + "|" + trContent
  }

  def isValidTrQueryString(queryString: String): Boolean = {
    val pieces = queryString.split("&hash=")
    val queryStringWithoutHash = pieces(0)
    val hash = pieces(1)
    hash == new Crypto().hmacHash(configuration.privateKey, queryStringWithoutHash)
  }

  protected def encodeMap(sourceMap: Map[String, String]): String = {
    sourceMap.map { case (key, value) => encodeParam(key,value) }.mkString("&")
  }

  def url: String = {
    configuration.baseMerchantURL + "/transparent_redirect_requests"
  }
}