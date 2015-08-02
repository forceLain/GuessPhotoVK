package com.forcelain.android.guessphotovk.api;

import com.google.gson.annotations.SerializedName;

public class UserEntity {
    public int id;

    @SerializedName("first_name")
    public String firstName;

    @SerializedName("last_name")
    public String lastName;
}
