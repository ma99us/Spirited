package org.maggus.spirit.services;

import org.junit.Test;
import org.maggus.spirit.models.FlavorProfile;

import java.util.List;

import static org.junit.Assert.*;

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
        FlavorProfile fp = parser.searchSingleProduct("BALBLAIR 2005", null, null);
        assertNotNull(fp);
        assertTrue(fp.getName().toLowerCase().contains("BALBLAIR 2005".toLowerCase()));
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

    }
}
