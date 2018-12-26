package org.maggus.spirit.services;

import org.junit.Test;
import org.maggus.spirit.models.FlavorProfile;
import org.maggus.spirit.models.Locators;

import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class DistillerParserTest {

    @Test
    public void getAllProducts() {
        DistillerParser parser = new DistillerParser();
        List<FlavorProfile> products = parser.loadAllProducts();
        assertNotNull(products);
        assertTrue(!products.isEmpty());
    }

    @Test
    public void searchSingleProduct() {
        DistillerParser parser = new DistillerParser();
        FlavorProfile fp = parser.searchSingleProduct("BALBLAIR 2005", null, null, null, null);
        assertNotNull(fp);
        assertEquals("BALBLAIR 2005 1ST RELEASE".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchSingleProductBlendedMalt() {
        DistillerParser parser = new DistillerParser();
        String name = "Poit Dhubh Blended Malt 8 YO";
        String type = "Blended";
        String country = "United Kingdom";
        String region = null;
        Integer age = Locators.Age.parse(name);
        name = parser.cleanupAnblWhiskyName(name);
        FlavorProfile fp = parser.searchSingleProduct(name, type, country, region, age);
        assertNotNull(fp);
        assertEquals("POIT DHUBH BLENDED MALT 8 YEAR".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchSingleProductWrongResultsOrder() {
        DistillerParser parser = new DistillerParser();
        String name = "Dalmore 18 YO";
        String type = "Single malt";
        String country = "United Kingdom";
        String region = "Highland";
        Integer age = Locators.Age.parse(name);
        name = parser.cleanupAnblWhiskyName(name);
        FlavorProfile fp = parser.searchSingleProduct(name, type, country, region, age);
        assertNotNull(fp);
        assertEquals("DALMORE 18 YEAR".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchSingleProductWrongRegion() {
        DistillerParser parser = new DistillerParser();
        String name = "Glenfarclas 17 YO";
        String type = "Single malt";
        String country = "United Kingdom";
        String region = "Speyside";
        Integer age = Locators.Age.parse(name);
        name = parser.cleanupAnblWhiskyName(name);
        FlavorProfile fp = parser.searchSingleProduct(name, type, country, region, age);
        assertNotNull(fp);
        assertEquals("GLENFARCLAS 17 YEAR".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchSingleProductJunkInName() {
        DistillerParser parser = new DistillerParser();
        String name = "Tomatin Scotch Single Malt 12 YO";
        String type = "Single malt";
        String country = "United Kingdom";
        String region = "Highland";
        Integer age = Locators.Age.parse(name);
        name = parser.cleanupAnblWhiskyName(name);
        FlavorProfile fp = parser.searchSingleProduct(name, type, country, region, age);
        assertNotNull(fp);
        assertEquals("TOMATIN 12 YEAR".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchSingleProductJunkInNameNotExact() {
        DistillerParser parser = new DistillerParser();
        String name = "Glen Moray Single Malt Scotch 15 YO";
        String type = "Single malt";
        String country = "United Kingdom";
        String region = "Speyside";
        Integer age = Locators.Age.parse(name);
        name = parser.cleanupAnblWhiskyName(name);
        FlavorProfile fp = parser.searchSingleProduct(name, type, country, region, age);
        assertNotNull(fp);
        assertEquals("GLEN MORAY 12 YEAR".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchSingleProductJunkInResultName() {
        DistillerParser parser = new DistillerParser();
        String name = "Glendronach 12 YO";
        String type = "Single malt";
        String country = "United Kingdom";
        String region = "Highland";
        Integer age = Locators.Age.parse(name);
        name = parser.cleanupAnblWhiskyName(name);
        FlavorProfile fp = parser.searchSingleProduct(name, type, country, region, age);
        assertNotNull(fp);
        assertEquals("GLENDRONACH ORIGINAL 12 YEAR".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void parseFlavorProfile(){
        FlavorProfile fp = new FlavorProfile("BALBLAIR 2005", "https://distiller.com/spirits/balblair-2005");
        DistillerParser parser = new DistillerParser();
        parser.loadFlavorProfile(fp);
        assertEquals(fp.getFlavors().toLowerCase(), "FULL BODIED".toLowerCase());
        assertEquals((long)fp.getSpicy(), 20L);
        assertEquals((long)fp.getFull_bodied(), 50L);
        assertEquals((long)fp.getVanilla(), 10L);
        //
        assertEquals((long)fp.getScore(), 86L);
        assertEquals(fp.getRating(), 3.66, 0.01);
    }
}
