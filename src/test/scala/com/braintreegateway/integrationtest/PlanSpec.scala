package com.braintreegateway.integrationtest

import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.scalatest.matchers.MustMatchers
import com.braintreegateway._
import com.braintreegateway.util.Http
import gw.BraintreeGateway
import java.math.BigDecimal
import java.util.Random
import scala.collection.JavaConversions._
import testhelpers.GatewaySpec

@RunWith(classOf[JUnitRunner])
class PlanSpec extends GatewaySpec with MustMatchers {

  describe("plans.all") {
    onGatewayIt("returnsAllPlans") {
      gateway =>
        val http = new Http(gateway.authorizationHeader, gateway.baseMerchantURL,
          Environment.DEVELOPMENT.certificateFilenames, BraintreeGateway.VERSION);
        val planId = "a_plan_id" + String.valueOf(new Random().nextInt)

        val request = new PlanRequest().billingDayOfMonth(1).billingFrequency(1).currencyIsoCode("USD").
          description("java test description").id(planId).name("java test plan").numberOfBillingCycles(12).
          price(new BigDecimal("100.00")).trialDuration(1).trialDurationUnit(Plan.DurationUnit.DAY).trialPeriod(false)
        http.post("/plans/create_plan_for_tests", request)

        val addOnRequest = new FakeModificationRequest().amount(new BigDecimal("100.00")).
          description("java test add-on description").kind("add_on").name("java test add-on name").neverExpires(false).
          numberOfBillingCycles(12).planId(planId)
        http.post("/modifications/create_modification_for_tests", addOnRequest)

        val discountRequest = new FakeModificationRequest().amount(new BigDecimal("100.00")).
          description("java test add-on description").kind("discount").name("java test discount name").
          neverExpires(false).numberOfBillingCycles(12).planId(planId)
        http.post("/modifications/create_modification_for_tests", discountRequest)

        val plans = gateway.plan.all
        val actualPlan = plans.filter(_.getId == planId).headOption.get

        actualPlan.getBillingDayOfMonth must be === (Integer valueOf 1)
        actualPlan.getBillingFrequency must be === (Integer valueOf 1)
        actualPlan.getCurrencyIsoCode must be === "USD"
        actualPlan.getDescription must be === "java test description"
        actualPlan.getName must be === "java test plan"
        actualPlan.getNumberOfBillingCycles must be === (Integer valueOf 12)
        actualPlan.getPrice must be === new BigDecimal("100.00")
        actualPlan.getTrialDuration must be === (Integer valueOf 1)
        actualPlan.getTrialDurationUnit must be === Plan.DurationUnit.DAY
        actualPlan.hasTrialPeriod must be === false

        val addOn = actualPlan.getAddOns.get(0)
        addOn.amount must be === new BigDecimal("100.00")
        addOn.kind must be === "add_on"

        val discount = actualPlan.getDiscounts.get(0)
        discount.amount must be === new BigDecimal("100.00")
        discount.kind must be === "discount"
    }
  }
}