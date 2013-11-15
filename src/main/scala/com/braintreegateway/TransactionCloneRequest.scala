package com.braintreegateway

import scala.math.BigDecimal

class TransactionCloneRequest extends BaseRequest {
  def amount(amount: BigDecimal) = {
    _amount = amount
    this
  }

  def channel(channel: String) = {
    _channel = channel
    this
  }

  def options: TransactionOptionsCloneRequest = {
    _transactionOptionsCloneRequest = new TransactionOptionsCloneRequest(this)
    _transactionOptionsCloneRequest
  }

  override def toXmlString: String = {
    buildRequest("transactionClone").toXmlString
  }

  protected def buildRequest(root: String): RequestBuilder = {
    new RequestBuilder(root).
      addElement("amount", _amount).
      addElement("channel", _channel).
      addElement("options", _transactionOptionsCloneRequest)
  }

  private var _amount: BigDecimal = null
  private var _channel: String = null
  private var _transactionOptionsCloneRequest: TransactionOptionsCloneRequest = null
}