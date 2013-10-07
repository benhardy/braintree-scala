package com.braintreegateway.testhelpers

import com.braintreegateway.Plan
import com.braintreegateway.util.NodeWrapperFactory

object PlanFixture {
  val PLAN_WITHOUT_TRIAL = new Plan(
    NodeWrapperFactory.create(
      <plan>
        <billing-day-of-month>1</billing-day-of-month>
        <billing-frequency>1</billing-frequency>
        <currency-iso-code>USD</currency-iso-code>
        <description>java test plan</description>
        <id>integration_trialless_plan</id>
        <name>Plan for integration tests -- without a trial</name>
        <number-of-billing-cycles>12</number-of-billing-cycles>
        <price>12.34</price>
        <trial-duration>1</trial-duration>
        <trial-duration-unit>day</trial-duration-unit>
        <trial-period>false</trial-period>
      </plan>.toString
  ))

  val PLAN_WITH_TRIAL = new Plan(NodeWrapperFactory.create(
    <plan>
      <billing-day-of-month>1</billing-day-of-month>
      <billing-frequency>2</billing-frequency>
      <currency-iso-code>USD</currency-iso-code>
      <description>java test plan</description>
      <id>integration_trial_plan</id>
      <name>Plan for integration tests -- with a trial</name>
      <number-of-billing-cycles>12</number-of-billing-cycles>
      <price>43.21</price>
      <trial-duration>2</trial-duration>
      <trial-duration-unit>day</trial-duration-unit>
      <trial-period>true</trial-period>
    </plan>.toString
  ))

  val BILLING_DAY_OF_MONTH_PLAN = new Plan(NodeWrapperFactory.create(
    <plan>
      <billing-day-of-month>1</billing-day-of-month>
      <billing-frequency>2</billing-frequency>
      <currency-iso-code>USD</currency-iso-code>
      <description>java test plan</description>
      <id>integration_plan_with_billing_day_of_month</id>
      <name>Plan for integration tests -- with billing day of month</name>
      <number-of-billing-cycles>12</number-of-billing-cycles>
      <price>8.88</price>
      <trial-duration>1</trial-duration>
      <trial-duration-unit>day</trial-duration-unit>
      <trial-period>false</trial-period>
    </plan>.toString
  ))

  val ADD_ON_DISCOUNT_PLAN = new Plan(NodeWrapperFactory.create(
    <plan>
      <billing-day-of-month>1</billing-day-of-month>
      <billing-frequency>2</billing-frequency>
      <currency-iso-code>USD</currency-iso-code>
      <description>java test plan</description>
      <id>integration_plan_with_add_ons_and_discounts</id>
      <name>Plan for integration tests -- with add-ons and discounts</name>
      <number-of-billing-cycles>12</number-of-billing-cycles>
      <price>9.99</price>
      <trial-duration>1</trial-duration>
      <trial-duration-unit>day</trial-duration-unit>
      <trial-period>true</trial-period>
    </plan>.toString
  ))

}