package com.forcelain.android.guessphotovk.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FriendListEntity {
    public FriendsResponse response;

    public class FriendsResponse {
        public List<UserEntity> items;
    }

    public class UserEntity {
        public int id;

        @SerializedName("first_name")
        public String firstName;

        @SerializedName("last_name")
        public String lastName;
    }
}
