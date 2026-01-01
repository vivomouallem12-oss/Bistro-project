package logic;

import java.io.Serializable;

public class OpenHours implements Serializable{
	
	private int day;
	private String open,close;
	
	public OpenHours(int day, String open, String close) {
		super();
		this.day = day;
		this.open = open;
		this.close = close;
	}
	public int getDay() {
		return day;
	}
	public void setDay(int day) {
		this.day = day;
	}
	public String getOpen() {
		return open;
	}
	public void setOpen(String open) {
		this.open = open;
	}
	public String getClose() {
		return close;
	}
	public void setClose(String close) {
		this.close = close;
	}
	
	

}
