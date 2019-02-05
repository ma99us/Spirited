package org.maggus.spirit.services;

import org.junit.Before;
import org.junit.Test;
import org.maggus.spirit.models.FlavorProfile;
import org.maggus.spirit.models.Whisky;

import static org.junit.Assert.*;

public class DistillerParserTest {

    DistillerParser parser;

    @Before
    public void before(){
        parser = new DistillerParser(true);
    }

//    @Test
//    public void getAllProducts() {
//        DistillerParser parser = new DistillerParser();
//        List<FlavorProfile> products = parser.loadAllProducts();
//        assertNotNull(products);
//        assertTrue(!products.isEmpty());
//    }

    @Test
    public void parseFlavorProfile() {
        FlavorProfile fp = new FlavorProfile("BALBLAIR 2005", "https://distiller.com/spirits/balblair-2005");

        parser.loadFlavorProfile(fp);
        assertEquals("FULL BODIED".toLowerCase(), fp.getFlavors().toLowerCase());
        assertEquals(20L, (long) fp.getSpicy());
        assertEquals(50L, (long) fp.getFull_bodied());
        assertEquals(10L, (long) fp.getVanilla());
        assertEquals(86L, (long) fp.getScore());
        assertTrue(fp.getRating() > 1.0);
    }

    @Test
    public void searchSingleProduct() {
        Whisky whisky = new Whisky("BALBLAIR 2005", null, null);
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("BALBLAIR 2005 1ST RELEASE".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchSingleProductBlendedMalt() {
        Whisky whisky = new Whisky("Poit Dhubh Blended Malt 8 YO", null, null);
        whisky.setType("Blended");
        whisky.setCountry("United Kingdom");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("POIT DHUBH BLENDED MALT 8 YEAR".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchSingleProductWrongResultsOrder() {
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
        Whisky whisky = new Whisky("Glen Moray Single Malt Scotch 15 YO", null, null);
        whisky.setType("Single malt");
        whisky.setCountry("United Kingdom");
        whisky.setRegion("Speyside");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("GLEN MORAY 16 YEAR".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchSingleProductJunkInResultName() {
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
        Whisky whisky = new Whisky("Glentauchers 1997", null, null);
        whisky.setType("Single malt");
        whisky.setCountry("United Kingdom");
        whisky.setRegion("Speyside");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("Glentauchers 2008 8 Year (The Exclusive Malts)".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchProductJunkInResult() {
        Whisky whisky = new Whisky("Yellow Spot Irish Whiskey", null, null);
        whisky.setType("Single malt");
        whisky.setCountry("Ireland");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("Yellow Spot 12 Year Single Pot Still".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchProductInperefectMatch() {
        Whisky whisky = new Whisky("Tamdhu Batch Strength III", null, null);
        whisky.setType("Single malt");
        whisky.setRegion("Speyside");
        whisky.setCountry("United Kingdom");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("TAMDHU BATCH STRENGTH #001".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchProductInperefectMatch1() {
        Whisky whisky = new Whisky("Hibiki Harmony", null, null);
        whisky.setType("Blended");
        whisky.setCountry("Japan");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("HIBIKI JAPANESE HARMONY".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchProductInperefectMatch2() {
        Whisky whisky = new Whisky("Antiquity Blue Blended Whisky", null, null);
        whisky.setType("Blended");
        whisky.setCountry("United Kingdom");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("ANTIQUITY BLUE".toUpperCase(), fp.getName().toUpperCase());   // India
    }

    @Test
    public void searchProductInperefectMatch3() {
        Whisky whisky = new Whisky("William Grant Family Reserve", null, null);
        whisky.setType("Blended");
        whisky.setCountry("United Kingdom");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("GRANT'S FAMILY RESERVE".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchProductInperefectMatch4() {
        Whisky whisky = new Whisky("Tokinoka", null, null);
        whisky.setType("Blended");
        whisky.setCountry("Japan");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("SUNTORY WHISKY TOKI".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchProductInperefectMatch5() {
        Whisky whisky = new Whisky("Jameson Irish", null, null);
        whisky.setType("Blended");
        whisky.setCountry("Ireland");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("JAMESON".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchProductInperefectMatch6() {
        Whisky whisky = new Whisky("AnCnoc 2002", null, null);
        whisky.setType("Single malt");
        whisky.setRegion("Highland");
        whisky.setCountry("United Kingdom");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("ANCNOC 1996".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchProductInperefectMatch7() {
        Whisky whisky = new Whisky("Tomatin Cask Strength 2007", null, null);
        whisky.setType("Single malt");
        whisky.setRegion("Highland");
        whisky.setCountry("United Kingdom");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("TOMATIN CASK STRENGTH".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchProductInperefectMatch8() {
        Whisky whisky = new Whisky("Glenfarclas 105 Cask", null, null);
        whisky.setType("Single malt");
        whisky.setRegion("Speyside");
        whisky.setCountry("United Kingdom");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("GLENFARCLAS 105 CASK STRENGTH".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchProductInperefectMatch9() {
        Whisky whisky = new Whisky("Canadian Club Reserve", null, null);
        whisky.setType("Blended");
        whisky.setCountry("Canada");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("CANADIAN CLUB RESERVE 9 YEAR".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchProductInperefectMatch10() {
        Whisky whisky = new Whisky("Tamdhu Speyside Single Malt Scotch Whisky 10 YO", null, null);
        whisky.setType("Single malt");
        whisky.setRegion("Speyside");
        whisky.setCountry("United Kingdom");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("TAMDHU 10 YEAR".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchProductInperefectMatch11() {
        Whisky whisky = new Whisky("Poit Dhubh Gaelic Blended Malt 21 YO", null, null);
        whisky.setType("Blended");
        whisky.setCountry("United Kingdom");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("POIT DHUBH BLENDED MALT 21 YEAR".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchProductInperefectMatch12() {
        Whisky whisky = new Whisky("J & B Rare", null, null);
        whisky.setType("Blended");
        whisky.setCountry("United Kingdom");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("J & B RARE BLENDED SCOTCH".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchProductInperefectMatch13() {
        Whisky whisky = new Whisky("Bearface Triple Oak Canadian 7 YO", null, null);
        whisky.setType("Single malt");
        whisky.setCountry("Canada");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("BEARFACE TRIPLE OAK".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchProductInperefectMatch14() {
        Whisky whisky = new Whisky("Connoisseurs Choice Caol Ila 2004", null, null);
        whisky.setType("Single malt");
        whisky.setRegion("Isla");
        whisky.setCountry("United Kingdom");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("CAOL ILA 2004 CONNOISSEURS CHOICE (GORDON & MACPHAIL)".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchProductInperefectMatch15() {
        Whisky whisky = new Whisky("Glengoyne Highland Single Malt Scotch Whisky 10 YO", null, null);
        whisky.setType("Single malt");
        whisky.setCountry("United Kingdom");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("GLENGOYNE 10 YEAR".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchProductInperefectMatch16() {
        Whisky whisky = new Whisky("Discovery Miltonduff 10 YO", null, null);
        whisky.setType("Single malt");
        whisky.setCountry("United Kingdom");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("MILTONDUFF 10 YEAR DISTILLERY LABELS (GORDON & MACPHAIL)".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchProductInperefectMatch17() {
        Whisky whisky = new Whisky("First Editions Auchroisk 19 YO", null, null);
        whisky.setType("Single malt");
        whisky.setCountry("United Kingdom");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("AUCHROISK 20 YEAR".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchProductInperefectMatch18() {
        Whisky whisky = new Whisky("Tomatin Limited Edition Cask Strength Edition", null, null);
        whisky.setType("Single malt");
        whisky.setCountry("United Kingdom");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("TOMATIN CASK STRENGTH".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchProductInperefectMatch19() {
        Whisky whisky = new Whisky("Tomatin Cu Bocan 2005", null, null);
        whisky.setType("Single malt");
        whisky.setCountry("United Kingdom");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("TOMATIN CÙ BÒCAN 2005 VINTAGE".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchProductNoResult() {
        Whisky whisky = new Whisky("Schenley Black Velvet", null, null);
        whisky.setCountry("Canada");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("SCHENLEY O.F.C. CANADIAN WHISKY".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchProductNoResult1() {
        Whisky whisky = new Whisky("Glen Breton Silver", null, null);
        whisky.setCountry("Canada");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("Glen Breton Ice 10 Year".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchProductBourbon() {
        Whisky whisky = new Whisky("Wild Turkey 81 Proof Kentucky Straight Bourbon", null, null);
        whisky.setType("Bourbon");
        whisky.setCountry("USA");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("WILD TURKEY 8 YEAR 101 PROOF".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchProductBourbon1() {
        Whisky whisky = new Whisky("Evan Williams 1783", null, null);
        whisky.setType("Bourbon");
        whisky.setCountry("USA");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("EVAN WILLIAMS 1783 BOURBON".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchProductBourbon2() {
        Whisky whisky = new Whisky("Jack Daniels Master Series 5", null, null);
        whisky.setType("Bourbon");
        whisky.setCountry("USA");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("JACK DANIEL'S MASTER DISTILLER SERIES NO. 3".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchProductBourbon3() {
        Whisky whisky = new Whisky("Wild Turkey 101", null, null);
        whisky.setType("Bourbon");
        whisky.setCountry("USA");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("WILD TURKEY BOURBON 101".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchProductBourbon4() {
        Whisky whisky = new Whisky("Bakers Bourbon 7 YO", null, null);
        whisky.setType("Bourbon");
        whisky.setCountry("USA");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("BAKER'S BOURBON".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchProductBourbon5() {
        Whisky whisky = new Whisky("Strathisla 2005", null, null);
        whisky.setType("Single malt");
        whisky.setCountry("United Kingdom");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("STRATHISLA 12 YEAR".toUpperCase(), fp.getName().toUpperCase());
    }

    @Test
    public void searchProductUsa() {
        Whisky whisky = new Whisky("Jack Daniels", null, null);
        whisky.setCountry("USA");
        FlavorProfile fp = parser.fuzzySearchFlavorProfile(whisky);
        assertNotNull(fp);
        assertEquals("JACK DANIEL'S OLD NO. 7".toUpperCase(), fp.getName().toUpperCase());
    }

}
