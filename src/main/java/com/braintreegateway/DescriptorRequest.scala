package com.braintreegateway

class DescriptorRequest[P <: Request](val done:P) extends BaseRequest with HasParent[P] {

  def name(name: String): this.type = {
    this.name = name
    this
  }

  def phone(phone: String): this.type = {
    this.phone = phone
    this
  }

  override def toXmlString: String = {
    buildRequest("descriptor").toXmlString
  }

  override def toQueryString(root: String): String = {
    buildRequest(root).toQueryString
  }

  protected def buildRequest(root: String): RequestBuilder = {
    new RequestBuilder(root).addElement("name", name).addElement("phone", phone)
  }

  protected var name: String = null
  protected var phone: String = null
}

object DescriptorRequest {
  def apply[P <: Request](parent: P) = new DescriptorRequest[P](parent)
}