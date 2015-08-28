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
import com.forcelain.android.guessphotovk.api.SongEntity;
import com.forcelain.android.guessphotovk.api.UserEntity;
import com.forcelain.android.guessphotovk.model.SongRoundModel;
import com.forcelain.android.guessphotovk.model.SongVariantModel;
import com.forcelain.android.guessphotovk.model.VariantModel;
import com.vk.sdk.VKAccessToken;

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
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class SongGameFragment extends AbstractGameFragment {

    @Bind(R.id.text_variant_1) Button variant1TextView;
    @Bind(R.id.text_variant_2) Button variant2TextView;
    @Bind(R.id.text_variant_3) Button variant3TextView;
    @Bind(R.id.text_variant_4) Button variant4TextView;
    @Bind(R.id.text_song_name) TextView songNameTextView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_song_game, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        newRound();
    }

    @Override
    protected void onRoundPreparing() {
        variant1TextView.setText(null);
        variant1TextView.setTextColor(Color.BLUE);
        variant2TextView.setText(null);
        variant2TextView.setTextColor(Color.BLUE);
        variant3TextView.setText(null);
        variant3TextView.setTextColor(Color.BLUE);
        variant4TextView.setText(null);
        variant4TextView.setTextColor(Color.BLUE);
        songNameTextView.setText(null);
    }

    @OnClick({R.id.text_variant_1, R.id.text_variant_2, R.id.text_variant_3, R.id.text_variant_4})
    void onVariantClicked(Button clickedButton){
        int songId = (int) songNameTextView.getTag();
        boolean success = checkVariantButton(songId, clickedButton, true);
        if (!success){
            checkVariantButton(songId, variant1TextView, false);
            checkVariantButton(songId, variant2TextView, false);
            checkVariantButton(songId, variant3TextView, false);
            checkVariantButton(songId, variant4TextView, false);
        }
        clickedButton.postDelayed(new Runnable() {
            @Override
            public void run() {
                newRound();
            }
        }, NEW_ROUND_DELAY_MS);
    }

    private boolean checkVariantButton(int songId, Button button, boolean showWrong) {
        List<Integer> songIdList = (List<Integer>) button.getTag();
        boolean success = songIdList.contains(songId);
        if (showWrong){
            button.setTextColor(success ? Color.GREEN : Color.RED);
        } else {
            if (success){
                button.setTextColor(Color.GREEN);
            }
        }
        return success;
    }


    @Override
    protected void prepareRound() {

        new Api(VKAccessToken.currentToken().accessToken).getAllFriends(null)
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
                .concatMap(new Func1<UserEntity, Observable<UserEntity>>() {
                    @Override
                    public Observable<UserEntity> call(UserEntity userEntity) {
                        Observable<UserEntity> singleUserObs = Observable.just(userEntity);
                        Observable<List<SongEntity>> songsObs = new Api(VKAccessToken.currentToken().accessToken).getAllSongs(userEntity.id)
                                .onErrorResumeNext(Observable.just(new ArrayList<SongEntity>()));
                        return Observable.zip(singleUserObs, songsObs, new Func2<UserEntity, List<SongEntity>, UserEntity>() {
                            @Override
                            public UserEntity call(UserEntity userEntity, List<SongEntity> list) {
                                userEntity.songList = list;
                                return userEntity;
                            }
                        });
                    }
                })
                .filter(new Func1<UserEntity, Boolean>() {
                    @Override
                    public Boolean call(UserEntity userEntity) {
                        return userEntity.songList != null && !userEntity.songList.isEmpty();
                    }
                })
                .take(4)
                .buffer(4)
                .map(new Func1<List<UserEntity>, SongRoundModel>() {
                    @Override
                    public SongRoundModel call(List<UserEntity> list) {
                        SongRoundModel roundModel = new SongRoundModel();
                        roundModel.versions = new ArrayList<>();
                        for (UserEntity userEntity : list) {
                            SongVariantModel variantModel = new SongVariantModel();
                            variantModel.title = userEntity.firstName + " " + userEntity.lastName;
                            variantModel.songIdList = new ArrayList<>();
                            for (SongEntity songEntity : userEntity.songList) {
                                variantModel.songIdList.add(songEntity.id);
                            }
                            roundModel.versions.add(variantModel);
                        }
                        List<SongEntity> songList = list.get(new Random().nextInt(list.size())).songList;
                        SongEntity randomSong = songList.get(new Random().nextInt(songList.size()));
                        roundModel.song = new VariantModel();
                        roundModel.song.title = randomSong.artist + " " + randomSong.title;
                        roundModel.song.id = randomSong.id;
                        return roundModel;
                    }
                })
                .timeout(30, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<SongRoundModel>() {

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
                    public void onNext(SongRoundModel roundModel) {
                        onRoundReady(roundModel);
                    }
                });
    }

    protected void onRoundReady(SongRoundModel roundModel) {
        SongVariantModel variantModel1 = roundModel.versions.get(0);
        SongVariantModel variantModel2 = roundModel.versions.get(1);
        SongVariantModel variantModel3 = roundModel.versions.get(2);
        SongVariantModel variantModel4 = roundModel.versions.get(3);
        variant1TextView.setText(variantModel1.title);
        variant1TextView.setTag(variantModel1.songIdList);
        variant2TextView.setText(variantModel2.title);
        variant2TextView.setTag(variantModel2.songIdList);
        variant3TextView.setText(variantModel3.title);
        variant3TextView.setTag(variantModel3.songIdList);
        variant4TextView.setText(variantModel4.title);
        variant4TextView.setTag(variantModel4.songIdList);
        songNameTextView.setText(roundModel.song.title);
        songNameTextView.setTag(roundModel.song.id);
    }
}
