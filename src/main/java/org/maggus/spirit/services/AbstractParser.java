package org.maggus.spirit.services;

import org.jsoup.select.Elements;

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
}
