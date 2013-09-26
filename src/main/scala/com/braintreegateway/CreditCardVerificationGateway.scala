package com.braintreegateway

import com.braintreegateway.exceptions.NotFoundException
import com.braintreegateway.util.Http
import java.util.{List =>JUList}
import scala.collection.JavaConversions._

class CreditCardVerificationGateway(http: Http, configuration: Configuration) {

  private[braintreegateway]
  def fetchCreditCardVerifications(query: CreditCardVerificationSearchRequest, ids: JUList[String]): JUList[CreditCardVerification] = {
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