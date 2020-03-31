package group.amazcontacts.model;

import android.provider.ContactsContract;

import java.io.Serializable;
import java.util.ArrayList;

public class PhoneNumber implements Serializable {
    private String phoneType;
    private String phoneNumber;

    public PhoneNumber(String phoneType, String phoneNumber) {
        this.phoneType = phoneType;
        this.phoneNumber = phoneNumber;
    }

    public static ArrayList<String> autoGenerateTypeStringList() {
        ArrayList<String> typeList = new ArrayList<>();
        typeList.add("Home");
        typeList.add("Mobile");
        typeList.add("Work");
        typeList.add("Main");
        typeList.add("Custom");
        typeList.add("Pager");
        typeList.add("Other");
        return typeList;
    }

    public static int getPhoneTypeInt(String type) {
        switch (type) {
            case "Home":
                return ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
            case "Mobile":
                return ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
            case "Work":
                return ContactsContract.CommonDataKinds.Phone.TYPE_WORK;
            case "Main":
                return ContactsContract.CommonDataKinds.Phone.TYPE_MAIN;
            case "Custom":
                return ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM;
            case "Pager":
                return ContactsContract.CommonDataKinds.Phone.TYPE_PAGER;
            default:
                return ContactsContract.CommonDataKinds.Phone.TYPE_OTHER;
        }
    }

    public static String getPhoneTypeString(int type) {
        switch (type) {
            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                return "Home";
            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                return "Mobile";
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                return "Work";
            case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN:
                return "Main";
            case ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM:
                return "Custom";
            case ContactsContract.CommonDataKinds.Phone.TYPE_PAGER:
                return "Pager";
            default:
                return "Other";
        }
    }

    public String getPhoneType() {
        return phoneType;
    }

    public void setPhoneType(String phoneType) {
        this.phoneType = phoneType;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDetailPhoneType(){
        try{
            return getPhoneTypeString(Integer.parseInt(getPhoneType()));
        }catch (Exception e ){
            e.printStackTrace();
            return "Other";
        }
    }

    @Override
    public String toString() {
        return "PhoneNumber{" +
                "phoneType='" + phoneType + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
