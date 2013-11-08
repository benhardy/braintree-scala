package com.braintreegateway.gw

import com.braintreegateway.util.Http
import com.braintreegateway.AddOn

class AddOnGateway(http: Http) {

  def all: List[AddOn] = {
    val node = http.get("/add_ons")
    node.findAll("add-on").map(new AddOn(_))
  }
}