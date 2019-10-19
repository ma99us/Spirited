package org.maggus.spirit.services;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.maggus.spirit.models.Locators;
import org.maggus.spirit.models.Whisky;

import javax.ejb.embeddable.EJBContainer;
import javax.persistence.*;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class WhiskyTestService {

    private static EJBContainer c;
    private static EntityManager em;

    @BeforeClass
    public static void setUpClass() throws Exception {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("spirited-test-local");
        em = emf.createEntityManager();

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
        if(em != null) {
            em.close();
        }
    }

    @Test
    public void testPersistence() throws Exception {

//        Map<String, Object> p = new HashMap<String, Object>();
//        p.put("spiritedTestDatabase", "new://Resource?type=DataSource");
//        p.put("spiritedTestDatabase.JdbcDriver", "com.mysql.cj.jdbc.Driver");
//        p.put("spiritedTestDatabase.JdbcUrl", "jdbc:mysql://localhost/spirited_test");
//        final Context context = EJBContainer.createEJBContainer(p).getContext();
//        assertNotNull(c);
//        WhiskyService whiskyService = (WhiskyService) c.getContext().lookup("java:global/classes/WhiskyService");
//        assertNotNull(whiskyService);
//
//        WhiskyService whiskyService = new WhiskyService("spirited-test");
//        int sizeBefore = whiskyService.getWhiskies().size();
//
////        whiskyService.insertWhisky(new Whisky("Glenlivet Founders Reserve", "330ml", 5500, true));
////        whiskyService.insertWhisky(new Whisky("Glenfiddich Special Reserve 12 YO", "500ml", 7000, true));
////        whiskyService.insertWhisky(new Whisky("Glen Moray Speyside Sherry Cask Finish Single Malt", "750ml", 12000, true));
//
//        List<Whisky> list = whiskyService.getWhiskies();
//        assertEquals("List.size()", sizeBefore + 3, list.size());
//
//        for (Whisky whisky : list) {
//            whiskyService.deleteWhisky(whisky);
//        }
//
//        assertEquals("WhiskyService.getWhiskies()", 0, whiskyService.getWhiskies().size());
    }

    @Test
    public void getAllWhiskies() {
        TypedQuery<Whisky> q = em.createQuery("select w from Whisky w", Whisky.class);
        List<Whisky> resultList = q.getResultList();
        assert (!resultList.isEmpty());
    }

    @Test
    public void extractWhiskyCharacter() {
//        TypedQuery<Whisky> q = em.createQuery("select w from Whisky w", Whisky.class);
//        List<Whisky> resultList = q.getResultList();
//        System.out.println("found " + resultList.size() + " products");
//        assert(!resultList.isEmpty());
//
//        int processed = 0;
//        Map<String, Integer> characterWords = new TreeMap<String, Integer>();
//        for(Whisky whisky : resultList) {
//            if(!Locators.SpiritType.hasType(whisky.getType(), Locators.SpiritType.BEER.toString())){
//                continue;
//            }
//            String desc = whisky.getDescription();
//            if(desc == null || desc.isEmpty()){
//                continue;
//            }
//            String[] words = desc.toUpperCase().replaceAll("[^a-zA-Z0-9-\\s]", "").split("\\s+");
//            for(String word: words) {
//                Integer count = characterWords.get(word);
//                if (count == null) {
//                    characterWords.put(word, 1);
//                } else {
//                    characterWords.put(word, count + 1);
//                }
//            }
//            processed++;
//        }
//        System.out.println("Processed: " + processed);
//
//        // filter out unwanted words
//        Iterator<Map.Entry<String, Integer>> iter = characterWords.entrySet().iterator();
//        while(iter.hasNext()){
//            Map.Entry<String, Integer> entry = iter.next();
//            if(entry.getKey().length() < 3 || entry.getValue() <=1){
//                iter.remove();
//            }
//        }
//
//        // sort by value desc
//        ArrayList<Map.Entry<String, Integer>> list = new ArrayList<>(characterWords.entrySet());
//        list.sort(new Comparator<Map.Entry<String, Integer>>() {
//            @Override
//            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
//                return o2.getValue().compareTo(o1.getValue());  // reverse order
//            }
//        });
//
//        // output to a file
//        try {
//            PrintWriter out = new PrintWriter(new FileOutputStream("words.csv"));
//            int idx = 0;
//            for (Map.Entry<String, Integer> entry : characterWords.entrySet()) {
//                out.println(entry.getKey() + "; " + entry.getValue() + "; ;" + list.get(idx).getValue() + "; " + list.get(idx).getKey());
//                idx++;
//            }
//            out.flush();
//            out.close();
//        }
//        catch(Exception ex) {
//            ex.printStackTrace();
//        }
    }
}
