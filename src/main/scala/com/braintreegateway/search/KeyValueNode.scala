package com.braintreegateway.search

import com.braintreegateway.SearchRequest

class KeyValueNode[T <: SearchRequest](nodeName:String, parent:T) extends SearchNode[T](nodeName, parent) {

  def is(value: AnyRef): T = {
    parent.addKeyValueCriteria(nodeName.toString, value.toString)
    parent
  }
}