package net.bhardy.braintree.scala.gw

import net.bhardy.braintree.scala.exceptions.NotFoundException
import net.bhardy.braintree.scala.util.Http
import net.bhardy.braintree.scala.{CreditCardVerificationPager, ResourceCollection, CreditCardVerification}
import net.bhardy.braintree.scala.search.CreditCardVerificationSearchRequest

class CreditCardVerificationGateway(http: Http, configuration: Configuration) {

  private[braintree]
  def fetchCreditCardVerifications(query: CreditCardVerificationSearchRequest, ids: List[String]): List[CreditCardVerification] = {
    query.ids.in(ids)
    val response = http.post("/verifications/advanced_search", query)
    response.findAll("verification").map { new CreditCardVerification(_) }
  }

  def find(id: String): CreditCardVerification = {
    if (id == null || (id.trim == "")) throw new NotFoundException
    new CreditCardVerification(http.get("/verifications/" + id))
  }

  def search(query: CreditCardVerificationSearchRequest): ResourceCollection[CreditCardVerification] = {
    val node = http.post("/verifications/advanced_search_ids", query)
    new ResourceCollection[CreditCardVerification](new CreditCardVerificationPager(this, query), node)
  }
}