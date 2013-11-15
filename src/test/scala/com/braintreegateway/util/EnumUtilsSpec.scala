package com.braintreegateway.util

import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.FunSpec
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.scalatest.matchers.MustMatchers
import com.braintreegateway._
import com.braintreegateway.Plan.DurationUnit
import com.braintreegateway.Transactions._

@RunWith(classOf[JUnitRunner])
class EnumUtilsSpec extends FunSpec with MustMatchers {
  describe("interactions with Transaction.Status") {
    it("returns UNDEFINED for null") {
      EnumUtils.findByName(classOf[Status], null) must be === Transactions.Status.UNDEFINED
    }

    it("returns UNRECOGNIZED for anything we don't know about") {
      EnumUtils.findByName(classOf[Status], "CHICKENIZED") must be === Transactions.Status.UNRECOGNIZED
    }

    it("returns exact matches") {
      EnumUtils.findByName(classOf[Status], "AUTHORIZED") must be === Transactions.Status.AUTHORIZED
    }

    it("is case insensitive") {
      EnumUtils.findByName(classOf[Type], "saLE") must be === Transactions.Type.SALE
    }

    it("defaults to UNRECOGNIZED if name does not match") {
      EnumUtils.findByName(classOf[Status], "blah") must be === Transactions.Status.UNRECOGNIZED
      EnumUtils.findByName(classOf[Type], "blah") must be === Transactions.Type.UNRECOGNIZED
    }
  }

  describe("interactions with DurationUnit") {
    it("defaults to UNRECOGNIZED for garbage values") {
      DurationUnit.fromString("DERP") must be === DurationUnit.UNRECOGNIZED
    }
    it("finds known values even if lowercase") {
      DurationUnit.fromString("day") must be === DurationUnit.DAY
    }
    it("finds known values  if uppercase") {
      DurationUnit.fromString("MONTH") must be === DurationUnit.MONTH
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
      EnumUtils.findByName(classOf[GatewayRejectionReason], null) must be === Transactions.GatewayRejectionReason.UNDEFINED
    }
    it("defaults to UNRECOGNIZED for garbage values") {
      EnumUtils.findByName(classOf[GatewayRejectionReason], "DERP") must be === Transactions.GatewayRejectionReason.UNRECOGNIZED
    }
    it("finds known values") {
      EnumUtils.findByName(classOf[GatewayRejectionReason], "avs") must be === Transactions.GatewayRejectionReason.AVS
    }
  }
  describe("interactions with Transaction.EscrowStatus") {
    it("defaults to UNDEFINED for null values") {
      EnumUtils.findByName(classOf[EscrowStatus], null) must be === Transactions.EscrowStatus.UNDEFINED
    }
    it("defaults to UNRECOGNIZED for garbage values") {
      EnumUtils.findByName(classOf[EscrowStatus], "DERP") must be === Transactions.EscrowStatus.UNRECOGNIZED
    }
    it("finds known values") {
      EnumUtils.findByName(classOf[EscrowStatus], "HELD") must be === Transactions.EscrowStatus.HELD
    }
  }
  describe("interactions with Transaction.Source") {
    it("defaults to UNDEFINED for null values") {
      EnumUtils.findByName(classOf[Source], null) must be === Transactions.Source.UNDEFINED
    }
    // might not be meaningful
    it("defaults to UNRECOGNIZED for garbage values") {
      EnumUtils.findByName(classOf[Source], "DERP") must be === Transactions.Source.UNRECOGNIZED
    }
    it("finds known values") {
      EnumUtils.findByName(classOf[Source], "api") must be === Transactions.Source.API
    }
  }
  describe("interactions with Transaction.Type") {
    it("defaults to UNDEFINED for null values") {
      EnumUtils.findByName(classOf[Type], null) must be === Transactions.Type.UNDEFINED
    }
    // might not be meaningful
    it("defaults to UNRECOGNIZED for garbage values") {
      EnumUtils.findByName(classOf[Type], "DERP") must be === Transactions.Type.UNRECOGNIZED
    }
    it("finds known values") {
      EnumUtils.findByName(classOf[Type], "sale") must be === Transactions.Type.SALE
    }
  }
}