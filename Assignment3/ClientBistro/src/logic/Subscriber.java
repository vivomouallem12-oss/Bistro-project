package logic;

import java.io.Serializable;

public class Subscriber implements Serializable {

    private int subscriberId;    
    private String username;     
    private String phone;       
    private String email;   
    
    public Subscriber(int id, String username, String email, String phone) {
        this.subscriberId = id;
        this.username = username;
        this.email = email;
        this.phone = phone;
    }


    // Getters & Setters
    public int getSubscriberId() { return subscriberId; }
    public void setSubscriberId(int subscriberId) { this.subscriberId = subscriberId; }

 

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return "Subscriber{id=" + subscriberId +
               ", username='" + username + '\'' +
               ", phone='" + phone + '\'' +
               ", email='" + email + '\'' +
               '}';
    }
}
