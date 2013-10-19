package com.braintreegateway.search

import com.braintreegateway.SearchRequest

class IsNode[T <: SearchRequest](nodeName: String, parent: T) extends SearchNode[T](nodeName, parent) {
  def is(value: String): T = {
    assembleCriteria("is", value)
  }
}