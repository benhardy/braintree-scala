package com.braintreegateway

import _root_.org.junit.runner.RunWith
import _root_.org.scalatest.FunSpec
import _root_.org.scalatest.junit.JUnitRunner
import _root_.org.scalatest.matchers.MustMatchers
import com.braintreegateway.util.NodeWrapperFactory
import gw.{Result, Failure}

import testhelpers.XmlHelper.xmlAsStringWithHeader
import com.braintreegateway.ValidationErrors.NoValidationErrors


@RunWith(classOf[JUnitRunner])
class ValidationErrorsSpec extends FunSpec with MustMatchers {

  describe("from gateway response XML via Result") {
    it("creates proper ValidationErrors tree") {
      val response = <api-error-response>
        <errors>
          <errors type="array"/>
          <address>
            <errors type="array">
              <error>
                <code>91815</code>
                <attribute type="symbol">base</attribute>
                <message>Provided country information is inconsistent.</message>
              </error>
            </errors>
          </address>
        </errors>
        <params>
          <address>
            <country-name>Tunisia</country-name>
            <country-code-alpha2>US</country-code-alpha2>
          </address>
          <action>create</action>
          <controller>addresses</controller>
          <merchant-id>integration_merchant_id</merchant-id>
          <customer-id>175821</customer-id>
        </params>
        <message>Provided country information is inconsistent.</message>
      </api-error-response>

      val node = NodeWrapperFactory.create(xmlAsStringWithHeader(response))
      val result = Result.address(node)
      result match {
        case Failure(errors,_,_,_,_,_) => {
          errors.deepSize must be === 1
          errors.size must be === 0
          errors.forObject("address").deepSize must be === 1
          errors.forObject("address").size must be === 1
          errors.forObject("address").onField("base").head.code must be === ValidationErrorCode.ADDRESS_INCONSISTENT_COUNTRY
        }
        case _ => fail("expected Failure")
      }
    }
  }

