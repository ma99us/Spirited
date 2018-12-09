package org.maggus.spirit.services;

import lombok.extern.java.Log;
import org.maggus.spirit.models.Whisky;

import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.TypedQuery;
import java.util.List;

@Stateful
@Log
public class WhiskyService {

    @PersistenceContext(unitName = "spirited", type = PersistenceContextType.EXTENDED)
    private EntityManager em;

//    public WhiskyService() {
//        this("spirited-test");
//    }
//
//    public WhiskyService(String unitName) {
//        if(em == null){ // only manually initialise if EntityManager injection did not happen
//            try {
//                Map properties = new HashMap();
//                em = Persistence.createEntityManagerFactory(unitName, properties).createEntityManager();
//            } catch (Exception ex) {
//                log.log(Level.SEVERE, "Error initiating Persistence engine and test DB entities", ex);
//            }
//        }
//    }

    public List<Whisky> getAllWhisky() throws Exception {
        TypedQuery<Whisky> q = em.createQuery("select w from Whisky w", Whisky.class);
        return q.getResultList();
    }

    public Whisky getWhiskyById(long id) throws Exception {
        return em.find(Whisky.class, id);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void insertWhisky(Whisky whisky) throws Exception {
        em.persist(whisky);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Whisky updateWhisky(Whisky whisky) throws Exception {
        return em.merge(whisky);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void deleteWhisky(Whisky whisky) throws Exception {
        em.remove(whisky);
    }
}
