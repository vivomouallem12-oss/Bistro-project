package gui;

import client.ClientController;

public interface SubscriberChildScreen {
    void setClient(ClientController client);
    void setSubscriber(int subscriberId, String subscriberName);
}
