package group.amazcontacts.model;

import group.amazcontacts.constant.PhoneType;

public class PhoneNumber {
    private PhoneType phoneType;
    private String Description;
    private String phoneNumber;

    public PhoneNumber() {
        phoneType = PhoneType.PRIMARY;
    }

    public PhoneNumber(PhoneType phoneType, String description, String phoneNumber) {
        this.phoneType = phoneType;
        Description = description;
        this.phoneNumber = phoneNumber;
    }

    public PhoneType getPhoneType() {
        return phoneType;
    }

    public void setPhoneType(PhoneType phoneType) {
        this.phoneType = phoneType;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
