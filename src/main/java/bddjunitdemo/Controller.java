package bddjunitdemo;

import java.util.Arrays;
import java.util.Collection;

import bddjunitdemo.audit.Audit;
import bddjunitdemo.audit.AuditService;
import bddjunitdemo.customer.Customer;
import bddjunitdemo.customer.CustomerService;
import bddjunitdemo.user.Role;
import bddjunitdemo.user.User;

public class Controller {
	private User user;
	private AuditService auditService;
	private CustomerService customerService;

	public Controller(User user, AuditService auditService, CustomerService customerService) {
		this.user = user;
		this.customerService = customerService;
		this.auditService = auditService;
	}

	public Collection<Customer> search(String name) {
		if (!Arrays.asList(Role.ADMIN, Role.USER).contains(user.getRole())) {
			auditService.post(new Audit(user.getName(), "Attempted to perform search"));
			throw new RuntimeException("User has no privilege for this action");
		}
		Collection<Customer> searchResults = customerService.search(name);
		searchResults.forEach(customer -> auditService
				.post(new Audit(user.getName(), "Viewed contact information for Customer id " + customer.getId())));
		return searchResults;
	}

	public void update(Customer customer) {
		if (Role.ADMIN != user.getRole()) {
			auditService.post(new Audit(user.getName(), "Attempted to update customer with id " + customer.getId()));
			throw new RuntimeException("Only admin users may update customers");
		}
		auditService.post(new Audit(user.getName(), "Updating customer :" + customer.getId()));
		customerService.save(customer);
	}
}
