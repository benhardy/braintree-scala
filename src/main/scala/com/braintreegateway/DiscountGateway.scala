package com.braintreegateway

import com.braintreegateway.util.Http
import com.braintreegateway.util.NodeWrapper
import java.util.{List=>JUList}
import scala.collection.JavaConversions._

class DiscountGateway(http: Http) {

  def all: JUList[Discount] = {
    val node = http.get("/discounts")
    node.findAll("discount").map{new Discount(_)}
  }
}