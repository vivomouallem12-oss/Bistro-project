package logic;

import java.io.Serializable;

public class Tables implements Serializable{
	private int table_num,places,order_num;




	public Tables(int table_num, int places) {
		super();
		this.table_num = table_num;
		this.places = places;
	}




	public int getTable_num() {
		return table_num;
	}




	public void setTable_num(int table_num) {
		this.table_num = table_num;
	}




	public int getPlaces() {
		return places;
	}




	public void setPlaces(int places) {
		this.places = places;
	}




	@Override
	public String toString() {
		return "Tables [table_num=" + table_num + ", places=" + places + "]";
	}	
	
	

}
