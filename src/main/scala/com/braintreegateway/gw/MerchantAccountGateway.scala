package com.braintreegateway.gw

import com.braintreegateway.util.Http
import com.braintreegateway.{MerchantAccount, Result, MerchantAccountRequest}

class MerchantAccountGateway(http: Http) {

  def create(request: MerchantAccountRequest): Result[MerchantAccount] = {
    val response = http.post(MerchantAccountGateway.CREATE_URL, request)
    new Result[MerchantAccount](response, classOf[MerchantAccount])
  }
}

object MerchantAccountGateway {
  final val CREATE_URL = "/merchant_accounts/create_via_api"
}
