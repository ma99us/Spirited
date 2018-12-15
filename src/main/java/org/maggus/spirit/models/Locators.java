package org.maggus.spirit.models;

public class Locators {
    public static enum Country {
        UK("United Kingdom"),
        CA("Canada"),
        FR("France"),
        DE("Germany"),
        IN("India"),
        IR("Ireland"),
        JP("Japan"),
        SA("South Africa"),
        US("USA");

        private final String type;

        Country(String type) {
            this.type = type;
        }

        public String toString() {
            return type;
        }
    }

    public static enum Region {
        US_CO("Colorado"),
        US_IL("Illinois"),
        US_IN("Indiana"),
        US_KE("Kentucky"),
        US_TE("Tennessee"),

        UK_CAMP("Campbeltown"),
        UK_HIGH("Highland"),
        UK_ISLANDS("Islands"),
        UK_ISLA("Isla"),
        UK_LOW("Lowland"),
        UK_SPEY("Speyside");

        private final String type;

        Region(String type) {
            this.type = type;
        }

        public String toString() {
            return type;
        }
    }

    public static enum WhiskyType {
        S_M("Single malt"),
        BLENDED("Blended"),
        FLAVOURED("Flavoured"),
        BOURBON("Bourbon"),
        RYE("Rye");

        private final String type;

        WhiskyType(String type) {
            this.type = type;
        }

        public String toString() {
            return type;
        }
    }
}
