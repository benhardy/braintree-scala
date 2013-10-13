package com.braintreegateway;

public class TransactionOptionsRequest extends BaseRequest {
    private Boolean addBillingAddressToPaymentMethod;
    private Boolean holdInEscrow;
    private TransactionRequest parent;
    private Boolean storeInVault;
    private Boolean storeInVaultOnSuccess;
    private Boolean storeShippingAddressInVault;
    private Boolean submitForSettlement;
    private String venmoSdkSession;

    public TransactionOptionsRequest(TransactionRequest parent) {
        this.parent = parent;
    }

    public TransactionOptionsRequest addBillingAddressToPaymentMethod(Boolean addBillingAddressToPaymentMethod) {
        this.addBillingAddressToPaymentMethod = addBillingAddressToPaymentMethod;
        return this;
    }

    public TransactionRequest done() {
        return parent;
    }

    public TransactionOptionsRequest holdInEscrow(Boolean holdInEscrow) {
        this.holdInEscrow = holdInEscrow;
        return this;
    }

    public TransactionOptionsRequest storeInVault(Boolean storeInVault) {
        this.storeInVault = storeInVault;
        return this;
    }

    public TransactionOptionsRequest storeInVaultOnSuccess(Boolean storeInVaultOnSuccess) {
        this.storeInVaultOnSuccess = storeInVaultOnSuccess;
        return this;
    }

    public TransactionOptionsRequest storeShippingAddressInVault(Boolean storeShippingAddressInVault) {
        this.storeShippingAddressInVault = storeShippingAddressInVault;
        return this;
    }

    public TransactionOptionsRequest submitForSettlement(Boolean submitForSettlement) {
        this.submitForSettlement = submitForSettlement;
        return this;
    }

    public TransactionOptionsRequest venmoSdkSession(String venmoSdkSession) {
        this.venmoSdkSession = venmoSdkSession;
        return this;
    }

    @Override
    public String toXML() {
        return buildRequest("options").toXML();
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
            addElement("holdInEscrow", holdInEscrow).
            addElement("storeInVault", storeInVault).
            addElement("storeInVaultOnSuccess", storeInVaultOnSuccess).
            addElement("addBillingAddressToPaymentMethod", addBillingAddressToPaymentMethod).
            addElement("storeShippingAddressInVault", storeShippingAddressInVault).
            addElement("submitForSettlement", submitForSettlement).
            addElement("venmoSdkSession", venmoSdkSession);
    }
}
