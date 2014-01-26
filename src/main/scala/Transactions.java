package net.bhardy.braintree.scala;

/**
 */
public class Transactions {
    public enum CreatedUsing {
        FULL_INFORMATION("full_information"),
        TOKEN("token");

        private final String name;

        CreatedUsing(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public enum EscrowStatus {
        HELD,
        HOLD_PENDING,
        RELEASE_PENDING,
        RELEASED,
        REFUNDED,
        UNRECOGNIZED,
        UNDEFINED;
    }

    public enum GatewayRejectionReason {
        AVS("avs"),
        AVS_AND_CVV("avs_and_cvv"),
        CVV("cvv"),
        DUPLICATE("duplicate"),
        UNRECOGNIZED("unrecognized"),
        UNDEFINED("undefined");

        private final String name;

        GatewayRejectionReason(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public enum Source {
        API("api"),
        CONTROL_PANEL("control_panel"),
        UNRECOGNIZED("unrecognized"),
        UNDEFINED("undefined");

        private final String name;

        Source(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public enum Status {
        AUTHORIZATION_EXPIRED, AUTHORIZED, AUTHORIZING, FAILED, GATEWAY_REJECTED,
        PROCESSOR_DECLINED, SETTLED, SETTLING, SUBMITTED_FOR_SETTLEMENT, VOIDED,
        UNRECOGNIZED,
        UNDEFINED;
    }

    public enum Type {
        CREDIT("credit"),
        SALE("sale"),
        UNRECOGNIZED("unrecognized"),
        UNDEFINED("undefined"); // absent

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
