package com.braintreegateway.util

import com.braintreegateway.Transaction
import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EnumUtilsSpec extends FunSpec with MustMatchers {
  describe("findByName") {
    it("returns null for null") {
      EnumUtils.findByName(classOf[Transaction.Status], null) must be === null
    }

    it("returns exact matches") {
      EnumUtils.findByName(classOf[Transaction.Status], "AUTHORIZED") must be === Transaction.Status.AUTHORIZED
    }

    it("is case insensitive") {
      EnumUtils.findByName(classOf[Transaction.Type], "saLE") must be === Transaction.Type.SALE
    }

    it("defaults to UNRECOGNIZED if name does not match") {
      EnumUtils.findByName(classOf[Transaction.Status], "blah") must be === Transaction.Status.UNRECOGNIZED
      EnumUtils.findByName(classOf[Transaction.Type], "blah") must be === Transaction.Type.UNRECOGNIZED
    }
  }
}