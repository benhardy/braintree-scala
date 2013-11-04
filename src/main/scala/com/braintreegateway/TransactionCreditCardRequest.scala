package com.braintreegateway

class TransactionCreditCardRequest(override val done: TransactionRequest)
    extends BaseRequest with HasParent[TransactionRequest] {

  private var cardholderName: String = null
  private var cvv: String = null
  private var expirationDate: String = null
  private var expirationMonth: String = null
  private var expirationYear: String = null
  private var number: String = null
  private var token: String = null

  def cardholderName(cardholderName: String): this.type = {
    this.cardholderName = cardholderName
    this
  }

  def cvv(cvv: String): this.type = {
    this.cvv = cvv
    this
  }

  def expirationDate(expirationDate: String): this.type = {
    this.expirationDate = expirationDate
    this
  }

  def expirationMonth(expirationMonth: String): this.type = {
    this.expirationMonth = expirationMonth
    this
  }

  def expirationYear(expirationYear: String): this.type = {
    this.expirationYear = expirationYear
    this
  }

  def getToken = token

  def number(number: String): this.type = {
    this.number = number
    this
  }

  def token(token: String): this.type = {
    this.token = token
    this
  }

  override def toXmlString = buildRequest("creditCard").toXmlString

  override def toQueryString(root: String) = buildRequest(root).toQueryString

  override def toQueryString =  toQueryString("creditCard")

  protected def buildRequest(root: String): RequestBuilder = {
    new RequestBuilder(root).
      addElement("cardholderName", cardholderName).
      addElement("cvv", cvv).
      addElement("number", number).
      addElement("expirationDate", expirationDate).
      addElement("expirationMonth", expirationMonth).
      addElement("expirationYear", expirationYear).
      addElement("token", token)
  }

}