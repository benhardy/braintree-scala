package com.braintreegateway

class TransactionCreditCardRequest(override val done: TransactionRequest)
    extends BaseRequest with HasParent[TransactionRequest] {

  private var cardholderName: Option[String] = None
  private var cvv: Option[String] = None
  private var expirationDate: Option[String] = None
  private var expirationMonth: Option[String] = None
  private var expirationYear: Option[String] = None
  private var number: Option[String] = None
  private var token: Option[String] = None

  def cardholderName(cardholderName: String): this.type = {
    this.cardholderName = Option(cardholderName)
    this
  }

  def cvv(cvv: String): this.type = {
    this.cvv = Option(cvv)
    this
  }

  def expirationDate(expirationDate: String): this.type = {
    this.expirationDate = Option(expirationDate)
    this
  }

  def expirationMonth(expirationMonth: String): this.type = {
    this.expirationMonth = Option(expirationMonth)
    this
  }

  def expirationYear(expirationYear: String): this.type = {
    this.expirationYear = Option(expirationYear)
    this
  }

  def getToken = token

  def number(number: String): this.type = {
    this.number = Option(number)
    this
  }

  def token(token: String): this.type = {
    this.token = Option(token)
    this
  }

  override def toXmlString = buildRequest("creditCard").toXmlString

  override def toQueryString(root: String) = buildRequest(root).toQueryString

  override def toQueryString = toQueryString("creditCard")

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