package com.braintreegateway.gw

import com.braintreegateway.util.Http
import com.braintreegateway.Plan

class PlanGateway(http: Http) {
  def all: List[Plan] = {
    val node = http.get("/plans")
    node.findAll("plan").map(new Plan(_))
  }
}