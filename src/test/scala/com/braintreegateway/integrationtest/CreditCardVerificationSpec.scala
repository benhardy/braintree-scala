package com.braintreegateway.integrationtest

import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.MustMatchers
import com.braintreegateway._
import gw.Failure
import testhelpers.CalendarHelper._
import com.braintreegateway.util.NodeWrapperFactory
import testhelpers.GatewaySpec
import testhelpers.XmlHelper._
import com.braintreegateway.CreditCards.CardType

@RunWith(classOf[JUnitRunner])
class CreditCardVerificationSpec extends FunSpec with MustMatchers with GatewaySpec {

  describe("CreditCardVerification constructor") {
    it("can construct from a response XML") {
      val response = <api-error-response>
        <verification>
          <avs-error-response-code nil="true"></avs-error-response-code>
          <avs-postal-code-response-code>I</avs-postal-code-response-code>
          <status>processor_declined</status>
          <processor-response-code>2000</processor-response-code>
          <avs-street-address-response-code>I</avs-street-address-response-code>
          <processor-response-text>Do Not Honor</processor-response-text>
          <cvv-response-code>M</cvv-response-code>
          <id>verification_id</id>
          <credit-card>
            <cardholder-name>Joe Johnson</cardholder-name>
            <number>4111111111111111</number>
            <expiration-date>12/2012</expiration-date>
            <prepaid>Unknown</prepaid>
          </credit-card>
          <billing>
            <postal-code>60601</postal-code>
          </billing>
        </verification>
        <errors>
          <errors type="array"/>
        </errors>
      </api-error-response>

      val xml: String = xmlAsStringWithHeader(response)
      val verificationNode = NodeWrapperFactory.create(xml).findFirst("verification")
      val verification = new CreditCardVerification(verificationNode)
      verification.getAvsErrorResponseCode must be === null
      verification.getAvsPostalCodeResponseCode must be === "I"
      verification.getStatus must be === CreditCardVerification.Status.PROCESSOR_DECLINED
      verification.getProcessorResponseCode must be === "2000"
      verification.getAvsStreetAddressResponseCode must be === "I"
      verification.getProcessorResponseText must be === "Do Not Honor"
      verification.getCvvResponseCode must be === "M"
      verification.getCreditCard.getPrepaid must be === CreditCard.KindIndicator.UNKNOWN
    }
  }

  describe("creditCardVerification.search") {
    onGatewayIt("can search on all text fields") { gateway =>
      val request = new CustomerRequest().creditCard.number("4000111111111115").expirationDate("11/12").
        cardholderName("Tom Smith").options.verifyCard(true).done.done
      val result = gateway.customer.create(request)
      result match {
        case Failure(_,_,_,Some(verification),_,_) => {
          val searchRequest = new CreditCardVerificationSearchRequest().
            id.is(verification.getId).creditCardCardholderName.
            is("Tom Smith").creditCardExpirationDate.
            is("11/2012").creditCardNumber.is("4000111111111115")
          val collection = gateway.creditCardVerification.search(searchRequest)
          collection.getMaximumSize must be === 1
          collection.getFirst.getId must be === verification.getId
        }
        case x => fail("expected Failure, got " + x)
      }
    }

    onGatewayIt("can search on multiple value fields") { gateway =>
      val requestOne = new CustomerRequest().creditCard.number("4000111111111115").expirationDate("11/12").
          options.verifyCard(true).done.done
      val resultOne = gateway.customer.create(requestOne)
      resultOne must not be ('success)

      val verificationOne = resultOne match { case Failure(_,_,_,Some(verification),_,_) => verification }
      val requestTwo = new CustomerRequest().creditCard.number("5105105105105100").expirationDate("06/12").
          options.verifyCard(true).done.done
      val resultTwo = gateway.customer.create(requestTwo)
      resultTwo must not be ('success)

      // TODO make this extraction not suck
      val verificationTwo = resultTwo  match { case Failure(_,_,_,Some(verification),_,_) => verification }
      val searchRequest = new CreditCardVerificationSearchRequest().
          ids.in(verificationOne.getId, verificationTwo.getId).
          creditCardCardType.in(CardType.VISA, CardType.MASTER_CARD)

      val collection = gateway.creditCardVerification.search(searchRequest)

      collection.getMaximumSize must be === 2
      val expectedIds = List(verificationOne.getId, verificationTwo.getId)
      expectedIds must contain (collection.getFirst.getId)
    }

    onGatewayIt("can search on range fields") { gateway =>
      val request = new CustomerRequest().creditCard.
          number("4000111111111115").
          expirationDate("11/12").
          cardholderName("Tom Smith").
          options.
            verifyCard(true).
            done.
          done

      val result = gateway.customer.create(request)

      result match {
        case Failure(_,_,_,Some(verification),_,_) => {
          val createdAt = verification.getCreatedAt
          val threeDaysEarlier = createdAt - 3.days
          val oneDayEarlier = createdAt - 1.days
          val oneDayLater = createdAt + 1.days

          def cardVerificationCreatedAt = {
            new CreditCardVerificationSearchRequest().id.is(verification.getId).createdAt
          }
          var searchRequest = cardVerificationCreatedAt.between(oneDayEarlier, oneDayLater)
          gateway.creditCardVerification.search(searchRequest).getMaximumSize must be === 1

          searchRequest = cardVerificationCreatedAt.greaterThanOrEqualTo(oneDayEarlier)
          gateway.creditCardVerification.search(searchRequest).getMaximumSize must be === 1

          searchRequest = cardVerificationCreatedAt.lessThanOrEqualTo(oneDayLater)
          gateway.creditCardVerification.search(searchRequest).getMaximumSize must be === 1

          searchRequest = cardVerificationCreatedAt.between(threeDaysEarlier, oneDayEarlier)
          gateway.creditCardVerification.search(searchRequest).getMaximumSize must be === 0
        }
        case x => fail("expected Failure, got " + x)
      }
    }
 }

  describe("CreditCardVerification") {
    onGatewayIt("Has Card Type indicators") { gateway =>
      val request = new CustomerRequest().
          creditCard.
            number("4000111111111115").expirationDate("11/12").cardholderName("Tom Smith").
            options.verifyCard(true).
            done.
          done

      val result = gateway.customer.create(request)

      result match {
        case Failure(_,_,_,Some(verification),_,_) => {

          val card = verification.getCreditCard
          card.getCommercial must be === CreditCard.KindIndicator.UNKNOWN
          card.getDebit must be === CreditCard.KindIndicator.UNKNOWN
          card.getDurbinRegulated must be === CreditCard.KindIndicator.UNKNOWN
          card.getHealthcare must be === CreditCard.KindIndicator.UNKNOWN
          card.getPayroll must be === CreditCard.KindIndicator.UNKNOWN
          card.getPrepaid must be === CreditCard.KindIndicator.UNKNOWN
          card.getCountryOfIssuance must be === "Unknown"
          card.getIssuingBank must be === "Unknown"
        }
        case x => fail("expected Failure, got " + x)
      }
    }
  }
}