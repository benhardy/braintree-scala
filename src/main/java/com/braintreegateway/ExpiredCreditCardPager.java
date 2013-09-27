package com.braintreegateway;

import com.braintreegateway.gw.CreditCardGateway;

import java.util.List;

public class ExpiredCreditCardPager implements Pager<CreditCard> {

    private CreditCardGateway gateway;

    public ExpiredCreditCardPager(CreditCardGateway gateway) {
        this.gateway = gateway;
    }

    public List<CreditCard> getPage(List<String> ids) {
        return gateway.fetchExpiredCreditCards(ids);
    }
}
