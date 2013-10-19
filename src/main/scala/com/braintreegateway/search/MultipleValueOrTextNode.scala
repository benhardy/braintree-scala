package com.braintreegateway.search

import com.braintreegateway.SearchRequest

class MultipleValueOrTextNode[T <: SearchRequest, S](nodeName: String, parent: T) extends TextNode[T](nodeName, parent) {

  def in(items: List[S]): T = {
     assembleMultiValueCriteria(items)
  }

  def in[S](items: S*): T = {
    assembleMultiValueCriteria(items.toList)
  }
}