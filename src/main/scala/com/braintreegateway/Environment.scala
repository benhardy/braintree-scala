package com.braintreegateway

class Environment(val baseURL: String, val certificateFilenames: List[String]) {

}
/**
 * Indicates the environment of the Braintree Gateway with which to interact.
 */
object Environment {
  def developmentPort: String = {
    val gatewayPort = Option(System.getenv.get("GATEWAY_PORT"))
    gatewayPort.getOrElse("3000")
  }

  /** For production. */
  final val PRODUCTION: Environment = new Environment("https://www.braintreegateway.com:443",
    List("ssl/www_braintreegateway_com.ca.der", "ssl/securetrust.ca.der"))

  /** For merchant's to use during their development and testing. */
  final val SANDBOX: Environment = new Environment("https://sandbox.braintreegateway.com:443",
    List("ssl/sandbox-godaddy-root.ca.der",
      "ssl/sandbox_braintreegateway_com.ca.der",
      "ssl/sandbox-godaddy-intermediate.ca.der")
  )

  val INTEGRATION_TEST = SANDBOX
}
