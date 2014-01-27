package net.bhardy.braintree.scala.gw

import net.bhardy.braintree.scala.util.Http
import net.bhardy.braintree.scala.{MerchantAccount, MerchantAccountRequest}

class MerchantAccountGateway(http: Http) {

  def create(request: MerchantAccountRequest): Result[MerchantAccount] = {
    val response = http.post(MerchantAccountGateway.CREATE_URL, request)
    Result.merchantAccount(response)
  }
}

object MerchantAccountGateway {
  final val CREATE_URL = "/merchant_accounts/create_via_api"
}
