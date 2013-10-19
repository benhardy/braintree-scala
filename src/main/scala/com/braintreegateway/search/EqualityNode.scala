package com.braintreegateway.search

import com.braintreegateway.SearchRequest

class EqualityNode[T <: SearchRequest](nodeName:String, parent:T) extends IsNode[T](nodeName, parent) {

  def isNot(value: String): T = {
    assembleCriteria("is_not", value)
  }
}