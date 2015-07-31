package com.forcelain.android.guessphotovk.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import com.forcelain.android.guessphotovk.R;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class MainActivity extends ActionBarActivity {

    @Bind(R.id.text_token) TextView tokenTextView;
    @Bind(R.id.text_friends) TextView friendsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        VKSdk.initialize(this);
    }

    @OnClick(R.id.button_login)
    void onLoginClicked(){
        VKSdk.login(this);
    }

    @OnClick(R.id.button_friends)
    void onFirendsClicked(){
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext(getFriends());
                subscriber.onCompleted();
            }
        })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(new Subscriber<String>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {
                    friendsTextView.setText(e.toString());
                }

                @Override
                public void onNext(String s) {
                    friendsTextView.setText(s);
                }
            });
    }

    private String getFriends() {
        String result = null;
        String accessToken = VKAccessToken.currentToken().accessToken;

        String url = "https://api.vk.com/method/friends.get?access_token=" + accessToken;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            result = response.body().string();
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
