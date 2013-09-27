package com.braintreegateway;

import com.braintreegateway.gw.CustomerGateway;

import java.util.List;

public class CustomerPager implements Pager<Customer> {
    private CustomerGateway gateway;
    private CustomerSearchRequest query;

    public CustomerPager(CustomerGateway gateway, CustomerSearchRequest query) {
        this.gateway = gateway;
        this.query = query;
    }

    public List<Customer> getPage(List<String> ids) {
        return gateway.fetchCustomers(query, ids);
    }
}
