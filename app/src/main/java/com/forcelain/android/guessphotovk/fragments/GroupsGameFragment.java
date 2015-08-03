package com.forcelain.android.guessphotovk.fragments;


import android.util.Log;

import com.forcelain.android.guessphotovk.api.Api;
import com.forcelain.android.guessphotovk.api.GroupEntity;
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

public class GroupsGameFragment extends AbstractPhotoGameFragment {

    private static final String TAG = "GameFragment";

    @Override
    protected void prepareRound() {
        new Api(VKAccessToken.currentToken().accessToken).getAllGroups()
                .map(new Func1<List<GroupEntity>, List<GroupEntity>>() {
                    @Override
                    public List<GroupEntity> call(List<GroupEntity> list) {
                        List<GroupEntity> shuffledFriendList = new ArrayList<>(list);
                        Collections.shuffle(shuffledFriendList);
                        return shuffledFriendList;
                    }
                })
                .flatMap(new Func1<List<GroupEntity>, Observable<GroupEntity>>() {
                    @Override
                    public Observable<GroupEntity> call(List<GroupEntity> list) {
                        return Observable.from(list);
                    }
                })
                .flatMap(new Func1<GroupEntity, Observable<GroupEntity>>() {
                    @Override
                    public Observable<GroupEntity> call(GroupEntity groupEntity) {
                        return new Api(VKAccessToken.currentToken().accessToken).getGroupAllPhotos(groupEntity)
                                .onErrorResumeNext(Observable.just(groupEntity));
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
