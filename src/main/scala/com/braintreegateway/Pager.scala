package com.braintreegateway

import gw._
import search.{TransactionSearchRequest, SubscriptionSearchRequest, CustomerSearchRequest, CreditCardVerificationSearchRequest}

abstract trait Pager[T] {
  def getPage(ids: List[String]): List[T]
}

class CreditCardVerificationPager(gateway:CreditCardVerificationGateway, query:CreditCardVerificationSearchRequest)
  extends Pager[CreditCardVerification] {

  def getPage(ids:List[String]) = {
    gateway.fetchCreditCardVerifications(query, ids)
  }
}

object Pager {
  def customer(gateway: CustomerGateway, query: CustomerSearchRequest) = new Pager[Customer] {
    def getPage(ids: List[String]): List[Customer] = {
      gateway.fetchCustomers(query, ids)
    }
  }

  def expiredCreditCard(gateway: CreditCardGateway) = new Pager[CreditCard] {
    def getPage(ids: List[String]): List[CreditCard] = {
       gateway.fetchExpiredCreditCards(ids)
    }
  }

  def expiringCreditCard(gateway: CreditCardGateway, queryString: String) = new Pager[CreditCard] {
    def getPage(ids: List[String]): List[CreditCard] = {
      gateway.fetchExpiringCreditCards(ids, queryString)
    }
  }

  def subscription(gateway: SubscriptionGateway, search: SubscriptionSearchRequest) = new Pager[Subscription] {
    def getPage(ids: List[String]): List[Subscription] = {
      gateway.fetchSubscriptions(search, ids)
    }
  }

  def transaction(gateway: TransactionGateway, query: TransactionSearchRequest) = new Pager[Transaction] {
    def getPage(ids: List[String]): List[Transaction] = {
      gateway.fetchTransactions(query, ids)
    }
  }
}