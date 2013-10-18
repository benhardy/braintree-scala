package com.braintreegateway;

/**
 */
public class Subscriptions {
    public enum DurationUnit {
        DAY, MONTH, UNRECOGNIZED, UNDEFINED
    }

    public enum Status {
        ACTIVE("Active"),
        CANCELED("Canceled"),
        EXPIRED("Expired"),
        PAST_DUE("Past Due"),
        PENDING("Pending"),
        UNRECOGNIZED("Unrecognized"),
        UNDEFINED("Undefined");

        private final String name;

        Status(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
