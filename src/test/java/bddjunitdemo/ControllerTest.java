package bddjunitdemo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import bddjunitdemo.audit.Audit;
import bddjunitdemo.audit.AuditService;
import bddjunitdemo.customer.Customer;
import bddjunitdemo.customer.CustomerService;
import bddjunitdemo.user.Role;
import bddjunitdemo.user.User;

@ExtendWith(MockitoExtension.class)
class ControllerTest {
	@Mock
	private User user;
	@Mock
	private AuditService auditService;
	@Mock
	private CustomerService customerService;
	@InjectMocks
	private Controller controller;

	@Test
	@DisplayName("Given the user has role USER and search returns one result, when user runs the search, then results are returned and operation is recorded to audit log")
	void testSearch() {
		Mockito.when(user.getRole()).thenReturn(Role.USER);

		Mockito.when(customerService.search("Erkki"))
				.thenReturn(Arrays.asList(new Customer(1, "Erkki Heimonen", null)));

		Collection<Customer> searchResults = controller.search("Erkki");

		assertNotNull(searchResults);
		assertEquals(1, searchResults.size());

		Mockito.verify(auditService).post(Mockito.any(Audit.class));

	}

	@Test
	@DisplayName("Given the user has role USER and search returns zero results, when user runs the search, the an empty collection is returned and operation is recorded to audit log")
	void noSearchResults() throws Exception {
		Mockito.when(user.getRole()).thenReturn(Role.USER);

		Mockito.when(customerService.search(Mockito.anyString())).thenReturn(Collections.emptyList());

		Collection<Customer> searchResults = controller.search("Paavo");
		assertNotNull(searchResults);
		assertEquals(0, searchResults.size());

		Mockito.verify(auditService).post(Mockito.any(Audit.class));
	}

	@Test
	@DisplayName("Given the user has no role, when user runs the search, then an error is reported")
	void userHasNoRole() throws Exception {
		Mockito.when(user.getRole()).thenReturn(null);

		try {
			controller.search("Liisa");
			fail();
		} catch (Exception e) {
		}
	}

	@Test
	@DisplayName("Given the user has role USER, when user updates a customer, then an error is reported")
	void testUpdateAsUser() throws Exception {
		Mockito.when(user.getRole()).thenReturn(Role.USER);
		try {
			controller.update(new Customer(0, null, null));
			fail("");
		} catch (Exception e) {

		}
	}

	@Test
	@DisplayName("Given the user has role ADMIN, when user updates a customer, then audit log is recorded")
	void testUpdateAsAdmin() {
		Mockito.when(user.getRole()).thenReturn(Role.ADMIN);
		controller.update(new Customer(0, null, null));

		Mockito.verify(auditService).post(Mockito.any(Audit.class));
	}

}
