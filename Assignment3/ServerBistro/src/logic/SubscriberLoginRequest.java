package logic;

import java.io.Serializable;

public class SubscriberLoginRequest implements Serializable {

    private String subscriberId;
    private String confirmationCode;

    public SubscriberLoginRequest(String id, String code) {
        this.subscriberId = id;
        this.confirmationCode = code;
    }

    public String getSubscriberId() { return subscriberId; }
    public String getConfirmationCode() { return confirmationCode; }
}
