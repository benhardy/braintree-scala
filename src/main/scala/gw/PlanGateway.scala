package net.bhardy.braintree.scala.gw

import net.bhardy.braintree.scala.util.Http
import net.bhardy.braintree.scala.Plan

class PlanGateway(http: Http) {
  def all: List[Plan] = {
    val node = http.get("/plans")
    node.findAll("plan").map(new Plan(_))
  }
}