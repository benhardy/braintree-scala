package com.braintreegateway.gw

import com.braintreegateway.Transaction.Type
import com.braintreegateway.exceptions.NotFoundException
import com.braintreegateway.util.Http
import com.braintreegateway.util.NodeWrapper
import com.braintreegateway.util.TrUtil
import java.math.BigDecimal
import java.util.{List=>JUList}
import scala.collection.JavaConversions._
import com.braintreegateway._


/**
 * Provides methods to interact with {@link Transaction Transactions}.
 * E.g. sales, credits, refunds, searches, etc.
 * This class does not need to be instantiated directly.
 * Instead, use {@link BraintreeGateway#transaction()} to get an instance of this class:
 *
 * <pre>
 * BraintreeGateway gateway = new BraintreeGateway(...);
 * gateway.transaction().create(...)
 * </pre>
 *
 * For more detailed information on {@link Transaction Transactions}, see <a href="http://www.braintreepayments.com/gateway/transaction-api" target="_blank">http://www.braintreepaymentsolutions.com/gateway/transaction-api</a>
 */
class TransactionGateway(http: Http, configuration: Configuration) {

  def cloneTransaction(id: String, request: TransactionCloneRequest): Result2[Transaction] = {
    val response: NodeWrapper = http.post("/transactions/" + id + "/clone", request)
    Result2.transaction(response)
  }

  /**
   * Please use gateway.transparentRedirect().confirmTransaction() instead
   */
  @Deprecated def confirmTransparentRedirect(queryString: String): Result2[Transaction] = {
    val trRequest: TransparentRedirectRequest = new TransparentRedirectRequest(configuration, queryString)
    val node: NodeWrapper = http.post("/transactions/all/confirm_transparent_redirect_request", trRequest)
    Result2.transaction(node)
  }

  /**
   * Creates a credit {@link Transaction}.
   * @param request the request.
   * @return a{ @link Result}
   */
  def credit(request: TransactionRequest): Result2[Transaction] = {
    val response: NodeWrapper = http.post("/transactions", request.`type`(Type.CREDIT))
    Result2.transaction(response)
  }

  /**
   * Creates transparent redirect data for a credit.
   * @param trData the request.
   * @param redirectURL the redirect URL.
   * @return aString representing the trData.
   */
  def creditTrData(trData: TransactionRequest, redirectURL: String): String = {
    new TrUtil(configuration).buildTrData(trData.`type`(Type.CREDIT), redirectURL)
  }

  /**
   * Finds a {@link Transaction} by id.
   * @param id the id of the { @link Transaction}.
   * @return the { @link Transaction} or raises a { @link com.braintreegateway.exceptions.NotFoundException}.
   */
  def find(id: String): Transaction = {
    if (id == null || (id.trim == "")) throw new NotFoundException
    new Transaction(http.get("/transactions/" + id))
  }

  /**
   * Refunds all or part of a previous sale {@link Transaction}.
   * @param id the id of the (sale) { @link Transaction} to refund.
   * @return a{ @link Result}.
   */
  def refund(id: String): Result2[Transaction] = {
    val response: NodeWrapper = http.post("/transactions/" + id + "/refund")
    Result2.transaction(response)
  }

  def refund(id: String, amount: BigDecimal): Result2[Transaction] = {
    val request: TransactionRequest = new TransactionRequest().amount(amount)
    val response: NodeWrapper = http.post("/transactions/" + id + "/refund", request)
    Result2.transaction(response)
  }

  /**
   * Creates a sale {@link Transaction}.
   * @param request the request.
   * @return a{ @link Result}.
   */
  def sale(request: TransactionRequest): Result2[Transaction] = {
    val response: NodeWrapper = http.post("/transactions", request.`type`(Type.SALE))
    Result2.transaction(response)
  }

  /**
   * Creates transparent redirect data for a sale.
   * @param trData the request.
   * @param redirectURL the redirect URL.
   * @return aString representing the trData.
   */
  def saleTrData(trData: TransactionRequest, redirectURL: String): String = {
    new TrUtil(configuration).buildTrData(trData.`type`(Type.SALE), redirectURL)
  }

  /**
   * Finds all Transactions that match the query and returns a {@link ResourceCollection}.
   * See: <a href="http://www.braintreepayments.com/gateway/transaction-api#searching" target="_blank">http://www.braintreepaymentsolutions.com/gateway/transaction-api#searching</a>
   * @return a{ @link ResourceCollection}.
   */
  def search(query: TransactionSearchRequest): ResourceCollection[Transaction] = {
    val node: NodeWrapper = http.post("/transactions/advanced_search_ids", query)
    new ResourceCollection[Transaction](new TransactionPager(this, query), node)
  }

  private[braintreegateway] def fetchTransactions(query: TransactionSearchRequest, ids: JUList[String]): JUList[Transaction] = {
    query.ids.in(ids)
    val response: NodeWrapper = http.post("/transactions/advanced_search", query)
    response.findAll("transaction").map(new Transaction(_))
  }

  /**
   * Cancels a pending release of a transaction with the given id from escrow.
   * @param id of the transaction to cancel release from escrow of.
   * @return a{ @link Result}.
   */
  def cancelRelease(id: String): Result2[Transaction] = {
    val request: TransactionRequest = new TransactionRequest
    val response: NodeWrapper = http.put("/transactions/" + id + "/cancel_release", request)
    Result2.transaction(response)
  }

  /**
   * Holds the transaction with the given id for escrow.
   * @param id of the transaction to hold for escrow.
   * @return a{ @link Result}.
   */
  def holdInEscrow(id: String): Result2[Transaction] = {
    val request: TransactionRequest = new TransactionRequest
    val response: NodeWrapper = http.put("/transactions/" + id + "/hold_in_escrow", request)
    Result2.transaction(response)
  }

  /**
   * Submits the transaction with the given id for release.
   * @param id of the transaction to submit for release.
   * @return a{ @link Result}.
   */
  def releaseFromEscrow(id: String): Result2[Transaction] = {
    val request: TransactionRequest = new TransactionRequest
    val response: NodeWrapper = http.put("/transactions/" + id + "/release_from_escrow", request)
    Result2.transaction(response)
  }

  /**
   * Submits the transaction with the given id for settlement.
   * @param id of the transaction to submit for settlement.
   * @return a{ @link Result}.
   */
  def submitForSettlement(id: String): Result2[Transaction] = {
    submitForSettlement(id, null)
  }

  /**
   * Submits the transaction with the given id to be settled for the given amount which must be less than or equal to the authorization amount.
   * @param id of the transaction to submit for settlement.
   * @param amount to settle. must be less than or equal to the authorization amount.
   * @{ @link Result}.
   */
  def submitForSettlement(id: String, amount: BigDecimal): Result2[Transaction] = {
    val request: TransactionRequest = new TransactionRequest().amount(amount)
    val response: NodeWrapper = http.put("/transactions/" + id + "/submit_for_settlement", request)
    Result2.transaction(response)
  }

  /**
   * Please use gateway.transparentRedirect().url() instead
   */
  @Deprecated def transparentRedirectURLForCreate: String = {
    configuration.baseMerchantURL + "/transactions/all/create_via_transparent_redirect_request"
  }

  /**
   * Voids the transaction with the given id.
   * @param id of the transaction to void.
   * @{ @link Result}.
   */
  def voidTransaction(id: String): Result2[Transaction] = {
    val response: NodeWrapper = http.put("/transactions/" + id + "/void")
    Result2.transaction(response)
  }
}