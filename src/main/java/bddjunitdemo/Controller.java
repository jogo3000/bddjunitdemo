package bddjunitdemo;

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
		auditService.post(new Audit(user.getName(), "Search for :" + name));
		return customerService.search(name);
	}

	public void update(Customer customer) {
		if (Role.ADMIN != user.getRole()) {
			throw new RuntimeException("User not admin");
		}
		auditService.post(new Audit(user.getName(), "Updating customer :" + customer.getId()));
		customerService.save(customer);
	}
}
