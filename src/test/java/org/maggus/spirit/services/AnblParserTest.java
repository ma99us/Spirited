package org.maggus.spirit.services;

import org.junit.Before;
import org.junit.Test;
import org.maggus.spirit.models.Whisky;

import java.util.List;

import static org.junit.Assert.*;

public class AnblParserTest {

    AnblParser parser;

    @Before
    public void before(){
        parser = new AnblParser(true);
    }

    @Test
    public void testBasicProductinfo() throws Exception {
        List<Whisky> whiskies = parser.loadProductCategoryPage(AnblParser.CacheUrls.SCOTCH_SM_ISLA.getUrl());
        assertNotNull(whiskies);
    }

    @Test
    public void testDetailedProductinfo1() throws Exception {
        Whisky whisky = new Whisky();
        whisky.setCacheExternalUrl("https://www.anbl.com/aberfeldy-12-yo-750ml-14703");
        parser.loadProductPage(whisky);
        assertEquals("5000277003457", whisky.getProductCode());
        assertTrue(whisky.getDescription() != null && whisky.getDescription().contains("Warm golden colour"));
        assertEquals(new Double(40.0), whisky.getAlcoholContent());
    }

    @Test
    public void testDetailedProductinfo2() throws Exception {
        Whisky whisky = new Whisky();
        whisky.setCacheExternalUrl("https://www.anbl.com/oban-14-yo-750ml-10869");
        parser.loadProductPage(whisky);
        assertEquals("622153618040", whisky.getProductCode());
        assertTrue(whisky.getDescription() != null && whisky.getDescription().contains("Mouth-filling late autumn fruits"));
        assertEquals(new Double(43.0), whisky.getAlcoholContent());
    }
}
