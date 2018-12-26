package org.maggus.spirit.services;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class WhiskyTestService {

    private static EJBContainer c;

    @BeforeClass
    public static void setUpClass() throws Exception {
//        Map<String, Object> p = new HashMap<String, Object>();
//        p.put("java.naming.factory.initial ", "org.apache.openejb.client.LocalInitialContextFactory");
//        //p.put(EJBContainer.MODULES, new File("/target/classes/META-INF"));
//
//        p.put("spiritedTestDatabase", "new://Resource?type=DataSource");
//        p.put("spiritedTestDatabase.JdbcDriver", "com.mysql.cj.jdbc.Driver");
//        p.put("spiritedTestDatabase.JdbcUrl", "jdbc:mysql://localhost/spirited_test");
//        p.put("spiritedTestDatabase.UserName", "root");
//        p.put("spiritedTestDatabase.Password", "qwe");
//
//        p.put("em", "new://TransactionManager?type=TransactionManager");
//
//        c = EJBContainer.createEJBContainer(p);
//        System.out.println("Opening the container: " + c);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if(c!=null){
            c.close();
            System.out.println("Closing the container");
        }
    }

    @Test
    public void testPersistence() throws Exception {

//        Map<String, Object> p = new HashMap<String, Object>();
//        p.put("spiritedTestDatabase", "new://Resource?type=DataSource");
//        p.put("spiritedTestDatabase.JdbcDriver", "com.mysql.cj.jdbc.Driver");
//        p.put("spiritedTestDatabase.JdbcUrl", "jdbc:mysql://localhost/spirited_test");
        //final Context context = EJBContainer.createEJBContainer(p).getContext();
//        assertNotNull(c);
        //WhiskyService whiskyService = (WhiskyService) c.getContext().lookup("java:global/classes/MyBean");
//        WhiskyService whiskyService = (WhiskyService) c.getContext().lookup("java:global/classes/WhiskyService");
//        assertNotNull(whiskyService);

//        WhiskyService whiskyService = new WhiskyService("spirited-test");
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
//        assertEquals("WhiskyService.getAllWhisky()", 0, whiskyService.getAllWhisky().size());
    }
}
