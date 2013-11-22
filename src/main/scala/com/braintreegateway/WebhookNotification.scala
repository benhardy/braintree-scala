package com.braintreegateway

import com.braintreegateway.util.EnumUtils
import com.braintreegateway.util.NodeWrapper
import com.braintreegateway.ValidationErrors.NoValidationErrors


final class WebhookNotification(node: NodeWrapper) {

  val kind = EnumUtils.findByNameOpt(classOf[WebhookNotifications.Kind])(node("kind"))
  val timestamp = node.findDateTime("timestamp")

  val subjectNode = node.findFirst("subject")
  val errorNode = subjectNode.findFirstOpt("api-error-response")
  val wrapperNode = errorNode.getOrElse(subjectNode)

  val subscription = wrapperNode.findFirstOpt("subscription") map {
    new Subscription(_)
  }

  val merchantAccount = wrapperNode.findFirstOpt("merchant-account") map {
    MerchantAccount(_)
  }

  val transaction = wrapperNode.findFirstOpt("transaction") map {
    new Transaction(_)
  }

  val errors = if (!wrapperNode.isSuccess) {
    ValidationErrors.apply(wrapperNode)
  } else {
    NoValidationErrors
  }
}