package logic;

import java.io.Serializable;

public class Order implements Serializable{
	
	private int order_number;
	private String order_date;
	private String order_time;
	private int number_of_guests;
	private int confirmation_code;
	private int subscriber_id; 
	private String customer_phone;
	private String customer_email;
	private String date_of_placing_order;
	private int table_num;
	private String order_status;
	private String status_datetime;
	
	
	public Order(int order_number, String order_date, String order_time, int number_of_guests, int confirmation_code, int subscriber_id,String customer_phone,String customer_email,String date_of_placing_order,int table_num,String order_status,String status_datetime) {
		this.order_number = order_number;
		this.order_date = order_date;
		this.order_time = order_time;
		this.number_of_guests = number_of_guests;
		this.confirmation_code = confirmation_code;
		this.subscriber_id = subscriber_id;
		this.customer_phone = customer_phone;
		this.customer_email = customer_email;
		this.date_of_placing_order = date_of_placing_order;
		this.table_num = table_num;
		this.order_status = order_status;
		this.status_datetime = status_datetime;
	}
	public String getCustomer_phone() {
		return customer_phone;
	}

	public void setCustomer_phone(String customer_phone) {
		this.customer_phone = customer_phone;
	}

	public int getOrder_number() {
		return order_number;
	}
	public void setOrder_number(int order_number) {
		this.order_number = order_number;
	}

	public String getCustomer_email() {
		return customer_email;
	}

	public void setCustomer_email(String customer_email) {
		this.customer_email = customer_email;
	}

	public String getOrder_date() {
		return order_date;
	}


	public void setOrder_date(String order_date) {
		this.order_date = order_date;
	}
	
	public String getOrder_time() {
		return order_time;
	}


	public void setOrder_time(String order_time) {
		this.order_time= order_time;
	}


	public int getNumber_of_guests() {
		return number_of_guests;
	}
	public void setNumber_of_guests(int number_of_guests) {
		this.number_of_guests = number_of_guests;
	}


	public int getConfirmation_code() {
		return confirmation_code;
	}
	public void setConfirmation_code(int confirmation_code) {
		this.confirmation_code = confirmation_code;
	}


	public int getSubscriber_id() {
		return subscriber_id;
	}
	public void setSubscriber_id(int subscriber_id) {
		this.subscriber_id = subscriber_id;
	}


	public String getDate_of_placing_order() {
		return date_of_placing_order;
	}
	public void setDate_of_placing_order(String date_of_placing_order) {
		this.date_of_placing_order = date_of_placing_order;
	}
	
	public int getTable_num() {
		return table_num;
	}
	
	public void setTable_num(int tableNum) {
		this.table_num = tableNum;
	}
	

	public String getOrder_status() {
		return order_status;
	}

	public void setOrder_status(String order_status) {
		this.order_status = order_status;
	}

	public String getStatus_datetime() {
		return status_datetime;
	}

	public void setStatus_datetime(String status_datetime) {
		this.status_datetime = status_datetime;
	}

	@Override
	public String toString() {
		return "Order [order_number=" + order_number + ", order_date=" + order_date + ", order_time=" + order_time
				+ ", number_of_guests=" + number_of_guests + ", confirmation_code=" + confirmation_code
				+ ", subscriber_id=" + subscriber_id + ", customer_phone=" + customer_phone + ", customer_email=" + customer_email 
				+ ", date_of_placing_order=" + date_of_placing_order + ", table_num=" + table_num + ", order_status="+order_status+", status_datetime="+status_datetime+"]";
	}



	
	
	
	

}
