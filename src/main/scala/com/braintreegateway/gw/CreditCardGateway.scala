package com.braintreegateway.gw

import com.braintreegateway.exceptions.NotFoundException
import com.braintreegateway.util.Http
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone
import scala.collection.JavaConversions._
import com.braintreegateway._
import search.IdsSearchRequest

/**
 * Provides methods to create, delete, find, and update {@link CreditCard}
 * objects. This class does not need to be instantiated directly. Instead, use
 * {@link BraintreeGateway#creditCard()} to get an instance of this class:
 *
 * <pre>
 * BraintreeGateway gateway = new BraintreeGateway(...);
 * gateway.creditCard().create(...)
 * </pre>
 *
 * For more detailed information on {@link CreditCard CreditCards}, see <a
 * href="http://www.braintreepayments.com/gateway/credit-card-api"
 * target
 * ="_blank">http://www.braintreepayments.com/gateway/credit-card-api
 * </a><br />
 * For more detailed information on credit card verifications, see <a href=
 * "http://www.braintreepayments.com/gateway/credit-card-verification-api"
 * target="_blank">http://www.braintreepayments.com/gateway/credit-card-
 * verification-api</a>
 */
class CreditCardGateway(http: Http, configuration: Configuration) {

  /**
   * Creates an {@link CreditCard}.
   *
   * @param request
     * the request.
   * @return a { @link Result}.
   */
  def create(request: CreditCardRequest): Result[CreditCard] = {
    val node = http.post("/payment_methods", request)
    Result.creditCard(node)
  }

  /**
   * Deletes a {@link CreditCard}.
   *
   * @param token
     * the CreditCard's token.
   * @return a { @link Result}.
   */
  def delete(token: String): Result[CreditCard] = {
    http.delete("/payment_methods/" + token)
    Result.deleted
  }

  /**
   * Finds a {@link CreditCard}.
   *
   * @param token
     * the CreditCard's token.
   * @return the { @link CreditCard} or raises a
   *         { @link com.braintreegateway.exceptions.NotFoundException}.
   */
  def find(token: String): CreditCard = {
    if ((token.trim == "") || token == null) throw new NotFoundException
    new CreditCard(http.get("/payment_methods/" + token))
  }

  /**
   * Updates a {@link CreditCard}.
   *
   * @param token
     * the CreditCard's token.
   * @param request
     * the request.
   * @return a { @link Result}.
   */
  def update(token: String, request: CreditCardRequest): Result[CreditCard] = {
    val node = http.put("/payment_methods/" + token, request)
    Result.creditCard(node)
  }

  /**
   * Returns a {@link ResourceCollection} of all expired credit cards.
   *
   * @return a { @link ResourceCollection}.
   */
  def expired: ResourceCollection[CreditCard] = {
    val response = http.post("/payment_methods/all/expired_ids")
    new ResourceCollection[CreditCard](Pager.expiredCreditCard(this), response)
  }

  private[braintreegateway] def fetchExpiredCreditCards(ids: List[String]): List[CreditCard] = {
    val query = new IdsSearchRequest().ids.in(ids)
    val response = http.post("/payment_methods/all/expired", query)
    response.findAll("credit-card").map(new CreditCard(_)).toList
  }

  /**
   * Returns a {@link ResourceCollection} of all credit cards expiring between
   * the given calendars.
   *
   * @return a { @link ResourceCollection}.
   */
  def expiringBetween(start: Calendar, end: Calendar): ResourceCollection[CreditCard] = {
    val queryString = dateQueryString(start, end)
    val response = http.post("/payment_methods/all/expiring_ids?" + queryString)
    new ResourceCollection[CreditCard](Pager.expiringCreditCard(this, queryString), response)
  }

  private[braintreegateway] def fetchExpiringCreditCards(ids: List[String], queryString: String): List[CreditCard] = {
    val query = new IdsSearchRequest().ids.in(ids)
    val response = http.post("/payment_methods/all/expiring?" + queryString, query)
    response.findAll("credit-card").map{new CreditCard(_)}.toList
  }

  private def dateQueryString(start: Calendar, end: Calendar): String = {
    val dateFormat = new SimpleDateFormat("MMyyyy")
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
    val formattedStart = dateFormat.format(start.getTime)
    val formattedEnd = dateFormat.format(end.getTime)
    String.format("start=%s&end=%s", formattedStart, formattedEnd)
  }

}