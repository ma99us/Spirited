package org.maggus.spirit.services;

import lombok.extern.java.Log;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log
abstract class AbstractParser {

    protected final boolean isDebugEnabled;

    protected AbstractParser(boolean isDebugEnabled) {
        this.isDebugEnabled = isDebugEnabled;
    }

    private final static Pattern numbers = Pattern.compile("(\\d+\\.\\d+|\\d+)");  // integer and decimal numbers

    protected Document loadDocument(Connection conn) throws IOException {
        Document doc = null;
        final int TIMEOUT = 60 * 1000;
        int retries = 3;
        while (doc == null && retries > 0) {
            try {
                doc = conn.timeout(TIMEOUT).get();    // connect and read with custom timeout
            } catch (SocketTimeoutException ex) {
                retries--;
                log.warning("SocketTimeoutException from " + conn + "; retrying...");
                try {
                    Thread.sleep(15 * 1000);    // wait a bit before retrying
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
        if (doc == null) {
            throw new SocketTimeoutException("Can not load " + conn);
        }
        return doc;
    }

    public static String getSafeElementText(Element el) {
        try {
            return el.text().trim();
        } catch (NullPointerException ex) {
            return "";
        }
    }

    public static String getSafeElementText(Elements els) {
        try {
            return getSafeElementText(els.first());
        } catch (NullPointerException ex) {
            return "";
        }
    }

    protected Integer getSafeElementInteger(Elements els) {
        return getSafeInteger(getNumberStr(getSafeElementText(els)));
    }

    public static Double getSafeElementDouble(Elements els) {
        return getSafeDouble(getNumberStr(getSafeElementText(els)));
    }

    public static Integer getSafeInteger(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception ex) {
            return null;
        }
    }

    public static Double getSafeDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (Exception ex) {
            return null;
        }
    }

    public static String getNumberStr(String str) {
        String digits = "";
        if (str == null) {
            return digits;
        }
        Matcher matcher = numbers.matcher(str);
        while (matcher.find()) {
            digits += matcher.group(1);
        }
        return digits;
    }

    public static double fuzzyMatchNames(String name1, String name2) {
        LevenshteinDistance ld = LevenshteinDistance.getDefaultInstance();
        int name1Len = name1.replaceAll("\\s+", "").length();
        int name2Len = name2.replaceAll("\\s+", "").length();
        List<String> split1 = new LinkedList<String>(Arrays.asList(name1.toUpperCase().trim().split("\\s+")));
        List<String> split2 = new LinkedList<String>(Arrays.asList(name2.toUpperCase().trim().split("\\s+")));
        double likeness = 0;
        // remove exact matches first
        for (ListIterator<String> iter1 = split1.listIterator(); iter1.hasNext(); ) {
            String tag1 = iter1.next();
            for (ListIterator<String> iter2 = split2.listIterator(); iter2.hasNext(); ) {
                String tag2 = iter2.next();
                int dist = ld.apply(tag1, tag2);
                if ((double) dist / tag1.length() <= 0.2) {   // 'exact' match
                    int multi = 1;
                    if (tag1.length() >= 4 && dist == 0) {
                        multi = 4;  // longer exact matches "worth" a lot of likeness
                    } else if (dist == 0) {
                        multi = 2;  // exact matches "worth" a bit more of likeness
                    }
                    likeness += -tag1.length() * multi;
                    iter1.remove();
                    iter2.remove();
                    break;
                } else {
                    Integer num1 = getSafeInteger(getNumberStr(tag1));
                    Integer num2 = getSafeInteger(getNumberStr(tag2));
                    if (num1 != null && num2 != null && !num1.equals(num2)) {
                        double abs = Math.abs((double) (num1 - num2) / Math.max(num1, num2));   // 0(match)...<1.0(mismatch)
                        //log.info("\t adjusting 'number' likeness: " + likeness + " => " + (likeness - (1 - abs)) + "; \"" + split1[i] + "\" => \"" + split2[i] + "\"");
                        likeness += tag1.length() * abs;
                        iter1.remove();
                        iter2.remove();
                        break;
                    }
                }
            }
        }
        // compare remaining mismatches now
        name1 = String.join(" ", split1).trim();
        name2 = String.join(" ", split2).trim();
        int dist = ld.apply(name1, name2);
        likeness += dist;
        return likeness / Math.max(name1Len, name2Len);
    }
}
