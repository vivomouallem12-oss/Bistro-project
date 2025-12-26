package logic;

import java.io.Serializable;

public class Subscriber implements Serializable {

    private int subscriberId;    
    private String fullName;      
    private String username;     
    private String phone;       
    private String email;   
    
    public Subscriber(int subscriberId, String fullName, String username,
                      String phone, String email) {
        this.subscriberId = subscriberId;
        this.fullName = fullName;
        this.username = username;
        this.phone = phone;
        this.email = email;
    }

    // Getters & Setters
    public int getSubscriberId() { return subscriberId; }
    public void setSubscriberId(int subscriberId) { this.subscriberId = subscriberId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return "Subscriber{id=" + subscriberId +
               ", name='" + fullName + '\'' +
               ", username='" + username + '\'' +
               ", phone='" + phone + '\'' +
               ", email='" + email + '\'' +
               '}';
    }
}
