package com.braintreegateway.integrationtest

import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import com.braintreegateway._
import com.braintreegateway.exceptions.NotFoundException
import java.util.Calendar

class AddressSpec extends FunSpec with MustMatchers {
  def createGateway = {
    new BraintreeGateway(Environment.DEVELOPMENT, "integration_merchant_id", "integration_public_key", "integration_private_key")
  }

  describe("create") {
    it("fills expected fields") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new AddressRequest().firstName("Joe").lastName("Smith").company("Smith Co.").
        streetAddress("1 E Main St").extendedAddress("Unit 2").locality("Chicago").region("Illinois").
        postalCode("60607").countryName("United States of America").countryCodeAlpha2("US").
        countryCodeAlpha3("USA").countryCodeNumeric("840")
      val createResult = gateway.address.create(customer.getId, request)
      createResult must be('success)
      val address = createResult.getTarget
      address.getFirstName must be === "Joe"
      address.getLastName must be === "Smith"
      address.getCompany must be === "Smith Co."
      address.getStreetAddress must be === "1 E Main St"
      address.getExtendedAddress must be === "Unit 2"
      address.getLocality must be === "Chicago"
      address.getRegion must be === "Illinois"
      address.getPostalCode must be === "60607"
      address.getCountryName must be === "United States of America"
      address.getCountryCodeAlpha2 must be === "US"
      address.getCountryCodeAlpha3 must be === "USA"
      address.getCountryCodeNumeric must be === "840"
      address.getCreatedAt.get(Calendar.YEAR) must be === Calendar.getInstance.get(Calendar.YEAR)
      address.getUpdatedAt.get(Calendar.YEAR) must be === Calendar.getInstance.get(Calendar.YEAR)
    }

  }

  describe("update") {
    it("changes expected fields") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new AddressRequest().streetAddress("1 E Main St").extendedAddress("Unit 2").
        locality("Chicago").region("Illinois").postalCode("60607").countryName("United States of America")
      val result = gateway.address.create(customer.getId, request)
      result must be('success)
      val address = result.getTarget
      val updateRequest = new AddressRequest().streetAddress("2 E Main St").extendedAddress("Unit 3").
        locality("Bartlett").region("Mass").postalCode("12345").countryName("Mexico").countryCodeAlpha2("MX").
        countryCodeAlpha3("MEX").countryCodeNumeric("484")

      val updateResult = gateway.address.update(address.getCustomerId, address.getId, updateRequest)
      updateResult must be('success)

      val updatedAddress = updateResult.getTarget
      updatedAddress.getStreetAddress must be === "2 E Main St"
      updatedAddress.getExtendedAddress must be === "Unit 3"
      updatedAddress.getLocality must be === "Bartlett"
      updatedAddress.getRegion must be === "Mass"
      updatedAddress.getPostalCode must be === "12345"
      updatedAddress.getCountryName must be === "Mexico"
      updatedAddress.getCountryCodeAlpha2 must be === "MX"
      updatedAddress.getCountryCodeAlpha3 must be === "MEX"
      updatedAddress.getCountryCodeNumeric must be === "484"
    }

  }

  describe("find") {
    it("return address in happy case") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new AddressRequest().streetAddress("1 E Main St")
      val createResult = gateway.address.create(customer.getId, request)
      createResult must be('success)
      val address = createResult.getTarget

      val foundAddress = gateway.address.find(address.getCustomerId, address.getId)

      foundAddress.getStreetAddress must be === "1 E Main St"
    }

    it("throws NotFoundException on empty ids") {
      val gateway = createGateway

      intercept[NotFoundException] {
        gateway.address.find(" ", "address_id")
      }
      intercept[NotFoundException] {
        gateway.address.find("customer_id", " ")
      }
    }
  }

  describe("delete") {
    it("causes an address to be deleted") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new AddressRequest().streetAddress("1 E Main St").extendedAddress("Unit 2").
        locality("Chicago").region("Illinois").postalCode("60607").countryName("United States of America")

      val createResult = gateway.address.create(customer.getId, request)
      createResult must be('success)
      val address = createResult.getTarget

      val deleteResult = gateway.address.delete(address.getCustomerId, address.getId)

      deleteResult must be('success)
      intercept[NotFoundException] {
        gateway.address.find(address.getCustomerId, address.getId)
      }
    }
  }

  describe("validation errors on create") {
    it("detects inconsistent country & country code") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new AddressRequest().countryName("Tunisia").countryCodeAlpha2("US")

      val createResult = gateway.address.create(customer.getId, request)

      createResult must not be ('success)
      createResult.getTarget must be === null
      val errors = createResult.getErrors
      val code = errors.forObject("address").onField("base").get(0).getCode
      code must be === ValidationErrorCode.ADDRESS_INCONSISTENT_COUNTRY
    }

    it("detects CountryCodeAlpha2") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new AddressRequest().countryCodeAlpha2("ZZ")

      val createResult = gateway.address.create(customer.getId, request)

      createResult must not be ('success)
      createResult.getTarget must be === null
      val errors = createResult.getErrors
      val code = errors.forObject("address").onField("countryCodeAlpha2").get(0).getCode
      code must be === ValidationErrorCode.ADDRESS_COUNTRY_CODE_ALPHA2_IS_NOT_ACCEPTED
    }

    it("detects invalid CountryCodeAlpha3") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new AddressRequest().countryCodeAlpha3("ZZZ")

      val createResult = gateway.address.create(customer.getId, request)

      createResult must not be ('success)
      createResult.getTarget must be === null
      val errors = createResult.getErrors
      val code = errors.forObject("address").onField("countryCodeAlpha3").get(0).getCode
      code must be === ValidationErrorCode.ADDRESS_COUNTRY_CODE_ALPHA3_IS_NOT_ACCEPTED
    }

    it("detects invalid CountryCodeNumeric") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new AddressRequest().countryCodeNumeric("000")

      val createResult = gateway.address.create(customer.getId, request)

      createResult must not be ('success)
      createResult.getTarget must be === null
      val errors = createResult.getErrors
      val code = errors.forObject("address").onField("countryCodeNumeric").get(0).getCode
      code must be === ValidationErrorCode.ADDRESS_COUNTRY_CODE_NUMERIC_IS_NOT_ACCEPTED
    }

  }

  describe("error parameters") {
    it("provides error parameters") {
      val gateway = createGateway

      val customer = gateway.customer.create(new CustomerRequest).getTarget
      val request = new AddressRequest().countryName("United States of Hammer")

      val createResult = gateway.address.create(customer.getId, request)

      createResult must not be ('success)
      val parameters = createResult.getParameters
      parameters.get("merchant_id") must be === "integration_merchant_id"
      parameters.get("customer_id") must be === customer.getId
      parameters.get("address[country_name]") must be === "United States of Hammer"
    }
  }
}