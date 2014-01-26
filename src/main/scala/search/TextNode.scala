package net.bhardy.braintree.scala.search

class TextNode[T <: SearchRequest[T]](nodeName: String, parent: T) extends PartialMatchNode[T](nodeName, parent) {
  def contains(value: String): T = {
    assembleCriteria("contains", value)
  }
}