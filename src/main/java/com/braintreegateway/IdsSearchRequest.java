package com.braintreegateway;

import com.braintreegateway.search.MultipleValueNode;
import com.braintreegateway.search.SearchRequest;

public class IdsSearchRequest extends SearchRequest<IdsSearchRequest> {
    public MultipleValueNode<IdsSearchRequest, String> ids() {
        return new MultipleValueNode<IdsSearchRequest, String>("ids", this);
    }

    public IdsSearchRequest getThis() {
        return this;
    }
}
