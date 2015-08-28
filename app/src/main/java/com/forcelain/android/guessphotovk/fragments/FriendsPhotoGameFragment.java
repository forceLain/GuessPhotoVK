package com.forcelain.android.guessphotovk.fragments;


import com.forcelain.android.guessphotovk.api.Api;
import com.forcelain.android.guessphotovk.api.PhotoEntity;
import com.forcelain.android.guessphotovk.api.UserEntity;
import com.forcelain.android.guessphotovk.model.RoundModel;
import com.forcelain.android.guessphotovk.model.VariantModel;
import com.forcelain.android.guessphotovk.rx.ListSerializerFunc;
import com.forcelain.android.guessphotovk.rx.ShuffleFunc;
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
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class FriendsPhotoGameFragment extends AbstractPhotoGameFragment {

    @Override
    protected void makeRound() {
        new Api(VKAccessToken.currentToken().accessToken).getAllFriends(null)
                .map(new ShuffleFunc<UserEntity>())
                .flatMap(new ListSerializerFunc<UserEntity>())
                .flatMap(new Func1<UserEntity, Observable<UserEntity>>() {
                    @Override
                    public Observable<UserEntity> call(UserEntity userEntity) {
                        Observable<UserEntity> userObs = Observable.just(userEntity);
                        Observable<List<PhotoEntity>> userPhotosObs = new Api(VKAccessToken.currentToken().accessToken).getPhotos(userEntity.id)
                                .onErrorResumeNext(Observable.just(new ArrayList<PhotoEntity>()));
                        return Observable.zip(userObs, userPhotosObs, new Func2<UserEntity, List<PhotoEntity>, UserEntity>() {
                            @Override
                            public UserEntity call(UserEntity userEntity, List<PhotoEntity> photoEntities) {
                                userEntity.photoList = photoEntities;
                                return userEntity;
                            }
                        });
                    }
                })
                .filter(new Func1<UserEntity, Boolean>() {
                    @Override
                    public Boolean call(UserEntity userEntity) {
                        return userEntity.photoList != null && !userEntity.photoList.isEmpty();
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
                .timeout(NEW_ROUND_TIMEOUT_SEC, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<RoundModel>() {

                    @Override
                    public void onCompleted() { }

                    @Override
                    public void onError(Throwable e) { }

                    @Override
                    public void onNext(RoundModel roundModel) {
                        onRoundReady(roundModel);
                    }
                });
    }
}
