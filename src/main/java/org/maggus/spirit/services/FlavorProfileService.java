package org.maggus.spirit.services;

import lombok.extern.java.Log;
import org.maggus.spirit.models.FlavorProfile;

import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.*;
import java.util.List;

@Stateful
@Log
public class FlavorProfileService {

    @PersistenceContext(unitName = "spirited-test", type = PersistenceContextType.EXTENDED)
    private EntityManager em;

    public List<FlavorProfile> getAllFlavorProfile() throws Exception {
        TypedQuery<FlavorProfile> q = em.createQuery("select w from FlavorProfile w", FlavorProfile.class);
        return q.getResultList();
    }

    public FlavorProfile getFlavorProfileById(long id) throws Exception {
        return em.find(FlavorProfile.class, id);
    }

    public FlavorProfile getFlavorProfileByName(String name) throws Exception {
        TypedQuery<FlavorProfile> q = em.createQuery("select w from FlavorProfile w where w.name=:name", FlavorProfile.class);
        q.setParameter("name", name);
        try {
            return q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public synchronized FlavorProfile persistFlavorProfile(FlavorProfile entity) throws Exception {
        if (entity.getId() > 0) {
            return em.merge(entity);
        } else {
            em.persist(entity);
            return entity;
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public synchronized void deleteFlavorProfile(FlavorProfile entity) throws Exception {
        em.remove(entity);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public synchronized void deleteAllFlavorProfiles() throws Exception {
        log.warning("! Clearing the whole DB FlavorProfile table!");
        Query q = em.createQuery("DELETE FROM FlavorProfile");
        q.executeUpdate();
        em.flush();
    }

    public FlavorProfile findFlavorProfileBestMatch(String name) throws Exception {
        TypedQuery<FlavorProfile> q = em.createQuery("select w from FlavorProfile w where LOWER(w.name) like :name", FlavorProfile.class);
        q.setParameter("name", name.toLowerCase()+"%");
        List<FlavorProfile> results = q.getResultList();
        if(results == null || results.isEmpty()){
            return null;
        }
        return results.get(0);  // just get first match
    }
}
