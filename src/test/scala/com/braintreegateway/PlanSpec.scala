package com.braintreegateway

import _root_.org.scalatest.FunSpec
import _root_.org.scalatest.matchers.MustMatchers
import util.NodeWrapperFactory

/**
 */
class PlanSpec extends FunSpec with MustMatchers {
  describe("constructor") {
    it("uses UNRECOGNIZED for unknown trial duration units") {
      val xml = <plan><trial-duration-unit>second</trial-duration-unit></plan>
      val node = NodeWrapperFactory.create(xml.toString)
      val plan = new Plan(node)
      plan.trialDurationUnit must be === Plan.DurationUnit.UNRECOGNIZED
    }
    it("uses UNDEFINED for missing trial duration units") {
      val xml = <plan></plan>
      val node = NodeWrapperFactory.create(xml.toString)
      val plan = new Plan(node)
      plan.trialDurationUnit must be === Plan.DurationUnit.UNDEFINED
    }
  }

}
