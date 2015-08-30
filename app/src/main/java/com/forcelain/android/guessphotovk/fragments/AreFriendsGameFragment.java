package com.forcelain.android.guessphotovk.fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.forcelain.android.guessphotovk.R;
import com.forcelain.android.guessphotovk.api.Api;
import com.forcelain.android.guessphotovk.api.UserEntity;
import com.forcelain.android.guessphotovk.model.AreFriendsRoundModel;
import com.forcelain.android.guessphotovk.model.VariantModel;
import com.forcelain.android.guessphotovk.rx.ListSerializerFunc;
import com.forcelain.android.guessphotovk.rx.RxApi;
import com.forcelain.android.guessphotovk.rx.ShuffleFunc;
import com.vk.sdk.VKAccessToken;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class AreFriendsGameFragment extends AbstractGameFragment {

    @Bind(R.id.text_variant_1) TextView variant1TextView;
    @Bind(R.id.text_variant_2) TextView variant2TextView;
    @Bind(R.id.variant_yes) Button variantYesButton;
    @Bind(R.id.variant_no) Button variantNoButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_are_friends_game, container, false);
    }

    @OnClick(R.id.button_go)
    void onGoClicked(){
        newRound();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        newRound();
    }

    @OnClick({R.id.variant_yes, R.id.variant_no})
    void onVariantClicked(Button button){
        Boolean correct = (Boolean) button.getTag();
        button.setTextColor(correct ? Color.GREEN : Color.RED);
    }

    @Override
    protected void onRoundPreparing() {
        variant1TextView.setText(null);
        variant2TextView.setText(null);
        variantYesButton.setTextColor(Color.BLUE);
        variantNoButton.setTextColor(Color.BLUE);
    }

    @Override
    protected void makeRound() {

        final Api api = new Api(VKAccessToken.currentToken().accessToken);
        new RxApi(api).getFriends(null)
                .map(new ShuffleFunc<UserEntity>())
                .flatMap(new ListSerializerFunc<UserEntity>())
                .flatMap(new Func1<UserEntity, Observable<UserEntity>>() {
                    @Override
                    public Observable<UserEntity> call(UserEntity userEntity) {
                        Observable<UserEntity> singleUserObs = Observable.just(userEntity);
                        Observable<List<UserEntity>> friendsObs = new RxApi(api).getFriends(userEntity.id)
                                .onErrorResumeNext(Observable.just(new ArrayList<UserEntity>()));
                        return Observable.zip(singleUserObs, friendsObs, new Func2<UserEntity, List<UserEntity>, UserEntity>() {
                            @Override
                            public UserEntity call(UserEntity userEntity, List<UserEntity> list) {
                                userEntity.friendList = new ArrayList<>();
                                for (UserEntity friend : list) {
                                    userEntity.friendList.add(friend.id);
                                }
                                return userEntity;
                            }
                        });
                    }
                })
                .filter(new Func1<UserEntity, Boolean>() {
                    @Override
                    public Boolean call(UserEntity userEntity) {
                        return userEntity.friendList != null && !userEntity.friendList.isEmpty();
                    }
                })
                .take(2)
                .buffer(2)
                .map(new Func1<List<UserEntity>, AreFriendsRoundModel>() {
                    @Override
                    public AreFriendsRoundModel call(List<UserEntity> list) {
                        UserEntity userEntity1 = list.get(0);
                        UserEntity userEntity2 = list.get(1);
                        AreFriendsRoundModel roundModel = new AreFriendsRoundModel();
                        roundModel.versions = new ArrayList<>();
                        VariantModel variantModel = new VariantModel();
                        variantModel.title = userEntity1.lastName + " " + userEntity1.firstName;
                        roundModel.versions.add(variantModel);
                        variantModel = new VariantModel();
                        variantModel.title = userEntity2.lastName + " " + userEntity2.firstName;
                        roundModel.versions.add(variantModel);
                        roundModel.areFriends = areFriends(userEntity1, userEntity2);
                        return roundModel;
                    }
                })
                .timeout(NEW_ROUND_TIMEOUT_SEC, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<AreFriendsRoundModel>() {

                    @Override
                    public void onCompleted() { }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }

                    @Override
                    public void onNext(AreFriendsRoundModel roundModel) {
                        onRoundReady(roundModel);
                    }
                });
    }

    private boolean areFriends(UserEntity userEntity1, UserEntity userEntity2) {
        return userEntity1.friendList.contains(userEntity2.id) && userEntity2.friendList.contains(userEntity1.id);
    }

    protected void onRoundReady(AreFriendsRoundModel roundModel) {
        VariantModel variantModel1 = roundModel.versions.get(0);
        VariantModel variantModel2 = roundModel.versions.get(1);
        variant1TextView.setText(variantModel1.title);
        variant2TextView.setText(variantModel2.title);
        variantYesButton.setTag(roundModel.areFriends);
        variantNoButton.setTag(!roundModel.areFriends);
    }
}
