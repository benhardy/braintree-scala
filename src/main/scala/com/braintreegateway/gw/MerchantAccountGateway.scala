package com.braintreegateway.gw

import com.braintreegateway.util.Http
import com.braintreegateway.{MerchantAccount, MerchantAccountRequest}

class MerchantAccountGateway(http: Http) {

  def create(request: MerchantAccountRequest): Result2[MerchantAccount] = {
    val response = http.post(MerchantAccountGateway.CREATE_URL, request)
    Result2.merchantAccount(response)
  }
}

object MerchantAccountGateway {
  final val CREATE_URL = "/merchant_accounts/create_via_api"
}
