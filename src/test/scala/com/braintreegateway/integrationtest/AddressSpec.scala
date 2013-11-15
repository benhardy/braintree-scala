package com.braintreegateway.integrationtest

import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.junit.JUnitRunner
import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import com.braintreegateway._
import com.braintreegateway.exceptions.NotFoundException
import testhelpers.GatewaySpec
import com.braintreegateway.gw.{Result, Success, Failure}
import com.braintreegateway.testhelpers.CalendarHelper._

@RunWith(classOf[JUnitRunner])
class AddressSpec extends FunSpec with MustMatchers with GatewaySpec {
  describe("create") {
    onGatewayIt("fills expected fields") {
      gateway =>
        val request = new AddressRequest().firstName("Joe").lastName("Smith").company("Smith Co.").
          streetAddress("1 E Main St").extendedAddress("Unit 2").locality("Chicago").region("Illinois").
          postalCode("60607").countryName("United States of America").countryCodeAlpha2("US").
          countryCodeAlpha3("USA").countryCodeNumeric("840")

        val createResult = for {
          customer <- gateway.customer.create(new CustomerRequest)
          address <- gateway.address.create(customer.getId, request)
        } yield address

        createResult match {
          case Success(address) => {
            address.firstName must be === "Joe"
            address.lastName must be === "Smith"
            address.company must be === "Smith Co."
            address.streetAddress must be === "1 E Main St"
            address.extendedAddress must be === "Unit 2"
            address.locality must be === "Chicago"
            address.region must be === "Illinois"
            address.postalCode must be === "60607"
            address.countryName must be === "United States of America"
            address.countryCodeAlpha2 must be === "US"
            address.countryCodeAlpha3 must be === "USA"
            address.countryCodeNumeric must be === "840"

            val thisYear = now.year
            address.createdAt.year must be === thisYear
            address.updatedAt.year must be === thisYear
          }
        }
    }

  }

  describe("update") {
    onGatewayIt("changes expected fields") {
      gateway =>
        val request = new AddressRequest().streetAddress("1 E Main St").extendedAddress("Unit 2").
          locality("Chicago").region("Illinois").postalCode("60607").countryName("United States of America")
        val updateRequest = new AddressRequest().streetAddress("2 E Main St").extendedAddress("Unit 3").
          locality("Bartlett").region("Mass").postalCode("12345").countryName("Mexico").countryCodeAlpha2("MX").
          countryCodeAlpha3("MEX").countryCodeNumeric("484")
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)
          address <- gateway.address.create(customer.getId, request)
          updated <- gateway.address.update(address.customerId, address.id, updateRequest)
        } yield updated

