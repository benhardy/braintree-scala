package com.braintreegateway;

import com.braintreegateway.search.MultipleValueNode;

public class IdsSearchRequest extends SearchRequest {
    public MultipleValueNode<IdsSearchRequest, String> ids() {
        return new MultipleValueNode<IdsSearchRequest, String>("ids", this);
    }
}
