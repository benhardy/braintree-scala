package com.braintreegateway.gw

import com.braintreegateway.util.Http
import com.braintreegateway.util.NodeWrapper
import com.braintreegateway.util.StringUtils
import com.braintreegateway.util.TrUtil
import com.braintreegateway._

object TransparentRedirectGateway {
  var CREATE_TRANSACTION: String = "create_transaction"
  var CREATE_CUSTOMER: String = "create_customer"
  var UPDATE_CUSTOMER: String = "update_customer"
  var CREATE_PAYMENT_METHOD: String = "create_payment_method"
  var UPDATE_PAYMENT_METHOD: String = "update_payment_method"
}

class TransparentRedirectGateway(http: Http, configuration: Configuration) {

  def url: String = {
    new TrUtil(configuration).url
  }

  def confirmCreditCard(queryString: String): Result[CreditCard] = {
    confirmTr(classOf[CreditCard], queryString)
  }

  def confirmCustomer(queryString: String): Result[Customer] = {
    confirmTr(classOf[Customer], queryString)
  }

  def confirmTransaction(queryString: String): Result[Transaction] = {
    confirmTr(classOf[Transaction], queryString)
  }

  def trData(trData: Request, redirectURL: String): String = {
    new TrUtil(configuration).buildTrData(trData, redirectURL)
  }

  private def confirmTr[T](klass: Class[T], queryString: String): Result[T] = {
    val trRequest: TransparentRedirectRequest = new TransparentRedirectRequest(configuration, queryString)
    val node: NodeWrapper = http.post("/transparent_redirect_requests/" + trRequest.getId + "/confirm", trRequest)
    if (!(node.getElementName == StringUtils.classToXMLName(klass)) && !(node.getElementName == "api-error-response")) {
      throw new IllegalArgumentException("You attemped to confirm a " + StringUtils.classToXMLName(klass) + ", but received a " + node.getElementName + ".")
    }
    new Result[T](node, klass)
  }

}