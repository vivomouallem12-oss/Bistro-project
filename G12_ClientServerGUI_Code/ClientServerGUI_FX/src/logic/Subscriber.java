package logic;

import java.io.Serializable;

public class Subscriber implements Serializable{
	
	private int subscriber_id;
	private String subscriber_name;
	
	public Subscriber(int subscriber_id, String subscriber_name) {
		this.subscriber_id = subscriber_id;
		this.subscriber_name = subscriber_name;
	}

	public int getSubscriber_id() {
		return subscriber_id;
	}
	public void setSubscriber_id(int subscriber_id) {
		this.subscriber_id = subscriber_id;
	}

	public String getSubscriber_name() {
		return subscriber_name;
	}
	public void setSubscriber_name(String subscriber_name) {
		this.subscriber_name = subscriber_name;
	}

	@Override
	public String toString() {
		return "Subscriber [subscriber_id=" + subscriber_id + ", subscriber_name=" + subscriber_name + "]";
	}
	
	
	
	
	

}
