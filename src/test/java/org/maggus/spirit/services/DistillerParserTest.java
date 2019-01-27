package org.maggus.spirit.services;

import org.junit.Test;
import org.maggus.spirit.models.FlavorProfile;
import org.maggus.spirit.models.Locators;
import org.maggus.spirit.models.Whisky;

import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class DistillerParserTest {

//    @Test
//    public void getAllProducts() {
//        DistillerParser parser = new DistillerParser();
//        List<FlavorProfile> products = parser.loadAllProducts();
//        assertNotNull(products);
//        assertTrue(!products.isEmpty());
//    }

    @Test
    public void searchSingleProduct() {
        DistillerParser parser = new DistillerParser();
        Whisky whisky = new Whisky("BALBLAIR 2005", null, null);
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("BALBLAIR 2005 1ST RELEASE".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchSingleProductBlendedMalt() {
        DistillerParser parser = new DistillerParser();
        Whisky whisky = new Whisky("Poit Dhubh Blended Malt 8 YO", null, null);
        whisky.setType("Blended");
        whisky.setCountry("United Kingdom");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("POIT DHUBH BLENDED MALT 8 YEAR".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchSingleProductWrongResultsOrder() {
        DistillerParser parser = new DistillerParser();
        Whisky whisky = new Whisky("Dalmore 18 YO", null, null);
        whisky.setType("Single malt");
        whisky.setCountry("United Kingdom");
        whisky.setRegion("Highland");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("DALMORE 18 YEAR".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchSingleProductWrongRegion() {
        DistillerParser parser = new DistillerParser();
        Whisky whisky = new Whisky("Glenfarclas 17 YO", null, null);
        whisky.setType("Single malt");
        whisky.setCountry("United Kingdom");
        whisky.setRegion("Speyside");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("GLENFARCLAS 17 YEAR".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchSingleProductJunkInName() {
        DistillerParser parser = new DistillerParser();
        Whisky whisky = new Whisky("Tomatin Scotch Single Malt 12 YO", null, null);
        whisky.setType("Single malt");
        whisky.setCountry("United Kingdom");
        whisky.setRegion("Highland");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("TOMATIN 12 YEAR".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchSingleProductJunkInNameNotExact() {
        DistillerParser parser = new DistillerParser();
        Whisky whisky = new Whisky("Glen Moray Single Malt Scotch 15 YO", null, null);
        whisky.setType("Single malt");
        whisky.setCountry("United Kingdom");
        whisky.setRegion("Speyside");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("GLEN MORAY 12 YEAR".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchSingleProductJunkInResultName() {
        DistillerParser parser = new DistillerParser();
        Whisky whisky = new Whisky("Glendronach 12 YO", null, null);
        whisky.setType("Single malt");
        whisky.setCountry("United Kingdom");
        whisky.setRegion("Highland");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("GLENDRONACH ORIGINAL 12 YEAR".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchProductStrangeMismatches() {
        DistillerParser parser = new DistillerParser();
        Whisky whisky = new Whisky("Glentauchers 1997", null, null);
        whisky.setType("Single malt");
        whisky.setCountry("United Kingdom");
        whisky.setRegion("Speyside");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("GLENTAUCHERS 1996".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void parseFlavorProfile() {
        FlavorProfile fp = new FlavorProfile("BALBLAIR 2005", "https://distiller.com/spirits/balblair-2005");
        DistillerParser parser = new DistillerParser();
        parser.loadFlavorProfile(fp);
        assertEquals("FULL BODIED".toLowerCase(), fp.getFlavors().toLowerCase());
        assertEquals(20L, (long) fp.getSpicy());
        assertEquals(50L, (long) fp.getFull_bodied());
        assertEquals(10L, (long) fp.getVanilla());
        assertEquals(86L, (long) fp.getScore());
        assertTrue(fp.getRating() > 1.0);
    }

}
