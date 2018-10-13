package bddjunitdemo;

import java.util.Collection;

import bddjunitdemo.customer.Customer;
import bddjunitdemo.customer.CustomerService;

public class Controller {
	private CustomerService customerService;

	public Controller(CustomerService customerService) {
		this.customerService = customerService;
	}

	public Collection<Customer> search(String name) {
		return customerService.search(name);
	}

	public void update(Customer customer) {

	}
}
