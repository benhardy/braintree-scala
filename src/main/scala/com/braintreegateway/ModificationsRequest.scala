package com.braintreegateway

import collection.mutable.ListBuffer

final class ModificationsRequest(parent: SubscriptionRequest, name: String) extends BaseRequest {

  private val adds = new ListBuffer[AddModificationRequest]
  private val updates = new ListBuffer[UpdateModificationRequest]
  private val removeModificationIds = new ListBuffer[String]

  def add: AddModificationRequest = {
    val addModificationRequest = new AddModificationRequest(this)
    adds += addModificationRequest
    addModificationRequest
  }

  def done: SubscriptionRequest = {
    parent
  }

  def remove(modificationIds: String*): ModificationsRequest = {
    remove(modificationIds.toList)
  }

  def remove(modificationIds: List[String]): ModificationsRequest = {
    removeModificationIds ++= modificationIds
    this
  }

  def update(existingId: String): UpdateModificationRequest = {
    val updateModificationRequest: UpdateModificationRequest = new UpdateModificationRequest(this, existingId)
    updates += updateModificationRequest
    updateModificationRequest
  }

  override def xmlName = name

  protected def buildRequest(root: String): RequestBuilder = {
    new RequestBuilder(root).
      addElement("add", adds.toList).
      addElement("remove", removeModificationIds.toList).
      addElement("update", updates.toList)
  }
}