package org.maggus.spirit.services;

import lombok.extern.java.Log;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.maggus.spirit.models.Locators;
import org.maggus.spirit.models.WarehouseQuantity;
import org.maggus.spirit.models.Whisky;
import org.maggus.spirit.models.WhiskyCategory;

import javax.ejb.Stateless;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Stateless
@Log
public class AnblParser extends AbstractParser {

    public enum CacheUrls {
        BASE_URL("http://www.anbl.com"),
        // Whisky
        // Scotch
        SCOTCH_SINGLE_MALTS(BASE_URL.getUrl() + "/scotch-single-malts-1"),
        SCOTCH_BLENDS(BASE_URL.getUrl() + "/scotch-whisky-blends-1"),
        SCOTCH_SM_ISLA(BASE_URL.getUrl() + "/islay"),
        SCOTCH_SM_HIGHLAND(BASE_URL.getUrl() + "/highland"),
        SCOTCH_SM_SPEYSIDE(BASE_URL.getUrl() + "/speyside-1"),
        SCOTCH_SM_ISLANDS(BASE_URL.getUrl() + "/islands-1"),
        SCOTCH_SM_LOWLAND(BASE_URL.getUrl() + "/lowland"),
        // Canadian
        CANADA_SM(BASE_URL.getUrl() + "/single-malt-grain-3"),
        CANADA_BLENDS(BASE_URL.getUrl() + "/blended-2"),
        CANADA_RYE(BASE_URL.getUrl() + "/rye-4"),
        CANADA_FLAVOURED(BASE_URL.getUrl() + "/spiced-flavoured-2"),
        // Ireland
        IRELAND_SM(BASE_URL.getUrl() + "/single-malt-grain-5"),
        IRELAND_BLENDS(BASE_URL.getUrl() + "/blended-3"),
        // Japan
        JAPAN_BLENDS(BASE_URL.getUrl() + "/japan-1"),
        // International
        INTERNATIONAL_W(BASE_URL.getUrl() + "/international-whisky-1"),
        // USA
        USA_RYE(BASE_URL.getUrl() + "/rye-1"),
        USA_BOURBON(BASE_URL.getUrl() + "/bourbon"),
        USA_FLAVOURED(BASE_URL.getUrl() + "/spiced-flavoured-3"),
        USA_TEN_BOURBON(BASE_URL.getUrl() + "/tennessee-sour-mash"),
        // Beer
        BEER_ALE(BASE_URL.getUrl() + "/ale-1"),
        BEER_HYBRID(BASE_URL.getUrl() + "/hybrid-2"),
        BEER_LAGER(BASE_URL.getUrl() + "/lager"),
        // Rum
        RUM(BASE_URL.getUrl() + "/rum-1"),
        // Tequila
        TEQUILA(BASE_URL.getUrl() + "/tequila"),
        // Gin
        GIN(BASE_URL.getUrl() + "/gin"),
        // Cider
        CIDER(BASE_URL.getUrl() + "/ciders-1"),
        // Brandy
        BRANDY(BASE_URL.getUrl() + "/brandy-4"),
        VODKA(BASE_URL.getUrl() + "/vodka"),
        ;

        private final String url;

        CacheUrls(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }
    }

    public AnblParser() {
        this(false);
    }

    public AnblParser(boolean isDebugEnabled) {
        super(isDebugEnabled);
    }

    public List<WhiskyCategory> buildProductsCategories(Locators.SpiritType... types) {
        List<WhiskyCategory> wcs = new ArrayList<>();

        for (Locators.SpiritType type : types) {
            if (Locators.SpiritType.WHISKY == type) {
                buildWhiskiesCategories(wcs);
            } else if (Locators.SpiritType.BEER == type) {
                buildBeersCategories(wcs);
            } else if (Locators.SpiritType.RUM == type) {
                buildRumCategories(wcs);
            } else if (Locators.SpiritType.TEQUILA == type) {
                buildTequilaCategories(wcs);
            } else if (Locators.SpiritType.GIN == type) {
                buildGinCategories(wcs);
            } else if (Locators.SpiritType.CIDER == type) {
                buildCiderCategories(wcs);
            } else if (Locators.SpiritType.BRANDY == type) {
                buildBrandyCategories(wcs);
            }else if (Locators.SpiritType.VODKA == type) {
                buildVodkaCategories(wcs);
            }
            //TODO: load more stuff here
        }

        return wcs;
    }

