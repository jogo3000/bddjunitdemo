package bddjunitdemo.customer;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerDatabaseTest {

    @Mock
    private TypedQuery<Customer> typedQuery;

    @Mock
    private EntityManager em;

    @InjectMocks
    private CustomerDatabase db;

    @Nested
    @DisplayName("As a client, I want to perform searches on the database")
    class SearchTests {

        @BeforeEach
        public void givenTypedQueiresAreUsed() {
            Mockito.when(em.createQuery(Mockito.anyString(), Mockito.eq(Customer.class))).thenReturn(typedQuery);
        }

        @Test
        @DisplayName("GIven database contains one result for search terms, search results are returned")
        void testGivenDatabaseContainsOneResultForSearchResultIsreturned() {
            givenForUsedSearchTermsDatabaseContains(new Customer(1, "Erkki", "foo@gmail.com"));
            Collection<Customer> search = db.search("Erkki");

            assertAll("Search returns the expected item", () -> assertTrue(search.size() == 1),
                    () -> assertTrue(search.contains(new Customer(1, "Erkki", "foo@gmail.com"))));
        }

        @Test
        @DisplayName("Given database contains no result for search terms, empty collection is returned")
        void testGivenDatabaseContainsNoResultsEmptyResultIsReturned() throws Exception {
            givenForUsedSearchTermsDatabaseContainsNothing();
            Collection<Customer> search = db.search("Liisa");

            assertTrue(search.isEmpty(), "Empty collection is returned");
        }

        @Test
        @DisplayName("Given database is not available, error is reported")
        void testGivenDatabaseIsNotAvailableErrorIsReported() throws Exception {
            Mockito.doThrow(new PersistenceException("Database unavailable")).when(typedQuery).getResultList();

            PersistenceException ex = assertThrows(PersistenceException.class, () -> db.search("Liisa"));

            assertEquals("Database unavailable", ex.getMessage());
        }

        private void givenForUsedSearchTermsDatabaseContainsNothing() {
            givenForUsedSearchTermsDatabaseContains();
        }

        private void givenForUsedSearchTermsDatabaseContains(Customer... customers) {
            Mockito.when(typedQuery.getResultList()).thenReturn(Arrays.asList(customers));
        }
    }

    @Nested
    @DisplayName("As a client, I want to update customer data")
    class UpdateTests {
        @Test
        @DisplayName("Given client saves a valid Customer object, it is saved to the database")
        void testSave() throws Exception {
            db.save(new Customer(1, "Jouko", "foo@gmail.com"));

            assertCustomerDataIsSavedToDatabase(new Customer(1, "Jouko", "foo@gmail.com"));
        }

        @Test
        @DisplayName("Given database is unavailable, when client saves a customer then an error is reported")
        void testGivenDatabaseIsUnavailableErrorIsReported() throws Exception {
            givenDatabaseIsUnavailable();

            PersistenceException ex = assertThrows(PersistenceException.class, () -> db.save(new Customer()));

            assertEquals("Database is unavailable", ex.getMessage());
        }

        private void assertCustomerDataIsSavedToDatabase(Customer customer) {
            Mockito.verify(em).persist(customer);
        }

        private void givenDatabaseIsUnavailable() {
            Mockito.doThrow(new PersistenceException("Database is unavailable")).when(em).persist(Mockito.any());
        }
    }
}
