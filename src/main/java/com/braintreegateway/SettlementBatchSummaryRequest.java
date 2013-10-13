package com.braintreegateway;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SettlementBatchSummaryRequest extends BaseRequest {
    
    private Calendar settlementDate;
    private String groupByCustomField;
    
    public SettlementBatchSummaryRequest() {
        super();
    }
    
    public String toXML() {
        return buildRequest("settlement-batch-summary").toXML();
    }
    
    public SettlementBatchSummaryRequest settlementDate(Calendar settlementDate) {
        this.settlementDate = settlementDate;
        return this;
    }


    protected RequestBuilder buildRequest(String root) {
        return new RequestBuilder(root).
                addElement("settlement-date", dateString(settlementDate)).
                addElementIf(groupByCustomField != null, "group-by-custom-field", groupByCustomField);
    }

    public static String dateString(Calendar settlementDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setCalendar(settlementDate);
        return dateFormat.format(settlementDate.getTime());
    }

    public SettlementBatchSummaryRequest groupByCustomField(String groupByCustomField) {
        this.groupByCustomField = groupByCustomField;
        return this;
    }
}
