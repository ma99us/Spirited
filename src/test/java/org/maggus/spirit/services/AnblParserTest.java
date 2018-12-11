package org.maggus.spirit.services;

import org.junit.Test;
import org.maggus.spirit.models.Whisky;

import java.util.List;

import static org.junit.Assert.assertNotNull;

public class AnblParserTest {

    @Test
    public void test() throws Exception {
        AnblParser parser = new AnblParser();
        List<Whisky> whiskies = parser.loadProductPage(AnblParser.SCOTCH_SM_ISLA);
        assertNotNull(whiskies);
    }
}
