package net.bhardy.braintree.scala

import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.FunSpec
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.scalatest.matchers.MustMatchers

import scala.math.BigDecimal
import net.bhardy.braintree.scala.Subscriptions.Status
import search.SubscriptionSearchRequest


@RunWith(classOf[JUnitRunner])
class SubscriptionSearchRequestSpec extends FunSpec with MustMatchers {
  describe("toXml") {
    it("daysPastDueXmlIsOperator") {
      val request = new SubscriptionSearchRequest().daysPastDue.is("42")
      val xml = request.toXml.get
      (xml \ "days_past_due" \ "is").text must be === "42"
    }

    it("daysPastDueXmlBetweenOperator") {
      val expected = <search><days_past_due><min>5</min><max>7</max></days_past_due></search>
      val actual = new SubscriptionSearchRequest().daysPastDue.between(5, 7).toXml
      actual.get must be === expected
    }

    it("daysPastDueXmlGreaterThanOrEqualOperator") {
      val expected = <search><days_past_due><min>42</min></days_past_due></search>
      val actual = new SubscriptionSearchRequest().daysPastDue.greaterThanOrEqualTo(42).toXml
      actual.get must be === expected
    }

    it("daysPastDueXmlLessThanOrEqualOperator") {
      val expected = <search><days_past_due><max>42</max></days_past_due></search>
      val actual = new SubscriptionSearchRequest().daysPastDue.lessThanOrEqualTo(42).toXml
      actual.get must be === expected
    }

    it("billingCyclesRemainingIsOperator") {
      val expected = <search><billing_cycles_remaining><is>42</is></billing_cycles_remaining></search>
      val actual = new SubscriptionSearchRequest().billingCyclesRemaining.is(42).toXml
      actual.get must be === expected
    }

    it("billingCyclesRemainingBetweenOperator") {
      val expected = <search><billing_cycles_remaining><min>1</min><max>2</max></billing_cycles_remaining></search>
      val actual = new SubscriptionSearchRequest().billingCyclesRemaining.between(1, 2).toXml
      actual.get must be === expected
    }

    it("billingCyclesRemainingLessThanOrEqualOperator") {
      val expected = <search><billing_cycles_remaining><max>42</max></billing_cycles_remaining></search>
      val actual = new SubscriptionSearchRequest().billingCyclesRemaining.lessThanOrEqualTo(42).toXml
      actual.get must be === expected
    }

    it("billingCyclesRemainingGreaterThanOrEqualOperator") {
      val expected = <search><billing_cycles_remaining><min>42</min></billing_cycles_remaining></search>
      val actual = new SubscriptionSearchRequest().billingCyclesRemaining.greaterThanOrEqualTo(42).toXml
      actual.get must be === expected
    }

    it("idXmlIsOperator") {
      val expected = <search><id><is>42</is></id></search>
      val actual = new SubscriptionSearchRequest().id.is("42").toXml
      actual.get must be === expected
    }

    it("idXmlIsNotOperator") {
      val expected = <search><id><is_not>42</is_not></id></search>
      val actual = new SubscriptionSearchRequest().id.isNot("42").toXml
      actual.get must be === expected
    }

    it("idXmlStartsWithOperator") {
      val expected = <search><id><starts_with>42</starts_with></id></search>
      val actual = new SubscriptionSearchRequest().id.startsWith("42").toXml
      actual.get must be === expected
    }

    it("idXmlEndsWithOperator") {
      val expected = <search><id><ends_with>42</ends_with></id></search>
      val actual = new SubscriptionSearchRequest().id.endsWith("42").toXml
      actual.get must be === expected
    }

    it("idXmlContainsOperator") {
      val expected = <search><id><contains>42</contains></id></search>
      val actual = new SubscriptionSearchRequest().id.contains("42").toXml
      actual.get must be === expected
    }

    it("merchantAccountIdXmlIsOperator") {
      val expected = <search><merchant_account_id type="array"><item>42</item></merchant_account_id></search>
      val actual = new SubscriptionSearchRequest().merchantAccountId.is("42").toXml
      actual.get must be === expected
    }

    it("merchantAccountIdXmlInVarargsOperator") {
      val expected = <search><merchant_account_id type="array"><item>42</item><item>43</item></merchant_account_id></search>
      val actual = new SubscriptionSearchRequest().merchantAccountId.in("42", "43").toXml
      actual.get must be === expected
    }

    it("merchantAccountIdXmlInListOperator") {
      val items = List("42", "43")
      val expected = <search><merchant_account_id type="array"><item>42</item><item>43</item></merchant_account_id></search>
      val actual = new SubscriptionSearchRequest().merchantAccountId.in(items).toXml
      actual.get must be === expected
    }

    it("planIdXmlIsOperator") {
      val expected = <search><plan_id><is>42</is></plan_id></search>
      val actual = new SubscriptionSearchRequest().planId.is("42").toXml
      actual.get must be === expected
    }

    it("planIdXmlIsNotOperator") {
      val expected = <search><plan_id><is_not>42</is_not></plan_id></search>
      val actual = new SubscriptionSearchRequest().planId.isNot("42").toXml
      actual.get must be === expected
    }

    it("planIdXmlStartsWithOperator") {
      val expected = <search><plan_id><starts_with>42</starts_with></plan_id></search>
      val actual = new SubscriptionSearchRequest().planId.startsWith("42").toXml
      actual.get must be === expected
    }

    it("planIdXmlEndsWithOperator") {
      val expected = <search><plan_id><ends_with>42</ends_with></plan_id></search>
      val actual = new SubscriptionSearchRequest().planId.endsWith("42").toXml
      actual.get must be === expected
    }

    it("planIdXmlContainsOperator") {
      val expected = <search><plan_id><contains>42</contains></plan_id></search>
      val actual = new SubscriptionSearchRequest().planId.contains("42").toXml
      actual.get must be === expected
    }

    it("plantIdXmlInVarargsOperator") {
      val expected = <search><plan_id type="array"><item>42</item><item>43</item></plan_id></search>
      val actual = new SubscriptionSearchRequest().planId.in("42", "43").toXml
      actual.get must be === expected
    }

    it("planIdXmlInListOperator") {
      val items = List("42", "43")
      val expected = <search><plan_id type="array"><item>42</item><item>43</item></plan_id></search>
      val actual = new SubscriptionSearchRequest().planId.in(items).toXml
      actual.get must be === expected
    }

    it("priceXmlBetweenOperator") {
      val expected = <search><price><min>5</min><max>15</max></price></search>
      val actual = new SubscriptionSearchRequest().price.between(BigDecimal(5), BigDecimal(15)).toXml
      actual.get must be === expected
    }

    it("priceXmlDeprecatedGreaterThanOrEqualOperator") {
      val expected = <search><price><min>5</min></price></search>
      val actual = new SubscriptionSearchRequest().price.greaterThanOrEqualTo(BigDecimal(5)).toXml
      actual.get must be === expected
    }

    it("priceXmlGreaterThanOrEqualToOperator") {
      val expected = <search><price><min>5</min></price></search>
      val actual = new SubscriptionSearchRequest().price.greaterThanOrEqualTo(BigDecimal(5)).toXml
      actual.get must be === expected
    }

    it("priceXmlDeprecatedLessThanOrEqualOperator") {
      val expected = <search><price><max>5</max></price></search>
      val actual = new SubscriptionSearchRequest().price.lessThanOrEqualTo(BigDecimal(5)).toXml
      actual.get must be === expected
    }

    it("priceXmlLessThanOrEqualToOperator") {
      val expected = <search><price><max>5</max></price></search>
      val actual = new SubscriptionSearchRequest().price.lessThanOrEqualTo(BigDecimal(5)).toXml
      actual.get must be === expected
    }

    it("priceXmlIsOperator") {
      val expected = <search><price><is>5</is></price></search>
      val actual = new SubscriptionSearchRequest().price.is(BigDecimal(5)).toXml
      actual.get must be === expected
    }

    it("toXmlEscapesXmlOnTextNodes") {
      val expected = <search><days_past_due><is>&lt;test&gt;</is></days_past_due></search>
      val actual = new SubscriptionSearchRequest().daysPastDue.is("<test>").toXml
      actual.get.toString must be === expected.toString
    }

    it("toXmlLEscapesXmlOnMultipleValueNodes") {
      val expected = <search><ids type="array"><item>&lt;a</item><item>b&amp;</item></ids></search>
      val actual = new SubscriptionSearchRequest().ids.in("<a", "b&").toXml
      actual.get.toString must be === expected.toString
    }

    it("statusReturnsCorrectStringRepresentation") {
      val expected = <search><status type="array"><item>Active</item><item>Canceled</item><item>Past Due</item></status></search>
      val statuses = List(Status.ACTIVE, Status.CANCELED, Status.PAST_DUE)
      val actual = new SubscriptionSearchRequest().status.in(statuses).toXml
      actual.get.toString must be === expected.toString
    }
  }
}