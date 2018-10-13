package bddjunitdemo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

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
	void testSearch() {
		Mockito.when(customerService.search("Erkki"))
				.thenReturn(Arrays.asList(new Customer(1, "Erkki Heimonen", null)));

		Collection<Customer> searchResults = controller.search("Erkki");

		assertNotNull(searchResults);
		assertEquals(1, searchResults.size());

		Mockito.when(customerService.search(Mockito.anyString())).thenReturn(Collections.emptyList());

		searchResults = controller.search("Paavo");
		assertNotNull(searchResults);
		assertEquals(0, searchResults.size());

		Mockito.verify(auditService, Mockito.times(2)).post(Mockito.any(Audit.class));
	}

	@Test
	void testUpdate() throws Exception {
		controller.update(new Customer(0, null, null));

		Mockito.verify(auditService).post(Mockito.any(Audit.class));
	}

}
