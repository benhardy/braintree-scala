package net.bhardy.braintree.scala

class AddModificationRequest(parent: ModificationsRequest) extends ModificationRequest(parent) {

  private var inheritedFromId: Option[String] = None

  def inheritedFromId(inheritedFromId: String): AddModificationRequest = {
    this.inheritedFromId = Option(inheritedFromId)
    this
  }

  protected override def buildRequest(root: String): RequestBuilder = {
    super.buildRequest(root).addElement("inheritedFromId", inheritedFromId)
  }
}