package net.bhardy.braintree.scala.gw

import net.bhardy.braintree.scala.util.Http
import net.bhardy.braintree.scala.AddOn

class AddOnGateway(http: Http) {

  def all: List[AddOn] = {
    val node = http.get("/add_ons")
    node.findAll("add-on").map(new AddOn(_))
  }
}