  describe("codes") {
    it("onField") {
      val errors = new ValidationErrors(
        List(
          new ValidationError("country_name", ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED, "invalid country")
        ), Map.empty
      )
      errors.onField("countryName").head.code must be === ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED
      errors.onField("countryName").head.message must be === "invalid country"
    }
  
    it("onFieldAlsoWorksWithUnderscores") {
      val errors = new ValidationErrors(
        List(
          new ValidationError("country_name", ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED, "invalid country")
        ), Map.empty
      )
      errors.onField("country_name").head.code must be === ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED
      errors.onField("country_name").head.message must be === "invalid country"
    }
  
    it("nonExistingField") {
      val errors = NoValidationErrors
      errors.onField("foo") must be ('empty)
    }
  
    it("forObject") {
      val addressErrors = new ValidationErrors(
        List(
          new ValidationError("country_name", ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED, "invalid country")
        ),
        Map.empty
      )
      val errors = new ValidationErrors(Nil, Map("address" -> addressErrors))

      errors.forObject("address").onField("countryName").head.code must be === ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED
      errors.forObject("address").onField("country_name").head.message must be === "invalid country"
    }
  
    it("forObjectOnNonExistingObject") {
      val errors = NoValidationErrors
      errors.forObject("invalid").size must be === 0
    }
  
    it("forObjectAlsoWorksWithUnderscores") {
      val addressErrors = new ValidationErrors(
        List(
          new ValidationError("name", ValidationErrorCode.ADDRESS_FIRST_NAME_IS_TOO_LONG, "invalid name")
        ),
        Map.empty
      )
      val errors = new ValidationErrors(Nil, Map("billing-address" -> addressErrors))
      errors.forObject("billing_address").onField("name").head.code must be === ValidationErrorCode.ADDRESS_FIRST_NAME_IS_TOO_LONG

    }
  }
  describe("size") {
    it("reports correct size") {
      val errors = new ValidationErrors(List(
        new ValidationError("countryName", ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED, "invalid country"),
        new ValidationError("anotherField", ValidationErrorCode.ADDRESS_COMPANY_IS_TOO_LONG, "another message")
      ), Map.empty)
      errors.size must be === 2
    }
  }
  describe("deepSize") {
    it("reports correct deepSize") {
      val addressErrors = new ValidationErrors(List(
        new ValidationError("countryName", ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED, "invalid country"),
        new ValidationError("anotherField", ValidationErrorCode.ADDRESS_COMPANY_IS_TOO_LONG, "another message")
      ), Map.empty)
      val errors = new ValidationErrors(
        List(
          new ValidationError("someField", ValidationErrorCode.ADDRESS_EXTENDED_ADDRESS_IS_TOO_LONG, "some message")
        ),
        Map("address" -> addressErrors)
      )
      errors.deepSize must be === 3
      errors.size must be === 1
      errors.forObject("address").deepSize must be === 2
      errors.forObject("address").size must be === 2
    }
  }

  describe("getting all validation errors") {
    it("getAllValidationErrors") {
      val addressErrors = new ValidationErrors(List(
        new ValidationError("countryName", ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED, "invalid country"),
        new ValidationError("anotherField", ValidationErrorCode.ADDRESS_COMPANY_IS_TOO_LONG, "another message")
      ), Map.empty)
      val errors = new ValidationErrors(
        List(
          new ValidationError("someField", ValidationErrorCode.ADDRESS_FIRST_NAME_IS_TOO_LONG, "some message")
        ),
        Map("address" -> addressErrors)
      )
      errors.getAllValidationErrors.size must be === 1
      errors.getAllValidationErrors.head.code must be === ValidationErrorCode.ADDRESS_FIRST_NAME_IS_TOO_LONG
    }

    it("getAllDeepValidationErrors") {
      val addressErrors = new ValidationErrors(List(
        new ValidationError("countryName", ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED, "1"),
        new ValidationError("anotherField", ValidationErrorCode.ADDRESS_COMPANY_IS_TOO_LONG, "2")
      ), Map.empty)
      val errors = new ValidationErrors(
        List(
          new ValidationError("someField", ValidationErrorCode.ADDRESS_FIRST_NAME_IS_TOO_LONG, "3")
        ),
        Map("address" -> addressErrors)
      )

      errors.getAllDeepValidationErrors.size must be === 3
      val validationErrors = errors.getAllDeepValidationErrors.sortWith((a,b) => a.code.compareTo(b.code) < 0)

      validationErrors.head.code must be === ValidationErrorCode.ADDRESS_COMPANY_IS_TOO_LONG
      validationErrors(1).code must be === ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED
      validationErrors(2).code must be === ValidationErrorCode.ADDRESS_FIRST_NAME_IS_TOO_LONG
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

      val errors = ValidationErrors(NodeWrapperFactory.create(xmlAsStringWithHeader(xml)))
      errors.deepSize must be === 1
      errors.forObject("address").onField("country_name").head.code must be === ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED
      errors.forObject("address").onField("countryName").head.code must be === ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED
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

      val errors = ValidationErrors(NodeWrapperFactory.create(xmlAsStringWithHeader(xml)))
      errors.deepSize must be === 2
      errors.forObject("address").onField("countryName").head.code must be === ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED
      errors.forObject("address").onField("streetAddress").head.code must be === ValidationErrorCode.ADDRESS_STREET_ADDRESS_IS_TOO_LONG
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

      val errors = ValidationErrors(NodeWrapperFactory.create(xmlAsStringWithHeader(xml)))
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

      val errors = ValidationErrors(NodeWrapperFactory.create(xmlAsStringWithHeader(xml)))
      errors.deepSize must be === 1
      errors.forObject("creditCard").forObject("billingAddress").onField("countryName").head.code must be === ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED
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

      val errors = ValidationErrors(NodeWrapperFactory.create(xmlAsStringWithHeader(xml)))
      errors.deepSize must be === 3
      errors.size must be === 0
      errors.forObject("customer").deepSize must be === 3
      errors.forObject("customer").size must be === 1
      errors.forObject("customer").onField("firstName").head.code must be === ValidationErrorCode.CUSTOMER_FIRST_NAME_IS_TOO_LONG
      errors.forObject("customer").forObject("creditCard").deepSize must be === 2
      errors.forObject("customer").forObject("creditCard").size must be === 1
      errors.forObject("customer").forObject("creditCard").onField("number").head.code must be === ValidationErrorCode.CREDIT_CARD_NUMBER_IS_INVALID
      errors.forObject("customer").forObject("creditCard").forObject("billingAddress").deepSize must be === 1
      errors.forObject("customer").forObject("creditCard").forObject("billingAddress").size must be === 1
      errors.forObject("customer").forObject("creditCard").forObject("billingAddress").onField("countryName").head.code must be === ValidationErrorCode.ADDRESS_COUNTRY_NAME_IS_NOT_ACCEPTED
    }
  }
}