package org.maggus.spirit.services;

import lombok.extern.java.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.maggus.spirit.models.Locators;
import org.maggus.spirit.models.Whisky;

import javax.ejb.Stateless;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Stateless
@Log
public class AnblParser {

    public static final String BASE_URL = "http://www.anbl.com";
    public static final String SCOTCH_SINGLE_MALTS = BASE_URL + "/scotch-single-malts-1";
    public static final String SCOTCH_BLENDS = BASE_URL + "/scotch-whisky-blends-1";
    public static final String SCOTCH_SM_ISLA = BASE_URL + "/islay";
    public static final String SCOTCH_SM_HIGHLAND = BASE_URL + "/highland";
    public static final String SCOTCH_SM_SPEYSIDE = BASE_URL + "/speyside-1";
    public static final String SCOTCH_SM_ISLANDS = BASE_URL + "/islands-1";
    public static final String SCOTCH_SM_LOWLAND = BASE_URL + "/lowland";

    private final Pattern numbers = Pattern.compile("(\\d+\\.\\d+|\\d+)");  // integer and decimal numbers

    public List<Whisky> loadProductsCategories() {
        //TODO: figure out better way to configure this
        List<Whisky> allWhiskies = new ArrayList<Whisky>();

        log.info("*** Loading all ANBL products");
        long t0 = System.currentTimeMillis();

        // load Blended
        List<Whisky> whiskies = loadProductPage(SCOTCH_BLENDS);
        for (Whisky w : whiskies) {
            w.setCountry(Locators.Country.UK.toString());
            w.setType(Locators.WhiskyType.BLENDED.toString());
        }
        allWhiskies.addAll(whiskies);

        // load Isla
        whiskies = loadProductPage(SCOTCH_SM_ISLA);
        for (Whisky w : whiskies) {
            w.setCountry(Locators.Country.UK.toString());
            w.setType(Locators.WhiskyType.S_M.toString());
            w.setRegion(Locators.Region.UK_ISLA.toString());
        }
        allWhiskies.addAll(whiskies);

        // load Highland
        whiskies = loadProductPage(SCOTCH_SM_HIGHLAND);
        for (Whisky w : whiskies) {
            w.setCountry(Locators.Country.UK.toString());
            w.setType(Locators.WhiskyType.S_M.toString());
            w.setRegion(Locators.Region.UK_HIGH.toString());
        }
        allWhiskies.addAll(whiskies);

        // load Speyside
        whiskies = loadProductPage(SCOTCH_SM_SPEYSIDE);
        for (Whisky w : whiskies) {
            w.setCountry(Locators.Country.UK.toString());
            w.setType(Locators.WhiskyType.S_M.toString());
            w.setRegion(Locators.Region.UK_SPEY.toString());
        }
        allWhiskies.addAll(whiskies);

        // load Islands
        whiskies = loadProductPage(SCOTCH_SM_ISLANDS);
        for (Whisky w : whiskies) {
            w.setCountry(Locators.Country.UK.toString());
            w.setType(Locators.WhiskyType.S_M.toString());
            w.setRegion(Locators.Region.UK_ISLANDS.toString());
        }
        allWhiskies.addAll(whiskies);

        // load Lowland
        whiskies = loadProductPage(SCOTCH_SM_LOWLAND);
        for (Whisky w : whiskies) {
            w.setCountry(Locators.Country.UK.toString());
            w.setType(Locators.WhiskyType.S_M.toString());
            w.setRegion(Locators.Region.UK_LOW.toString());
        }
        allWhiskies.addAll(whiskies);

        //TODO: load more stuff here
        long dt = System.currentTimeMillis() - t0;
        log.info("*** Done loading all ANBL products. Done in " + dt / 1000 + " seconds");

        return allWhiskies;
    }

    public List<Whisky> loadProductPage(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .cookie("ProductListing_SortBy", "Value=title")
                    .cookie("ProductListing_DisplayMode", "Value=list")
                    .cookie("ProductListing_ResultPerPage", "Value=333")
                    .get();      // ProductListing_SortBy=Value=title; ProductListing_DisplayMode=Value=list; ProductListing_ResultPerPage=Value=200;
            String title = doc.title();
            Elements products = doc.select("div.ejs-productitem");
            log.info("** Parsing page \"" + title + "\", found " + products.size() + " products");
            List<Whisky> allWhisky = new ArrayList<Whisky>();
            products.forEach(new Consumer<Element>() {
                @Override
                public void accept(Element element) {
                    try {
                        allWhisky.add(parseProduct(element));
                    } catch (Exception ex) {
                        log.log(Level.WARNING, "Failed to parse Product element: " + element, ex);
                    }
                }
            });
            log.info("** Parsing done with " + allWhisky.size() + " results");
            return allWhisky;
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Failed to parse ANBL page: " + url, ex);
            return null;
        }
    }

    private Whisky parseProduct(Element el) throws Exception {
        String thumbnailUrl = el.select("li.product-photo > img").first().attr("src");
        String fullName = el.select("li.product-title > a").first().attr("title");
        String name = fixName(fullName);
        Integer volume = fixVolume(fullName);
        String detailsUrl = fixDetailsUrl(el.select("li.product-title > a").first().attr("href"));
        BigDecimal price = fixPrice(el.select("span.price").first().text());
        Whisky whisky = new Whisky(name, volume, price);
        whisky.setThumbnailUrl(thumbnailUrl);
        whisky.setAnblUrl(detailsUrl);
        log.info("* " + whisky.toString());
        return whisky;
    }

    private String fixName(String fullName) throws Exception {
        String[] tokens = fullName.split("\\s+");
        if (tokens.length > 1) {
            int volume = Integer.parseInt(getNumberStr(tokens[tokens.length - 1]));
            tokens[tokens.length - 1] = "";
        }
        return String.join(" ", tokens).trim();
    }

    private Integer fixVolume(String fullName) throws Exception {
        String[] tokens = fullName.split("\\s+");
        if (tokens.length <= 1) {
            throw new IllegalArgumentException("Can not extract Volume from full name: " + fullName);
        }
        return Integer.parseInt(getNumberStr(tokens[tokens.length - 1]));
    }

    private BigDecimal fixPrice(String priceStr) throws Exception {
        return new BigDecimal(getNumberStr(priceStr));
    }

    private String getNumberStr(String str) {
        String digits = "";
        Matcher matcher = numbers.matcher(str);
        while (matcher.find()) {
            digits += matcher.group(1);
        }
        return digits;
    }

    private String fixDetailsUrl(String url) throws Exception {
        if (!url.startsWith("http")) {
            String prefix = BASE_URL;
            if (!url.startsWith("/")) {
                prefix += "/";
            }
            url = prefix + url;
        }
        return url;
    }

}
