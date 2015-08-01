package com.forcelain.android.guessphotovk.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.forcelain.android.guessphotovk.R;
import com.forcelain.android.guessphotovk.api.FriendListEntity;
import com.forcelain.android.guessphotovk.api.PhotoListEntity;
import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


public class MainActivity extends ActionBarActivity {

    @Bind(R.id.text_token) TextView tokenTextView;
    @Bind(R.id.text_friends) TextView friendsTextView;
    @Bind(R.id.image_photo) ImageView photoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        VKSdk.initialize(this);
    }

    @OnClick(R.id.button_login)
    void onLoginClicked() {
        VKSdk.login(this, "friends", "photos");
    }

    @OnClick(R.id.button_friends)
    void onFirendsClicked() {
        Observable.create(new Observable.OnSubscribe<List<Integer>>() {
            @Override
            public void call(Subscriber<? super List<Integer>> subscriber) {
                subscriber.onNext(getFriends());
                subscriber.onCompleted();
            }
        })
        .map(new Func1<List<Integer>, Integer>() {
            @Override
            public Integer call(List<Integer> list) {
                return list.get(new Random().nextInt(list.size()));
            }
        })
        .map(new Func1<Integer, List<PhotoListEntity.PhotoEntity>>() {
            @Override
            public List<PhotoListEntity.PhotoEntity> call(Integer integer) {
                return getPhotoList(integer);
            }
        })
        .map(new Func1<List<PhotoListEntity.PhotoEntity>, PhotoListEntity.PhotoEntity>() {
            @Override
            public PhotoListEntity.PhotoEntity call(List<PhotoListEntity.PhotoEntity> photoEntities) {
                return photoEntities.get(new Random().nextInt(photoEntities.size()));
            }
        })
        .map(new Func1<PhotoListEntity.PhotoEntity, String>() {
            @Override
            public String call(PhotoListEntity.PhotoEntity photoEntity) {
                PhotoListEntity.PhotoSizeEntity photoSizeEntity = photoEntity.sizes.get(photoEntity.sizes.size()-1);
                return photoSizeEntity.src;
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                friendsTextView.setText(e.toString());
            }

            @Override
            public void onNext(String url) {
                friendsTextView.setText(url);
                Picasso.with(MainActivity.this).load(url).into(photoView);
            }
        });
    }

    private List<PhotoListEntity.PhotoEntity> getPhotoList(Integer ownerId) {
        List<PhotoListEntity.PhotoEntity> result = null;
        String accessToken = VKAccessToken.currentToken().accessToken;

        Uri.Builder builder = new Uri.Builder();
        String url = builder.scheme("https")
                .authority("api.vk.com")
                .appendPath("method")
                .appendPath("photos.getAll")
                .appendQueryParameter("owner_id", String.valueOf(ownerId))
                .appendQueryParameter("access_token", accessToken)
                .appendQueryParameter("count", "200")
                .appendQueryParameter("extended", "0")
                .appendQueryParameter("v", "5.35")
                .appendQueryParameter("photo_sizes", "1").build().toString();


        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            PhotoListEntity photoListEntity = new Gson().fromJson(response.body().charStream(), PhotoListEntity.class);
            result = photoListEntity .response.items;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private List<Integer> getFriends() {
        List<Integer> result = null;
        String accessToken = VKAccessToken.currentToken().accessToken;

        String url = "https://api.vk.com/method/friends.get?access_token=" + accessToken;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            FriendListEntity friendListEntity = new Gson().fromJson(response.body().charStream(), FriendListEntity.class);
            result = friendListEntity.response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                res.save();
                tokenTextView.setText(res.accessToken);
            }

            @Override
            public void onError(VKError error) {
                tokenTextView.setText(error.errorMessage);
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
