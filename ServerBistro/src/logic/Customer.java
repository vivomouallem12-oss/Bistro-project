package logic;

import java.io.Serializable;

public class Customer implements Serializable{

	private String customer_name,customer_email,customer_phone;
	
	public Customer(String customer_name, String customer_email, String customer_phone) {
		super();
		this.customer_name = customer_name;
		this.customer_email = customer_email;
		this.customer_phone = customer_phone;
	}

	public String getCustomer_name() {
		return customer_name;
	}

	public void setCustomer_name(String customer_name) {
		this.customer_name = customer_name;
	}

	public String getCustomer_email() {
		return customer_email;
	}

	public void setCustomer_email(String customer_email) {
		this.customer_email = customer_email;
	}

	public String getCustomer_phone() {
		return customer_phone;
	}

	public void setCustomer_phone(String customer_phone) {
		this.customer_phone = customer_phone;
	}
	
	@Override
	public String toString() {
		return "Customer [customer_name=" + customer_name + ", customer_email=" + customer_email + ", customer_phone="
				+ customer_phone + "]";
	}


	
	
	

}
