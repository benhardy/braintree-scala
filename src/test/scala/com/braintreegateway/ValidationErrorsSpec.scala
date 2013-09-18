package com.braintreegateway

import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.FunSpec
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.scalatest.matchers.MustMatchers
import com.braintreegateway.util.NodeWrapperFactory
import scala.collection.JavaConversions._

import testhelpers.XmlHelper.xmlAsStringWithHeader


@RunWith(classOf[JUnitRunner])
class ValidationErrorsSpec extends FunSpec with MustMatchers {
  describe("codes") {
    it("onField") {
      val errors = new ValidationErrors
      errors.addError(new ValidationError("country_name", ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED, "invalid country"))
      errors.onField("countryName").get(0).getCode must be === ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED
      errors.onField("countryName").get(0).getMessage must be === "invalid country"
    }
  
    it("onFieldAlsoWorksWithUnderscores") {
      val errors = new ValidationErrors
      errors.addError(new ValidationError("country_name", ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED, "invalid country"))
      errors.onField("country_name").get(0).getCode must be === ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED
      errors.onField("country_name").get(0).getMessage must be === "invalid country"
    }
  
    it("nonExistingField") {
      val errors = new ValidationErrors
      errors.onField("foo") must be ('empty)
    }
  
    it("forObject") {
      val addressErrors = new ValidationErrors
      addressErrors.addError(new ValidationError("country_name", ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED, "invalid country"))
      val errors = new ValidationErrors
      errors.addErrors("address", addressErrors)
      errors.forObject("address").onField("countryName").get(0).getCode must be === ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED
      errors.forObject("address").onField("country_name").get(0).getMessage must be === "invalid country"
    }
  
    it("forObjectOnNonExistingObject") {
      val errors = new ValidationErrors
      errors.forObject("invalid").size must be === 0
    }
  
    it("forObjectAlsoWorksWithUnderscores") {
      val addressErrors = new ValidationErrors
      addressErrors.addError(new ValidationError("name", ValidationErrorCode.ADDRESS_FIRST_NAME_IS_TOO_LONG, "invalid name"))
      val errors = new ValidationErrors
      errors.addErrors("billing-address", addressErrors)
      errors.forObject("billing_address").onField("name").get(0).getCode must be === ValidationErrorCode.ADDRESS_FIRST_NAME_IS_TOO_LONG
    }
  }
  describe("size") {
    it("reports correct size") {
      val errors = new ValidationErrors
      errors.addError(new ValidationError("countryName", ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED, "invalid country"))
      errors.addError(new ValidationError("anotherField", ValidationErrorCode.ADDRESS_COMPANY_IS_TOO_LONG, "another message"))
      errors.size must be === 2
    }
  }
  describe("deepSize") {
    it("reports correct deepSize") {
      val addressErrors = new ValidationErrors
      addressErrors.addError(new ValidationError("countryName", ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED, "invalid country"))
      addressErrors.addError(new ValidationError("anotherField", ValidationErrorCode.ADDRESS_COMPANY_IS_TOO_LONG, "another message"))
      val errors = new ValidationErrors
      errors.addError(new ValidationError("someField", ValidationErrorCode.ADDRESS_EXTENDED_ADDRESS_IS_TOO_LONG, "some message"))
      errors.addErrors("address", addressErrors)
      errors.deepSize must be === 3
      errors.size must be === 1
      errors.forObject("address").deepSize must be === 2
      errors.forObject("address").size must be === 2
    }
  }

  describe("getting all validation errors") {
    it("getAllValidationErrors") {
      val addressErrors = new ValidationErrors
      addressErrors.addError(new ValidationError("countryName", ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED, "invalid country"))
      addressErrors.addError(new ValidationError("anotherField", ValidationErrorCode.ADDRESS_COMPANY_IS_TOO_LONG, "another message"))
      val errors = new ValidationErrors
      errors.addError(new ValidationError("someField", ValidationErrorCode.ADDRESS_FIRST_NAME_IS_TOO_LONG, "some message"))
      errors.addErrors("address", addressErrors)
      errors.getAllValidationErrors.size must be === 1
      errors.getAllValidationErrors.get(0).getCode must be === ValidationErrorCode.ADDRESS_FIRST_NAME_IS_TOO_LONG
    }

    it("getAllDeepValidationErrors") {
      val addressErrors = new ValidationErrors
      addressErrors.addError(new ValidationError("countryName", ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED, "1"))
      addressErrors.addError(new ValidationError("anotherField", ValidationErrorCode.ADDRESS_COMPANY_IS_TOO_LONG, "2"))
      val errors = new ValidationErrors
      errors.addError(new ValidationError("someField", ValidationErrorCode.ADDRESS_FIRST_NAME_IS_TOO_LONG, "3"))
      errors.addErrors("address", addressErrors)
      errors.getAllDeepValidationErrors.size must be === 3
      val validationErrors = errors.getAllDeepValidationErrors.sortWith((a,b) => a.getCode.compareTo(b.getCode) < 0)

      validationErrors.get(0).getCode must be === ValidationErrorCode.ADDRESS_COMPANY_IS_TOO_LONG
      validationErrors.get(1).getCode must be === ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED
      validationErrors.get(2).getCode must be === ValidationErrorCode.ADDRESS_FIRST_NAME_IS_TOO_LONG
    }
  }

  describe("validation parsing") {

    it("parseSimpleValidationErrors") {
      val xml = <api-error-response>
        <errors>
          <address>
            <errors type="array">
              <error>
                <code>91803</code>
                <message>Country name is not an accepted country.</message>
                <attribute type="symbol">country_name</attribute>
              </error>
            </errors>
          </address>
          <errors type="array"/>
        </errors>
      </api-error-response>

      val errors = new ValidationErrors(NodeWrapperFactory.instance.create(xmlAsStringWithHeader(xml)))
      errors.deepSize must be === 1
      errors.forObject("address").onField("country_name").get(0).getCode must be === ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED
      errors.forObject("address").onField("countryName").get(0).getCode must be === ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED
    }

    it("parseMulitpleValidationErrorsOnOneObject") {
      val xml = <api-error-response>
        <errors>
          <address>
            <errors type="array">
              <error>
                <code>91803</code>
                <message>Country name is not an accepted country.</message>
                <attribute type="symbol">country_name</attribute>
              </error>
              <error>
                <code>81812</code>
                <message>Street address is too long.</message>
                <attribute type="symbol">street_address</attribute>
              </error>
            </errors>
          </address>
          <errors type="array"/>
        </errors>
      </api-error-response>

      val errors = new ValidationErrors(NodeWrapperFactory.instance.create(xmlAsStringWithHeader(xml)))
      errors.deepSize must be === 2
      errors.forObject("address").onField("countryName").get(0).getCode must be === ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED
      errors.forObject("address").onField("streetAddress").get(0).getCode must be === ValidationErrorCode.ADDRESS_STREET_ADDRESS_IS_TOO_LONG
    }

    it("parseMulitpleValidationErrorsOnOneField") {
      val xml = <api-error-response>
        <errors>
          <transaction>
            <errors type="array">
              <error>
                <code>91516</code>
                <message>Cannot provide both payment_method_token and customer_id unless the payment_method belongs to the customer.</message>
                <attribute type="symbol">base</attribute>
              </error>
              <error>
                <code>91515</code>
                <message>Cannot provide both payment_method_token and credit_card attributes.</message>
                <attribute type="symbol">base</attribute>
              </error>
            </errors>
          </transaction>
          <errors type="array"/>
        </errors>
      </api-error-response>

      val errors = new ValidationErrors(NodeWrapperFactory.instance.create(xmlAsStringWithHeader(xml)))
      errors.deepSize must be === 2
      errors.forObject("transaction").onField("base").size must be === 2
    }

    it("parseValidationErrorOnNestedObject") {
      val xml = <api-error-response>
        <errors>
          <errors type="array"/>
          <credit-card>
            <billing-address>
              <errors type="array">
                <error>
                  <code>91803</code>
                  <message>Country name is not an accepted country.</message>
                  <attribute type="symbol">country_name</attribute>
                </error>
              </errors>
            </billing-address>
            <errors type="array"/>
          </credit-card>
        </errors>
      </api-error-response>

      val errors = new ValidationErrors(NodeWrapperFactory.instance.create(xmlAsStringWithHeader(xml)))
      errors.deepSize must be === 1
      errors.forObject("creditCard").forObject("billingAddress").onField("countryName").get(0).getCode must be === ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED
    }

    it("parseValidationErrorsAtMultipleLevels") {
      val xml = <api-error-response>
        <errors>
          <customer>
            <errors type="array">
              <error>
                <code>81608</code>
                <message>First name is too long.</message>
                <attribute type="symbol">first_name</attribute>
              </error>
            </errors>
            <credit-card>
              <billing-address>
                <errors type="array">
                  <error>
                    <code>91803</code>
                    <message>Country name is not an accepted country.</message>
                    <attribute type="symbol">country_name</attribute>
                  </error>
                </errors>
              </billing-address>
              <errors type="array">
                <error>
                  <code>81715</code>
                  <message>Credit card number is invalid.</message>
                  <attribute type="symbol">number</attribute>
                </error>
              </errors>
            </credit-card>
          </customer>
          <errors type="array"/>
        </errors>
      </api-error-response>

      val errors = new ValidationErrors(NodeWrapperFactory.instance.create(xmlAsStringWithHeader(xml)))
      errors.deepSize must be === 3
      errors.size must be === 0
      errors.forObject("customer").deepSize must be === 3
      errors.forObject("customer").size must be === 1
      errors.forObject("customer").onField("firstName").get(0).getCode must be === ValidationErrorCode.CUSTOMER_FIRST_NAME_IS_TOO_LONG
      errors.forObject("customer").forObject("creditCard").deepSize must be === 2
      errors.forObject("customer").forObject("creditCard").size must be === 1
      errors.forObject("customer").forObject("creditCard").onField("number").get(0).getCode must be === ValidationErrorCode.CREDIT_CARD_NUMBER_IS_INVALID
      errors.forObject("customer").forObject("creditCard").forObject("billingAddress").deepSize must be === 1
      errors.forObject("customer").forObject("creditCard").forObject("billingAddress").size must be === 1
      errors.forObject("customer").forObject("creditCard").forObject("billingAddress").onField("countryName").get(0).getCode must be === ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED
    }
  }
}