package com.braintreegateway

import java.util.ArrayList
import java.util.List
import scala.collection.JavaConversions._

final class ModificationsRequest(parent: SubscriptionRequest, name: String) extends BaseRequest {

  private val adds = new ArrayList[AddModificationRequest]
  private val updates = new ArrayList[UpdateModificationRequest]
  private val removeModificationIds = new ArrayList[String]

  def add: AddModificationRequest = {
    val addModificationRequest = new AddModificationRequest(this)
    adds.add(addModificationRequest)
    addModificationRequest
  }

  def done: SubscriptionRequest = {
    parent
  }

  def remove(modificationIds: String*): ModificationsRequest = {
    remove(modificationIds.toList)
  }

  def remove(modificationIds: List[String]): ModificationsRequest = {
    removeModificationIds.addAll(modificationIds)
    this
  }

  def update(existingId: String): UpdateModificationRequest = {
    val updateModificationRequest: UpdateModificationRequest = new UpdateModificationRequest(this, existingId)
    updates.add(updateModificationRequest)
    updateModificationRequest
  }

  override def toXmlString: String = {
    buildRequest(name).toXmlString
  }

  protected def buildRequest(root: String): RequestBuilder = {
    new RequestBuilder(root).addElement("add", adds).addElement("remove", removeModificationIds).addElement("update", updates)
  }
}