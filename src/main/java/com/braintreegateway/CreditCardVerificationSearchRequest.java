package com.braintreegateway;

import com.braintreegateway.search.*;

public class CreditCardVerificationSearchRequest extends SearchRequest<CreditCardVerificationSearchRequest> {
    public TextNode<CreditCardVerificationSearchRequest> id() {
        return new TextNode<CreditCardVerificationSearchRequest>("id", this);
    }

    public TextNode<CreditCardVerificationSearchRequest> creditCardCardholderName() {
        return new TextNode<CreditCardVerificationSearchRequest>("credit_card_cardholder_name", this);
    }

    public EqualityNode<CreditCardVerificationSearchRequest> creditCardExpirationDate() {
        return new EqualityNode<CreditCardVerificationSearchRequest>("credit_card_expiration_date", this);
    }

    public PartialMatchNode<CreditCardVerificationSearchRequest> creditCardNumber() {
        return new PartialMatchNode<CreditCardVerificationSearchRequest>("credit_card_number", this);
    }

    public MultipleValueNode<CreditCardVerificationSearchRequest, String> ids() {
        return new MultipleValueNode<CreditCardVerificationSearchRequest, String>("ids", this);
    }

    public MultipleValueNode<CreditCardVerificationSearchRequest, CreditCards.CardType> creditCardCardType() {
        return new MultipleValueNode<CreditCardVerificationSearchRequest, CreditCards.CardType>("credit_card_card_type", this);
    }

    public DateRangeNode<CreditCardVerificationSearchRequest> createdAt() {
        return new DateRangeNode<CreditCardVerificationSearchRequest>("created_at", this);
    }

    @Override
    public CreditCardVerificationSearchRequest getThis() {
        return this;
    }
}
