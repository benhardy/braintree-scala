package com.braintreegateway;

public class TransactionOptionsCloneRequest extends BaseRequest {
    private TransactionCloneRequest parent;
    private Boolean submitForSettlement;

    public TransactionOptionsCloneRequest(TransactionCloneRequest parent) {
        this.parent = parent;
    }

    public TransactionOptionsCloneRequest submitForSettlement(Boolean submitForSettlement) {
        this.submitForSettlement = submitForSettlement;
        return this;
    }

    public TransactionCloneRequest done() {
        return parent;
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
        return new RequestBuilder(root).addElement("submitForSettlement", submitForSettlement);
    }
}
