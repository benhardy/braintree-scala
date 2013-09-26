package com.braintreegateway

import com.braintreegateway.util.Http
import java.util.{List => JUList}
import scala.collection.JavaConversions._

class AddOnGateway(http: Http) {

  def all: JUList[AddOn] = {
    val node = http.get("/add_ons")
    node.findAll("add-on").map(new AddOn(_))
  }
}