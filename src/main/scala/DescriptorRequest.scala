package net.bhardy.braintree.scala

class DescriptorRequest[P <: Request](val done:P) extends BaseRequest with HasParent[P] {

  protected var name: Option[String] = None
  protected var phone: Option[String] = None

  override val xmlName = "descriptor"

  def name(name: String): this.type = {
    this.name = Option(name)
    this
  }

  def phone(phone: String): this.type = {
    this.phone = Option(phone)
    this
  }

  override def toQueryString(root: String): String = {
    buildRequest(root).toQueryString
  }

  protected def buildRequest(root: String): RequestBuilder = {
    new RequestBuilder(root).
      addElement("name", name).
      addElement("phone", phone)
  }
}

object DescriptorRequest {
  def apply[P <: Request](parent: P) = new DescriptorRequest[P](parent)
}