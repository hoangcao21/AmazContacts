package group.amazcontacts.model;

import java.io.Serializable;
import java.util.List;

public class Contact implements Serializable {
    private String id;
    private String email;
    private String avatar_url;
    private String name;
    private List<PhoneNumber> phoneNumbers; // 1st phone number is the main number, 2nd phone number is optional.
    // When on display, the 1st phone number is shown
    private boolean isFavored;

    public Contact() {

    }

    public Contact(String id, String email, String avatar_url, String name, List<PhoneNumber> phoneNumbers, boolean isFavored) {
        this.id = id;
        this.email = email;
        this.avatar_url = avatar_url;
        this.name = name;
        this.phoneNumbers = phoneNumbers;
        this.isFavored = isFavored;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PhoneNumber> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(List<PhoneNumber> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public boolean isFavored() {
        return isFavored;
    }

    public void setFavored(boolean favored) {
        isFavored = favored;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", avatar_url='" + avatar_url + '\'' +
                ", name='" + name + '\'' +
                ", phoneNumbers=" + phoneNumbers +
                ", isFavored=" + isFavored +
                '}';
    }
}
