package logic;

import java.io.Serializable;

public class PaymentHistory implements Serializable {

    private int paymentId;
    private String date;
    private double amount;
    private String status;

    public PaymentHistory(int paymentId, String date, double amount, String status) {
        this.paymentId = paymentId;
        this.date = date;
        this.amount = amount;
        this.status = status;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public String getDate() {
        return date;
    }

    public double getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }
}
