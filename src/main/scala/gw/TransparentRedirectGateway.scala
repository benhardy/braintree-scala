package net.bhardy.braintree.scala.gw

import net.bhardy.braintree.scala.util.Http
import net.bhardy.braintree.scala.util.NodeWrapper
import net.bhardy.braintree.scala.util.StringUtils
import net.bhardy.braintree.scala.util.TrUtil
import net.bhardy.braintree.scala._

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
    Result.creditCard(confirmTr(classOf[CreditCard], queryString))
  }

  def confirmCustomer(queryString: String): Result[Customer] = {
    Result.customer(confirmTr(classOf[Customer], queryString))
  }

  def confirmTransaction(queryString: String): Result[Transaction] = {
    Result.transaction(confirmTr(classOf[Transaction], queryString))
  }

  def trData(trData: Request, redirectURL: String): String = {
    new TrUtil(configuration).buildTrData(trData, redirectURL)
  }

  private def confirmTr[T](klass: Class[T], queryString: String): NodeWrapper = {
    val trRequest = new TransparentRedirectRequest(configuration, queryString)
    val node = http.post("/transparent_redirect_requests/" + trRequest.getId + "/confirm", trRequest)
    val classXmlName = StringUtils.classToXMLName(klass)
    if (!(node.getElementName == classXmlName) && !(node.getElementName == "api-error-response")) {
      throw new IllegalArgumentException("You attemped to confirm a " + classXmlName + ", but received a " + node.getElementName + ".")
    }
    node
  }

}