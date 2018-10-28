package bddjunitdemo.customer;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

public class CustomerDatabase implements CustomerService {

    private final EntityManager em;

    public CustomerDatabase() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("defaultPersistenceUnit");
        this.em = emf.createEntityManager();
    }

    public CustomerDatabase(EntityManager em) {
        this.em = em;
    }

    @Override
    public Collection<Customer> search(String name) {
        TypedQuery<Customer> typedQuery = em.createQuery("from Customer c where c.name = ?1", Customer.class);
        typedQuery.setParameter(1, name);
        return typedQuery.getResultList();
    }

    @Override
    public void save(Customer customer) {
        em.persist(customer);
    }

}
