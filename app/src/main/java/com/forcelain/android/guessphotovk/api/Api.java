package com.forcelain.android.guessphotovk.api;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

public class Api {

    private final String accessToken;

    public Api(String accessToken) {
        this.accessToken = accessToken;
    }

    public Observable<List<UserEntity>> getAllFriends(){
        return Observable.create(new Observable.OnSubscribe<List<UserEntity>>() {
            @Override
            public void call(Subscriber<? super List<UserEntity>> subscriber) {
                List<UserEntity> allFriends = getFriends();
                subscriber.onNext(allFriends);
                subscriber.onCompleted();
            }
        });
    }

    public Observable<UserEntity> getUserAllPhotos(final UserEntity userEntity){
        return Observable.create(new Observable.OnSubscribe<UserEntity>() {
            @Override
            public void call(Subscriber<? super UserEntity> subscriber) {
                userEntity.photoList = getPhotoList(userEntity.id);
                subscriber.onNext(userEntity);
                subscriber.onCompleted();
            }
        });
    }

    private List<UserEntity> getFriends() {

        List<UserEntity> result;

        String url = getDefaultUriBuilder()
                .appendPath("friends.get")
                .appendQueryParameter("fields", "nickname")
                .build().toString();


        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            Type type = new TypeToken<ApiResponse<FriendsResponse>>(){}.getType();
            ApiResponse<FriendsResponse> apiResponse = new Gson().fromJson(response.body().charStream(), type);
            if (apiResponse.error != null){
                throw new ApiException(apiResponse.error.errorCode, apiResponse.error.errorMsg);
            }
            result = apiResponse.response.items;
        } catch (IOException|JsonParseException e) {
            throw new RuntimeException("Unexpected friend response");
        }
        return result;
    }

    private @NonNull
    List<PhotoEntity> getPhotoList(int ownerId) {
        List<PhotoEntity> result;

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
        try {
            Response response = client.newCall(request).execute();
            Type type = new TypeToken<ApiResponse<PhotoListResponse>>(){}.getType();
            ApiResponse<PhotoListResponse> apiResponse = new Gson().fromJson(response.body().charStream(), type);
            result = apiResponse.response.items;
        } catch (IOException|JsonParseException e) {
            throw new RuntimeException("Unexpected photo response");
        }
        return result;
    }

    private Uri.Builder getDefaultUriBuilder(){
        return new Uri.Builder()
                .scheme("https")
                .authority("api.vk.com")
                .appendPath("method")
                .appendQueryParameter("v", "5.35")
                .appendQueryParameter("access_token", accessToken);
    }

}
