package com.forcelain.android.guessphotovk.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.forcelain.android.guessphotovk.R;
import com.forcelain.android.guessphotovk.api.Api;
import com.forcelain.android.guessphotovk.api.ApiException;
import com.forcelain.android.guessphotovk.api.PhotoEntity;
import com.forcelain.android.guessphotovk.api.UserEntity;
import com.forcelain.android.guessphotovk.model.RoundModel;
import com.forcelain.android.guessphotovk.model.UserModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final long NEW_ROUND_DELAY_MS = 1000;
    @Bind(R.id.image_photo) ImageView photoView;
    @Bind({R.id.buttons_1_2, R.id.buttons_3_4}) List<View> buttonBars;
    @Bind({R.id.button_var1, R.id.button_var2, R.id.button_var3, R.id.button_var4}) List<Button> variantsButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        VKSdk.initialize(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkToken();
    }

    private void checkToken() {
        VKAccessToken accessToken = VKAccessToken.currentToken();
        if (accessToken == null || accessToken.isExpired()){
            askForLogin();
        }
    }

    private void askForLogin() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setMessage(R.string.need_to_login)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        login();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);
        builder.create().show();
    }

    void login() {
        VKSdk.login(this, "friends", "photos");
    }

    @OnClick({R.id.button_var1, R.id.button_var2, R.id.button_var3, R.id.button_var4})
    void onVariantButtonClicked(Button view){
        view.setTextColor(Color.RED);
        for (Button button : variantsButton) {
            Boolean correct = (Boolean) button.getTag();
            button.setClickable(false);
            if (correct){
                button.setTextColor(Color.GREEN);
            }
        }
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                newRound();
            }
        }, NEW_ROUND_DELAY_MS);
    }

    @OnClick(R.id.button_go)
    void newRound() {

        onRoundPreparing();

        new Api(VKAccessToken.currentToken().accessToken).getAllFriends()
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
        .flatMap(new Func1<UserEntity, Observable<UserEntity>>() {
            @Override
            public Observable<UserEntity> call(UserEntity userEntity) {
                return new Api(VKAccessToken.currentToken().accessToken).getUserAllPhotos(userEntity)
                        .onErrorResumeNext(Observable.just(userEntity));
            }
        })
        .map(new Func1<UserEntity, UserModel>() {
            @Override
            public UserModel call(UserEntity userEntity) {
                UserModel userModel = new UserModel();
                userModel.firstName = userEntity.firstName;
                userModel.lastName = userEntity.lastName;
                userModel.id = userEntity.id;

                if (userEntity.photoList != null && !userEntity.photoList.isEmpty()) {
                    PhotoEntity photoEntity = userEntity.photoList.get(new Random().nextInt(userEntity.photoList.size()));
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
        .timeout(30, TimeUnit.SECONDS)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<RoundModel>() {

            @Override
            public void onCompleted() {
                Log.d(TAG, "onCompleted");
                //TODO Check if no onNext was called
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, Log.getStackTraceString(e));
                //TODO show error fragment
            }

            @Override
            public void onNext(RoundModel roundModel) {
                onRoundReady(roundModel);
            }
        });
    }

    private void showApiExceptionDialog(ApiException e) {
        String[] items = new String[]{getString(R.string.try_again), getString(R.string.try_later), getString(R.string.try_login)};
        new AlertDialog.Builder(this)
                .setTitle(e.getErrorMessage())
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                newRound();
                                break;
                            case 1:
                                //ignore
                                break;
                            case 2:
                                login();
                                break;
                        }
                    }
                })
                .create()
                .show();
    }

    private void onRoundPreparing(){
        photoView.setImageDrawable(null);
        for (View buttonBar : buttonBars) {
            buttonBar.setVisibility(View.INVISIBLE);
        }
    }

    private void onRoundReady(final RoundModel roundModel) {

        Picasso.with(this).load(roundModel.correctAnswer.photoSrc).into(photoView, new Callback() {
            @Override
            public void onSuccess() {
                for (int i = 0; i < variantsButton.size(); i++) {
                    Button button = variantsButton.get(i);
                    UserModel userModel = roundModel.versions.get(i);
                    button.setClickable(true);
                    button.setText(userModel.firstName + " " + userModel.lastName);
                    button.setTextColor(Color.BLUE);
                    button.setTag(userModel.id == roundModel.correctAnswer.id);
                }

                for (View buttonBar : buttonBars) {
                    buttonBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError() {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                res.save();
            }

            @Override
            public void onError(VKError error) {
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
