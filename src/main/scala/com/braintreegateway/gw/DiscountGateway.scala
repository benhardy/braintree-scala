package com.braintreegateway.gw

import com.braintreegateway.util.Http
import com.braintreegateway.Discount

class DiscountGateway(http: Http) {

  def all: List[Discount] = {
    http.get("/discounts").findAll("discount").map{ new Discount(_) }
  }
}