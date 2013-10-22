package com.braintreegateway.search

class IdsSearchRequest extends SearchRequest[IdsSearchRequest] {
  def ids = multipleValueNode[String]("ids")

  def getThis = this
}