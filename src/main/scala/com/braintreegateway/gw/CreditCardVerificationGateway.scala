package com.braintreegateway.gw

import com.braintreegateway.exceptions.NotFoundException
import com.braintreegateway.util.Http
import com.braintreegateway.{CreditCardVerificationPager, ResourceCollection, CreditCardVerification}
import com.braintreegateway.search.CreditCardVerificationSearchRequest

class CreditCardVerificationGateway(http: Http, configuration: Configuration) {

  private[braintreegateway]
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