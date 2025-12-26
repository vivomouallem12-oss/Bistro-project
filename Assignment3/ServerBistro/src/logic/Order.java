package logic;

import java.io.Serializable;

public class Order implements Serializable {

    private int orderNumber;            // reservation id
    private String orderDate;           // date of reservation (the day they come)
    private int numberOfGuests;
    private String confirmationCode;    // may include letters
    private Integer subscriberId;       // nullable (guest or subscriber)
    private String dateOfPlacingOrder;  // when order was created
    private String status;              // confirmed / cancelled / completed / no_show

    public Order(int orderNumber,
                 String orderDate,
                 int numberOfGuests,
                 String confirmationCode,
                 Integer subscriberId,
                 String dateOfPlacingOrder,
                 String status) {

        this.orderNumber = orderNumber;
        this.orderDate = orderDate;
        this.numberOfGuests = numberOfGuests;
        this.confirmationCode = confirmationCode;
        this.subscriberId = subscriberId;
        this.dateOfPlacingOrder = dateOfPlacingOrder;
        this.status = status;
    }

    public Order(int i, Object object, int j, int k, int l, Object object2) {
		// TODO Auto-generated constructor stub
	}

	public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(int numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    public Integer getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(Integer subscriberId) {
        this.subscriberId = subscriberId;
    }

    public String getDateOfPlacingOrder() {
        return dateOfPlacingOrder;
    }

    public void setDateOfPlacingOrder(String dateOfPlacingOrder) {
        this.dateOfPlacingOrder = dateOfPlacingOrder;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderNumber=" + orderNumber +
                ", orderDate='" + orderDate + '\'' +
                ", numberOfGuests=" + numberOfGuests +
                ", confirmationCode='" + confirmationCode + '\'' +
                ", subscriberId=" + subscriberId +
                ", dateOfPlacingOrder='" + dateOfPlacingOrder + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
