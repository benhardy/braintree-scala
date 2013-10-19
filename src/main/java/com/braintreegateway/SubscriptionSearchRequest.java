package com.braintreegateway;

import com.braintreegateway.search.*;

/**
 * Provides a fluent interface to build up requests around {@link Subscription}
 * searches.
 */
public class SubscriptionSearchRequest extends SearchRequest {
    public SubscriptionSearchRequest() {
        super();
    }

    public RangeNode<SubscriptionSearchRequest> daysPastDue() {
        return new RangeNode<SubscriptionSearchRequest>("days_past_due", this);
    }

    public TextNode<SubscriptionSearchRequest> id() {
        return new TextNode<SubscriptionSearchRequest>("id", this);
    }

    public MultipleValueNode<SubscriptionSearchRequest, String> ids() {
        return new MultipleValueNode<SubscriptionSearchRequest, String>("ids", this);
    }

    public MultipleValueNode<SubscriptionSearchRequest, Boolean> inTrialPeriod() {
        return new MultipleValueNode<SubscriptionSearchRequest, Boolean>("in_trial_period", this);
    }

    public MultipleValueNode<SubscriptionSearchRequest, String> merchantAccountId() {
        return new MultipleValueNode<SubscriptionSearchRequest, String>("merchant_account_id", this);
    }

    public DateRangeNode<SubscriptionSearchRequest> nextBillingDate() {
        return new DateRangeNode<SubscriptionSearchRequest>("next_billing_date", this);
    }

    public MultipleValueOrTextNode<SubscriptionSearchRequest, String> planId() {
        return new MultipleValueOrTextNode<SubscriptionSearchRequest, String>("plan_id", this);
    }

    public RangeNode<SubscriptionSearchRequest> price() {
        return new RangeNode<SubscriptionSearchRequest>("price", this);
    }

    public MultipleValueNode<SubscriptionSearchRequest, Subscriptions.Status> status() {
        return new MultipleValueNode<SubscriptionSearchRequest, Subscriptions.Status>("status", this);
    }
    
    public TextNode<SubscriptionSearchRequest> transactionId() {
        return new TextNode<SubscriptionSearchRequest>("transaction-id", this);
    }

    public RangeNode<SubscriptionSearchRequest> billingCyclesRemaining() {
        return new RangeNode<SubscriptionSearchRequest>("billing_cycles_remaining", this);
    }
}