        result match {
          case Success(updatedAddress) => {
            updatedAddress.streetAddress must be === "2 E Main St"
            updatedAddress.extendedAddress must be === "Unit 3"
            updatedAddress.locality must be === "Bartlett"
            updatedAddress.region must be === "Mass"
            updatedAddress.postalCode must be === "12345"
            updatedAddress.countryName must be === "Mexico"
            updatedAddress.countryCodeAlpha2 must be === "MX"
            updatedAddress.countryCodeAlpha3 must be === "MEX"
            updatedAddress.countryCodeNumeric must be === "484"
          }
        }
    }
  }

  describe("find") {
    onGatewayIt("return address in happy case") {
      gateway =>
        val setup = for {
          customer <- gateway.customer.create(new CustomerRequest)
          request = new AddressRequest().streetAddress("1 E Main St")
          address <- gateway.address.create(customer.getId, request)
        } yield address

        setup match {
          case Success(address) => {
            val foundAddress = gateway.address.find(address.customerId, address.id)
            foundAddress.streetAddress must be === "1 E Main St"
          }
          case _ => fail("expected success")
        }
    }

    onGatewayIt("throws NotFoundException on empty ids") {
      gateway =>
        intercept[NotFoundException] {
          gateway.address.find(" ", "address_id")
        }
        intercept[NotFoundException] {
          gateway.address.find("customer_id", " ")
        }
    }
  }

  describe("delete") {
    onGatewayIt("causes an address to be deleted") {
      gateway =>
        val setup = for {
          customer <- gateway.customer.create(new CustomerRequest)
          request = new AddressRequest().streetAddress("1 E Main St").extendedAddress("Unit 2").
            locality("Chicago").region("Illinois").postalCode("60607").countryName("United States of America")

          address <- gateway.address.create(customer.getId, request)
        } yield address

        setup match {
          case Success(address) => {
            val deleteResult = gateway.address.delete(address.customerId, address.id)

            deleteResult must be('success)
            deleteResult must be === (Result.deleted)

            intercept[NotFoundException] {
              gateway.address.find(address.customerId, address.id)
            }
          }
          case _ => fail("test setup failure")
        }
    }
  }

  describe("validation errors on create") {
    onGatewayIt("detects inconsistent country & country code") {
      gateway =>
        val setup = for {
          customer <- gateway.customer.create(new CustomerRequest)
          request = new AddressRequest().countryName("Tunisia").countryCodeAlpha2("US")
          address <- gateway.address.create(customer.getId, request)
        } yield address
        setup match {
          case Failure(errors, _, _, _, _, _) => {
            val code = errors.forObject("address").onField("base")(0).code
            code must be === ValidationErrorCode.ADDRESS_INCONSISTENT_COUNTRY
          }
          case _ => fail("expected validation failure")
        }
    }

    onGatewayIt("detects CountryCodeAlpha2") {
      gateway =>

        val createResult = for {
          customer <- gateway.customer.create(new CustomerRequest)
          request = new AddressRequest().countryCodeAlpha2("ZZ")
          address <- gateway.address.create(customer.getId, request)
        } yield address

        createResult match {
          case Failure(errors, _, _, _, _, _) => {
            val code = errors.forObject("address").onField("countryCodeAlpha2")(0).code
            code must be === ValidationErrorCode.ADDRESS_COUNTRY_CODE_ALPHA2_IS_NOT_ACCEPTED
          }
          case _ => fail("expected failure")
        }
    }

    onGatewayIt("detects invalid CountryCodeAlpha3") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)
          request = new AddressRequest().countryCodeAlpha3("ZZZ")

          address <- gateway.address.create(customer.getId, request)
        } yield address

        result match {
          case Failure(errors, _, _, _, _, _) => {
            val code = errors.forObject("address").onField("countryCodeAlpha3")(0).code
            code must be === ValidationErrorCode.ADDRESS_COUNTRY_CODE_ALPHA3_IS_NOT_ACCEPTED
          }
          case _ => fail("expected failure")
        }
    }

    onGatewayIt("detects invalid CountryCodeNumeric") {
      gateway =>
        val result = for {
          customer <- gateway.customer.create(new CustomerRequest)
          request = new AddressRequest().countryCodeNumeric("000")

          address <- gateway.address.create(customer.getId, request)
        } yield address

        result match {
          case Failure(errors, _, _, _, _, _) => {
            val code = errors.forObject("address").onField("countryCodeNumeric")(0).code
            code must be === ValidationErrorCode.ADDRESS_COUNTRY_CODE_NUMERIC_IS_NOT_ACCEPTED
          }
          case _ => fail("expected failure")
        }
    }

  }

  describe("error parameters") {
    onGatewayIt("provides error parameters") {
      gateway =>
        val customer = gateway.customer.create(new CustomerRequest) match {
          case Success(c) => c
        }
        val request = new AddressRequest().countryName("United States of Hammer")
        val result = for {
          address <- gateway.address.create(customer.getId, request)
        } yield address

        result match {
          case Failure(_, parameters, _, _, _, _) => {
            parameters("merchant_id") must be === "integration_merchant_id"
            parameters("customer_id") must be === customer.getId
            parameters("address[country_name]") must be === "United States of Hammer"
          }
          case _ => fail("expected failure")
        }
    }
  }
}
