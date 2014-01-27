package net.bhardy.braintree.scala.test;

public class VenmoSdk {
    public enum PaymentMethodCode {
        Visa("4111111111111111"),
        Invalid("invalid-payment-method-code");

        public final String code;

        private PaymentMethodCode(String number) {
            this.code = VenmoSdk.generateTestPaymentMethodCode(number);
        }
    }

    public static String generateTestPaymentMethodCode(String number) {
        return "stub-" + number;
    }

    public enum Session {
        Valid("stub-session"),
        Invalid("stub-invalid-session");

        public final String value;

        private Session(String validity) {
            this.value = validity;
        }
    }
}
