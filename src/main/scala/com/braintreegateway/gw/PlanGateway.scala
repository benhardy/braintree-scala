package com.braintreegateway.gw

import com.braintreegateway.util.Http
import java.util.{List=>JUList}
import scala.collection.JavaConversions._
import com.braintreegateway.Plan

class PlanGateway(http: Http) {
  def all: JUList[Plan] = {
    val node = http.get("/plans")
    node.findAll("plan").map(new Plan(_))
  }
}