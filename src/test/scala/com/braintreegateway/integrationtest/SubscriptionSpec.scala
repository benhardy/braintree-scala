package com.braintreegateway.integrationtest

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.MustMatchers
import com.braintreegateway._
import com.braintreegateway.SandboxValues.TransactionAmount
import com.braintreegateway.Subscription.Status
import com.braintreegateway.exceptions.NotFoundException
import gw.{Success, BraintreeGateway, Failure}
import testhelpers.{PlanFixture, MerchantAccountTestConstants, TestHelper, GatewaySpec}
import com.braintreegateway.util.Http
import com.braintreegateway.util.NodeWrapperFactory
import java.math.BigDecimal
import java.util.{Calendar, TimeZone, Random}
import MerchantAccountTestConstants._
import scala.collection.JavaConversions._
import TestHelper._
import com.braintreegateway.testhelpers.CalendarHelper._

@RunWith(classOf[JUnitRunner])
class SubscriptionSpec extends GatewaySpec with MustMatchers {
  private def createFixtures(gateway: BraintreeGateway) = {
    new {
      lazy val customer:Customer = {
        val request = new CustomerRequest
        request.creditCard.cardholderName("Fred Jones").number("5105105105105100").expirationDate("05/12").done
        gateway.customer.create(request) match {
          case Success(customer) => customer
          case _ => fail("setup")
        }
      }
      lazy val creditCard = customer.getCreditCards.get(0)
    }
  }

  implicit def withCardFixture(block: CreditCard => Unit) = {
    (gateway: BraintreeGateway) =>
      val fixture = createFixtures(gateway)
      val creditCard = fixture.creditCard
      block(creditCard)
  }

  def SortById[T <: Modification](a: T, b: T) = a.getId.compareTo(b.getId) < 0

  describe("create") {
    val mountainTimeZone = TimeZone.getTimeZone("US/Mountain")
    onGatewayIt("createSimpleSubscriptionWithoutTrial") {
      gateway => (creditCard: CreditCard) =>
        val plan = PlanFixture.PLAN_WITHOUT_TRIAL
        val request = new SubscriptionRequest().
          paymentMethodToken(creditCard.getToken).planId(plan.getId)
        val createResult = gateway.subscription.create(request)
        //createResult must be('success)
        val subscription = createResult match { case Success(sub) => sub }

        val rightAboutNow = now in mountainTimeZone
        val expectedNextBillingDate = rightAboutNow + plan.getBillingFrequency.months
        val expectedBillingPeriodEndDate = rightAboutNow + plan.getBillingFrequency.months - 1.days
        val expectedBillingPeriodStartDate = rightAboutNow
        val expectedFirstDate = rightAboutNow

        subscription.getPaymentMethodToken must be === creditCard.getToken
        subscription.getPlanId must be === plan.getId
        subscription.getPrice must be === plan.getPrice
        subscription.getBalance must be === new BigDecimal("0.00")
        subscription.getCurrentBillingCycle must be === new Integer(1)
        subscription.getNextBillAmount must be === new BigDecimal("12.34")
        subscription.getNextBillingPeriodAmount must be === new BigDecimal("12.34")
        subscription.getId must fullyMatch regex ("^\\w{6}$")
        subscription.getStatus must be === Subscription.Status.ACTIVE
        subscription.getFailureCount must be === new Integer(0)
        subscription.hasTrialPeriod must be === false
        subscription.getMerchantAccountId must be === DEFAULT_MERCHANT_ACCOUNT_ID
        subscription.getBillingPeriodEndDate must beSameDayAs(expectedBillingPeriodEndDate)
        subscription.getBillingPeriodEndDate must beSameDayAs(expectedBillingPeriodEndDate)
        subscription.getBillingPeriodStartDate must beSameDayAs(expectedBillingPeriodStartDate)
        subscription.getPaidThroughDate must beSameDayAs(expectedBillingPeriodEndDate)
        subscription.getNextBillingDate must beSameDayAs(expectedNextBillingDate)
        subscription.getFirstBillingDate must beSameDayAs(expectedFirstDate)
    }

    onGatewayIt("createReturnsTransactionWithSubscriptionBillingPeriod") {
      gateway => (creditCard: CreditCard) =>
        val plan = PlanFixture.PLAN_WITHOUT_TRIAL
        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId)
        val createResult = gateway.subscription.create(request)
        createResult match {
          case Success(subscription) => {
            val transaction = subscription.getTransactions.get(0)
            transaction.getSubscription.getBillingPeriodStartDate must be === subscription.getBillingPeriodStartDate
            transaction.getSubscription.getBillingPeriodEndDate must be === subscription.getBillingPeriodEndDate
          }
        }
    }

    onGatewayIt("createSimpleSubscriptionWithTrial") {
      gateway => (creditCard: CreditCard) =>
        val plan = PlanFixture.PLAN_WITH_TRIAL
        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId)
        val createResult = gateway.subscription.create(request)
        createResult must be('success)  // TODO redundant?
        val subscription = createResult match { case Success(sub) => sub }
        val expectedFirstAndNextBillingDate = (now in mountainTimeZone) + plan.getTrialDuration.days

        subscription.getPlanId must be === plan.getId
        subscription.getPrice must be === plan.getPrice
        subscription.getPaymentMethodToken must be === creditCard.getToken
        subscription.getId must fullyMatch regex "^\\w{6}$"
        subscription.getStatus must be === Subscription.Status.ACTIVE
        subscription.getBillingPeriodStartDate must be === null
        subscription.getBillingPeriodEndDate must be === null
        subscription.getCurrentBillingCycle must be === new Integer(0)
        subscription.getFailureCount must be === new Integer(0)
        subscription.hasTrialPeriod must be === true
        subscription.getTrialDuration must be === plan.getTrialDuration
        subscription.getTrialDurationUnit.toString must be === plan.getTrialDurationUnit.toString
        subscription.getNextBillingDate must beSameDayAs(expectedFirstAndNextBillingDate)
        subscription.getFirstBillingDate must beSameDayAs(expectedFirstAndNextBillingDate)
    }

    onGatewayIt("overridePlanAddTrial") {
      gateway => (creditCard: CreditCard) =>
        val plan = PlanFixture.PLAN_WITHOUT_TRIAL
        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId).
            trialPeriod(true).trialDuration(2).trialDurationUnit(Subscription.DurationUnit.MONTH)

        val createResult = gateway.subscription.create(request)

