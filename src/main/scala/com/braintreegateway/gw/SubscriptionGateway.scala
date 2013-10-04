package com.braintreegateway.gw

import com.braintreegateway.exceptions.NotFoundException
import com.braintreegateway.util.Http
import java.math.BigDecimal
import java.util.{List=>JUList}
import scala.collection.JavaConversions._
import com.braintreegateway._

/**
 * Provides methods to interact with {@link Subscription Subscriptions}.
 * Including create, find, update, cancel, etc.
 * This class does not need to be instantiated directly.
 * Instead, use {@link BraintreeGateway#subscription()} to get an instance of this class:
 *
 * <pre>
 * BraintreeGateway gateway = new BraintreeGateway(...);
 * gateway.subscription().create(...)
 * </pre>
 *
 * For more detailed information on {@link Subscription Subscriptions}, see <a href="http://www.braintreepayments.com/gateway/subscription-api" target="_blank">http://www.braintreepaymentsolutions.com/gateway/subscription-api</a>
 */
class SubscriptionGateway(http: Http) {

  /**
   * Cancels the {@link Subscription} with the given id.
   * @param id of the { @link Subscription} to cancel.
   * @return a { @link Result}.
   */
  def cancel(id: String): Result2[Subscription] = {
    val node = http.put("/subscriptions/" + id + "/cancel")
    Result2.subscription(node)
  }

  /**
   * Creates a {@link Subscription}.
   * @param request the request.
   * @return a { @link Result}.
   */
  def create(request: SubscriptionRequest): Result2[Subscription] = {
    val node = http.post("/subscriptions", request)
    Result2.subscription(node)
  }

  def delete(customerId: String, id: String): Result2[Subscription] = {
    http.delete("/subscriptions/" + id)
    Result2.deleted
  }

  /**
   * Finds a {@link Subscription} by id.
   * @param id the id of the { @link Subscription}.
   * @return the { @link Subscription} or raises a { @link com.braintreegateway.exceptions.NotFoundException}.
   */
  def find(id: String): Subscription = {
    if (id == null || (id.trim == "")) throw new NotFoundException
    new Subscription(http.get("/subscriptions/" + id))
  }

  /**
   * Updates a {@link Subscription}.
   * @param id the id of the { @link Subscription}.
   * @param request the request.
   * @return a { @link Result}.
   */
  def update(id: String, request: SubscriptionRequest): Result2[Subscription] = {
    val node = http.put("/subscriptions/" + id, request)
    Result2.subscription(node)
  }

  /**
   * Search for a {@link Subscription}.
   * @param searchRequest the { @link SubscriptionSearchRequest}.
   * @return a { @link Result}.
   */
  def search(searchRequest: SubscriptionSearchRequest): ResourceCollection[Subscription] = {
    val node = http.post("/subscriptions/advanced_search_ids", searchRequest)
    new ResourceCollection[Subscription](new SubscriptionPager(this, searchRequest), node)
  }

  private[braintreegateway] def fetchSubscriptions(search: SubscriptionSearchRequest, ids: JUList[String]): JUList[Subscription] = {
    search.ids.in(ids)
    val response = http.post("/subscriptions/advanced_search", search)
    response.findAll("subscription").map{new Subscription(_)}
  }

  private def retryCharge(txnRequest: SubscriptionTransactionRequest): Result2[Transaction] = {
    val response = http.post("/transactions", txnRequest)
    Result2.transaction(response)
  }

  def retryCharge(subscriptionId: String): Result2[Transaction] = {
    retryCharge(new SubscriptionTransactionRequest().subscriptionId(subscriptionId))
  }

  def retryCharge(subscriptionId: String, amount: BigDecimal): Result2[Transaction] = {
    retryCharge(new SubscriptionTransactionRequest().subscriptionId(subscriptionId).amount(amount))
  }
}