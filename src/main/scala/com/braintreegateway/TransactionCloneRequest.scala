package com.braintreegateway

import scala.math.BigDecimal

class TransactionCloneRequest extends BaseRequest {

  private var _amount: Option[BigDecimal] = None
  private var _channel: Option[String] = None
  private var _transactionOptionsCloneRequest: Option[TransactionOptionsCloneRequest] = None

  def amount(amount: BigDecimal) = {
    this._amount = Option(amount)
    this
  }

  def channel(channel: String) = {
    this._channel = Option(channel)
    this
  }

  def options: TransactionOptionsCloneRequest = {
    val subRequest = new TransactionOptionsCloneRequest(this)
    this._transactionOptionsCloneRequest = Some(subRequest)
    subRequest
  }

  override val xmlName = "transactionClone"

  protected def buildRequest(root: String): RequestBuilder = {
    new RequestBuilder(root).
      addElement("amount", _amount).
      addElement("channel", _channel).
      addElement("options", _transactionOptionsCloneRequest)
  }
}