package org.maggus.spirit.services;

import lombok.extern.java.Log;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.maggus.spirit.models.FlavorProfile;
import org.maggus.spirit.models.Locators;
import org.maggus.spirit.models.Whisky;

import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.logging.Level;

@Stateless
@Log
public class DistillerParser extends AbstractParser {

    public static final String BASE_URL = "https://distiller.com";

    public DistillerParser(){
        this(false);
    }

    public DistillerParser(boolean isDebugEnabled) {
        super(isDebugEnabled);
    }

    protected void prepareConnection(HttpURLConnection conn) throws ProtocolException {
        conn.setRequestMethod("GET");
        conn.setRequestProperty("accept", "application/json,text/*");
        conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");
        conn.setRequestProperty("referer", "https://distiller.com/search");
        conn.setRequestProperty("x-distiller-developer-token", "8e01b58d-6bc8-407e-b7fb-5b989b5b23e9");
    }

    protected String readResponse(HttpURLConnection conn) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuffer resp = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
            resp.append(line);
        }
        reader.close();
        return resp.toString();
    }

    protected String urlEncodeSearchParameters(String findName) throws UnsupportedEncodingException {
        List<String> fixTags = new ArrayList<>();
        String[] tags = findName.split("\\s+");
        for (String tag : tags) {
            fixTags.add(URLEncoder.encode(tag, "UTF-8"));
        }
        return String.join("+", fixTags);
    }

    public List<FlavorProfile> loadAllProducts() {
        URL url = null;
        try {
            url = new URL(BASE_URL + "/api/v1/spirits");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            prepareConnection(conn);
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new IOException("bad http response: " + responseCode);
            }
            JsonReader jsonReader = Json.createReader(new StringReader(readResponse(conn)));
            JsonArray json = jsonReader.readArray();
            jsonReader.close();
            log.info("* Parsing external API response from " + url + ", found " + json.size() + " products");
            List<FlavorProfile> flavors = new ArrayList<>();
            for (int i = 0, size = json.size(); i < size; i++) {
                JsonObject item = json.getJsonObject(i);
                String name = item.getString("name");
                String extUrl = BASE_URL + "/spirits/" + item.getString("slug");
                flavors.add(new FlavorProfile(name, extUrl));
            }
            return flavors;
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Failed to parse All Products page: " + url, ex);
            return null;
        }
    }

    private String cleanupInputWhiskyName(String name) {
        name = name.replaceAll("\\s+YO", " Year");
//        name = name.replaceAll("(?i)\\s+Scotch\\s+", " ");
//        name = name.replaceAll("(?i)\\s+Single Malt\\s+", " ");
        return name.trim();
    }

    private final String[] junks = {"\\(.*\\)", "Single Malt", "Blended Malt", "Speyside Whisky",
            "Single Pot Still", "Irish Whiskey", "1st Release", "2nd Release", "3rd Release", "4th Release",
            "First Editions", "Original", "Speyside", "Canadian", "Japanese", "Blended", "Scotch", "Whisky",
            "Whiskey", "Strength", "Gaelic", "Kentucky", "Bourbon", "Discovery", "Limited", "Edition", "The",
            "Vintage", "Old", "NO" };

    private double fuzzyMatchNames(String name1, String name2, boolean doFilterJunk) {
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
                    likeness += -tag1.length();
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
        if (dist > 0 && doFilterJunk) {
            // try to cleanup the names and compare again
            String adjName1 = name1;
            String adjName2 = name2;
            for (String junk : junks) {
                adjName1 = adjName1.replaceAll("(?i)\\s*" + junk + "\\s*", " ");
                adjName2 = adjName2.replaceAll("(?i)\\s*" + junk + "\\s*", " ");
            }
            adjName1 = adjName1.trim();
            adjName2 = adjName2.trim();
            int adjLikeness = ld.apply(adjName1, adjName2);
            //if (adjLikeness == 0) {   // use that only if exact match
                //log.info("\t adjusting 'junk' likeness: " + likeness + " => " + adjLikeness + "; \"" + adjName1 + "\" => \"" + adjName2 + "\"");
                dist = adjLikeness;
            //}
        }
        likeness += dist;
        return likeness/Math.max(name1Len, name2Len);
    }

    private Document loadSearchPage(String url) throws IOException {
        Document doc = loadDocument(Jsoup.connect(url)
                .cookie("distiller_default_spirit_family", "whiskey")
                .referrer("https://distiller.com/search"));
        return doc;
    }

    public Map<Double, FlavorProfile> searchSingleProduct(String findName, String type, String country, String region, Integer age) {
        String url = null;
        try {
            //String nextPageUrl = null;
            Map<Double, FlavorProfile> candidates = new TreeMap<>();
            do {
                if(url == null){
                    url = BASE_URL + "/search?term=" + urlEncodeSearchParameters(findName) + "&official_status=official";
                }
                Document doc = loadSearchPage(url);
                Elements pagination = doc.select("div.pagination-control-container > div > nav > span.next > a");

                if (pagination != null && !pagination.isEmpty()) {
                    url = BASE_URL + pagination.first().attr("href");
                } else {
                    url = null;
                }
                Elements spirits = doc.select("ol.spirits").select("li.spirit");
                if (spirits == null || spirits.isEmpty()) {
                    //log.warning("No Such Product found: \"" + findName + "\"");
                    return null;
                }
                // search for best matching whisky in result list
                for (Element spirit : spirits) {
                    String itemName = getSafeElementText(spirit.select("div.name"));
                    String originTxt = getSafeElementText(spirit.select("p.origin"));
                    String[] tags = originTxt.split(",");
                    String itemType = null;
                    String itemCountry = null;
                    String itemRegion = null;
                    String itemExtUrl = BASE_URL + spirit.select("a").first().attr("href");
                    if (tags.length == 5) {
                        itemType = tags[0].trim();
                        itemRegion = tags[1].trim() + " " + tags[2].trim() + " " + tags[3].trim();
                        itemCountry = tags[4].trim();
                    } else if (tags.length == 4) {
                        itemType = tags[0].trim();
                        itemRegion = tags[1].trim() + " " + tags[2].trim();
                        itemCountry = tags[3].trim();
                    } else if (tags.length == 3) {
                        itemType = tags[0].trim();
                        itemRegion = tags[1].trim();
                        itemCountry = tags[2].trim();
                    } else if (tags.length == 2) {
                        itemType = tags[0].trim();
                        itemCountry = tags[1].trim();
                    } else {
                        throw new IllegalArgumentException("Unexpected Origin: " + originTxt);
                    }
                    Integer itemAge = Locators.Age.parse(itemName);
                    // calculate 'likeness' factor for each search result
                    double likeness = fuzzyMatchNames(findName, itemName, true);    // fuzzy match names
                    if (country != null && !Locators.Country.equals(country, itemCountry)) {
                        likeness += 0.5;  // mismatch country is a big deal
                    }
                    if (type != null && !Locators.SpiritType.equals(type, itemType)) {
                        if (Locators.Country.equals("United Kingdom", itemCountry)) {
                            likeness += 0.4;  // mismatch type is quite a big deal, but only for Scotch
                        } else {
                            likeness += 0.2;  // otherwise, not a big deal
                        }
                    }
                    if (region != null && !Locators.Region.equals(region, itemRegion)) {
                        likeness += 0.1;  // mismatch region is not a big deal
                    }
                    if (age != null && !age.equals(itemAge)) {
                        likeness += 0.2;  // mismatch age is more important then region
                    }
                    if (candidates.containsKey(likeness)) {
                        likeness += 0.01;  // if same likeness already exists, it should maintain precedence
                    }
                    candidates.put(likeness, new FlavorProfile(itemName, itemExtUrl, likeness));
                }
            } while (url != null);
            if (candidates.isEmpty()) {
                //log.warning("No Such Product found: \"" + findName + "\"");
                return null;
            }
            return candidates;
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Failed to parse Product Search page: " + url, ex);
            return null;
        }
    }

    public FlavorProfile fuzzySearchFlavorProfile(Whisky whisky) {
        String name = cleanupInputWhiskyName(whisky.getName());// fix ANBL whisky names to match Distiller
        Map<Double, FlavorProfile> bestCandidates = new TreeMap<>();
        FlavorProfile fp = null;
        do {
            Map<Double, FlavorProfile> candidates = searchSingleProduct(name, whisky.getType(), whisky.getCountry(), whisky.getRegion(), Locators.Age.parse(whisky.getName()));// find on a external site
            if (candidates != null) {
                //judge the overall match quality of the 'top' candidate, and ignore apparent 'bad' matches by returning null
                Map.Entry<Double, FlavorProfile> first = candidates.entrySet().iterator().next();
                double minLikeness = first.getKey();
                FlavorProfile foundFP = first.getValue();
                int words = name.split("\\s+").length;
                boolean isGood = minLikeness < -0.2;    //FIXME: should not be hardcoded?
                if (isDebugEnabled) {
                    log.info("* FP lookup; \"" + name + "\" => \"" + foundFP.getName() + "\", likeness=" + minLikeness
                            + "; candidates: " + candidates.size() + "; words count: " + words
                            + " - " + (isGood ? "GOOD" : "BAD"));
                }
                bestCandidates.put(minLikeness, foundFP);
                fp = isGood ? foundFP : null;
            }
            else{
                if (isDebugEnabled) {
                    log.info("* FP lookup; \"" + name + "\" => -no results- ");
                }
            }

            String simName = simplifyWhiskyName(name);
            if (simName.equals(name)) {
                break;  // search string can not be simplified anymore, we are done
            } else {
                name = simName;
            }
        } while (fp == null);
        if(fp == null && !bestCandidates.isEmpty()){
            Map.Entry<Double, FlavorProfile> first = bestCandidates.entrySet().iterator().next();
            FlavorProfile fp1 = first.getValue();
            if(fp1.getLikeness() < 0.2) {
                if (isDebugEnabled) {
                    log.info("* Using candidate FP for: \"" + whisky.getName() + "\" !=> \"" + fp1.getName() + "\", likeness=" + fp1.getLikeness());
                }
                fp = fp1;
            }
        }
        if(isDebugEnabled && fp == null){
            log.warning("* NO matching FP for: \"" + whisky.getName() + "\"");
        }
        return fp;
    }

    private String simplifyWhiskyName(String name) {
        // remove junk first
        name = name.trim();
        String adjName = name;
        for (String junk : junks) {
            adjName = adjName.replaceAll("(?i)\\s*" + junk + "\\s*", " ");
        }
        adjName = adjName.trim();
        if(!adjName.equals(name)){
            return adjName;
        }
        // if no junk, start to remove words form the end
        List<String> fixed = new ArrayList<>();
        String[] tags = name.split("\\s+");
        int lastNonDigitTagIdx = 0;
        for (int i = 0; i < tags.length; i++) {
            String tag = tags[i];
            if (i == 0) {
                // always use first word as is
                fixed.add(tag);
            } else {
                fixed.add(tag);
                if (tag.matches("\\d+")/* || (tags[i - 1].matches("\\d+") && tag.equalsIgnoreCase("Year"))*/) {
                } else {
                    lastNonDigitTagIdx = i; // candidate for truncation
                }
            }
        }
        // finally drop last word without any numbers in it
        if (lastNonDigitTagIdx > 0 && lastNonDigitTagIdx < fixed.size()) {
            fixed.remove(lastNonDigitTagIdx);
        }
        return name = String.join(" ", fixed);
    }

    public void loadFlavorProfile(FlavorProfile fp) {
        try {
            Document doc = loadDocument(Jsoup.connect(fp.getCacheExternalUrl())
                    .header("referer", "https://distiller.com/search")
                    .header("x-distiller-developer-token", "8e01b58d-6bc8-407e-b7fb-5b989b5b23e9"));
            String title = doc.title();
            // parse distiller score
            Integer distScore = getSafeElementInteger(doc.select("li.stat.distiller-rating > div > span.expert-rating"));
            fp.setScore(distScore);
            // parse average rating
            Integer ratingCount = getSafeElementInteger(doc.select("div.total-ratings > span"));
            if (ratingCount != null && (int) ratingCount > 5) {    // do not count ratings from too few reviews
                Double avgRating = getSafeElementDouble(doc.select("div.average-rating > span"));
                fp.setRating(avgRating);
            }
            // parse cask type
            String caskType = getSafeElementText(doc.select("li.detail.cask-type > div.value"));
            fp.setCaskType(caskType);
            // parse actual flavor profile chart data
            Elements div = doc.select("div.flavor-profile");
            if (div == null || div.isEmpty() || div.first() == null) {
                //log.warning("Product \"" + fp.getName() + "\" does not have Flavor Profile");
                return;
            }
            Element flProfDiv = div.first();
            String flavors = getSafeElementText(flProfDiv.select("h3.flavors"));
            String flChartData = flProfDiv.select("canvas.js-flavor-profile-chart").first().attr("data-flavors");
            JsonReader jsonReader = Json.createReader(new StringReader(flChartData));
            JsonObject json = jsonReader.readObject();
            jsonReader.close();
            fp.setFlavors(flavors);
            fp.setSmoky(json.getInt("smoky", 0));
            fp.setPeaty(json.getInt("peaty", 0));
            fp.setSpicy(json.getInt("spicy", 0));
            fp.setHerbal(json.getInt("herbal", 0));
            fp.setOily(json.getInt("oily", 0));
            fp.setFull_bodied(json.getInt("full_bodied", 0));
            fp.setRich(json.getInt("rich", 0));
            fp.setSweet(json.getInt("sweet", 0));
            fp.setBriny(json.getInt("briny", 0));
            fp.setSalty(json.getInt("salty", 0));
            fp.setVanilla(json.getInt("vanilla", 0));
            fp.setTart(json.getInt("tart", 0));
            fp.setFruity(json.getInt("fruity", 0));
            fp.setFloral(json.getInt("floral", 0));
            //log.info("* Parsing external API response from " + fp.getCacheExternalUrl() + " - \"" + title + "\", resulting in: " + fp);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Failed to parse Product Flavor Chart page: " + fp.getCacheExternalUrl(), ex);
        }
    }
}
