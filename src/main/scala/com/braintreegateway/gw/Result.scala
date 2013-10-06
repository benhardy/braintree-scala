package com.braintreegateway.gw

import com.braintreegateway._
import com.braintreegateway.util.NodeWrapper
import scala.collection.JavaConverters._

// TODO rename to Result when the Java one's removed
sealed trait Result2[+T] {
  def isSuccess: Boolean

  def map[B](f: T=>B): Result2[B]
  def flatMap[B](f: T=>Result2[B]): Result2[B]
  def filter(f: T=>Boolean): Result2[T] = ???
  def foreach(f: T=>Unit):Unit

  /**
   * Provided to temporarily make tests less grumpy. This will go away
   * before release.
   * @return
   */
  @deprecated def getTarget: T = this match { case Success(t) => t }
}

case class Success[T](target: T) extends Result2[T] {
  def map[B](f: T=>B) = Success(f(target))
  def flatMap[B](f: T=>Result2[B]) = f(target)
  def foreach(f: T=>Unit):Unit = f(target)
  def isSuccess = true
}

case class Failure(
    errors: ValidationErrors,
    parameters: Map[String, String],
    message: String,
    creditCardVerification: Option[CreditCardVerification] = None,
    transaction: Option[Transaction] = None,
    subscription: Option[Subscription] = None
    ) extends Result2[Nothing] {

  def isSuccess: Boolean =false
  def map[B](f: Nothing=>B) = this.copy()
  def flatMap[B](f: Nothing=>Result2[B]) = this.copy()
  def foreach(f: Nothing=>Unit):Unit = {}
}

case object Deleted extends Result2[Nothing] {
  def isSuccess = true
  def map[B](f: Nothing=>B) = this
  def flatMap[B](f: Nothing=>Result2[B]) = this
  def foreach(f: Nothing=>Unit):Unit = {}
}

object Result2 {
  def settlementBatchSummary(node: NodeWrapper): Result2[SettlementBatchSummary] = {
    apply(node, new SettlementBatchSummary(_))
  }

  def address(node: NodeWrapper): Result2[Address] = apply(node, new Address(_))

  def transaction(node: NodeWrapper): Result2[Transaction] = apply(node, new Transaction(_))
  def subscription(node: NodeWrapper): Result2[Subscription] = apply(node, new Subscription(_))
  def customer(node: NodeWrapper): Result2[Customer] = apply(node, new Customer(_))
  def creditCard(node: NodeWrapper): Result2[CreditCard] = apply(node, new CreditCard(_))
  def merchantAccount(node: NodeWrapper): Result2[MerchantAccount] = apply(node, new MerchantAccount(_))

  def apply[T](node: NodeWrapper, maker: NodeWrapper=>T): Result2[T] = {
    if (node.isSuccess) {
      Success(maker(node))
    } else {
      Failure(
        errors = new ValidationErrors(node),
        creditCardVerification = Option(node.findFirst("verification")).map{ new CreditCardVerification(_) },
        transaction = Option(node.findFirst("transaction")).map { new Transaction(_) },
        subscription = Option(node.findFirst("subscription")).map { new Subscription(_) },
        parameters = node.findFirst("params").getFormParameters.asScala.toMap,
        message = node.findString("message")
      )
    }
  }

  def deleted[T]: Result2[T] = Deleted
}
