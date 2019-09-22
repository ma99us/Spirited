package org.maggus.spirit.models;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Locators {

    public static enum Country {
        UK("United Kingdom", "Scotland", "England", "Great Britain"),
        CA("Canada"),
        FR("France"),
        DE("Germany"),
        IN("India"),
        IR("Ireland"),
        JP("Japan"),
        SA("South Africa"),
        US("USA", "US", "America", "US of A"),
        INTERNATIONAL("International")
        ;

        private final String[] type;

        Country(String... type) {
            this.type = type;
        }

        public String toString() {
            return type[0];
        }

        public static Country parse(String str) {
            for (Country c : Country.values()) {
                if (Arrays.stream(c.type).anyMatch(t -> t.equalsIgnoreCase(str))) {
                    return c;
                }
            }
            return null;
        }

        public static boolean equals(String obj1, String obj2) {
            Country c1 = Country.parse(obj1);
            Country c2 = Country.parse(obj2);
            return c1 != null && c1.equals(c2);
        }
    }

    public static enum Region {
        US_CO("Colorado"),
        US_IL("Illinois"),
        US_IN("Indiana"),
        US_KE("Kentucky"),
        US_TE("Tennessee"),

        UK_CAMP("Campbeltown"),
        UK_HIGH("Highland", "Highlands"),
        UK_ISLANDS("Islands", "Island"),
        UK_ISLA("Isla", "Islay"),
        UK_LOW("Lowland"),
        UK_SPEY("Speyside");

        private final String[] type;

        Region(String... type) {
            this.type = type;
        }

        public String toString() {
            return type[0];
        }

        public static Region parse(String str) {
            for (Region s : Region.values()) {
                if (Arrays.stream(s.type).anyMatch(t -> t.equalsIgnoreCase(str))) {
                    return s;
                }
            }
            return null;
        }

        public static boolean equals(String obj1, String obj2) {
            Region c1 = Region.parse(obj1);
            Region c2 = Region.parse(obj2);
            return c1 != null && c1.equals(c2);
        }
    }

//    public static enum Spirit {
//        WHISKY("Whisky", "Whiskey"),
//        VODKA("Vodka", "Flavored vodka"),
//        RUM("Rum", "Dark rum", "Flavored rum", "Gold rum", "Amber rum", "White rum", "Spiced rum"),
//        COGNAC("Cognac"),
//        GIN("Gin", "Distilled gin", "Dry gin");
//
//        private final String[] type;
//
//        Spirit(String... type) {
//            this.type = type;
//        }
//
//        public String toString() {
//            return type[0];
//        }
//
//        public static Spirit parse(String str) {
//            for (Spirit s : Spirit.values()) {
//                if (Arrays.stream(s.type).anyMatch(t -> t.equalsIgnoreCase(str))) {
//                    return s;
//                }
//            }
//            return null;
//        }
//
//        public static boolean equals(String obj1, String obj2) {
//            Spirit c1 = Spirit.parse(obj1);
//            Spirit c2 = Spirit.parse(obj2);
//            return c1 != null && c1.equals(c2);
//        }
//    }

    public static enum SpiritType {
        WHISKY("Whisky"),
        // Whisky sub-types
        S_M("Single malt", "Single grain", "Peated Single Malt"),
        BLENDED("Blended", "Blended grain", "Blended malt", "Peated blend", "Peated blended malt", "Canadian", "Single Pot Still"),
        FLAVOURED("Flavoured", "Spiced/Flavoured"),
        BOURBON("Bourbon", "Tennessee"),
        RYE("Rye"),
        // non-Whisky spirits
        BRANDY("Brandy", "Armagnac", "Calvados", "Cognac"),
        VODKA("Vodka"),
        RUM("Rum"),
        TEQUILA("Tequila"),
        GIN("Gin"),
        MOONSHINE("Moonshine"),
        VINE("Vine"),
        BEER("Beer"),
        CIDER("Cider")
        //TODO: add more types here
        ;

        private final String[] type;

        private static SpiritType[] allWhiskyTypes = {WHISKY, S_M, BLENDED, FLAVOURED, BOURBON, RYE};

        SpiritType(String... type) {
            this.type = type;
        }

        public String toString() {
            return type[0];
        }

        public static SpiritType parse(String str) {
            for (SpiritType wt : SpiritType.values()) {
                if (Arrays.stream(wt.type).anyMatch(t -> t.equalsIgnoreCase(str))) {
                    return wt;
                }
            }
            return null;
        }

        public static boolean equals(String obj1, String obj2) {
            SpiritType c1 = SpiritType.parse(obj1);
            SpiritType c2 = SpiritType.parse(obj2);
            return c1 != null && c1.equals(c2);
        }

        public static boolean isWhisky(String type) {
            SpiritType wType = parse(type);
            return wType != null && Arrays.asList(allWhiskyTypes).contains(wType);
        }
    }

    public static class Age {
        private static final Pattern numbers = Pattern.compile("(\\d+\\.\\d+|\\d+)");  // integer and decimal numbers
        private static final Pattern xxYO = Pattern.compile("(?i)(\\d+\\s+YO)"); // ANBL whisky age notation
        private static final Pattern xxYear = Pattern.compile("(?i)(\\d+\\s+Year)"); // Distiller whisky age notation

        public static Integer parse(String str) {
            String tag = null;
            if (tag == null) {
                Matcher matcher = xxYO.matcher(str);
                while (matcher.find()) {
                    tag = matcher.group(1);
                }
            }
            if (tag == null) {
                Matcher matcher = xxYear.matcher(str);
                while (matcher.find()) {
                    tag = matcher.group(1);
                }
            }
            if (tag != null) {
                Matcher matcher = numbers.matcher(tag);
                while (matcher.find()) {
                    return Integer.parseInt(matcher.group(1));
                }
            }
            return null;
        }

        public static boolean equals(String obj1, String obj2) {
            Integer c1 = Age.parse(obj1);
            Integer c2 = Age.parse(obj2);
            return c1 != null && c1.equals(c2);
        }
    }
}
