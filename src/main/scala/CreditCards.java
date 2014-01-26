package net.bhardy.braintree.scala;

import net.bhardy.braintree.scala.util.EnumUtils;
import scala.Function1;
import scala.Option;

/**
 */
public final class CreditCards {

    public enum CardType {
        AMEX("American Express"),
        CARTE_BLANCHE("Carte Blanche"),
        CHINA_UNION_PAY("China UnionPay"),
        DINERS_CLUB_INTERNATIONAL("Diners Club"),
        DISCOVER("Discover"),
        JCB("JCB"),
        LASER("Laser"),
        MAESTRO("Maestro"),
        MASTER_CARD("MasterCard"),
        SOLO("Solo"),
        SWITCH("Switch"),
        VISA("Visa"),
        UNRECOGNIZED("unrecognized"),
        UNDEFINED("undefined");

        private final String name;

        private CardType(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }

        public final static Function1<String, Option<CardType>> lookup = (
                EnumUtils.createLookupFromString(CardType.values())
        );

        public static CardType fromString(String name) {
            return CreditCard.lookupCardType(name);
        }
    }
}
