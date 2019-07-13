package org.maggus.spirit.services;

import lombok.extern.java.Log;
import org.maggus.spirit.models.WhiskyCategory;

import javax.ejb.Singleton;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.*;

@Stateful
@Log
public class WhiskyCategoryService {

    @PersistenceContext(unitName = "spirited-test", type = PersistenceContextType.EXTENDED)
    private EntityManager em;

    public WhiskyCategory getWhiskyCategoryById(long id) throws Exception {
        return em.find(WhiskyCategory.class, id);
    }

    public WhiskyCategory getWhiskyCategoryByName(String name) throws Exception {
        TypedQuery<WhiskyCategory> q = em.createQuery("select w from WhiskyCategory w where w.name=:name", WhiskyCategory.class);
        q.setParameter("name", name);
        try {
            return q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public synchronized WhiskyCategory persistWhiskyCategory(WhiskyCategory wc) throws Exception {
        if (wc.getId() > 0) {
            return em.merge(wc);
        } else {
            em.persist(wc);
            return wc;
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public synchronized void deleteWhiskyCategory(WhiskyCategory wc) throws Exception {
        em.remove(wc);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public synchronized void deleteAllWhiskyCategories() throws Exception {
        log.warning("! Clearing the whole DB WhiskyCategory table!");
        Query q = em.createQuery("DELETE FROM WhiskyCategory");
        q.executeUpdate();
        em.flush();
    }
}
