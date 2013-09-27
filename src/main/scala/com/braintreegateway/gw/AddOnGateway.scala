package com.braintreegateway.gw

import com.braintreegateway.util.Http
import java.util.{List => JUList}
import scala.collection.JavaConversions._
import com.braintreegateway.AddOn

class AddOnGateway(http: Http) {

  def all: JUList[AddOn] = {
    val node = http.get("/add_ons")
    node.findAll("add-on").map(new AddOn(_))
  }
}