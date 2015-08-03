package com.forcelain.android.guessphotovk.fragments;


import android.util.Log;

import com.forcelain.android.guessphotovk.api.Api;
import com.forcelain.android.guessphotovk.api.PhotoEntity;
import com.forcelain.android.guessphotovk.api.UserEntity;
import com.forcelain.android.guessphotovk.model.RoundModel;
import com.forcelain.android.guessphotovk.model.VariantModel;
import com.vk.sdk.VKAccessToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class FriendsGameFragment extends AbstractPhotoGameFragment {

    private static final String TAG = "GameFragment";

    @Override
    protected void prepareRound() {
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
                .map(new Func1<UserEntity, VariantModel>() {
                    @Override
                    public VariantModel call(UserEntity userEntity) {
                        VariantModel variantModel = new VariantModel();
                        variantModel.title = userEntity.firstName + " " + userEntity.lastName;
                        variantModel.id = userEntity.id;

                        if (userEntity.photoList != null && !userEntity.photoList.isEmpty()) {
                            PhotoEntity photoEntity = userEntity.photoList.get(new Random().nextInt(userEntity.photoList.size()));
                            variantModel.photoSrc = photoEntity.sizes.get(photoEntity.sizes.size() - 1).src;
                        }

                        return variantModel;
                    }
                })
                .filter(new Func1<VariantModel, Boolean>() {
                    @Override
                    public Boolean call(VariantModel variantModel) {
                        return variantModel.photoSrc != null;
                    }
                })
                .take(4)
                .buffer(4)
                .map(new Func1<List<VariantModel>, RoundModel>() {
                    @Override
                    public RoundModel call(List<VariantModel> variantModels) {
                        RoundModel roundModel = new RoundModel();
                        roundModel.correctAnswer = variantModels.get(0);
                        roundModel.versions = new ArrayList<>(variantModels);
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
}
