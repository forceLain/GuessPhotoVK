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

import rx.Observable;
import rx.Subscriber;

public class Api {

    private final String accessToken;

    public Api(String accessToken) {
        this.accessToken = accessToken;
    }

    public Observable<List<UserEntity>> getUsers(final Iterable<Integer> ids){
        return Observable.create(new Observable.OnSubscribe<List<UserEntity>>() {
            @Override
            public void call(Subscriber<? super List<UserEntity>> subscriber) {
                List<UserEntity> userList;
                try {
                    userList = getUserList(ids);
                    subscriber.onNext(userList);
                } catch (ApiException e){
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    private List<UserEntity> getUserList(final Iterable<Integer> ids) {
        String url = getDefaultUriBuilder()
                .appendPath("users.get")
                .appendQueryParameter("user_ids", TextUtils.join(",", ids))
                .build().toString();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        List<UserEntity> result;
        try {
            Response response = client.newCall(request).execute();
            Type type = new TypeToken<ApiResponse<List<UserEntity>>>(){}.getType();
            ApiResponse<List<UserEntity>> apiResponse = new Gson().fromJson(response.body().charStream(), type);
            if (apiResponse.error != null){
                throw new ApiException(apiResponse.error.errorCode, apiResponse.error.errorMsg);
            }
            result = apiResponse.response;
        } catch (IOException|JsonParseException e) {
            throw new ApiException(ApiException.ERROR_CODE_UNKNOWN, "Unexpected groups response");
        }
        return result;
    }

    public Observable<List<Integer>> getMutual(final int sourceId, final int targetId){
        return Observable.create(new Observable.OnSubscribe<List<Integer>>() {
            @Override
            public void call(Subscriber<? super List<Integer>> subscriber) {
                List<Integer> mutualList;
                try {
                    mutualList = getMutualList(sourceId, targetId);
                    subscriber.onNext(mutualList);
                } catch (ApiException e){
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    private List<Integer> getMutualList(int sourceId, int targetId) {
        String url = getDefaultUriBuilder()
                .appendPath("friends.getMutual")
                .appendQueryParameter("source_uid", String.valueOf(sourceId))
                .appendQueryParameter("target_uid", String.valueOf(targetId))
                .build().toString();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        List<Integer> result;
        try {
            Response response = client.newCall(request).execute();
            Type type = new TypeToken<ApiResponse<List<Integer>>>(){}.getType();
            ApiResponse<List<Integer>> apiResponse = new Gson().fromJson(response.body().charStream(), type);
            if (apiResponse.error != null){
                throw new ApiException(apiResponse.error.errorCode, apiResponse.error.errorMsg);
            }
            result = apiResponse.response;
        } catch (IOException|JsonParseException e) {
            throw new ApiException(ApiException.ERROR_CODE_UNKNOWN, "Unexpected groups response");
        }
        return result;
    }

    public Observable<List<GroupEntity>> getAllGroups(){
        return Observable.create(new Observable.OnSubscribe<List<GroupEntity>>() {
            @Override
            public void call(Subscriber<? super List<GroupEntity>> subscriber) {
                List<GroupEntity> allGroups;
                try {
                    allGroups = getGroups();
                    subscriber.onNext(allGroups);
                } catch (ApiException e){
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    private List<GroupEntity> getGroups() {
        List<GroupEntity> result;

        String url = getDefaultUriBuilder()
                .appendPath("groups.get")
                .appendQueryParameter("extended", "1")
                .appendQueryParameter("count", "1000")
                .build().toString();


        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            Type type = new TypeToken<ApiResponse<GroupsResponse>>(){}.getType();
            ApiResponse<GroupsResponse> apiResponse = new Gson().fromJson(response.body().charStream(), type);
            if (apiResponse.error != null){
                throw new ApiException(apiResponse.error.errorCode, apiResponse.error.errorMsg);
            }
            result = apiResponse.response.items;
        } catch (IOException|JsonParseException e) {
            throw new ApiException(ApiException.ERROR_CODE_UNKNOWN, "Unexpected groups response");
        }
        return result;
    }

    public Observable<List<UserEntity>> getAllFriends(final Integer userId){
        return Observable.create(new Observable.OnSubscribe<List<UserEntity>>() {
            @Override
            public void call(Subscriber<? super List<UserEntity>> subscriber) {
                List<UserEntity> allFriends;
                try {
                    allFriends = getFriends(userId);
                    subscriber.onNext(allFriends);
                } catch (ApiException e){
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    public Observable<List<SongEntity>> getAllSongs(final int userId){
        return Observable.create(new Observable.OnSubscribe<List<SongEntity>>() {
            @Override
            public void call(Subscriber<? super List<SongEntity>> subscriber) {
                List<SongEntity> allSongs;
                try {
                    allSongs = getSongs(userId);
                    subscriber.onNext(allSongs);
                } catch (ApiException e){
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    private List<SongEntity> getSongs(int userId) {
        List<SongEntity> result;

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
        try {
            Response response = client.newCall(request).execute();
            Type type = new TypeToken<ApiResponse<SongsResponse>>(){}.getType();
            ApiResponse<SongsResponse> apiResponse = new Gson().fromJson(response.body().charStream(), type);
            if (apiResponse.error != null){
                throw new ApiException(apiResponse.error.errorCode, apiResponse.error.errorMsg);
            }
            result = apiResponse.response.items;
        } catch (IOException|JsonParseException e) {
            throw new ApiException(ApiException.ERROR_CODE_UNKNOWN, "Unexpected friend response");
        }
        return result;
    }

    public Observable<UserEntity> getUserAllPhotos(final UserEntity userEntity){
        return Observable.create(new Observable.OnSubscribe<UserEntity>() {
            @Override
            public void call(Subscriber<? super UserEntity> subscriber) {
                try {
                    userEntity.photoList = getPhotoList(userEntity.id);
                    subscriber.onNext(userEntity);
                } catch (ApiException e){
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    public Observable<GroupEntity> getGroupAllPhotos(final GroupEntity groupEntity) {
        return Observable.create(new Observable.OnSubscribe<GroupEntity>() {
            @Override
            public void call(Subscriber<? super GroupEntity> subscriber) {
                try {
                    groupEntity.photoList = getPhotoList(-groupEntity.id);
                    subscriber.onNext(groupEntity);
                } catch (ApiException e){
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    private List<UserEntity> getFriends(Integer userId){

        List<UserEntity> result;

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
        try {
            Response response = client.newCall(request).execute();
            Type type = new TypeToken<ApiResponse<FriendsResponse>>(){}.getType();
            ApiResponse<FriendsResponse> apiResponse = new Gson().fromJson(response.body().charStream(), type);
            if (apiResponse.error != null){
                throw new ApiException(apiResponse.error.errorCode, apiResponse.error.errorMsg);
            }
            result = apiResponse.response.items;
        } catch (IOException|JsonParseException e) {
            throw new ApiException(ApiException.ERROR_CODE_UNKNOWN, "Unexpected friend response");
        }
        return result;
    }

    private List<PhotoEntity> getPhotoList(int ownerId) {
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
            if (apiResponse.error != null){
                throw new ApiException(apiResponse.error.errorCode, apiResponse.error.errorMsg);
            }
            result = apiResponse.response.items;
        } catch (IOException|JsonParseException e) {
            throw new ApiException(ApiException.ERROR_CODE_UNKNOWN, "Unexpected photo response");
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
