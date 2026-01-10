package logic;

import java.io.Serializable;

public class VisitHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    private String visitDate;
    private int people;
    private String status;

    public VisitHistory(String visitDate, int people, String status) {
        this.visitDate = visitDate;
        this.people = people;
        this.status = status;
    }

    public String getVisitDate() {
        return visitDate;
    }

    public int getPeople() {
        return people;
    }

    public String getStatus() {
        return status;
    }
}
