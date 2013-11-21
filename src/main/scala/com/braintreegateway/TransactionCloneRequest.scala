package com.braintreegateway

import scala.math.BigDecimal

class TransactionCloneRequest extends BaseRequest {
  def amount(amount: BigDecimal) = {
    _amount = Some(amount)
    this
  }

  def channel(channel: String) = {
    _channel = Some(channel)
    this
  }

  def options: TransactionOptionsCloneRequest = {
    val subRequest = new TransactionOptionsCloneRequest(this)
    _transactionOptionsCloneRequest = Some(subRequest)
    subRequest
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

  private var _amount: Option[BigDecimal] = None
  private var _channel: Option[String] = None
  private var _transactionOptionsCloneRequest: Option[TransactionOptionsCloneRequest] = None
}