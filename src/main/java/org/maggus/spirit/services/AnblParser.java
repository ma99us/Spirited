package org.maggus.spirit.services;

import lombok.extern.java.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.maggus.spirit.models.Locators;
import org.maggus.spirit.models.Warehouse;
import org.maggus.spirit.models.Whisky;

import javax.ejb.Stateless;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Stateless
@Log
public class AnblParser {

    public enum CacheUrls {
        BASE_URL("http://www.anbl.com"),
        SCOTCH_SINGLE_MALTS(BASE_URL.getUrl() + "/scotch-single-malts-1"),
        SCOTCH_BLENDS(BASE_URL.getUrl() + "/scotch-whisky-blends-1"),
        SCOTCH_SM_ISLA(BASE_URL.getUrl() + "/islay"),
        SCOTCH_SM_HIGHLAND(BASE_URL.getUrl() + "/highland"),
        SCOTCH_SM_SPEYSIDE(BASE_URL.getUrl() + "/speyside-1"),
        SCOTCH_SM_ISLANDS(BASE_URL.getUrl() + "/islands-1"),
        SCOTCH_SM_LOWLAND(BASE_URL.getUrl() + "/lowland");

        private final String url;

        CacheUrls(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }
    }

    private final Pattern numbers = Pattern.compile("(\\d+\\.\\d+|\\d+)");  // integer and decimal numbers

    public List<Whisky> loadProductsCategories() {
        //TODO: figure out better way to configure this
        List<Whisky> allWhiskies = new ArrayList<Whisky>();

        log.info("*** Loading all ANBL products categories");
        long t0 = System.currentTimeMillis();

        // load Blended
        List<Whisky> whiskies = loadProductCategoryPage(CacheUrls.SCOTCH_BLENDS.getUrl());
        for (Whisky w : whiskies) {
            w.setCountry(Locators.Country.UK.toString());
            w.setType(Locators.WhiskyType.BLENDED.toString());
        }
        allWhiskies.addAll(whiskies);

        // load Isla
        whiskies = loadProductCategoryPage(CacheUrls.SCOTCH_SM_ISLA.getUrl());
        for (Whisky w : whiskies) {
            w.setCountry(Locators.Country.UK.toString());
            w.setType(Locators.WhiskyType.S_M.toString());
            w.setRegion(Locators.Region.UK_ISLA.toString());
        }
        allWhiskies.addAll(whiskies);

        // load Highland
        whiskies = loadProductCategoryPage(CacheUrls.SCOTCH_SM_HIGHLAND.getUrl());
        for (Whisky w : whiskies) {
            w.setCountry(Locators.Country.UK.toString());
            w.setType(Locators.WhiskyType.S_M.toString());
            w.setRegion(Locators.Region.UK_HIGH.toString());
        }
        allWhiskies.addAll(whiskies);

        // load Speyside
        whiskies = loadProductCategoryPage(CacheUrls.SCOTCH_SM_SPEYSIDE.getUrl());
        for (Whisky w : whiskies) {
            w.setCountry(Locators.Country.UK.toString());
            w.setType(Locators.WhiskyType.S_M.toString());
            w.setRegion(Locators.Region.UK_SPEY.toString());
        }
        allWhiskies.addAll(whiskies);

        // load Islands
        whiskies = loadProductCategoryPage(CacheUrls.SCOTCH_SM_ISLANDS.getUrl());
        for (Whisky w : whiskies) {
            w.setCountry(Locators.Country.UK.toString());
            w.setType(Locators.WhiskyType.S_M.toString());
            w.setRegion(Locators.Region.UK_ISLANDS.toString());
        }
        allWhiskies.addAll(whiskies);

        // load Lowland
        whiskies = loadProductCategoryPage(CacheUrls.SCOTCH_SM_LOWLAND.getUrl());
        for (Whisky w : whiskies) {
            w.setCountry(Locators.Country.UK.toString());
            w.setType(Locators.WhiskyType.S_M.toString());
            w.setRegion(Locators.Region.UK_LOW.toString());
        }
        allWhiskies.addAll(whiskies);

        //TODO: load more stuff here
        long dt = System.currentTimeMillis() - t0;
        log.info("*** Done loading all ANBL product categories. Done in " + dt / 1000 + " seconds");

        return allWhiskies;
    }

    public List<Whisky> loadProductCategoryPage(String url) {
        try {
            log.info("** Loading " + url);
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
                        allWhisky.add(parseBasicProduct(element));
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

    public void loadProduct(Whisky whisky) {
        try{
            //log.info("** Loading " + whisky.getCacheExternalUrl());
            Document doc = Jsoup.connect(whisky.getCacheExternalUrl()).get();
            String title = doc.title();
            log.info("* Parsing product page \"" + title + "\"");
            String prodCode = getNumberStr(getSafeFirstElementText(doc.select("p.product-details-code")));
            Double alcoholContent = Double.parseDouble(getNumberStr(getSafeFirstElementText(doc.select("div.informationAttributesSection span.attribute-value"))));
            String description = getSafeFirstElementText(doc.select("div.span6 > p"));
            Elements whRows = doc.select("div#ANBLSectionModalBody > table > tbody > tr");
            whisky.getQuantities().clear(); // just clear all old quantities and start fresh
            for(Element el : whRows){
                String store = getSafeFirstElementText(el.select("td.warehouseName > span"));
                String address = getSafeFirstElementText(el.select("td.warehouseAddress > span"));
                String city = getSafeFirstElementText(el.select("td.warehouseCity > span"));
                Integer qty = Integer.parseInt(getSafeFirstElementText(el.select("td.warehouseQty > span")));
                whisky.setStoreQuantity(new Warehouse(store, address, city), qty);
            }
            whisky.setAnblProdCode(prodCode);
            whisky.setAlcoholContent(alcoholContent);
            whisky.setDescription(description);
            //log.info("* Parsing product page \"" + title + "\" Done.");
        }
        catch(Exception ex){
            log.log(Level.SEVERE, "Failed to parse ANBL Product page: " + whisky.getCacheExternalUrl(), ex);
        }
    }

    private Whisky parseBasicProduct(Element el) throws Exception {
        String thumbnailUrl = el.select("li.product-photo > img").first().attr("src");
        String fullName = el.select("li.product-title > a").first().attr("title");
        String name = fixName(fullName);
        Integer volume = fixVolume(fullName);
        String detailsUrl = fixDetailsUrl(el.select("li.product-title > a").first().attr("href"));
        BigDecimal price = fixPrice(el.select("span.price").first().text());
        Whisky whisky = new Whisky(name, volume, price);
        whisky.setThumbnailUrl(thumbnailUrl);
        whisky.setCacheExternalUrl(detailsUrl);
        //log.info("* " + whisky.toString());
        return whisky;
    }

    private String getSafeFirstElementText(Elements els){
        try{
            return els.first().text();
        }
        catch(NullPointerException ex){
            return "";
        }
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
        if(str == null){
            return digits;
        }
        Matcher matcher = numbers.matcher(str);
        while (matcher.find()) {
            digits += matcher.group(1);
        }
        return digits;
    }

    private String fixDetailsUrl(String url) throws Exception {
        if (url != null && !url.startsWith("http")) {
            String prefix = CacheUrls.BASE_URL.getUrl();
            if (!url.startsWith("/")) {
                prefix += "/";
            }
            url = prefix + url;
        }
        return url;
    }

}
