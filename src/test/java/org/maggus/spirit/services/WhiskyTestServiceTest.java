package org.maggus.spirit.services;

import junit.framework.TestCase;
import org.junit.Test;
import org.maggus.spirit.models.Whisky;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WhiskyTestServiceTest {

    @Test
    public void testPersistence() throws Exception {
///////////
//        Properties p = new Properties();
//        p.put("spiritedDatabase", "new://Resource?type=DataSource");
//        p.put("spiritedDatabase.JdbcDriver", "com.mysql.jdbc.Driver");
//        p.put("spiritedDatabase.JdbcUrl", "jdbc:mysql:localhost/spirited");
//        final Context context = EJBContainer.createEJBContainer(p).getContext();
//        assertNotNull(context);
//        WhiskyTestService whiskyService = (WhiskyTestService) context.lookup("java:global/jpa-eclipselink/WhiskyTestService");
//        assertNotNull(whiskyService);

//        WhiskyTestService whiskyService = new WhiskyTestService("spirited-test");
//        int sizeBefore = whiskyService.getAllWhisky().size();
//
//        whiskyService.insertWhisky(new Whisky("Glenlivet Founders Reserve", "330ml", 5500, true));
//        whiskyService.insertWhisky(new Whisky("Glenfiddich Special Reserve 12 YO", "500ml", 7000, true));
//        whiskyService.insertWhisky(new Whisky("Glen Moray Speyside Sherry Cask Finish Single Malt", "750ml", 12000, true));
//
//        List<Whisky> list = whiskyService.getAllWhisky();
//        assertEquals("List.size()", sizeBefore + 3, list.size());
//
//        for (Whisky whisky : list) {
//            whiskyService.deleteWhisky(whisky);
//        }
//
//        assertEquals("WhiskyTestService.getAllWhisky()", 0, whiskyService.getAllWhisky().size());
    }
}
