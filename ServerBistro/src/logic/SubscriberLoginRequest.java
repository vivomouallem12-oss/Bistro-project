package logic;

import java.io.Serializable;

public class SubscriberLoginRequest implements Serializable {

    private String subscriberId;
    private String SubscriberName;

    public SubscriberLoginRequest(String id, String Name) {
        this.subscriberId = id;
        this.SubscriberName = Name;
    }

    public String getSubscriberId() { return subscriberId; }
    public String getSubscriberName() { return SubscriberName; }
}
