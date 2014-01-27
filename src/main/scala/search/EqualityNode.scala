package net.bhardy.braintree.scala.search

class EqualityNode[T <: SearchRequest[T]](nodeName:String, parent:T) extends IsNode[T](nodeName, parent) {

  def isNot(value: String): T = {
    assembleCriteria("is_not", value)
  }
}