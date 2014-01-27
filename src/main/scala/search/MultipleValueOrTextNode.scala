package net.bhardy.braintree.scala.search

class MultipleValueOrTextNode[T <: SearchRequest[T], S](nodeName: String, parent: T) extends TextNode[T](nodeName, parent) {

  def in(items: List[S]): T = {
    assembleMultiValueCriteria(items)
  }

  def in[S](items: S*): T = {
    assembleMultiValueCriteria(items.toList)
  }
}