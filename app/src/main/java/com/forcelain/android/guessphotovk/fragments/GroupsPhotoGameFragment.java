package com.forcelain.android.guessphotovk.fragments;


import android.util.Log;

import com.forcelain.android.guessphotovk.api.Api;
import com.forcelain.android.guessphotovk.api.GroupEntity;
import com.forcelain.android.guessphotovk.api.PhotoEntity;
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

public class GroupsPhotoGameFragment extends AbstractPhotoGameFragment {

    @Override
    protected void makeRound() {
        new Api(VKAccessToken.currentToken().accessToken).getAllGroups()
                .map(new ShuffleFunc<GroupEntity>())
                .flatMap(new ListSerializerFunc<GroupEntity>())
                .flatMap(new Func1<GroupEntity, Observable<GroupEntity>>() {
                    @Override
                    public Observable<GroupEntity> call(GroupEntity groupEntity) {
                        Observable<GroupEntity> groupObs = Observable.just(groupEntity);
                        Observable<List<PhotoEntity>> photosObs = new Api(VKAccessToken.currentToken().accessToken).getPhotos(-groupEntity.id)
                                .onErrorResumeNext(Observable.just(new ArrayList<PhotoEntity>()));

                        return Observable.zip(groupObs, photosObs, new Func2<GroupEntity, List<PhotoEntity>, GroupEntity>() {
                            @Override
                            public GroupEntity call(GroupEntity groupEntity, List<PhotoEntity> photoEntities) {
                                groupEntity.photoList = photoEntities;
                                return groupEntity;
                            }
                        });
                    }
                })
                .filter(new Func1<GroupEntity, Boolean>() {
                    @Override
                    public Boolean call(GroupEntity groupEntity) {
                        return groupEntity.photoList != null && !groupEntity.photoList.isEmpty();
                    }
                })
                .map(new Func1<GroupEntity, VariantModel>() {
                    @Override
                    public VariantModel call(GroupEntity groupEntity) {
                        VariantModel variantModel = new VariantModel();
                        variantModel.title = groupEntity.name;
                        variantModel.id = groupEntity.id;

                        if (groupEntity.photoList != null && !groupEntity.photoList.isEmpty()) {
                            PhotoEntity photoEntity = groupEntity.photoList.get(new Random().nextInt(groupEntity.photoList.size()));
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
                    public void onError(Throwable e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }

                    @Override
                    public void onNext(RoundModel roundModel) {
                        onRoundReady(roundModel);
                    }
                });
    }
}
