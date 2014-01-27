package net.bhardy.braintree.scala.util

import net.bhardy.braintree.scala.CreditCard
import net.bhardy.braintree.scala.Transaction
import java.io.ByteArrayInputStream
import org.scalatest.matchers.MustMatchers
import org.scalatest.FunSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class StringUtilsSpec extends FunSpec with MustMatchers {
  describe("underscore") {
    it("returns null for null") {
      StringUtils.underscore(null) must be === null
    }

    it("does nothing to already underscored string") {
      StringUtils.underscore("foo_bar") must be === "foo_bar"
    }

    it("separates camelcase") {
      StringUtils.underscore("firstName") must be === "first_name"
    }

    it("converts dasherized") {
      StringUtils.underscore("first-name") must be === "first_name"
    }

    it("separates pascal cased") {
      StringUtils.underscore("FirstName") must be === "first_name"
    }
  }

  describe("dasherize") {
    it("returns null for null") {
      StringUtils.dasherize(null) must be === null
    }

    it("does nothing to already dasherized string") {
      StringUtils.dasherize("foo-bar") must be === "foo-bar"
    }

    it("separates camelcased words with dashes") {
      StringUtils.dasherize("firstName") must be === "first-name"
    }

    it("separates pascal-cased words with dashes") {
      StringUtils.dasherize("FirstName") must be === "first-name"
    }

    it("converts underscores to dashes") {
      StringUtils.dasherize("first_name") must be === "first-name"
    }
  }

  describe("nullIfEmpty") {
    it("returns null for null") {
      StringUtils.nullIfEmpty(null) must be === null
    }

    it("returns original string if not null or empty") {
      StringUtils.nullIfEmpty("hello") must be === "hello"
    }

    it("returns null for empty string") {
      StringUtils.nullIfEmpty("") must be === null
    }
  }

  describe("streamToString") {
    it("works for single line strings") {
      val inputStream = new ByteArrayInputStream("hello world".getBytes)
      StringUtils.inputStreamToString(inputStream) must be === "hello world"
    }

    it("works for multi line strings") {
      val inputStream = new ByteArrayInputStream("foo\r\nbar\nbaz".getBytes)
      StringUtils.inputStreamToString(inputStream) must be === "foo\r\nbar\nbaz"
    }
  }

  describe("classToXMLName") {
    it("converts class names to xml-compatible name") {
      StringUtils.classToXMLName(classOf[CreditCard]) must be === "credit-card"
      StringUtils.classToXMLName(classOf[Transaction]) must be === "transaction"
    }
  }

  describe("join") {
    it("join at work") {
      var result = StringUtils.join(",", "one", "two", "three")
      result must be === "one,two,three"
      result = StringUtils.join(",", "one", "two", "three")
      result must be === "one,two,three"
      result = StringUtils.join(",")
      result must be === ""
      result = StringUtils.join(",", "one")
      result must be === "one"
    }
  }
}