        createResult match {
          case Success(subscription) => {
            subscription.hasTrialPeriod must be === true
            subscription.getTrialDuration must be === new Integer(2)
            subscription.getTrialDurationUnit must be === Subscription.DurationUnit.MONTH
           }
        }

    }

    onGatewayIt("overridePlanRemoveTrial") {
      gateway => (creditCard: CreditCard) =>
        val plan = PlanFixture.PLAN_WITH_TRIAL
        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId).trialPeriod(false)
        val createResult = gateway.subscription.create(request)

        createResult match {
          case Success(subscription) => {
            subscription.hasTrialPeriod must be === false
          }
        }
    }

    onGatewayIt("overridePlanPrice") {
      gateway => (creditCard: CreditCard) =>
        val plan = PlanFixture.PLAN_WITH_TRIAL
        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId).
          price(new BigDecimal("482.48"))
        val createResult = gateway.subscription.create(request)

        createResult match {
          case Success(subscription) => {
            subscription.getPrice must be === new BigDecimal("482.48")
          }
        }
    }

    onGatewayIt("overridePlanNumberOfBillingCycles") {
      gateway => (creditCard: CreditCard) =>
        val plan = PlanFixture.PLAN_WITH_TRIAL
        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId)
        val createResult = gateway.subscription.create(request)

        createResult match {
          case Success(subscription) => {
            subscription.getNumberOfBillingCycles must be === plan.getNumberOfBillingCycles
          }
        }
        val overrideRequest = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId).
          numberOfBillingCycles(10)
        val overrideResult = gateway.subscription.create(overrideRequest)
        overrideResult match {
          case Success(overriddenSubscription) => {
            overriddenSubscription.getNumberOfBillingCycles must be === new Integer(10)
            overriddenSubscription.neverExpires must be === false
          }
        }
    }

    onGatewayIt("setNeverExpires") {
      gateway => (creditCard: CreditCard) =>
        val plan = PlanFixture.PLAN_WITH_TRIAL
        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId).neverExpires(true)
        val createResult = gateway.subscription.create(request)

        createResult match {
          case Success(subscription) => {
            subscription.getNumberOfBillingCycles must be(null) // TODO wrap this in a case
            subscription.neverExpires must be === true
          }
        }
    }

    onGatewayIt("setNumberOfBillingCyclesAndUpdateToNeverExpire") {
      gateway => (creditCard: CreditCard) =>
        val plan = PlanFixture.PLAN_WITH_TRIAL
        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId).numberOfBillingCycles(10)

        val result = for {
         subscription <- gateway.subscription.create(request)
         updateRequest = new SubscriptionRequest().neverExpires(true)
         updated <- gateway.subscription.update(subscription.getId, updateRequest)
        } yield updated

        result match {
          case Success(updatedSubscription) => {
            updatedSubscription.getNumberOfBillingCycles must be (null)
            updatedSubscription.neverExpires must be === true
          }
        }
    }

    onGatewayIt("setNumberOfBillingCyclesAndUpdate") {
      gateway => (creditCard: CreditCard) =>
        val plan = PlanFixture.PLAN_WITH_TRIAL
        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId).numberOfBillingCycles(10)
        val result = for {
          subscription <- gateway.subscription.create(request)
          updateRequest = new SubscriptionRequest().numberOfBillingCycles(14)
          updated <- gateway.subscription.update(subscription.getId, updateRequest)
        } yield updated

        result match {
          case Success(updatedSubscription) => {
            updatedSubscription.getNumberOfBillingCycles must be === new Integer(14)
          }
        }
    }

    onGatewayIt("inheritBillingDayOfMonth") {
      gateway => (creditCard: CreditCard) =>
        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).
          planId(PlanFixture.BILLING_DAY_OF_MONTH_PLAN.getId)

        val createResult = gateway.subscription.create(request)

        createResult match {
          case Success(subscription) => {
            subscription.getBillingDayOfMonth must be === new Integer(5)
          }
        }
    }

    onGatewayIt("overrideBillingDayOfMonth") {
      gateway => (creditCard: CreditCard) =>
        val request = new SubscriptionRequest().billingDayOfMonth(19).paymentMethodToken(creditCard.getToken).
          planId(PlanFixture.BILLING_DAY_OF_MONTH_PLAN.getId)
        val createResult = gateway.subscription.create(request)
        createResult match {
          case Success(subscription) => {
            subscription.getBillingDayOfMonth must be === new Integer(19)
          }
        }
    }

    onGatewayIt("overrideBillingDayOfMonthWithStartImmediately") {
      gateway => (creditCard: CreditCard) =>
        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).
          planId(PlanFixture.BILLING_DAY_OF_MONTH_PLAN.getId).options.startImmediately(true).done
        val createResult = gateway.subscription.create(request)
        createResult match {
          case Success(subscription) => {
            subscription.getTransactions.size must be === 1
          }
        }
    }

    onGatewayIt("setFirstBillingDate") {
      gateway => (creditCard: CreditCard) =>
        val UTC = TimeZone.getTimeZone("UTC")
        val firstBillingDate = (now in UTC) + 3.days

        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).
          planId(PlanFixture.BILLING_DAY_OF_MONTH_PLAN.getId).firstBillingDate(firstBillingDate)

        val createResult = gateway.subscription.create(request)

        createResult match {
          case Success(subscription) => {
            subscription.getFirstBillingDate must beSameDayAs(firstBillingDate)
            subscription.getStatus must be === Subscription.Status.PENDING
          }
        }
    }

    onGatewayIt("setFirstBillingDateInThePast") {
      gateway => (creditCard: CreditCard) =>
        val firstBillingDate = Calendar.getInstance
        firstBillingDate.add(Calendar.DAY_OF_MONTH, -3)
        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(PlanFixture.BILLING_DAY_OF_MONTH_PLAN.getId).firstBillingDate(firstBillingDate)

        val createResult = gateway.subscription.create(request)

        createResult match {
          case Failure(allErrors,_,_,_,_,_) => {
            val errors = allErrors.forObject("subscription").onField("firstBillingDate")
            errors.get(0).getCode must be === ValidationErrorCode.SUBSCRIPTION_FIRST_BILLING_DATE_CANNOT_BE_IN_THE_PAST
          }
        }
    }

    onGatewayIt("setId") {
      gateway => (creditCard: CreditCard) =>
        val plan = PlanFixture.PLAN_WITH_TRIAL
        val newId = "new-id-" + new Random().nextInt
        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId).
          price(new BigDecimal("482.48")).id(newId)

        val createResult = gateway.subscription.create(request)

        createResult match {
          case Success(subscription) => {
            subscription.getId must be === newId
          }
        }
    }

    onGatewayIt("setMerchantAccountId") {
      gateway => (creditCard: CreditCard) =>
        val plan = PlanFixture.PLAN_WITH_TRIAL
        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId).
          price(new BigDecimal("482.48")).merchantAccountId(NON_DEFAULT_MERCHANT_ACCOUNT_ID)

        val createResult = gateway.subscription.create(request)

        createResult match {
          case Success(subscription) => {
            subscription.getMerchantAccountId must be === NON_DEFAULT_MERCHANT_ACCOUNT_ID
          }
        }
    }

    onGatewayIt("hasTransactionOnCreateWithNoTrial") {
      gateway => (creditCard: CreditCard) =>
        val plan = PlanFixture.PLAN_WITHOUT_TRIAL
        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId).price(new BigDecimal("482.48"))

        val createResult = gateway.subscription.create(request)

        createResult match {
          case Success(subscription) => {
            subscription.getMerchantAccountId must be === NON_DEFAULT_MERCHANT_ACCOUNT_ID
            subscription.getTransactions.size must be === 1
            val transaction = subscription.getTransactions.get(0)
            transaction.getAmount must be === new BigDecimal("482.48")
            transaction.getType must be === Transaction.Type.SALE
            transaction.getSubscriptionId must be === subscription.getId
          }
        }
    }

    onGatewayIt("hasTransactionOnCreateWhenTransactionFails") {
      gateway => (creditCard: CreditCard) =>
        val plan = PlanFixture.PLAN_WITHOUT_TRIAL
        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId).price(SandboxValues.TransactionAmount.DECLINE.amount)
        val result = gateway.subscription.create(request)
        val transaction = result match {
          case Failure(_,_,_,_,Some(txn),_)  => txn
        }
        transaction.getStatus must be === Transaction.Status.PROCESSOR_DECLINED
    }

    onGatewayIt("hasNoTransactionOnCreateWithATrial") {
      gateway => (creditCard: CreditCard) =>
        val plan = PlanFixture.PLAN_WITH_TRIAL
        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId)

        val createResult = gateway.subscription.create(request)

        createResult match {
          case Success(subscription) => {
            subscription.getTransactions.size must be === 0
          }
        }
    }

    onGatewayIt("createInheritsNoAddOnsAndDiscountsWhenOptionIsPassed") {
      gateway => (creditCard: CreditCard) =>
        val plan = PlanFixture.ADD_ON_DISCOUNT_PLAN
        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId).options.doNotInheritAddOnsOrDiscounts(true).done
        val createResult = gateway.subscription.create(request)

        createResult match {
          case Success(subscription) => {
            subscription.getAddOns.size must be === 0
            subscription.getDiscounts.size must be === 0
          }
        }
    }

    onGatewayIt("createInheritsAddOnsAndDiscountsFromPlan") {
      gateway => (creditCard: CreditCard) =>
        val plan = PlanFixture.ADD_ON_DISCOUNT_PLAN
        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId)
        val result = gateway.subscription.create(request)
        result must be('success)
        val subscription = result match { case Success(sub) => sub }
        val addOns = subscription.getAddOns.sortWith(SortById).toVector
        addOns.size must be === 2
        addOns.get(0).getId must be === "increase_10"
        addOns.get(0).getAmount must be === new BigDecimal("10.00")
        addOns.get(0).getQuantity must be === new Integer(1)
        addOns.get(0).neverExpires must be === true
        addOns.get(0).getNumberOfBillingCycles must be(null)
        addOns.get(1).getId must be === "increase_20"
        addOns.get(1).getAmount must be === new BigDecimal("20.00")
        addOns.get(1).getQuantity must be === new Integer(1)
        addOns.get(1).neverExpires must be === true
        addOns.get(1).getNumberOfBillingCycles must be(null)
        val discounts = subscription.getDiscounts.sortWith(SortById).toVector
        discounts.size must be === 2
        discounts.get(0).getId must be === "discount_11"
        discounts.get(0).getAmount must be === new BigDecimal("11.00")
        discounts.get(0).getQuantity must be === new Integer(1)
        discounts.get(0).neverExpires must be === true
        discounts.get(0).getNumberOfBillingCycles must be(null)
        discounts.get(1).getId must be === "discount_7"
        discounts.get(1).getAmount must be === new BigDecimal("7.00")
        discounts.get(1).getQuantity must be === new Integer(1)
        discounts.get(1).neverExpires must be === true
        discounts.get(1).getNumberOfBillingCycles must be(null)
    }

    onGatewayIt("createOverridesInheritedAddOnsAndDiscounts") {
      gateway => (creditCard: CreditCard) =>
        val plan = PlanFixture.ADD_ON_DISCOUNT_PLAN
        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId).addOns.update("increase_10").amount(new BigDecimal("30.00")).numberOfBillingCycles(3).quantity(9).done.update("increase_20").amount(new BigDecimal("40.00")).done.done.discounts.update("discount_7").amount(new BigDecimal("15.00")).neverExpires(true).done.update("discount_11").amount(new BigDecimal("23.00")).done.done
        val result = gateway.subscription.create(request)
        result must be('success)
        val subscription = result match { case Success(sub) => sub }
        val addOns = subscription.getAddOns.sortWith(SortById).toVector
        addOns.size must be === 2
        addOns.get(0).getId must be === "increase_10"
        addOns.get(0).getAmount must be === new BigDecimal("30.00")
        addOns.get(0).getNumberOfBillingCycles must be === new Integer(3)
        addOns.get(0).neverExpires must be === false
        addOns.get(0).getQuantity must be === new Integer(9)
        addOns.get(1).getId must be === "increase_20"
        addOns.get(1).getAmount must be === new BigDecimal("40.00")
        addOns.get(1).getQuantity must be === new Integer(1)
        val discounts = subscription.getDiscounts.sortWith(SortById).toVector
        discounts.size must be === 2
        discounts.get(0).getId must be === "discount_11"
        discounts.get(0).getAmount must be === new BigDecimal("23.00")
        discounts.get(0).getNumberOfBillingCycles must be(null)
        discounts.get(0).neverExpires must be === true
        discounts.get(0).getQuantity must be === new Integer(1)
        discounts.get(1).getId must be === "discount_7"
        discounts.get(1).getAmount must be === new BigDecimal("15.00")
        discounts.get(1).getQuantity must be === new Integer(1)
    }

    onGatewayIt("createRemovesInheritedAddOnsAndDiscounts") {
      gateway => (creditCard: CreditCard) =>
        val plan = PlanFixture.ADD_ON_DISCOUNT_PLAN
        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId).addOns.remove("increase_10", "increase_20").done.discounts.remove("discount_7", "discount_11").done
        val result = gateway.subscription.create(request)
        result match {
          case Success(subscription) => {
            subscription.getAddOns.size must be === 0
            subscription.getDiscounts.size must be === 0
          }
          case _ => fail("expected success")
        }
    }

    onGatewayIt("createRemovesInheritedAddOnsAndDiscountsWithListsOrChaining") {
      gateway => (creditCard: CreditCard) =>
        val plan = PlanFixture.ADD_ON_DISCOUNT_PLAN
        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId).
          addOns.remove(List("increase_10", "increase_20")).done.
          discounts.remove("discount_7").remove("discount_11").done
        val result = gateway.subscription.create(request)
        result match {
          case Success(subscription) => {
            subscription.getAddOns.size must be === 0
            subscription.getDiscounts.size must be === 0
          }
        }
    }

    onGatewayIt("createAddsNewAddOnsAndDiscounts") {
      gateway => (creditCard: CreditCard) =>
        val plan = PlanFixture.ADD_ON_DISCOUNT_PLAN
        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId).addOns.remove("increase_10", "increase_20").add.inheritedFromId("increase_30").amount(new BigDecimal("40.00")).neverExpires(false).numberOfBillingCycles(6).quantity(3).done.done.discounts.remove("discount_7", "discount_11").add.inheritedFromId("discount_15").amount(new BigDecimal("17.00")).neverExpires(true).numberOfBillingCycles(null).quantity(2).done.done
        val result = gateway.subscription.create(request)
        result match {
          case Success(subscription) => {
            subscription.getAddOns.size must be === 1
            subscription.getAddOns.get(0).getAmount must be === new BigDecimal("40.00")
            subscription.getAddOns.get(0).getNumberOfBillingCycles must be === new Integer(6)
            subscription.getAddOns.get(0).neverExpires must be === false
            subscription.getAddOns.get(0).getQuantity must be === new Integer(3)
            subscription.getDiscounts.size must be === 1
            subscription.getDiscounts.get(0).getAmount must be === new BigDecimal("17.00")
            subscription.getDiscounts.get(0).getNumberOfBillingCycles must be(null)
            subscription.getDiscounts.get(0).neverExpires must be === true
            subscription.getDiscounts.get(0).getQuantity must be === new Integer(2)
          }
        }
    }

    onGatewayIt("createWithBadQuantityCorrectlyParsesValidationErrors") {
      gateway => (creditCard: CreditCard) =>
        val plan = PlanFixture.ADD_ON_DISCOUNT_PLAN
        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId).addOns.
          update("addon_7").amount(new BigDecimal("-15")).done.update("discount_7").quantity(-10).done.done
        val result = gateway.subscription.create(request)
        result must not be ('success)
        val updateErrors = result match {
          case Failure(errors,_,_,_,_,_)  => errors.forObject("subscription").forObject("addOns").forObject("update")
        }
        updateErrors.forIndex(0).
          onField("amount").get(0).getCode must be === ValidationErrorCode.SUBSCRIPTION_MODIFICATION_AMOUNT_IS_INVALID
        updateErrors.forIndex(1).
          onField("quantity").get(0).getCode must be === ValidationErrorCode.SUBSCRIPTION_MODIFICATION_QUANTITY_IS_INVALID
    }

    onGatewayIt("createWithBadPlanId") {
      gateway => (creditCard: CreditCard) => {
        val createRequest = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId("noSuchPlanId")
        val result = gateway.subscription.create(createRequest)
        val planErrors = result match {
          case Failure(allErrors,_,_,_,_,_) => allErrors.forObject("subscription").onField("planId")
        }
        planErrors.get(0).getCode must be === ValidationErrorCode.SUBSCRIPTION_PLAN_ID_IS_INVALID
      }
    }

    onGatewayIt("createWithBadPaymentMethod") {
      gateway =>
        val createRequest = new SubscriptionRequest().paymentMethodToken("invalidToken").planId(PlanFixture.PLAN_WITHOUT_TRIAL.getId)
        val result = gateway.subscription.create(createRequest)
        val errors = result match {
          case Failure(allErrors,_,_,_,_,_) => allErrors
        }
        val code = errors.forObject("subscription").onField("paymentMethodToken").get(0).getCode
        code must be === ValidationErrorCode.SUBSCRIPTION_PAYMENT_METHOD_TOKEN_IS_INVALID
    }

    onGatewayIt("createWithDescriptor") {
      gateway => (creditCard: CreditCard) =>

        val plan = PlanFixture.PLAN_WITHOUT_TRIAL
        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId).descriptor.name("123*123456789012345678").phone("3334445555").done
        val createResult = gateway.subscription.create(request)
        val subscription = createResult match { case Success(sub) => sub }
        subscription.getDescriptor.getName must be === "123*123456789012345678"
        subscription.getDescriptor.getPhone must be === "3334445555"
        val transaction = subscription.getTransactions.get(0)
        transaction.getDescriptor.getName must be === "123*123456789012345678"
        transaction.getDescriptor.getPhone must be === "3334445555"
    }

    onGatewayIt("createWithDescriptorValidation") {
      gateway =>
        val request = new SubscriptionRequest().descriptor.name("xxxx").phone("xxx").done
        val result = gateway.subscription.create(request)
        val errors = result match {
          case Failure(errors,_,_,_,_,_) => errors
        }
        errors.forObject("subscription").forObject("descriptor").onField("name").get(0).getCode must be === ValidationErrorCode.DESCRIPTOR_NAME_FORMAT_IS_INVALID
        errors.forObject("subscription").forObject("descriptor").onField("phone").get(0).getCode must be === ValidationErrorCode.DESCRIPTOR_PHONE_FORMAT_IS_INVALID
    }

    onGatewayIt("validationErrorsOnCreate") {
      gateway => (creditCard: CreditCard) =>

        val plan = PlanFixture.PLAN_WITHOUT_TRIAL
        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId).id("invalid id")
        val createResult = gateway.subscription.create(request)
        createResult match {
          case Failure(errors,_,_,_,_,_) => {
            errors.forObject("subscription").onField("id").get(0).getCode must be === ValidationErrorCode.SUBSCRIPTION_TOKEN_FORMAT_IS_INVALID
          }
        }
    }

  }

  describe("find") {
    onGatewayIt("finds") {
      gateway => (creditCard: CreditCard) =>
        val plan = PlanFixture.PLAN_WITHOUT_TRIAL
        val createRequest = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId)
        val createResult = gateway.subscription.create(createRequest)
        val subscription = createResult match { case Success(sub) => sub }
        val foundSubscription = gateway.subscription.find(subscription.getId)
        foundSubscription.getId must be === subscription.getId
        creditCard.getToken must be === subscription.getPaymentMethodToken
        plan.getId must be === subscription.getPlanId
    }

    onGatewayIt("find throws NotFoundException on empty id list") {
      gateway =>
        intercept[NotFoundException] {
          gateway.subscription.find(" ")
        }
    }

    onGatewayIt("pastDueSubscriptionReportsCorrectStatus") {
      gateway => (creditCard: CreditCard) =>
        val createRequest = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(PlanFixture.PLAN_WITHOUT_TRIAL.getId)
        val createResult = gateway.subscription.create(createRequest)
        val subscription = createResult match { case Success(sub) => sub }
        makePastDue(gateway, subscription, 1)
        val foundSubscription = gateway.subscription.find(subscription.getId)
        foundSubscription.getStatus must be === Status.PAST_DUE
    }
  }

  describe("update") {
    onGatewayIt("updates id") {
      gateway => (creditCard: CreditCard) =>
        val oldId = "old-id-" + new Random().nextInt
        val newId = "new-id-" + new Random().nextInt
        val plan = PlanFixture.PLAN_WITHOUT_TRIAL
        val createRequest = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId).id(oldId)
        
        val result = for {
          created <- gateway.subscription.create(createRequest)
          updateRequest = new SubscriptionRequest().id(newId)
          updated <- gateway.subscription.update(oldId, updateRequest)
        } yield updated

        result match {
          case Success(subscription) => {
            subscription.getId must be === newId
            (gateway.subscription.find(newId)) must not be (null)
          }
        }
    }

    onGatewayIt("updateMerchantAccountId") {
      gateway => (creditCard: CreditCard) =>
        val plan = PlanFixture.PLAN_WITHOUT_TRIAL
        val createRequest = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId)
        
        val result = for {
          created <- gateway.subscription.create(createRequest)
          updateRequest = new SubscriptionRequest().merchantAccountId(NON_DEFAULT_MERCHANT_ACCOUNT_ID)
          updated <- gateway.subscription.update(created.getId, updateRequest)
        } yield updated

        result match {
          case Success(subscription) => {
            subscription.getMerchantAccountId must be === NON_DEFAULT_MERCHANT_ACCOUNT_ID
          }
        }
    }

    onGatewayIt("updatePlan") {
      gateway => (creditCard: CreditCard) =>

        val originalPlan = PlanFixture.PLAN_WITHOUT_TRIAL
        val newPlan = PlanFixture.PLAN_WITH_TRIAL
        val createRequest = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(originalPlan.getId)
        
        val result = for {
          created <- gateway.subscription.create(createRequest)
          updateRequest = new SubscriptionRequest().planId(newPlan.getId)
          updated <- gateway.subscription.update(created.getId, updateRequest)
        } yield updated

        result match {
          case Success(subscription) => {
            subscription.getPlanId must be === newPlan.getId
          }
        }
    }

    onGatewayIt("updatePaymentMethod") {
      gateway => (creditCard: CreditCard) =>

        val fixture = createFixtures(gateway)

        val customer = fixture.customer
        val originalPlan = PlanFixture.PLAN_WITHOUT_TRIAL
        val createRequest = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(originalPlan.getId)
        val cardRequest = new CreditCardRequest().customerId(customer.getId).cardholderName("John Doe").cvv("123").number("5105105105105100").expirationDate("05/12")
        
        val result = for {
          created <- gateway.subscription.create(createRequest)
          newCreditCard <- gateway.creditCard.create(cardRequest)
          updateRequest = new SubscriptionRequest().paymentMethodToken(newCreditCard.getToken)
          updated <- gateway.subscription.update(created.getId, updateRequest)
        } yield (newCreditCard,updated)

        result match {
          case Success((newCreditCard,subscription)) => {
            subscription.getPaymentMethodToken must be === newCreditCard.getToken
          }
        }
    }

    onGatewayIt("createAProrationTransactionOnPriceIncreaseWhenFlagIsNotPassed") {
      gateway => (creditCard: CreditCard) =>

        val originalPlan = PlanFixture.PLAN_WITHOUT_TRIAL
        val createRequest = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(originalPlan.getId).price(new BigDecimal("1.23"))

        val result = for {
          subscription <- gateway.subscription.create(createRequest)
          updateRequest = new SubscriptionRequest().price(new BigDecimal("4.56"))
          updated <- gateway.subscription.update(subscription.getId, updateRequest)
        } yield updated

        result match {
          case Success(subscription) => {
            subscription.getPrice must be === new BigDecimal("4.56")
            subscription.getTransactions.size must be === 2
          }
        }
    }

    onGatewayIt("createAProrationTransactionOnPriceIncreaseWhenProrationFlagIsTrue") {
      gateway => (creditCard: CreditCard) =>

        val originalPlan = PlanFixture.PLAN_WITHOUT_TRIAL
        val createRequest = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(originalPlan.getId).price(new BigDecimal("1.23"))
        val result = for {
          subscription <- gateway.subscription.create(createRequest)
          updateRequest = new SubscriptionRequest().price(new BigDecimal("4.56")).options.prorateCharges(true).done
          updated <- gateway.subscription.update(subscription.getId, updateRequest)
        } yield updated

        result match {
          case Success(subscription) => {
            subscription.getPrice must be === new BigDecimal("4.56")
            subscription.getTransactions.size must be === 2
          }
      }
    }

    onGatewayIt("doNotCreateAProrationTransactionOnPriceIncreaseWhenProrationFlagIsFalse") {
      gateway => (creditCard: CreditCard) =>

        val originalPlan = PlanFixture.PLAN_WITHOUT_TRIAL
        val createRequest = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(originalPlan.getId).price(new BigDecimal("1.23"))
        val result = for {
          subscription <- gateway.subscription.create(createRequest)
          updateRequest = new SubscriptionRequest().price(new BigDecimal("4.56")).options.prorateCharges(false).done
          updated <- gateway.subscription.update(subscription.getId, updateRequest)
        } yield updated

        result match {
          case Success(subscription) => {
            subscription.getPrice must be === new BigDecimal("4.56")
            subscription.getTransactions.size must be === 1
          }
        }
    }

    onGatewayIt("doNotUpdateIfReverting") {
      gateway => (creditCard: CreditCard) =>

        val originalPlan = PlanFixture.PLAN_WITHOUT_TRIAL
        val createRequest = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(originalPlan.getId).price(new BigDecimal("1.23"))
        val updateRequest = new SubscriptionRequest().price(new BigDecimal("2100")).options.prorateCharges(true).
            revertSubscriptionOnProrationFailure(true).done

        val result = for {
          original <- gateway.subscription.create(createRequest)
          updated <- gateway.subscription.update(original.getId, updateRequest)
        } yield (original, updated)

        result match {
          case Success((original,updated)) => {
            updated.getTransactions.size must be === (original.getTransactions.size + 1)
            updated.getTransactions.get(0).getStatus must be === Transaction.Status.PROCESSOR_DECLINED
            updated.getBalance must be === new BigDecimal("0.00")
            updated.getPrice must be === new BigDecimal("1.23")
          }
        }
    }

    onGatewayIt("UpdateIfNotReverting") {
      gateway => (creditCard: CreditCard) =>

        val originalPlan = PlanFixture.PLAN_WITHOUT_TRIAL
        val createRequest = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(originalPlan.getId).price(new BigDecimal("1.23"))
        
        val result = for {
          createdSubscription <- gateway.subscription.create(createRequest)
          updateRequest = new SubscriptionRequest().price(new BigDecimal("2100")).options.prorateCharges(true).revertSubscriptionOnProrationFailure(false).done
          updated <- gateway.subscription.update(createdSubscription.getId, updateRequest)
        } yield (createdSubscription, updated)

        result match {
          case Success((original, updatedSubscription)) => {
            updatedSubscription.getTransactions.size must be === (original.getTransactions.size + 1)
            updatedSubscription.getTransactions.get(0).getStatus must be === Transaction.Status.PROCESSOR_DECLINED
            updatedSubscription.getBalance must be === original.getTransactions.get(0).getAmount
            updatedSubscription.getPrice must be === new BigDecimal("2100.00")
          }
        }
    }

    onGatewayIt("dontIncreasePriceAndDontAddTransaction") {
      gateway => (creditCard: CreditCard) =>

        val originalPlan = PlanFixture.PLAN_WITHOUT_TRIAL
        val createRequest = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(originalPlan.getId)
        
        val result = for {
          subscription <- gateway.subscription.create(createRequest)
          updateRequest = new SubscriptionRequest().price(new BigDecimal("4.56"))
          updated <- gateway.subscription.update(subscription.getId, updateRequest)
        } yield updated

        result match {
          case Success(updatedSubscription) => {
            updatedSubscription.getPrice must be === new BigDecimal("4.56")
            updatedSubscription.getTransactions.size must be === 1
          }
        }
    }

    onGatewayIt("updateAddOnsAndDiscounts") {
      gateway => (creditCard: CreditCard) =>

        val plan = PlanFixture.ADD_ON_DISCOUNT_PLAN
        val createRequest = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId)
        val result = for {
          subscription <- gateway.subscription.create(createRequest)
          request = new SubscriptionRequest().addOns.update("increase_10").amount(new BigDecimal("30.00")).quantity(9).done.remove("increase_20").add.inheritedFromId("increase_30").amount(new BigDecimal("31.00")).quantity(7).done.done.discounts.update("discount_7").amount(new BigDecimal("15.00")).done.remove("discount_11").add.inheritedFromId("discount_15").amount(new BigDecimal("23.00")).done.done
          updated <- gateway.subscription.update(subscription.getId, request)
        } yield updated

        result match {
          case Success(updatedSubscription) => {
            val addOns = updatedSubscription.getAddOns.sortWith(SortById).toVector
            addOns.size must be === 2
            addOns.get(0).getAmount must be === new BigDecimal("30.00")
            addOns.get(0).getQuantity must be === new Integer(9)
            addOns.get(1).getAmount must be === new BigDecimal("31.00")
            addOns.get(1).getQuantity must be === new Integer(7)
            val discounts = updatedSubscription.getDiscounts.sortWith(SortById).toVector
            discounts.size must be === 2
            discounts.get(0).getId must be === "discount_15"
            discounts.get(0).getAmount must be === new BigDecimal("23.00")
            discounts.get(0).getQuantity must be === new Integer(1)
            discounts.get(1).getAmount must be === new BigDecimal("15.00")
            discounts.get(1).getId must be === "discount_7"
            discounts.get(1).getQuantity must be === new Integer(1)
          }
        }
    }

    onGatewayIt("updateCanReplaceAllAddOnsAndDiscounts") {
      gateway => (creditCard: CreditCard) =>

        val plan = PlanFixture.ADD_ON_DISCOUNT_PLAN
        val createRequest = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId)
        val result = for {
          subscription <- gateway.subscription.create(createRequest)

          request = new SubscriptionRequest().addOns.add.inheritedFromId("increase_30").done.done.
            discounts.add.inheritedFromId("discount_15").done.done.options.replaceAllAddOnsAndDiscounts(true).done

          updated <- gateway.subscription.update(subscription.getId, request)
        } yield updated

        result match {
          case Success(updatedSubscription) => {
            updatedSubscription.getAddOns.size must be === 1
            updatedSubscription.getDiscounts.size must be === 1
          }
        }
    }

    onGatewayIt("updateWithDescriptor") {
      gateway => (creditCard: CreditCard) =>

        val plan = PlanFixture.PLAN_WITH_TRIAL
        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId).
          numberOfBillingCycles(10).descriptor.name("123*123456789012345678").phone("1234567890").done

        val result = for {
          subscription <- gateway.subscription.create(request)

          updateRequest = new SubscriptionRequest().descriptor.name("999*99").phone("1234567891").done

          updatedSubscription <- gateway.subscription.update(subscription.getId, updateRequest)
        } yield updatedSubscription

        result match {
          case Success(updatedSubscription) => {
            updatedSubscription.getDescriptor.getName must be === "999*99"
            updatedSubscription.getDescriptor.getPhone must be === "1234567891"
          }
        }
    }

    onGatewayIt("validationErrorsOnUpdate") {
      gateway => (creditCard: CreditCard) =>

        val plan = PlanFixture.PLAN_WITHOUT_TRIAL
        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId)
        val createResult = gateway.subscription.create(request)
        createResult must be('success)
        val createdSubscription = createResult match { case Success(sub) => sub }
        val updateRequest = new SubscriptionRequest().id("invalid id")
        val result = gateway.subscription.update(createdSubscription.getId, updateRequest)
        result match {
          case Failure(errors,_,_,_,_,_) => {
            val code = errors.forObject("subscription").onField("id").get(0).getCode
            code must be === ValidationErrorCode.SUBSCRIPTION_TOKEN_FORMAT_IS_INVALID
          }
        }
    }
  }

  onGatewayIt("getParamsOnError") {
    gateway => (creditCard: CreditCard) =>

      val plan = PlanFixture.PLAN_WITHOUT_TRIAL
      val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId).id("invalid id")
      val createResult = gateway.subscription.create(request)
      createResult match {
        case Failure(_,parameters,_,_,_,_) => {
          parameters.get("plan_id") must be === plan.getId
          parameters.get("id") must be === "invalid id"
        }
      }
  }

  describe("cancel") {
    onGatewayIt("cancels") {
      gateway => (creditCard: CreditCard) =>

        val plan = PlanFixture.PLAN_WITHOUT_TRIAL
        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(plan.getId)
        val result = for {
          subscription <- gateway.subscription.create(request)
          cancellation <- gateway.subscription.cancel(subscription.getId)
        } yield (subscription, cancellation)

        result match {
          case Success((subscription, cancellation)) => {
            cancellation.getStatus must be === Subscription.Status.CANCELED
            gateway.subscription.find(subscription.getId).getStatus must be === Subscription.Status.CANCELED
          }
        }
    }
  }

  describe("search") {
    onGatewayIt("searchOnBillingCyclesRemaining") {
      gateway => (creditCard: CreditCard) =>

        val request12 = new SubscriptionRequest().numberOfBillingCycles(12).paymentMethodToken(creditCard.getToken).planId(PlanFixture.PLAN_WITH_TRIAL.getId).price(new BigDecimal(5))
        val request11 = new SubscriptionRequest().numberOfBillingCycles(11).paymentMethodToken(creditCard.getToken).planId(PlanFixture.PLAN_WITH_TRIAL.getId).price(new BigDecimal(5))
        val subscription12 = gateway.subscription.create(request12) match { case Success(sub) => sub }
        val subscription11 = gateway.subscription.create(request11) match { case Success(sub) => sub }
        val search = new SubscriptionSearchRequest().billingCyclesRemaining.is(12).price.is(new BigDecimal(5))
        val results = gateway.subscription.search(search)
        results must includeSubscription(subscription12)
        results must not (includeSubscription(subscription11))
    }

    onGatewayIt("searchOnDaysPastDue") {
      gateway => (creditCard: CreditCard) =>

        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(PlanFixture.PLAN_WITH_TRIAL.getId)
        val subscription = gateway.subscription.create(request) match { case Success(sub) => sub }
        makePastDue(gateway, subscription, 3)
        val search = new SubscriptionSearchRequest().daysPastDue.between(2, 10)
        val results = gateway.subscription.search(search)
        results.getMaximumSize > 0 must be === true
        for (foundSubscription <- results) {
          foundSubscription.getDaysPastDue >= 2 && foundSubscription.getDaysPastDue <= 10 must be === true
        }
    }

    onGatewayIt("searchOnIdIs") {
      gateway => (creditCard: CreditCard) =>
        val rand = new Random

        val request1 = new SubscriptionRequest().id("find_me" + rand.nextInt).paymentMethodToken(creditCard.getToken).planId(PlanFixture.PLAN_WITH_TRIAL.getId).price(new BigDecimal(2))
        val request2 = new SubscriptionRequest().id("do_not_find_me" + rand.nextInt).paymentMethodToken(creditCard.getToken).planId(PlanFixture.PLAN_WITH_TRIAL.getId).price(new BigDecimal(2))
        val subscription1 = gateway.subscription.create(request1) match { case Success(sub) => sub }
        val subscription2 = gateway.subscription.create(request2) match { case Success(sub) => sub }
        val search = new SubscriptionSearchRequest().id.startsWith("find_me").price.is(new BigDecimal(2))
        val results = gateway.subscription.search(search)
        results must includeSubscription(subscription1)
        results must not (includeSubscription(subscription2))
    }

    onGatewayIt("searchOnInTrialPeriod") {
        gateway => (creditCard: CreditCard) =>

        val rand = new Random
        val request1 = new SubscriptionRequest().id("find_me" + rand.nextInt).paymentMethodToken(creditCard.getToken).planId(PlanFixture.PLAN_WITH_TRIAL.getId).price(new BigDecimal(2))
        val request2 = new SubscriptionRequest().id("do_not_find_me" + rand.nextInt).paymentMethodToken(creditCard.getToken).planId(PlanFixture.PLAN_WITHOUT_TRIAL.getId).price(new BigDecimal(2))
        val subscriptionWithTrial = gateway.subscription.create(request1) match { case Success(sub) => sub }
        val subscriptionWithoutTrial = gateway.subscription.create(request2) match { case Success(sub) => sub }

        val search1 = new SubscriptionSearchRequest().inTrialPeriod.is(true)
        val subscriptionsWithTrialPeriods = gateway.subscription.search(search1)
        subscriptionsWithTrialPeriods must includeSubscription(subscriptionWithTrial)
        subscriptionsWithTrialPeriods must not (includeSubscription(subscriptionWithoutTrial))

        val search2 = new SubscriptionSearchRequest().inTrialPeriod.is(false)
        val subscriptionsWithoutTrialPeriods = gateway.subscription.search(search2)
        subscriptionsWithoutTrialPeriods must includeSubscription(subscriptionWithoutTrial)
        subscriptionsWithoutTrialPeriods must not (includeSubscription(subscriptionWithTrial))
    }

    onGatewayIt("searchOnMerchantAccountIdIs") {
      gateway => (creditCard: CreditCard) =>

        val request1 = new SubscriptionRequest().merchantAccountId(DEFAULT_MERCHANT_ACCOUNT_ID).paymentMethodToken(creditCard.getToken).planId(PlanFixture.PLAN_WITH_TRIAL.getId).price(new BigDecimal(3))
        val request2 = new SubscriptionRequest().merchantAccountId(NON_DEFAULT_MERCHANT_ACCOUNT_ID).paymentMethodToken(creditCard.getToken).planId(PlanFixture.PLAN_WITH_TRIAL.getId).price(new BigDecimal(3))
        val subscriptionDefaultMerchantAccount = gateway.subscription.create(request1) match { case Success(sub) => sub }
        val subscriptionNonDefaultMerchantAccount = gateway.subscription.create(request2) match { case Success(sub) => sub }

        val search = new SubscriptionSearchRequest().merchantAccountId.is(NON_DEFAULT_MERCHANT_ACCOUNT_ID).price.is(new BigDecimal(3))
        val results = gateway.subscription.search(search)
        results must includeSubscription(subscriptionNonDefaultMerchantAccount)
        results must not (includeSubscription(subscriptionDefaultMerchantAccount))
    }

    onGatewayIt("searchOnBogusMerchantAccountIdIs") {
      gateway => (creditCard: CreditCard) =>

        val request1 = new SubscriptionRequest().merchantAccountId(DEFAULT_MERCHANT_ACCOUNT_ID).paymentMethodToken(creditCard.getToken).planId(PlanFixture.PLAN_WITH_TRIAL.getId).price(new BigDecimal(5))
        val subscription = gateway.subscription.create(request1) match { case Success(sub) => sub }
        var search = new SubscriptionSearchRequest().merchantAccountId.is(subscription.getMerchantAccountId).price.is(new BigDecimal(5))
        var results = gateway.subscription.search(search)
        results must includeSubscription(subscription)
        search = new SubscriptionSearchRequest().merchantAccountId.in(subscription.getMerchantAccountId, "totally_bogus").price.is(new BigDecimal(5))
        results = gateway.subscription.search(search)
        results must includeSubscription(subscription)
        search = new SubscriptionSearchRequest().merchantAccountId.is("totally_bogus").price.is(new BigDecimal(5))
        results = gateway.subscription.search(search)
        results must not (includeSubscription(subscription))
    }

    onGatewayIt("searchOnNextBillingDate") {
      gateway => (creditCard: CreditCard) =>

        val request1 = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(PlanFixture.PLAN_WITH_TRIAL.getId)
        val request2 = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(PlanFixture.PLAN_WITHOUT_TRIAL.getId)
        val trialSubscription = gateway.subscription.create(request1) match { case Success(sub) => sub }
        val triallessSubscription = gateway.subscription.create(request2) match { case Success(sub) => sub }
        val expectedNextBillingDate = now + 5.days
        val search = new SubscriptionSearchRequest().nextBillingDate.greaterThanOrEqualTo(expectedNextBillingDate)
        val results = gateway.subscription.search(search)
        results must includeSubscription(triallessSubscription)
        results must not (includeSubscription(trialSubscription))
    }

    onGatewayIt("searchOnPlanIdIs") {
      gateway => (creditCard: CreditCard) =>

        val trialPlan = PlanFixture.PLAN_WITH_TRIAL
        val triallessPlan = PlanFixture.PLAN_WITHOUT_TRIAL
        val request1 = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(trialPlan.getId).price(new BigDecimal(7))
        val request2 = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(triallessPlan.getId).price(new BigDecimal(7))
        val subscription1 = gateway.subscription.create(request1) match { case Success(sub) => sub }
        val subscription2 = gateway.subscription.create(request2) match { case Success(sub) => sub }
        val search = new SubscriptionSearchRequest().planId.is(trialPlan.getId).price.is(new BigDecimal(7))
        val results = gateway.subscription.search(search)
        results must includeSubscription(subscription1)
        results must not (includeSubscription(subscription2))
    }

    onGatewayIt("searchOnPlanIdIsNot") {
      gateway => (creditCard: CreditCard) =>

        val trialPlan = PlanFixture.PLAN_WITH_TRIAL
        val triallessPlan = PlanFixture.PLAN_WITHOUT_TRIAL
        val request1 = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(trialPlan.getId).price(new BigDecimal(8))
        val request2 = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(triallessPlan.getId).price(new BigDecimal(8))
        val subscription1 = gateway.subscription.create(request1) match { case Success(sub) => sub }
        val subscription2 = gateway.subscription.create(request2) match { case Success(sub) => sub }
        val search = new SubscriptionSearchRequest().planId.isNot(trialPlan.getId).price.is(new BigDecimal(8))
        val results = gateway.subscription.search(search)
        results must includeSubscription(subscription2)
        results must not (includeSubscription(subscription1))
    }

    onGatewayIt("searchOnPlanIdEndsWith") {
      gateway => (creditCard: CreditCard) =>

        val trialPlan = PlanFixture.PLAN_WITH_TRIAL
        val triallessPlan = PlanFixture.PLAN_WITHOUT_TRIAL
        val request1 = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(trialPlan.getId).price(new BigDecimal(9))
        val request2 = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(triallessPlan.getId).price(new BigDecimal(9))
        val subscription1 = gateway.subscription.create(request1) match { case Success(sub) => sub }
        val subscription2 = gateway.subscription.create(request2) match { case Success(sub) => sub }
        val search = new SubscriptionSearchRequest().planId.endsWith("trial_plan").price.is(new BigDecimal(9))
        val results = gateway.subscription.search(search)
        results must includeSubscription(subscription1)
        results must not (includeSubscription(subscription2))
    }

    onGatewayIt("searchOnPlanIdStartsWith") {
      gateway => (creditCard: CreditCard) =>

        val trialPlan = PlanFixture.PLAN_WITH_TRIAL
        val triallessPlan = PlanFixture.PLAN_WITHOUT_TRIAL
        val request1 = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(trialPlan.getId).price(new BigDecimal(10))
        val request2 = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(triallessPlan.getId).price(new BigDecimal(10))
        val subscription1 = gateway.subscription.create(request1) match { case Success(sub) => sub }
        val subscription2 = gateway.subscription.create(request2) match { case Success(sub) => sub }
        val search = new SubscriptionSearchRequest().planId.startsWith("integration_trial_p").price.is(new BigDecimal(10))
        val results = gateway.subscription.search(search)
        results must includeSubscription(subscription1)
        results must not (includeSubscription(subscription2))
    }

    onGatewayIt("searchOnPlanIdContains") {
      gateway => (creditCard: CreditCard) =>

        val trialPlan = PlanFixture.PLAN_WITH_TRIAL
        val triallessPlan = PlanFixture.PLAN_WITHOUT_TRIAL
        val request1 = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(trialPlan.getId).price(new BigDecimal(11))
        val request2 = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(triallessPlan.getId).price(new BigDecimal(11))
        val subscription1 = gateway.subscription.create(request1) match { case Success(sub) => sub }
        val subscription2 = gateway.subscription.create(request2) match { case Success(sub) => sub }
        val search = new SubscriptionSearchRequest().planId.contains("trial_p").price.is(new BigDecimal(11))
        val results = gateway.subscription.search(search)
        results must includeSubscription(subscription1)
        results must not (includeSubscription(subscription2))
    }

    onGatewayIt("searchOnPlanIdIn") {
      gateway => (creditCard: CreditCard) =>

        val request1 = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(PlanFixture.PLAN_WITH_TRIAL.getId).price(new BigDecimal(6))
        val request2 = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(PlanFixture.PLAN_WITHOUT_TRIAL.getId).price(new BigDecimal(6))
        val request3 = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(PlanFixture.ADD_ON_DISCOUNT_PLAN.getId).price(new BigDecimal(6))
        val subscription1 = gateway.subscription.create(request1) match { case Success(sub) => sub }
        val subscription2 = gateway.subscription.create(request2) match { case Success(sub) => sub }
        val subscription3 = gateway.subscription.create(request3) match { case Success(sub) => sub }
        val search = new SubscriptionSearchRequest().planId.in(PlanFixture.PLAN_WITH_TRIAL.getId, PlanFixture.PLAN_WITHOUT_TRIAL.getId).price.is(new BigDecimal(6))
        val results = gateway.subscription.search(search)
        results must includeSubscription(subscription1)
        results must includeSubscription(subscription2)
        results must not (includeSubscription(subscription3))
    }

    onGatewayIt("searchOnStatusIn") {
      gateway => (creditCard: CreditCard) =>

        val trialPlan = PlanFixture.PLAN_WITH_TRIAL
        val request1 = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(trialPlan.getId).price(new BigDecimal(12))
        val request2 = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(trialPlan.getId).price(new BigDecimal(12))
        val subscription1 = gateway.subscription.create(request1) match { case Success(sub) => sub }
        val subscription2 = gateway.subscription.create(request2) match { case Success(sub) => sub }
        gateway.subscription.cancel(subscription2.getId)
        val search = new SubscriptionSearchRequest().status.in(Status.ACTIVE).price.is(new BigDecimal(12))
        val results = gateway.subscription.search(search)
        results must includeSubscription(subscription1)
        results must not (includeSubscription(subscription2))
    }

    onGatewayIt("searchOnStatusExpired") {
      gateway =>
        val search = new SubscriptionSearchRequest().status.in(Status.EXPIRED)
        val results = gateway.subscription.search(search)
        results.getMaximumSize > 0 must be === true
        for (subscription <- results) {
          subscription.getStatus must be === Status.EXPIRED
        }
    }

    onGatewayIt("searchOnStatusInWithMultipleStatusesAsList") {
      gateway => (creditCard: CreditCard) =>

        val trialPlan = PlanFixture.PLAN_WITH_TRIAL
        val request1 = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(trialPlan.getId).price(new BigDecimal(13))
        val request2 = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(trialPlan.getId).price(new BigDecimal(13))
        val subscription1 = gateway.subscription.create(request1) match { case Success(sub) => sub }
        val subscription2 = gateway.subscription.create(request2) match { case Success(sub) => sub }
        gateway.subscription.cancel(subscription2.getId)

        val statuses = List(Status.ACTIVE, Status.CANCELED)
        val search = new SubscriptionSearchRequest().status.in(statuses).price.is(new BigDecimal(13))
        val results = gateway.subscription.search(search)
        results must includeSubscription(subscription1)
        results must includeSubscription(subscription2)
    }

    onGatewayIt("searchOnStatusInWithMultipleStatuses") {
      gateway => (creditCard: CreditCard) =>
        val trialPlan = PlanFixture.PLAN_WITH_TRIAL
        val request1 = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(trialPlan.getId).price(new BigDecimal(14))
        val request2 = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(trialPlan.getId).price(new BigDecimal(14))
        val subscription1 = gateway.subscription.create(request1) match { case Success(sub) => sub }
        val subscription2 = gateway.subscription.create(request2) match { case Success(sub) => sub }
        gateway.subscription.cancel(subscription2.getId)
        
        val search = new SubscriptionSearchRequest().status.in(Status.ACTIVE, Status.CANCELED).price.is(new BigDecimal(14))
        val results = gateway.subscription.search(search)
        results must includeSubscription(subscription1)
        results must includeSubscription(subscription2)
    }

    onGatewayIt("searchOnTransactionId") {
      gateway => (creditCard: CreditCard) =>

        val plan = PlanFixture.PLAN_WITHOUT_TRIAL
        val request1 = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).
          planId(plan.getId).price(new BigDecimal(14))
        val result = for {
          subscription1 <- gateway.subscription.create(request1)

          request2 = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).
              planId(plan.getId).price(new BigDecimal(14))

          subscription2 <- gateway.subscription.create(request2)
        } yield (subscription1, subscription2)

        val (subscription1, subscription2) = result match {
          case Success((sub1, sub2)) => (sub1, sub2)
        }
        val search = new SubscriptionSearchRequest().transactionId.is(subscription1.getTransactions.get(0).getId)
        val results = gateway.subscription.search(search)
        results must includeSubscription(subscription1)
        results must not (includeSubscription(subscription2))
    }
  }

  describe("unrecognized things")  {

    onGatewayIt("unrecognizedStatus") {
      gateway =>
        val xml = <subscription>
                    <status>foobar</status>
                  </subscription>
        val transaction = new Subscription(NodeWrapperFactory.instance.create(xml.toString))
        transaction.getStatus must be === Subscription.Status.UNRECOGNIZED
    }

    onGatewayIt("unrecognizedDurationUnit") {
      gateway =>
        val xml = <subscription>
          <trial-duration-unit>foobar</trial-duration-unit>
        </subscription>
        val transaction = new Subscription(NodeWrapperFactory.instance.create(xml.toString))
        transaction.getTrialDurationUnit must be === Subscription.DurationUnit.UNRECOGNIZED
    }
  }

  describe("retryCharge") {
    onGatewayIt("retryChargeWithAmount") {
      gateway => (creditCard: CreditCard) =>

        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(PlanFixture.PLAN_WITHOUT_TRIAL.getId)
        val result = for {
          subscription <- gateway.subscription.create(request)
          updated = makePastDue(gateway, subscription, 1)
          retry <- gateway.subscription.retryCharge(subscription.getId, TransactionAmount.AUTHORIZE.amount)
        } yield retry
        
        result match {
          case Success(transaction) => {
            transaction.getAmount must be === TransactionAmount.AUTHORIZE.amount
            transaction.getProcessorAuthorizationCode must not be (null)
            transaction.getType must be === Transaction.Type.SALE
            transaction.getStatus must be === Transaction.Status.AUTHORIZED
            transaction.getCreatedAt.year must be === Calendar.getInstance.year
            transaction.getUpdatedAt.year must be === Calendar.getInstance.year
          }
        }
    }

    onGatewayIt("retryChargeWithoutAmount") {
      gateway => (creditCard: CreditCard) =>

        val request = new SubscriptionRequest().paymentMethodToken(creditCard.getToken).planId(PlanFixture.PLAN_WITHOUT_TRIAL.getId)
        val result = for {
          subscription <- gateway.subscription.create(request)
          updated = makePastDue(gateway, subscription, 1)
          retriedCharge <- gateway.subscription.retryCharge(subscription.getId)
        } yield (subscription, retriedCharge)

        result match {
          case Success((subscription, transaction)) => {
            transaction.getAmount must be === subscription.getPrice
            transaction.getProcessorAuthorizationCode must not be (null)
            transaction.getType must be === Transaction.Type.SALE
            transaction.getStatus must be === Transaction.Status.AUTHORIZED
            transaction.getCreatedAt.year must be === Calendar.getInstance.year
            transaction.getUpdatedAt.year must be === Calendar.getInstance.year
          }
        }
    }
  }

  private def makePastDue(gateway: BraintreeGateway, subscription: Subscription, numberOfDaysPastDue: Int) = {
    val response = new Http(gateway.authorizationHeader, gateway.baseMerchantURL,
      Environment.DEVELOPMENT.certificateFilenames, BraintreeGateway.VERSION).
      put("/subscriptions/" + subscription.getId + "/make_past_due?days_past_due=" + numberOfDaysPastDue)
    response must be('success)
    subscription
  }
}
