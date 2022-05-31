package com.letscooee.models;

import androidx.annotation.NonNull;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.letscooee.enums.WrapperType;

/**
 * WrapperDetails can hold wrapper information
 *
 * @author Ashish Gaikwad 31/05/22
 * @since 1.3.12
 */
public class WrapperDetails {

    @SerializedName("code")
    @Expose
    int versionCode;

    @SerializedName("ver")
    @Expose
    String versionNumber;

    @SerializedName("name")
    @Expose
    WrapperType wrapperType;

    public WrapperDetails(int versionCode, String versionNumber, WrapperType wrapperType) {
        this.versionCode = versionCode;
        this.versionNumber = versionNumber;
        this.wrapperType = wrapperType;
    }

    @NonNull
    @Override
    public String toString() {
        return "WrapperDetails{" +
                "versionCode=" + versionCode +
                ", versionNumber='" + versionNumber + '\'' +
                ", wrapperType=" + wrapperType +
                '}';
    }
}
