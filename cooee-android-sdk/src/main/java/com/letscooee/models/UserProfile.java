package com.letscooee.models;

import com.google.gson.annotations.SerializedName;

/**
 * @author Abhishek Taparia
 * UserProfile class will store user data from server
 */
public class UserProfile {
    @SerializedName("name")
    String fullName;
    String customerId;
    String email;
    String mobileNumber;

    public UserProfile() {
    }

    public UserProfile(String fullName, String customerId, String email, String mobileNumber) {
        this.email = email;
        this.fullName = fullName;
        this.mobileNumber = mobileNumber;
        this.customerId = customerId;
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

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "fullName='" + fullName + '\'' +
                ", customerId='" + customerId + '\'' +
                ", email='" + email + '\'' +
                ", mobileNumber='" + mobileNumber + '\'' +
                '}';
    }
}
