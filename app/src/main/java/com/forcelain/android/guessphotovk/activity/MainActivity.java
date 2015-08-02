package com.forcelain.android.guessphotovk.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.forcelain.android.guessphotovk.R;
import com.forcelain.android.guessphotovk.api.FriendListEntity;
import com.forcelain.android.guessphotovk.api.PhotoEntity;
import com.forcelain.android.guessphotovk.api.PhotoListEntity;
import com.forcelain.android.guessphotovk.api.UserEntity;
import com.forcelain.android.guessphotovk.model.RoundModel;
import com.forcelain.android.guessphotovk.model.UserModel;
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
import java.util.ArrayList;
import java.util.Collections;
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

    private static final String TAG = "MainActivity";
    @Bind(R.id.text_token) TextView tokenTextView;
    @Bind(R.id.text_friends) TextView friendsTextView;
    @Bind(R.id.image_photo) ImageView photoView;
    @Bind(R.id.button_var1) Button variant1Button;
    @Bind(R.id.button_var2) Button variant2Button;
    @Bind(R.id.button_var3) Button variant3Button;
    @Bind(R.id.button_var4) Button variant4Button;

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

        photoView.setImageDrawable(null);
        friendsTextView.setText(null);

        Observable.create(new Observable.OnSubscribe<List<UserEntity>>() {
            @Override
            public void call(Subscriber<? super List<UserEntity>> subscriber) {
                subscriber.onNext(getFriends());
                subscriber.onCompleted();
            }
        })
        .map(new Func1<List<UserEntity>, List<UserEntity>>() {
            @Override
            public List<UserEntity> call(List<UserEntity> friendList) {
                List<UserEntity> shuffledFriendList = new ArrayList<>(friendList);
                Collections.shuffle(shuffledFriendList);
                return shuffledFriendList;
            }
        })
        .flatMap(new Func1<List<UserEntity>, Observable<UserEntity>>() {
            @Override
            public Observable<UserEntity> call(List<UserEntity> friendList) {
                return Observable.from(friendList);
            }
        })
        .map(new Func1<UserEntity, UserModel>() {
            @Override
            public UserModel call(UserEntity userEntity) {
                UserModel userModel = new UserModel();
                userModel.firstName = userEntity.firstName;
                userModel.lastName = userEntity.lastName;
                userModel.id = userEntity.id;

                List<PhotoEntity> photoList = getPhotoList(userEntity.id);
                if (!photoList.isEmpty()) {
                    PhotoEntity photoEntity = photoList.get(new Random().nextInt(photoList.size()));
                    userModel.photoSrc = photoEntity.sizes.get(photoEntity.sizes.size() - 1).src;
                }
                return userModel;
            }
        })
        .filter(new Func1<UserModel, Boolean>() {
            @Override
            public Boolean call(UserModel userModel) {
                return userModel.photoSrc != null;
            }
        })
        .take(4)
        .buffer(4)
        .map(new Func1<List<UserModel>, RoundModel>() {
            @Override
            public RoundModel call(List<UserModel> userModels) {
                RoundModel roundModel = new RoundModel();
                roundModel.correctAnswer = userModels.get(0);
                roundModel.versions = new ArrayList<>(userModels);
                Collections.shuffle(roundModel.versions);
                return roundModel;
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<RoundModel>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }

            @Override
            public void onNext(RoundModel roundModel) {
                friendsTextView.setText(roundModel.correctAnswer.firstName + " " + roundModel.correctAnswer.lastName);
                variant1Button.setText(roundModel.versions.get(0).firstName+" " + roundModel.versions.get(0).lastName);
                variant2Button.setText(roundModel.versions.get(1).firstName+" " + roundModel.versions.get(1).lastName);
                variant3Button.setText(roundModel.versions.get(2).firstName+" " + roundModel.versions.get(2).lastName);
                variant4Button.setText(roundModel.versions.get(3).firstName+" " + roundModel.versions.get(3).lastName);
                Picasso.with(MainActivity.this).load(roundModel.correctAnswer.photoSrc).into(photoView);
            }
        });
    }

    private @NonNull List<PhotoEntity> getPhotoList(Integer ownerId) {
        List<PhotoEntity> result = new ArrayList<>();
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
            if (photoListEntity.response != null && photoListEntity.response.items != null){
                result = photoListEntity.response.items;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private List<UserEntity> getFriends() {

        List<UserEntity> result = null;

        String accessToken = VKAccessToken.currentToken().accessToken;

        String url = new Uri.Builder()
                .scheme("https")
                .authority("api.vk.com")
                .appendPath("method")
                .appendPath("friends.get")
                .appendQueryParameter("access_token", accessToken)
                .appendQueryParameter("fields", "nickname")
                .appendQueryParameter("v", "5.35").build().toString();


        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            FriendListEntity friendListEntity = new Gson().fromJson(response.body().charStream(), FriendListEntity.class);
            result = friendListEntity.response.items;
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
