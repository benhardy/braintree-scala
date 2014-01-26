package net.bhardy.braintree.scala.gw

import net.bhardy.braintree.scala.util.Http
import net.bhardy.braintree.scala.Discount

class DiscountGateway(http: Http) {

  def all: List[Discount] = {
    http.get("/discounts").findAll("discount").map{ new Discount(_) }
  }
}