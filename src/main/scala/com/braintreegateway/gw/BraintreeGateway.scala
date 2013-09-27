package com.braintreegateway.gw

import com.braintreegateway.org.apache.commons.codec.binary.Base64
import com.braintreegateway.util.ClientLibraryProperties
import com.braintreegateway.util.Http
import com.braintreegateway.util.TrUtil
import com.braintreegateway._

/**
 * This is the primary interface to the Braintree Gateway. It is used to
 * interact with:
 * <ul>
 * <li> {@link AddressGateway Addresses}
 * <li> {@link CreditCardGateway CreditCards}
 * <li> {@link CustomerGateway Customers}
 * <li> {@link SubscriptionGateway Subscriptions}
 * <li> {@link TransactionGateway Transactions}
 * </ul>
 *
 * Quick Start Example:
 *
 * <pre>
 * import java.math.BigDecimal
 * import com.braintreegateway.*
 *
 * object BraintreeExample {
 *
 *   def main(String[] args) {
 *     val gateway = new BraintreeGateway(Environment.SANDBOX, &quot;the_merchant_id&quot;, &quot;the_public_key&quot;, &quot;the_private_key&quot;)
 *
 *     val request = new TransactionRequest().amount(new BigDecimal(&quot;100.00&quot;)).creditCard().number(&quot;4111111111111111&quot;).expirationDate(&quot;05/2012&quot;).done()
 *
 *     val transaction = gateway.transaction().sale(request).getTarget()
 *     println(&quot;Transaction ID: &quot; + transaction.getId())
 *     println(&quot;Status: &quot; + transaction.getStatus())
 *   }
 * }
 * </pre>
 *
 * Class parameters needed by BraintreeGateway. Use the values provided by Braintree.
 *
 * @param environment
 * Either { @link Environment#SANDBOX} or
 *                  { @link Environment#PRODUCTION}.
 * @param merchantId
 *                 the merchant id provided by Braintree.
 * @param publicKey
 *                 the public key provided by Braintree.
 * @param privateKey
 *                 the private key provided by Braintree.
 */
class BraintreeGateway(environment: Environment, merchantId: String, publicKey: String, privateKey: String) {

  val baseMerchantURL = environment.baseURL + "/merchants/" + merchantId

  val configuration = new Configuration(baseMerchantURL, publicKey, privateKey)

  val authorizationHeader = "Basic " + Base64.encodeBase64String((publicKey + ":" + privateKey).getBytes).trim

  val http = new Http(authorizationHeader, baseMerchantURL, environment.certificateFilenames, BraintreeGateway.VERSION)

  /**
   * Returns an {@link AddOnGateway} for interacting with {@link AddOn}
   * objects.
   *
   * @return an { @link AddOnGateway}.
   */
  def addOn = new AddOnGateway(http)

  /**
   * Returns an {@link AddressGateway} for interacting with {@link Address}
   * objects.
   *
   * @return an { @link AddressGateway}.
   */
  def address = new AddressGateway(http)

  /**
   * Returns an {@link CreditCardGateway} for interacting with
   * {@link CreditCard} objects.
   *
   * @return an { @link CreditCardGateway}.
   */
  def creditCard = new CreditCardGateway(http, configuration)

  def creditCardVerification = new CreditCardVerificationGateway(http, configuration)

  /**
   * Returns an {@link CustomerGateway} for interacting with {@link Customer}
   * objects.
   *
   * @return an { @link CustomerGateway}.
   */
  def customer = new CustomerGateway(http, configuration)

  /**
   * Returns an {@link DiscountGateway} for interacting with {@link Discount}
   * objects.
   *
   * @return an { @link DiscountGateway}.
   */
  def discount = new DiscountGateway(http)

  /**
   * Returns an {@link PlanGateway} for interacting with {@link Plan} objects.
   *
   * @return an { @link PlanGateway}.
   */
  def plan = new PlanGateway(http)

  def settlementBatchSummary = new SettlementBatchSummaryGateway(http)

  /**
   * Returns an {@link SubscriptionGateway} for interacting with
   * {@link Subscription} objects.
   *
   * @return an { @link SubscriptionGateway}.
   */
  def subscription = new SubscriptionGateway(http)

  /**
   * Returns an {@link TransactionGateway} for interacting with
   * {@link Transaction} objects.
   *
   * @return an { @link TransactionGateway}.
   */
  def transaction = new TransactionGateway(http, configuration)

  def transparentRedirect = new TransparentRedirectGateway(http, configuration)

  def webhookNotification = new WebhookNotificationGateway(configuration)

  def webhookTesting = new WebhookTestingGateway(configuration)

  /**
   * Returns an {@link MerchantAccountGateway} for interacting with
   * {@link MerchantAccount} objects.
   *
   * @return an { @link MerchantAccountGateway}.
   */
  def merchantAccount = new MerchantAccountGateway(http)

  /**
   * Returns encoded transparent redirect data for the given {@link Request}
   * and redirect URL
   *
   * @param trData
     * the transparent redirect data as a { @link Request} object.
   * @param redirectURL
     * the redirect URL for the user after the transparent redirect
   *   POST.
   * @return a String of encoded transparent redirect data.
   */
  def trData(trData: Request, redirectURL: String): String = {
    new TrUtil(configuration).buildTrData(trData, redirectURL)
  }
}

object BraintreeGateway {
  val VERSION = new ClientLibraryProperties().version
}

