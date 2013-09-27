package com.braintreegateway.gw

case class Configuration(baseMerchantURL: String, publicKey: String, privateKey: String) {

}

object Configuration {
  val apiVersion = "3"
}
