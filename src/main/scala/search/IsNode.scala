package net.bhardy.braintree.scala.search

class IsNode[T <: SearchRequest[T]](nodeName: String, parent: T) extends SearchNode[T](nodeName, parent) {
  def is(value: String): T = {
    assembleCriteria("is", value)
  }
  def getThis = this
}