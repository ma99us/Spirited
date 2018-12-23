package org.maggus.spirit.services;

import lombok.extern.java.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.maggus.spirit.models.FlavorProfile;
import org.maggus.spirit.models.Locators;

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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Stateless
@Log
public class DistillerParser {

    public static final String BASE_URL = "https://distiller.com";

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

    protected String fixSearchParameters(String findName) throws UnsupportedEncodingException {
        List<String> fixTags = new ArrayList<>();
        String[] tags = findName.split("\\s+");
        for(String tag : tags){
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

    public FlavorProfile searchSingleProduct(String findName, String type, String country) {
        URL url = null;
        try {
            url = new URL(BASE_URL + "/api/v1/spirits/search?term=" + fixSearchParameters(findName));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            prepareConnection(conn);
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new IOException("bad http response: " + responseCode);
            }
            JsonReader jsonReader = Json.createReader(new StringReader(readResponse(conn)));
            JsonObject json = jsonReader.readObject();
            jsonReader.close();
            JsonArray spirits = json.getJsonArray("spirits");
            if (spirits == null || spirits.isEmpty()) {
                //log.warning("No Such Product found: \"" + findName + "\"");
                return null;
            }
            // search for first whisky
            JsonObject found = null;
            for (int i = 0; i < spirits.size(); i++) {
                JsonObject item = spirits.getJsonObject(i);
                String itemFamily = item.getString("spirit_family_slug");
                String itemType = item.getString("spirit_style_name");
                String itemCountry = item.getString("country");
                if (Locators.Spirit.equals(itemFamily, "whisky")
                        && (type == null || Locators.WhiskyType.equals(type, itemType))
                        && (country == null || Locators.Country.equals(country, itemCountry))) {
                    found = item;
                    break;
                }
            }
            if (found == null) {
                //log.warning("No Such Product found: \"" + findName + "\"");
                return null;
            }
            String name = found.getString("name");
            String extUrl = BASE_URL + "/spirits/" + found.getString("slug");
            //log.info("* Parsing external API response from " + url + ", searched: \"" + findName + "\" => found: \"" + name + "\"");
            return new FlavorProfile(name, extUrl);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Failed to parse Product Search page: " + url, ex);
            return null;
        }
    }

    public void loadFlavorProfile(FlavorProfile fp) {
        try {
            Document doc = Jsoup.connect(fp.getCacheExternalUrl())
                    .header("referer", "https://distiller.com/search")
                    .header("x-distiller-developer-token", "8e01b58d-6bc8-407e-b7fb-5b989b5b23e9")
                    .get();
            String title = doc.title();
            Elements div = doc.select("div.flavor-profile");
            if (div == null || div.isEmpty() || div.first() == null) {
                //log.warning("Product \"" + fp.getName() + "\" does not have Flavor Profile");
                return;
            }
            Element flProfDiv = div.first();
            String flavors = flProfDiv.select("h3.flavors").first().text();
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
