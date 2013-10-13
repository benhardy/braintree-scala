package com.braintreegateway;

import java.util.List;

public class SearchCriteria extends BaseRequest {
    private String xml;

    public SearchCriteria(String type, Object value) {
        this.xml = RequestBuilder.buildXMLElement(type, value);
    }

    public SearchCriteria(List<?> items) {
        StringBuilder builder = new StringBuilder();
        for (Object item : items) {
            builder.append(RequestBuilder.buildXMLElement("item", item.toString()));
        }
        this.xml = builder.toString();
    }

    @Override
    public String toXML() {
        return this.xml;
    }

    @Override
    public String toQueryString(String parent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toQueryString() {
        throw new UnsupportedOperationException();
    }
}