    private List<WhiskyCategory> buildWhiskiesCategories(List<WhiskyCategory> wcs) {
        // load Scotch
        wcs.add(new WhiskyCategory(CacheUrls.SCOTCH_BLENDS.name(), CacheUrls.SCOTCH_BLENDS.getUrl(),
                Locators.Country.UK.toString(), null, Locators.SpiritType.BLENDED.toString()));
        wcs.add(new WhiskyCategory(CacheUrls.SCOTCH_SM_ISLA.name(), CacheUrls.SCOTCH_SM_ISLA.getUrl(),
                Locators.Country.UK.toString(), Locators.Region.UK_ISLA.toString(), Locators.SpiritType.S_M.toString()));
        wcs.add(new WhiskyCategory(CacheUrls.SCOTCH_SM_HIGHLAND.name(), CacheUrls.SCOTCH_SM_HIGHLAND.getUrl(),
                Locators.Country.UK.toString(), Locators.Region.UK_HIGH.toString(), Locators.SpiritType.S_M.toString()));
        wcs.add(new WhiskyCategory(CacheUrls.SCOTCH_SM_SPEYSIDE.name(), CacheUrls.SCOTCH_SM_SPEYSIDE.getUrl(),
                Locators.Country.UK.toString(), Locators.Region.UK_SPEY.toString(), Locators.SpiritType.S_M.toString()));
        wcs.add(new WhiskyCategory(CacheUrls.SCOTCH_SM_ISLANDS.name(), CacheUrls.SCOTCH_SM_ISLANDS.getUrl(),
                Locators.Country.UK.toString(), Locators.Region.UK_ISLANDS.toString(), Locators.SpiritType.S_M.toString()));
        wcs.add(new WhiskyCategory(CacheUrls.SCOTCH_SM_LOWLAND.name(), CacheUrls.SCOTCH_SM_LOWLAND.getUrl(),
                Locators.Country.UK.toString(), Locators.Region.UK_LOW.toString(), Locators.SpiritType.S_M.toString()));
        // load Canadian
        wcs.add(new WhiskyCategory(CacheUrls.CANADA_SM.name(), CacheUrls.CANADA_SM.getUrl(),
                Locators.Country.CA.toString(), null, Locators.SpiritType.S_M.toString()));
        wcs.add(new WhiskyCategory(CacheUrls.CANADA_BLENDS.name(), CacheUrls.CANADA_BLENDS.getUrl(),
                Locators.Country.CA.toString(), null, Locators.SpiritType.BLENDED.toString()));
        wcs.add(new WhiskyCategory(CacheUrls.CANADA_RYE.name(), CacheUrls.CANADA_RYE.getUrl(),
                Locators.Country.CA.toString(), null, Locators.SpiritType.RYE.toString()));
        wcs.add(new WhiskyCategory(CacheUrls.CANADA_FLAVOURED.name(), CacheUrls.CANADA_FLAVOURED.getUrl(),
                Locators.Country.CA.toString(), null, Locators.SpiritType.FLAVOURED.toString()));
        // load Irish
        wcs.add(new WhiskyCategory(CacheUrls.IRELAND_SM.name(), CacheUrls.IRELAND_SM.getUrl(),
                Locators.Country.IR.toString(), null, Locators.SpiritType.S_M.toString()));
        wcs.add(new WhiskyCategory(CacheUrls.IRELAND_BLENDS.name(), CacheUrls.IRELAND_BLENDS.getUrl(),
                Locators.Country.IR.toString(), null, Locators.SpiritType.BLENDED.toString()));
        // load Japan
        wcs.add(new WhiskyCategory(CacheUrls.JAPAN_BLENDS.name(), CacheUrls.JAPAN_BLENDS.getUrl(),
                Locators.Country.JP.toString(), null, Locators.SpiritType.BLENDED.toString()));
        // load International
        wcs.add(new WhiskyCategory(CacheUrls.INTERNATIONAL_W.name(), CacheUrls.INTERNATIONAL_W.getUrl(),
                Locators.Country.INTERNATIONAL.toString(), null, Locators.SpiritType.WHISKY.toString()));
        // load American
        wcs.add(new WhiskyCategory(CacheUrls.USA_RYE.name(), CacheUrls.USA_RYE.getUrl(),
                Locators.Country.US.toString(), null, Locators.SpiritType.RYE.toString()));
        wcs.add(new WhiskyCategory(CacheUrls.USA_BOURBON.name(), CacheUrls.USA_BOURBON.getUrl(),
                Locators.Country.US.toString(), null, Locators.SpiritType.BOURBON.toString()));
        wcs.add(new WhiskyCategory(CacheUrls.USA_FLAVOURED.name(), CacheUrls.USA_FLAVOURED.getUrl(),
                Locators.Country.US.toString(), null, Locators.SpiritType.FLAVOURED.toString()));
        wcs.add(new WhiskyCategory(CacheUrls.USA_TEN_BOURBON.name(), CacheUrls.USA_TEN_BOURBON.getUrl(),
                Locators.Country.US.toString(), Locators.Region.US_TE.toString(), Locators.SpiritType.BOURBON.toString()));

        return wcs;
    }

