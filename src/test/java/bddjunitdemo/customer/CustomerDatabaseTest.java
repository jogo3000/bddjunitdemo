package bddjunitdemo.customer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerDatabaseTest {

    @Mock
    EntityManager em;

    @InjectMocks
    CustomerDatabase db;

    @Test
    void test() {
        @SuppressWarnings("unchecked")
        TypedQuery<Customer> typedQuery = Mockito.mock(TypedQuery.class);
        Mockito.when(em.createQuery(Mockito.anyString(), Mockito.eq(Customer.class))).thenReturn(typedQuery);
        Mockito.when(typedQuery.getResultList()).thenReturn(Arrays.asList(new Customer(1, "Erkki", "foo@gmail.com")));
        Collection<Customer> search = db.search("Erkki");

        assertTrue(search.contains(new Customer(1, "Erkki", "foo@gmail.com")));
    }

}
