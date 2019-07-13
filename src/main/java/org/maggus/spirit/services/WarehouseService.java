package org.maggus.spirit.services;

import lombok.extern.java.Log;
import org.maggus.spirit.models.Warehouse;

import javax.ejb.Singleton;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.*;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

@Stateful
@Log
public class WarehouseService {

    @PersistenceContext(unitName = "spirited-test", type = PersistenceContextType.EXTENDED)
    private EntityManager em;

    public List<Warehouse> getAllWarehouses() throws Exception {
        TypedQuery<Warehouse> q = em.createQuery("select w from Warehouse w", Warehouse.class);
        return q.getResultList();
    }

    public Warehouse getWarehouseById(long id) throws Exception {
        return em.find(Warehouse.class, id);
    }

    public Warehouse getWarehouseByName(String name) throws Exception {
        TypedQuery<Warehouse> q = em.createQuery("select w from Warehouse w where w.name=:name", Warehouse.class);
        q.setParameter("name", name);
        try {
            return q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public synchronized Warehouse persistWarehouse(Warehouse warehouse) throws Exception {
        if (warehouse.getId() > 0) {
            return em.merge(warehouse);
        } else {
            em.persist(warehouse);
            return warehouse;
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public synchronized void deleteWarehouse(Warehouse warehouse) throws Exception {
        em.remove(warehouse);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public synchronized void deleteAllWarehouses() throws Exception {
        log.warning("! Clearing the whole DB Warehouse table!");
        Query q = em.createQuery("DELETE FROM Warehouse");
        q.executeUpdate();
        em.flush();
    }
}
