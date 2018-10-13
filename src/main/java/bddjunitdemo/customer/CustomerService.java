package bddjunitdemo.customer;

import java.util.Collection;

public interface CustomerService {
	public Collection<Customer> search(String name);

	public void save(Customer customer);
}
