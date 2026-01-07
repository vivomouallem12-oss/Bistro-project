package logic;

import java.io.Serializable;
import java.util.List;

public class AccountHistoryResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<Order> orders;
    private final List<VisitHistory> visits;

    public AccountHistoryResponse(List<Order> orders, List<VisitHistory> visits) {
        this.orders = orders;
        this.visits = visits;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public List<VisitHistory> getVisits() {
        return visits;
    }
}
