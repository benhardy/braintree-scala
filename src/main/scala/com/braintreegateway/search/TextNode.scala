package com.braintreegateway.search

import com.braintreegateway.SearchRequest

class TextNode[T <: SearchRequest](nodeName: String, parent: T) extends PartialMatchNode[T](nodeName, parent) {
  def contains(value: String): T = {
    assembleCriteria("contains", value)
  }
}