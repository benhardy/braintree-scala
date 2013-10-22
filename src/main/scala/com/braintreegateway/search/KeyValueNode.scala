package com.braintreegateway.search

class KeyValueNode[T <: SearchRequest[T]](nodeName:String, parent:T) extends SearchNode[T](nodeName, parent) {

  def is(value: AnyRef): T = {
    parent.addKeyValueCriteria(nodeName.toString, value.toString)
  }
}