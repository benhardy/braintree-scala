package com.braintreegateway.search

import com.braintreegateway.SearchRequest

class PartialMatchNode[T <: SearchRequest](nodeName: String, parent: T) extends EqualityNode[T](nodeName, parent) {
  def endsWith(value: String): T = {
    assembleCriteria("ends_with", value)
  }

  def startsWith(value: String): T = {
    assembleCriteria("starts_with", value)
  }
}