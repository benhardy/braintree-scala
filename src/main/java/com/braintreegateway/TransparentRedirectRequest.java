package com.braintreegateway;

import com.braintreegateway.exceptions.ForgedQueryStringException;
import com.braintreegateway.gw.Configuration;
import com.braintreegateway.util.Http;
import com.braintreegateway.util.TrUtil;

import java.util.HashMap;
import java.util.Map;

public class TransparentRedirectRequest extends BaseRequest {
    private String id;

    public TransparentRedirectRequest(Configuration configuration, String queryString) {
        Map<String, String> paramMap = new HashMap<String, String>();
        String[] queryParams = queryString.split("&");

        for (String queryParam : queryParams) {
            String[] items = queryParam.split("=");
            paramMap.put(items[0], items[1]);
        }

        Http.throwExceptionIfErrorStatusCode(Integer.valueOf(paramMap.get("http_status")), paramMap.get("bt_message"));
        
        if (!new TrUtil(configuration).isValidTrQueryString(queryString)) {
            throw new ForgedQueryStringException();
        }

        id = paramMap.get("id");
    }
    
    public String getId() {
        return id;
    }

    @Override
    public String toXmlString() {
        StringBuilder builder = new StringBuilder();
        builder.append(RequestBuilder.buildXMLElement("id", id));
        return builder.toString();
    }

    @Override
    public String toQueryString(String parent) {
        return null;
    }

    @Override
    public String toQueryString() {
        return null;
    }
}
