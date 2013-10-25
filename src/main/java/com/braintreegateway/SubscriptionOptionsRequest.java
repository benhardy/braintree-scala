package com.braintreegateway;

public class SubscriptionOptionsRequest extends BaseRequest {
    private Boolean doNotInheritAddOnsOrDiscounts;
    private SubscriptionRequest parent;
    private Boolean prorateCharges;
    private Boolean replaceAllAddOnsAndDiscounts;
    private Boolean revertSubscriptionOnProrationFailure;
    private Boolean startImmediately;

    public SubscriptionOptionsRequest(SubscriptionRequest parent) {
        this.parent = parent;
    }

    public SubscriptionRequest done() {
        return parent;
    }

    public SubscriptionOptionsRequest doNotInheritAddOnsOrDiscounts(Boolean doNotInheritAddOnsOrDiscounts) {
        this.doNotInheritAddOnsOrDiscounts = doNotInheritAddOnsOrDiscounts;
        return this;
    }

    public SubscriptionOptionsRequest prorateCharges(Boolean prorateCharges) {
        this.prorateCharges = prorateCharges;
        return this;
    }

    public SubscriptionOptionsRequest replaceAllAddOnsAndDiscounts(Boolean replaceAllAddonsAndDiscounts) {
        this.replaceAllAddOnsAndDiscounts = replaceAllAddonsAndDiscounts;
        return this;
    }

    public SubscriptionOptionsRequest revertSubscriptionOnProrationFailure(Boolean revertSubscriptionOnProrationFailure) {
      this.revertSubscriptionOnProrationFailure = revertSubscriptionOnProrationFailure;
      return this;
    }

    public SubscriptionOptionsRequest startImmediately(Boolean startImmediately) {
        this.startImmediately = startImmediately;
        return this;
    }

    @Override
    public String toXmlString() {
        return buildRequest("options").toXmlString();
    }

    protected RequestBuilder buildRequest(String root) {
        return new RequestBuilder(root).
            addElement("doNotInheritAddOnsOrDiscounts", doNotInheritAddOnsOrDiscounts).
            addElement("prorateCharges", prorateCharges).
            addElement("replaceAllAddOnsAndDiscounts", replaceAllAddOnsAndDiscounts).
            addElement("revertSubscriptionOnProrationFailure", revertSubscriptionOnProrationFailure).
            addElement("startImmediately", startImmediately);
    }
}