    private List<WhiskyCategory> buildBeersCategories(List<WhiskyCategory> wcs) {
        wcs.add(new WhiskyCategory(CacheUrls.BEER_ALE.name(), CacheUrls.BEER_ALE.getUrl(), null, null,
                Locators.SpiritType.BEER.toString()));
        wcs.add(new WhiskyCategory(CacheUrls.BEER_HYBRID.name(), CacheUrls.BEER_HYBRID.getUrl(), null, null,
                Locators.SpiritType.BEER.toString()));
        wcs.add(new WhiskyCategory(CacheUrls.BEER_LAGER.name(), CacheUrls.BEER_LAGER.getUrl(), null, null,
                Locators.SpiritType.BEER.toString()));

        return wcs;
    }

    private List<WhiskyCategory> buildRumCategories(List<WhiskyCategory> wcs) {
        wcs.add(new WhiskyCategory(CacheUrls.RUM.name(), CacheUrls.RUM.getUrl(), null, null,
                Locators.SpiritType.RUM.toString()));

        return wcs;
    }

    private List<WhiskyCategory> buildTequilaCategories(List<WhiskyCategory> wcs) {
        wcs.add(new WhiskyCategory(CacheUrls.TEQUILA.name(), CacheUrls.TEQUILA.getUrl(), null, null,
                Locators.SpiritType.TEQUILA.toString()));

        return wcs;
    }

    private List<WhiskyCategory> buildGinCategories(List<WhiskyCategory> wcs) {
        wcs.add(new WhiskyCategory(CacheUrls.GIN.name(), CacheUrls.GIN.getUrl(), null, null,
                Locators.SpiritType.GIN.toString()));

        return wcs;
    }

    private List<WhiskyCategory> buildCiderCategories(List<WhiskyCategory> wcs) {
        wcs.add(new WhiskyCategory(CacheUrls.CIDER.name(), CacheUrls.CIDER.getUrl(), null, null,
                Locators.SpiritType.CIDER.toString()));

        return wcs;
    }

    private List<WhiskyCategory> buildBrandyCategories(List<WhiskyCategory> wcs) {
        wcs.add(new WhiskyCategory(CacheUrls.BRANDY.name(), CacheUrls.BRANDY.getUrl(), null, null,
                Locators.SpiritType.BRANDY.toString()));

        return wcs;
    }

    private List<WhiskyCategory> buildVodkaCategories(List<WhiskyCategory> wcs) {
        wcs.add(new WhiskyCategory(CacheUrls.VODKA.name(), CacheUrls.VODKA.getUrl(), null, null,
                Locators.SpiritType.VODKA.toString()));

        return wcs;
    }

    public List<Whisky> loadProductCategoryPage(String url) {
        List<Whisky> allWhisky = new ArrayList<Whisky>();
        try {
            //log.info("** Loading " + url);
            if (url == null) {
                throw new NullPointerException("url can not be null");
            }
            Document doc = loadDocument(Jsoup.connect(url)
                    .cookie("ProductListing_SortBy", "Value=title")
                    .cookie("ProductListing_DisplayMode", "Value=list")
                    .cookie("ProductListing_ResultPerPage", "Value=333"));        // ProductListing_SortBy=Value=title; ProductListing_DisplayMode=Value=list; ProductListing_ResultPerPage=Value=200;
            String title = doc.title();
            Elements products = doc.select("div.ejs-productitem");
            log.info("** Parsing page \"" + title + "\", found " + products.size() + " products");
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

        } catch (HttpStatusException ex) {
            log.log(Level.WARNING, "HTTP error: " + ex.getMessage() + " from " + url);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Failed to parse ANBL Product Category page: " + url, ex);
        }
        return allWhisky;
    }

