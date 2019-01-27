package org.maggus.spirit.services;

import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract class AbstractParser {

    private final Pattern numbers = Pattern.compile("(\\d+\\.\\d+|\\d+)");  // integer and decimal numbers

    protected String getSafeElementText(Elements els) {
        try {
            return els.first().text().trim();
        } catch (NullPointerException ex) {
            return "";
        }
    }

    protected Integer getSafeElementInteger(Elements els) {
        return getSafeInteger(getNumberStr(getSafeElementText(els)));
    }

    protected Double getSafeElementDouble(Elements els) {
        return getSafeDouble(getNumberStr(getSafeElementText(els)));
    }

    protected Integer getSafeInteger(String str){
        try{
            return Integer.parseInt(str);
        }
        catch(Exception ex){
            return null;
        }
    }

    protected Double getSafeDouble(String str){
        try{
            return Double.parseDouble(str);
        }
        catch(Exception ex){
            return null;
        }
    }

    protected String getNumberStr(String str) {
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

    protected String simplifyWhiskyName(String name) {
        List<String> fixed = new ArrayList<>();
        String[] tags = name.split("\\s+");
        int lastNonDigitTagIdx = 0;
        for (int i = 0; i < tags.length; i++) {
            String tag = tags[i];
            if (i == 0) {
                // always use first word as is
                fixed.add(tag);
            } else {
                String digits = tag.replaceAll("[^\\d]+", "");
                if (digits.isEmpty()) {
                    lastNonDigitTagIdx = i;
                    fixed.add(tag); // candidate for truncation
                } else {
                    fixed.add(digits);  // add only numbers
                }
            }
        }
        // finally drop last word without any numbers in it
        if (lastNonDigitTagIdx > 0 && lastNonDigitTagIdx < fixed.size()) {
            fixed.remove(lastNonDigitTagIdx);
        }
        return name = String.join(" ", fixed);
    }
}
