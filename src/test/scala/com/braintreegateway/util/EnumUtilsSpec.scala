package com.braintreegateway.util

import com.braintreegateway.{CreditCards, CreditCard, Transaction}
import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.braintreegateway.Plan.DurationUnit

@RunWith(classOf[JUnitRunner])
class EnumUtilsSpec extends FunSpec with MustMatchers {
  describe("interactions with Transaction.Status") {
    it("returns UNDEFINED for null") {
      EnumUtils.findByName(classOf[Transaction.Status], null) must be === Transaction.Status.UNDEFINED
    }

    it("returns UNRECOGNIZED for anything we don't know about") {
      EnumUtils.findByName(classOf[Transaction.Status], "CHICKENIZED") must be === Transaction.Status.UNRECOGNIZED
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

  describe("interactions with DurationUnit") {
    it("defaults to UNDEFINED for null values") {
      EnumUtils.findByName(classOf[DurationUnit], null) must be === DurationUnit.UNDEFINED
    }
    it("defaults to UNRECOGNIZED for garbage values") {
      EnumUtils.findByName(classOf[DurationUnit], "DERP") must be === DurationUnit.UNRECOGNIZED
    }
    it("finds known values") {
      EnumUtils.findByName(classOf[DurationUnit], "day") must be === DurationUnit.DAY
    }
  }

  describe("interactions with CreditCard.CardType") {
    it("defaults to UNDEFINED for null values") {
      CreditCards.CardType.fromString(null) must be === CreditCards.CardType.UNDEFINED
    }
    it("defaults to UNRECOGNIZED for garbage values") {
      CreditCards.CardType.fromString("DERP") must be === CreditCards.CardType.UNRECOGNIZED
    }
    it("finds known values") {
      CreditCards.CardType.fromString("Visa") must be === CreditCards.CardType.VISA
    }
  }
  describe("interactions with Transaction.GatewayRejectionReason") {
    it("defaults to UNDEFINED for null values") {
      EnumUtils.findByName(classOf[Transaction.GatewayRejectionReason], null) must be === Transaction.GatewayRejectionReason.UNDEFINED
    }
    it("defaults to UNRECOGNIZED for garbage values") {
      EnumUtils.findByName(classOf[Transaction.GatewayRejectionReason], "DERP") must be === Transaction.GatewayRejectionReason.UNRECOGNIZED
    }
    it("finds known values") {
      EnumUtils.findByName(classOf[Transaction.GatewayRejectionReason], "avs") must be === Transaction.GatewayRejectionReason.AVS
    }
  }
  describe("interactions with Transaction.EscrowStatus") {
    it("defaults to UNDEFINED for null values") {
      EnumUtils.findByName(classOf[Transaction.EscrowStatus], null) must be === Transaction.EscrowStatus.UNDEFINED
    }
    it("defaults to UNRECOGNIZED for garbage values") {
      EnumUtils.findByName(classOf[Transaction.EscrowStatus], "DERP") must be === Transaction.EscrowStatus.UNRECOGNIZED
    }
    it("finds known values") {
      EnumUtils.findByName(classOf[Transaction.EscrowStatus], "HELD") must be === Transaction.EscrowStatus.HELD
    }
  }
  describe("interactions with Transaction.Source") {
    it("defaults to UNDEFINED for null values") {
      EnumUtils.findByName(classOf[Transaction.Source], null) must be === Transaction.Source.UNDEFINED
    }
    // might not be meaningful
    it("defaults to UNRECOGNIZED for garbage values") {
      EnumUtils.findByName(classOf[Transaction.Source], "DERP") must be === Transaction.Source.UNRECOGNIZED
    }
    it("finds known values") {
      EnumUtils.findByName(classOf[Transaction.Source], "api") must be === Transaction.Source.API
    }
  }
  describe("interactions with Transaction.Type") {
    it("defaults to UNDEFINED for null values") {
      EnumUtils.findByName(classOf[Transaction.Type], null) must be === Transaction.Type.UNDEFINED
    }
    // might not be meaningful
    it("defaults to UNRECOGNIZED for garbage values") {
      EnumUtils.findByName(classOf[Transaction.Type], "DERP") must be === Transaction.Type.UNRECOGNIZED
    }
    it("finds known values") {
      EnumUtils.findByName(classOf[Transaction.Type], "sale") must be === Transaction.Type.SALE
    }
  }
}