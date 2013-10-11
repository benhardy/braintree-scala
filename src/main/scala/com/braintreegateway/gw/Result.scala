package com.braintreegateway.gw

import com.braintreegateway._
import com.braintreegateway.util.NodeWrapper

sealed trait Result[+T] {
  def isSuccess: Boolean

  def map[B](f: T => B): Result[B]

  def flatMap[B](f: T => Result[B]): Result[B]

  def filter(f: T => Boolean): Result[T] = ???

  def foreach(f: T => Unit): Unit

  /**
   * Provided to temporarily make tests less grumpy. This will go away
   * before release. TODO
   * @return
   */
  @deprecated def getTarget: T = this match {
    case Success(t) => t
  }
}

case class Success[T](target: T) extends Result[T] {
  def map[B](f: T => B) = Success(f(target))

  def flatMap[B](f: T => Result[B]) = f(target)

  def foreach(f: T => Unit): Unit = f(target)

  def isSuccess = true
}

case class Failure(
                    errors: ValidationErrors,
                    parameters: Map[String, String],
                    message: String,
                    creditCardVerification: Option[CreditCardVerification] = None,
                    transaction: Option[Transaction] = None,
                    subscription: Option[Subscription] = None
                    ) extends Result[Nothing] {

  def isSuccess: Boolean = false

  def map[B](f: Nothing => B) = this.copy()

  def flatMap[B](f: Nothing => Result[B]) = this.copy()

  def foreach(f: Nothing => Unit): Unit = {}
}

case object Deleted extends Result[Nothing] {
  def isSuccess = true

  def map[B](f: Nothing => B) = this

  def flatMap[B](f: Nothing => Result[B]) = this

  def foreach(f: Nothing => Unit): Unit = {}
}

object Result {
  def settlementBatchSummary(node: NodeWrapper): Result[SettlementBatchSummary] = {
    apply(node, new SettlementBatchSummary(_))
  }

  def address(node: NodeWrapper): Result[Address] = apply(node, new Address(_))

  def transaction(node: NodeWrapper): Result[Transaction] = apply(node, new Transaction(_))

  def subscription(node: NodeWrapper): Result[Subscription] = apply(node, new Subscription(_))

  def customer(node: NodeWrapper): Result[Customer] = apply(node, new Customer(_))

  def creditCard(node: NodeWrapper): Result[CreditCard] = apply(node, new CreditCard(_))

  def merchantAccount(node: NodeWrapper): Result[MerchantAccount] = apply(node, new MerchantAccount(_))

  def apply[T](node: NodeWrapper, maker: NodeWrapper => T): Result[T] = {
    if (node.isSuccess) {
      Success(maker(node))
    } else {
      Failure(
        errors = new ValidationErrors(node),
        creditCardVerification = Option(node.findFirst("verification")).map {
          new CreditCardVerification(_)
        },
        transaction = Option(node.findFirst("transaction")).map {
          new Transaction(_)
        },
        subscription = Option(node.findFirst("subscription")).map {
          new Subscription(_)
        },
        parameters = node.findFirst("params").getFormParameters,
        message = node.findString("message")
      )
    }
  }

  def deleted[T]: Result[T] = Deleted
}
