package net.bhardy.braintree.scala.search

class PartialMatchNode[T <: SearchRequest[T]](nodeName: String, parent: T) extends EqualityNode[T](nodeName, parent) {
  def endsWith(value: String): T = {
    assembleCriteria("ends_with", value)
  }

  def startsWith(value: String): T = {
    assembleCriteria("starts_with", value)
  }
}