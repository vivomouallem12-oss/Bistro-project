package logic;

import java.io.Serializable;

public class OrderCustomer implements Serializable{
	
	private Order order;
	private Customer customer;
	

	public OrderCustomer(Order order, Customer customer) {
		super();
		this.order = order;
		this.customer = customer;
	}
	public Order getOrder() {
		return order;
	}
	public void setOrder(Order order) {
		this.order = order;
	}
	public Customer getCustomer() {
		return customer;
	}
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
	@Override
	public String toString() {
		return "OrderCustomer [order=" + order + ", customer=" + customer + "]";
	}

	
	

}
