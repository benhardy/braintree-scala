package com.braintreegateway;

public class CreditCardOptionsRequest extends BaseRequest {
    private CreditCardRequest parent;
    private String verificationMerchantAccountId;
    private Boolean failOnDuplicatePaymentMethod;
    private Boolean verifyCard;
    private Boolean makeDefault;
    private String updateExistingToken;
    private String venmoSdkSession;

    public CreditCardOptionsRequest(CreditCardRequest parent) {
        this.parent = parent;
    }

    public CreditCardRequest done() {
        return parent;
    }

    public CreditCardOptionsRequest verificationMerchantAccountId(String verificationMerchantAccountId) {
        this.verificationMerchantAccountId = verificationMerchantAccountId;
        return this;
    }

    public CreditCardOptionsRequest failOnDuplicatePaymentMethod(Boolean failOnDuplicatePaymentMethod) {
        this.failOnDuplicatePaymentMethod = failOnDuplicatePaymentMethod;
        return this;
    }

    public CreditCardOptionsRequest verifyCard(Boolean verifyCard) {
        this.verifyCard = verifyCard;
        return this;
    }

    public CreditCardOptionsRequest makeDefault(Boolean makeDefault) {
        this.makeDefault = makeDefault;
        return this;
    }

    public CreditCardOptionsRequest updateExistingToken(String token) {
        this.updateExistingToken = token;
        return this;
    }

    public CreditCardOptionsRequest venmoSdkSession(String venmoSdkSession) {
        this.venmoSdkSession = venmoSdkSession;
        return this;
    }

    @Override
    public String toXmlString() {
        return buildRequest("options").toXmlString();
    }

    @Override
    public String toQueryString() {
        return toQueryString("options");
    }

    @Override
    public String toQueryString(String root) {
        return buildRequest(root).toQueryString();
    }

    protected RequestBuilder buildRequest(String root) {
        return new RequestBuilder(root).
                addElement("failOnDuplicatePaymentMethod", failOnDuplicatePaymentMethod).
                addElement("verifyCard", verifyCard).
                addElement("verificationMerchantAccountId", verificationMerchantAccountId).
                addElementIf(makeDefault != null && makeDefault.booleanValue(), "makeDefault", makeDefault).
                addElement("updateExistingToken", updateExistingToken).
                addElement("venmoSdkSession", venmoSdkSession);
    }
}
