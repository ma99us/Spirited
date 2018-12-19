package org.maggus.spirit.services;

import org.junit.Test;
import org.maggus.spirit.models.Whisky;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AnblParserTest {

    @Test
    public void testBasicProductinfo() throws Exception {
        AnblParser parser = new AnblParser();
        List<Whisky> whiskies = parser.loadProductCategoryPage(AnblParser.CacheUrls.SCOTCH_SM_ISLA.getUrl());
        assertNotNull(whiskies);
    }

    @Test
    public void testDetailedProductinfo() throws Exception {
        Whisky whisky = new Whisky();
        whisky.setCacheExternalUrl("https://www.anbl.com/aberfeldy-12-yo-750ml-14703");
        AnblParser parser = new AnblParser();
        parser.loadProductPage(whisky);
        assertEquals("5000277003457", whisky.getProductCode());
        assertTrue(whisky.getDescription() != null && whisky.getDescription().contains("Warm golden colour"));
        assertEquals(new Double(40.0), whisky.getAlcoholContent());
    }

}
