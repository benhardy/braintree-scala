package com.braintreegateway;

import com.braintreegateway.Transaction.Type;

import java.math.BigDecimal;

public class SubscriptionTransactionRequest extends BaseRequest {

    private BigDecimal amount;
    private String subscriptionId;

    public SubscriptionTransactionRequest amount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public SubscriptionTransactionRequest subscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
        return this;
    }
    
    @Override
    public String toXML() {
        return buildRequest("transaction").toXML();
    }

    protected RequestBuilder buildRequest(String root) {
        return new RequestBuilder(root).
            addElement("amount", amount).
            addElement("subscriptionId", subscriptionId).
            addElement("type", Type.SALE.toString().toLowerCase());
    }
}
