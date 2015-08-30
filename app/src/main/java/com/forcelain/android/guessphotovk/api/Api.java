package com.forcelain.android.guessphotovk.api;

import android.net.Uri;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class Api {

    private final String accessToken;

    public Api(String accessToken) {
        this.accessToken = accessToken;
    }

    public List<UserEntity> getUsers(final Iterable<Integer> ids) throws IOException {

        String url = getDefaultUriBuilder()
                .appendPath("users.get")
                .appendQueryParameter("user_ids", TextUtils.join(",", ids))
                .build().toString();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();


        Response response = client.newCall(request).execute();
        Type type = new TypeToken<ApiResponse<List<UserEntity>>>(){}.getType();
        ApiResponse<List<UserEntity>> apiResponse = parseApiResponse(type, response);
        return apiResponse.response;
    }

    public List<Integer> getCommonFriends(int sourceId, int targetId) throws IOException {

        String url = getDefaultUriBuilder()
                .appendPath("friends.getMutual")
                .appendQueryParameter("source_uid", String.valueOf(sourceId))
                .appendQueryParameter("target_uid", String.valueOf(targetId))
                .build().toString();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        Type type = new TypeToken<ApiResponse<List<Integer>>>(){}.getType();
        ApiResponse<List<Integer>> apiResponse = parseApiResponse(type, response);
        return apiResponse.response;
    }

    public List<GroupEntity> getGroups() throws IOException {

        String url = getDefaultUriBuilder()
                .appendPath("groups.get")
                .appendQueryParameter("extended", "1")
                .appendQueryParameter("count", "1000")
                .build().toString();


        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        Type type = new TypeToken<ApiResponse<GroupsResponse>>(){}.getType();
        ApiResponse<GroupsResponse> apiResponse = parseApiResponse(type, response);
        return apiResponse.response.items;
    }

    public List<SongEntity> getSongs(int userId) throws IOException {

        String url = getDefaultUriBuilder()
                .appendPath("audio.get")
                .appendQueryParameter("owner_id", String.valueOf(userId))
                .appendQueryParameter("need_user", "0")
                .appendQueryParameter("count", "1000")
                .build().toString();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        Type type = new TypeToken<ApiResponse<SongsResponse>>(){}.getType();
        ApiResponse<SongsResponse> apiResponse = parseApiResponse(type, response);
        return apiResponse.response.items;
    }

    public List<UserEntity> getFriends(Integer userId) throws IOException {

        Uri.Builder builder = getDefaultUriBuilder()
                .appendPath("friends.get")
                .appendQueryParameter("fields", "nickname");

        if (userId != null){
            builder.appendQueryParameter("user_id", userId.toString());
        }

        String url = builder.build().toString();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        Type type = new TypeToken<ApiResponse<FriendsResponse>>(){}.getType();
        ApiResponse<FriendsResponse> apiResponse = parseApiResponse(type, response);
        return apiResponse.response.items;
    }

    public List<PhotoEntity> getPhotos(int ownerId) throws IOException {

        String url = getDefaultUriBuilder()
                .appendPath("photos.getAll")
                .appendQueryParameter("owner_id", String.valueOf(ownerId))
                .appendQueryParameter("count", "200")
                .appendQueryParameter("extended", "0")
                .appendQueryParameter("photo_sizes", "1").build().toString();


        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        Type type = new TypeToken<ApiResponse<PhotoListResponse>>(){}.getType();
        ApiResponse<PhotoListResponse> apiResponse = parseApiResponse(type, response);
        return apiResponse.response.items;
    }

    private Uri.Builder getDefaultUriBuilder(){
        return new Uri.Builder()
                .scheme("https")
                .authority("api.vk.com")
                .appendPath("method")
                .appendQueryParameter("v", "5.35")
                .appendQueryParameter("access_token", accessToken);
    }

    private <T> T parseApiResponse(Type type, Response response) throws IOException {
        try {
            T object = new Gson().fromJson(response.body().charStream(), type);
            if (object instanceof ApiResponse){
                ApiResponse apiResponse = (ApiResponse) object;
                if (apiResponse.error != null){
                    throw new ApiException(apiResponse.error.errorCode, apiResponse.error.errorMsg);
                }
            }
            return object;
        } catch (JsonParseException e) {
            throw new ApiException(ApiException.ERROR_CODE_UNKNOWN, "Unexpected groups response");
        }
    }
}
