package com.braintreegateway.gw

import com.braintreegateway.util.Http
import com.braintreegateway.Plan

class PlanGateway(http: Http) {
  def all: List[Plan] = {
    val node = http.get("/plans")
    import scala.collection.JavaConversions._  // TODO cleanup
    node.findAll("plan").toList.map(new Plan(_))
  }
}