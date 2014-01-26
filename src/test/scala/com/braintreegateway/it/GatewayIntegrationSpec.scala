package com.braintreegateway.it

import com.braintreegateway.Environment
import org.scalatest.{Tag, FunSpec}
import com.braintreegateway.gw.BraintreeGateway

/**
 * Extension for FunSpec that allows it blocks in tests to take a fresh gateway as a parameter
 * on each execution.
 */
trait GatewayIntegrationSpec extends FunSpec {

  def onGatewayIt(description:String, tags:Tag*)(block: BraintreeGateway => Unit): Unit = {
    it(description, tags:_*) { block(createGateway) }
  }

  def createGateway = {
    new BraintreeGateway(Environment.SANDBOX,
      System.getProperty("braintree.merchant_id"),
      System.getProperty("braintree.public_key"),
      System.getProperty("braintree.private_key")
    )
  }

  /**
   * allow injection of fresh gateway instances into "it" blocks
   */
  implicit def using(gwUser: BraintreeGateway => Unit): (Unit=>Unit) = {
    Unit => { gwUser(createGateway) }
  }
}
