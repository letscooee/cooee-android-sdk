package com.letscooee.models;

/**
 * @author Abhishek Taparia
 * UserProfile class will store user data from server
 */
public class UserProfile {
    String userName;
    String email;
    String fullName;
    String mobileNumber;
    String address;
    String pinCode;

    public UserProfile() {
    }

    public UserProfile(String userName, String email, String fullName, String mobileNumber, String address, String pinCode) {
        this.userName = userName;
        this.email = email;
        this.fullName = fullName;
        this.mobileNumber = mobileNumber;
        this.address = address;
        this.pinCode = pinCode;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", mobileNumber='" + mobileNumber + '\'' +
                ", address='" + address + '\'' +
                ", pinCode='" + pinCode + '\'' +
                '}';
    }
}
