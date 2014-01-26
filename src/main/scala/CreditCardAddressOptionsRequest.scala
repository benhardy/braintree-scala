package net.bhardy.braintree.scala

import xml.Elem

class CreditCardAddressOptionsRequest[P <: CreditCardAddressRequest[_]](parent: P) extends BaseRequest {

  private var updateExisting: Option[Boolean] = None

  def updateExisting(updateExisting: Boolean): HasParent[P] = {
    this.updateExisting = Some(updateExisting)
    new HasParent[P] {
      def done = parent
    }
  }

  override def toQueryString: String = {
    toQueryString("options")
  }

  override def toQueryString(root: String): String = {
    buildRequest(root).toQueryString
  }

  override val xmlName = "options"

  protected def buildRequest(root: String): RequestBuilder = {
    val start = new RequestBuilder(root)
    updateExisting.map {
      update => start.addElement("updateExisting", update)
    } getOrElse {
      start
    }
  }

}