package bddjunitdemo;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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

	private static String USER_NAME = "t389kk";

	@Captor
	private ArgumentCaptor<Audit> auditCaptor;
	@Captor
	private ArgumentCaptor<Customer> customerCaptor;

	@Mock
	private AuditService auditService;
	@Mock
	private CustomerService customerService;

	private Controller controller;

	static class RolesPermittedToPerformSearch implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return Stream.of(Role.ADMIN, Role.USER).map(Arguments::of);
		}
	}

	static class RolesNotPermittedToPerformUpdate implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			Stream<Role> allRolesButAdmin = Stream.of(Role.values()).filter(role -> role != Role.ADMIN);
			Stream<Role> noRoleAndAllRolesButAdmin = Stream.concat(Stream.of((Role) null), allRolesButAdmin);
			return noRoleAndAllRolesButAdmin.map(Arguments::of);
		}
	}

	@Nested
	@DisplayName("As a user, I want to search and view Customer contact information")
	class SearchTests {

		@Nested
		@DisplayName("Given user has privilege for search")
		class GivenUserHasPrivilegeForSearch {

			@ParameterizedTest
			@DisplayName("and the search yields one result, then results are returned and viewed id is recorded to audit log")
			@ArgumentsSource(RolesPermittedToPerformSearch.class)
			void testSearch(Role role) {
				givenUserRoleIs(role);
				givenSearchedReturns(new Customer(1, "Erkki Heimonen", null));

				Collection<Customer> actualResult = whenSearched();

				assertAll(() -> assertSearchResultsAre(actualResult, new Customer(1, "Erkki Heimonen", null)),
						() -> assertAuditsAreRecorded(
								new Audit(USER_NAME, "Viewed contact information for Customer id 1")));

			}

			@ParameterizedTest
			@DisplayName("and the search yields multiple results, then results are returned and all viewed ids are recorded to audit log")
			@ArgumentsSource(RolesPermittedToPerformSearch.class)
			void testSearchReturnsMultipleResults(Role role) throws Exception {
				givenUserRoleIs(role);
				givenSearchedReturns(new Customer(1, "Erkki Heimonen", null),
						new Customer(2, "Lasse Parjatmaa", "lasse.parjatmaa@yaymail.com"));

				Collection<Customer> actualResult = whenSearched();

				assertAll(
						() -> assertSearchResultsAre(actualResult, new Customer(1, "Erkki Heimonen", null),
								new Customer(2, "Lasse Parjatmaa", "lasse.parjatmaa@yaymail.com")),
						() -> assertAuditsAreRecorded(
								new Audit(USER_NAME, "Viewed contact information for Customer id 1"),
								new Audit(USER_NAME, "Viewed contact information for Customer id 2")));

			}

			@ParameterizedTest
			@DisplayName("and search yields no results, when user runs the search, then an empty collection is returned and nothing is recorded to audit log")
			@ArgumentsSource(RolesPermittedToPerformSearch.class)
			void noSearchResults(Role role) throws Exception {
				givenUserRoleIs(role);
				givenSearchedReturns();

				Collection<Customer> actualResult = whenSearched();

				assertAll(() -> assertTrue(actualResult.isEmpty(), "Empty collection is returned"),
						() -> Mockito.verifyZeroInteractions(auditService));
			}

			@ParameterizedTest
			@DisplayName("and search is fails, then an error is reported and nothing is recorded to audit log")
			@ArgumentsSource(RolesPermittedToPerformSearch.class)
			void searchFails(Role role) throws Exception {
				givenUserRoleIs(role);
				givenCustomerServiceIsUnavailable();

				RuntimeException thrown = assertThrows(RuntimeException.class, () -> whenSearched(),
						"Error is reported");

				assertAll("Error is reported and nothing is recorded to audit log",
						() -> assertEquals("Customer service unavailable", thrown.getMessage()),
						() -> Mockito.verifyZeroInteractions(auditService));
			}

			@ParameterizedTest
			@DisplayName("and recording audit log fails, error is reported and nothing is recorded to audit log")
			@ArgumentsSource(RolesPermittedToPerformSearch.class)
			void auditFails(Role role) throws Exception {
				givenUserRoleIs(role);
				givenSearchedReturns(new Customer(1, "Göran Pullarsson", null));
				givenAuditServiceIsUnavailable();

				RuntimeException thrown = assertThrows(RuntimeException.class, () -> whenSearched(),
						"Error is reported");

				assertEquals("Audit service unavailable", thrown.getMessage(), "Expected error is reported");
			}

		}

		@Test
		@DisplayName("Given the user has no role, when user runs the search, then an error is reported and audit log is recorded")
		void userHasNoRole() throws Exception {
			givenUserRoleIs(null);

			assertThrows(RuntimeException.class, () -> whenSearched(), "User may not perform the action");

			assertAll(() -> Mockito.verifyZeroInteractions(customerService),
					() -> assertAuditsAreRecorded(new Audit(USER_NAME, "Attempted to perform search")));
		}

		private void givenCustomerServiceIsUnavailable() {
			Mockito.when(customerService.search(Mockito.anyString()))
					.thenThrow(new RuntimeException("Customer service unavailable"));
		}

	}

	@Nested
	@DisplayName("As an admin user, I want to update users' contact information")
	class UpdateTest {

		@ParameterizedTest
		@DisplayName("Given the user has no privileges, when user updates a customer, then an error is reported and audit log is recorded")
		@ArgumentsSource(RolesNotPermittedToPerformUpdate.class)
		void testUpdateAsUnprivilegedUser(Role role) throws Exception {
			givenUserRoleIs(role);

			assertThrows(RuntimeException.class, () -> whenUpdating(new Customer(1, null, null)),
					"User may not perform the action");

			assertAll("No updates are made and audit log is recorded",
					() -> Mockito.verifyZeroInteractions(customerService),
					() -> assertAuditsAreRecorded(new Audit(USER_NAME, "Attempted to update customer with id 1")));
		}

		@Test
		@DisplayName("Given the user has role ADMIN, when user updates a customer, then audit log is recorded")
		void testUpdateAsAdmin() {
			givenUserRoleIs(Role.ADMIN);

			whenUpdating(new Customer(1, "Jaakko Kopiainen", "jaakko.kop123@gmailer.com"));

			assertAll("Customer is updated and audit log is recorded",
					() -> assertUpdatedWith(new Customer(1, "Jaakko Kopiainen", "jaakko.kop123@gmailer.com")),
					() -> assertAuditsAreRecorded(new Audit(USER_NAME, "Updating customer :1")));
		}

		@Test
		@DisplayName("Given the user has role ADMIN and audit service is unavailable, error is reported and update is not made")
		void testCustomerServiceUnavailable() {
			givenUserRoleIs(Role.ADMIN);
			givenAuditServiceIsUnavailable();

			RuntimeException thrown = assertThrows(RuntimeException.class,
					() -> whenUpdating(new Customer(1, "Jaakko Kopiainen", "jaakko.kop123@gmailer.com")));

			assertAll("Error is reported and update is not made",
					() -> assertEquals("Audit service unavailable", thrown.getMessage()),
					() -> Mockito.verifyZeroInteractions(customerService));
		}
	}

	private void givenUserRoleIs(Role role) {
		User user = new User(USER_NAME, role);
		controller = new Controller(user, auditService, customerService);
	}

	private void givenSearchedReturns(Customer... customers) {
		Mockito.when(customerService.search(Mockito.anyString())).thenReturn(Arrays.asList(customers));
	}

	private void givenAuditServiceIsUnavailable() {
		Mockito.doThrow(new RuntimeException("Audit service unavailable")).when(auditService).post(Mockito.any());
	}

	private Collection<Customer> whenSearched() {
		return controller.search("*");
	}

	private void whenUpdating(Customer customer) {
		controller.update(customer);
	}

	private void assertSearchResultsAre(Collection<Customer> actualSearchResult, Customer... customers) {
		assertIterableEquals(Arrays.asList(customers), actualSearchResult, "Search returns expected customers");
	}

	private void assertAuditsAreRecorded(Audit... audits) {
		Mockito.verify(auditService, Mockito.atLeastOnce()).post(auditCaptor.capture());
		assertEquals(Arrays.asList(audits), auditCaptor.getAllValues(), "Operations are recorded to audit log");
	}

	private void assertUpdatedWith(Customer customer) {
		Mockito.verify(customerService).save(customerCaptor.capture());
		assertEquals(customer, customerCaptor.getValue(), "Expected customer data is saved");
	}

}
