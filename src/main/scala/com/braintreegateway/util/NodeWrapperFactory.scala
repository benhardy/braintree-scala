package com.braintreegateway.util

trait NodeWrapperFactory {

  // TODO make this take an xml.Elem
  def create(xml: String): NodeWrapper
}

object NodeWrapperFactory extends NodeWrapperFactory {
  def create(xml: String) = SimpleNodeWrapper.parse(xml)
}