    public boolean loadProductPage(Whisky whisky) {
        try {
            //log.info("** Loading " + whisky.getCacheExternalUrl());
            if (whisky.getCacheExternalUrl() == null) {
                throw new NullPointerException("cacheExternalUrl can not be null");
            }

            Document doc = loadDocument(Jsoup.connect(whisky.getCacheExternalUrl()));
            String title = doc.title();
            //log.info("* Parsing product page \"" + title + "\"");
            String type = parseProductType(doc.select("ul.breadcrumb a"), whisky.getType());
            String prodCode = getNumberStr(getSafeElementText(doc.select("p.product-details-code")));
            Double alcoholContent = getSafeElementDouble(doc.select("div.informationAttributesSection > ul > li:nth-child(1) > span.attribute-value"));
            Integer qtyPerContaner = getSafeElementInteger(doc.select("div.informationAttributesSection > ul > li:nth-child(2) > span.attribute-value"));
            Integer unitVolumeMl = getSafeElementInteger(doc.select("div.informationAttributesSection > ul > li:nth-child(3) > span.attribute-value"));
            String country = getSafeElementText(doc.select("div.informationAttributesSection > ul > li:nth-child(4) > span.attribute-value"));
            String description = getSafeElementText(doc.select("div.span6 > p"));
            Elements whRows = doc.select("div#ANBLSectionModalBody > table > tbody > tr");
            whisky.getQuantities().clear(); // just clear all old quantities and start fresh
            for (Element el : whRows) {
                String store = getSafeElementText(el.select("td.warehouseName > span"));
                String address = getSafeElementText(el.select("td.warehouseAddress > span"));
                String city = getSafeElementText(el.select("td.warehouseCity > span"));
                Integer qty = getSafeElementInteger(el.select("td.warehouseQty > span"));
                whisky.setStoreQuantity(new WarehouseQuantity(store, address, city, qty));
            }
            if (type != null && !type.isEmpty()) {
                whisky.setType(type);
            }
            whisky.setProductCode(prodCode);
            whisky.setAlcoholContent(alcoholContent);
            whisky.setQtyPerContainer(qtyPerContaner);
            if (whisky.getUnitVolumeMl() == null && unitVolumeMl != null) {
                whisky.setUnitVolumeMl(unitVolumeMl);
            }
            if (whisky.getCountry() == null && country != null) {
                whisky.setCountry(country);
            }
            whisky.setDescription(description);
            //log.info("* Parsing product page \"" + title + "\" Done.");
            return true;
        }
        catch(HttpStatusException ex){
            log.log(Level.WARNING, "HTTP error: " + ex.getMessage() + " from " + whisky.getCacheExternalUrl());
        }
        catch (Exception ex) {
            log.log(Level.SEVERE, "Failed to parse ANBL Product Details page: " + whisky.getCacheExternalUrl(), ex);
        }
        return false;
    }

    private String parseProductType(Elements elms, String customType) {
        List<String> types = new ArrayList<String>();
        boolean hasCustomType = false;
        for (Element el : elms) {
            String text = getSafeElementText(el);
            if (types.contains(text)
                    || "ANBL".equalsIgnoreCase(text)
                    || "Catalog".equalsIgnoreCase(text)
                    || "Spirits".equalsIgnoreCase(text)) {
                continue;
            }
            if (customType != null && Locators.SpiritType.hasType(text, customType)) {
                hasCustomType = true;
            }
            types.add(text);
        }
        if (!hasCustomType && customType != null && !customType.contains(",")) {
            types.add(0, customType);
        }
        return types.stream().collect(Collectors.joining(", "));
    }

    private Whisky parseBasicProduct(Element el) throws Exception {
        String thumbnailUrl = el.select("li.product-photo > img").first().attr("src");
        String fullName = el.select("li.product-title > a").first().attr("title");
        String name = fixName(fullName);
        Integer volume = fixVolume(fullName);
        String detailsUrl = fixDetailsUrl(el.select("li.product-title > a").first().attr("href"));
        BigDecimal price = fixPrice(getSafeElementText(el.select("span.price")));
        Whisky whisky = new Whisky(name, volume, price);
        whisky.setThumbnailUrl(thumbnailUrl);
        whisky.setCacheExternalUrl(detailsUrl);
        //log.info("* " + whisky.toString());
        return whisky;
    }

    private String fixName(String fullName) throws Exception {
        String[] tokens = fullName.split("\\s+");
        if (tokens.length > 1) {
            Integer volume = getSafeInteger(getNumberStr(tokens[tokens.length - 1]));
            if (volume != null) {
                tokens[tokens.length - 1] = "";
            }
        }
        return String.join(" ", tokens).trim();
    }

    private Integer fixVolume(String fullName) throws Exception {
        String[] tokens = fullName.split("\\s+");
        if (tokens.length <= 1) {
            throw new IllegalArgumentException("Can not extract Volume from full name: " + fullName);
        }
        return getSafeInteger(getNumberStr(tokens[tokens.length - 1]));
    }

    private BigDecimal fixPrice(String priceStr) throws Exception {
        String numberStr = getNumberStr(priceStr);
        return !numberStr.isEmpty() ? new BigDecimal(numberStr) : null;
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
