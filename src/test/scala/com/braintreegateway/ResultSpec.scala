package com.braintreegateway

import _root_.org.scalatest.{FunSpec, Inside}
import _root_.org.scalatest.matchers.MustMatchers
import gw.{Deleted, Success, Failure}
import com.braintreegateway.ValidationErrors.NoValidationErrors

/**
 */
class ResultSpec extends FunSpec with MustMatchers with Inside {

  describe("Failure") {
    it("is not successful") {
      val result = Failure(NoValidationErrors, Map.empty, "", None, None, None)
      result.isSuccess must be === false
    }

    it("composes to Failure") {
      val result = Failure(NoValidationErrors, Map.empty, "", None, None, None)
      val result2 = result.flatMap { _ => Success(5) }
      result2.isSuccess must be === false
      result2 match {
        case Failure(_, _, _, _, _, _) => // ok
        case other => fail("expected failure, got " + other)
      }
    }
  }

  describe("Failure") {
    it("is successful") {
      val result = Deleted
      result.isSuccess must be === true
    }

    it("composes to Deleted") {
      val result = Deleted
      val result2 = result.flatMap { _ => Success(5) }
      result2.isSuccess must be === true
      result2 match {
        case Deleted => // ok
        case other => fail("expected Deleted, got " + other)
      }
    }
  }

  describe("Success") {
    it("is successful") {
      val result = Success(5)
      result.isSuccess must be === true
    }

    it("holds a result target") {
      val result = Success(5)
      result.target must be === 5
    }

    it("composes with other Successes to Success") {
      val result = Success("6")
      val result2 = result.flatMap { _ => Success("OK") }
      result2.isSuccess must be === true
      result2 match {
        case Success("OK") => // ok
        case other => fail("expected Sucesss, got " + other)
      }
    }

    it("composes with Failure to produce Failure") {
      val result = Success("Start OK")
      val result2 = result.flatMap { _ => Failure(NoValidationErrors, Map.empty, "FAIL", None, None, None) }
      result2.isSuccess must be === false
      result2 match {
        case Failure(_, _, "FAIL", _, _, _) => // ok
        case other => fail("expected Failure, got " + other)
      }
    }
  }

}
