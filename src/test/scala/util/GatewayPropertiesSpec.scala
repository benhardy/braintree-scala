package net.bhardy.braintree.scala.util

import org.scalatest.matchers.{MatchResult, Matcher, MustMatchers}
import org.scalatest.FunSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import net.bhardy.braintree.scala.gw.BraintreeGateway

@RunWith(classOf[JUnitRunner])
class GatewayPropertiesSpec extends FunSpec with MustMatchers {

  private val VERSION_PATTERN = "^\\d+([.]\\d+)*(-SNAPSHOT)?$".r

  val beValidVersion = Matcher {
    (version: String) => {
      val isValid = VERSION_PATTERN.findFirstIn(version).isDefined
      val failMsg = s"'${version}' is not a valid version string"
      val negated = s"'${version}' is a valid version string"
      MatchResult(isValid, failMsg, negated)
    }
  }

  describe("beValidVersion") {
    it("matches valid version strings") {
      "1" must beValidVersion
      "52.2" must beValidVersion
      "6.74.3" must beValidVersion
      "4.9-SNAPSHOT" must beValidVersion
      "1.1.17-SNAPSHOT" must beValidVersion
    }

    it("doesn't match invalid version strings") {
      "1.1.17-SNAPSHOT-SNAPSHOT" must not (beValidVersion)
      "1.1-chicken" must not (beValidVersion)
      "1." must not (beValidVersion)
      ".1" must not (beValidVersion)
      "junk1.1" must not (beValidVersion)
      "1.1-junk" must not (beValidVersion)
    }
  }

  describe("ClientLibraryProperties") {
    describe("version") {
      it("can retrieve a valid version number") {
        val version = new ClientLibraryProperties().version
        version must beValidVersion
      }

      it("is used by gateway") {
        val gatewayVersion = BraintreeGateway.VERSION
        val propertiesVersion = new ClientLibraryProperties().version
        gatewayVersion must be === propertiesVersion
      }
    }
  }
}