package org.maggus.spirit.services;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.maggus.spirit.models.Locators;
import org.maggus.spirit.models.Whisky;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.util.List;

public class SpiritCharacterServiceTest {

    private static EntityManager em;
    private static SpiritCharacterParser scs;

    @BeforeClass
    public static void setUpClass() throws Exception {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("spirited-test-local");
        em = emf.createEntityManager();

        scs = new SpiritCharacterParser();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (em != null) {
            em.close();
        }
    }

    @Test
    public void extractSpiritCharactersTest() {
        TypedQuery<Whisky> q = em.createQuery("select w from Whisky w", Whisky.class);
        List<Whisky> resultList = q.getResultList();
        assert (!resultList.isEmpty());

        for (Whisky whisky : resultList) {
            if (!Locators.SpiritType.hasType(whisky.getType(), Locators.SpiritType.BEER)) {
                continue;   // take only Beers
            }
            String desc = whisky.getDescription();
            if (desc == null || desc.isEmpty()) {
                continue;
            }

            String characters = scs.extractSpiritCharacter(whisky.getDescription(), Locators.SpiritType.BEER);
            //assert(characters != null && !characters.isEmpty());
            if(characters == null || characters.isEmpty()){
                System.out.println(whisky.getName() + " - no character - " + whisky.getDescription());
            }
            else{
                System.out.println(whisky.getName() + " - " + characters);
            }
        }
    }

}
