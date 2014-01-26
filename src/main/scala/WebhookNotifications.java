package net.bhardy.braintree.scala;

/**
 */
public class WebhookNotifications {
    public enum Kind {
        SUB_MERCHANT_ACCOUNT_APPROVED("sub_merchant_account_approved"),
        SUB_MERCHANT_ACCOUNT_DECLINED("sub_merchant_account_declined"),
        SUBSCRIPTION_CANCELED("subscription_canceled"),
        SUBSCRIPTION_CHARGED_SUCCESSFULLY("subscription_charged_successfully"),
        SUBSCRIPTION_CHARGED_UNSUCCESSFULLY("subscription_charged_unsuccessfully"),
        SUBSCRIPTION_EXPIRED("subscription_expired"),
        SUBSCRIPTION_TRIAL_ENDED("subscription_trial_ended"),
        SUBSCRIPTION_WENT_ACTIVE("subscription_went_active"),
        SUBSCRIPTION_WENT_PAST_DUE("subscription_went_past_due"),
        TRANSACTION_DISBURSED("transaction_disbursed"),
        UNRECOGNIZED("unrecognized");

        private final String name;

        Kind(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
