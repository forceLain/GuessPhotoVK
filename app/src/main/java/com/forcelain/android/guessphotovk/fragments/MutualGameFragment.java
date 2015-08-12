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
import com.forcelain.android.guessphotovk.model.AbstractRoundModel;
import com.forcelain.android.guessphotovk.model.MutualRoundModel;
import com.forcelain.android.guessphotovk.model.VariantModel;
import com.vk.sdk.VKAccessToken;

import java.util.ArrayList;
import java.util.Collections;
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

public class MutualGameFragment extends AbstractGameFragment {

    @Bind(R.id.mutual_container) View mutualContainer;
    @Bind(R.id.variants_container) View variantsContainer;
    @Bind(R.id.text_mutual_1) TextView mutual1TextView;
    @Bind(R.id.text_mutual_2) TextView mutual2TextView;
    @Bind(R.id.variant_yes) Button variantYesButton;
    @Bind(R.id.variant_no) Button variantNoButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mutual_game, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        newRound();
    }

    @OnClick(R.id.button_go)
    void onGoClicked(){
        newRound();
    }

    @OnClick({R.id.variant_yes, R.id.variant_no})
    void onVariantClicked(Button button){
        Boolean correct = (Boolean) button.getTag();
        button.setTextColor(correct ? Color.GREEN : Color.RED);
    }

    @Override
    protected void onRoundPreparing() {
        variantYesButton.setTextColor(Color.BLUE);
        variantNoButton.setTextColor(Color.BLUE);
    }

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
                .take(2)
                .buffer(2)
                .flatMap(new Func1<List<UserEntity>, Observable<MutualRoundModel>>() {
                    @Override
                    public Observable<MutualRoundModel> call(List<UserEntity> userEntities) {

                        Observable<List<Integer>> mutualObs = new Api(VKAccessToken.currentToken().accessToken).getMutual(userEntities.get(0).id, userEntities.get(1).id);
                        Observable<List<UserEntity>> randomGuysObs = Observable.just(userEntities);

                        return Observable.zip(mutualObs, randomGuysObs, new Func2<List<Integer>, List<UserEntity>, MutualRoundModel>() {
                            @Override
                            public MutualRoundModel call(List<Integer> mutualList, List<UserEntity> randomGuys) {
                                MutualRoundModel mutualRoundModel = new MutualRoundModel();
                                mutualRoundModel.targets = new ArrayList<>();
                                for (UserEntity userEntity : randomGuys) {
                                    VariantModel model = new VariantModel();
                                    model.title = userEntity.firstName + " " + userEntity.lastName;
                                    model.id = userEntity.id;
                                    mutualRoundModel.targets.add(model);
                                }
                                mutualRoundModel.mutuals = new ArrayList<>();
                                for (Integer integer : mutualList) {
                                    VariantModel model = new VariantModel();
                                    model.title = String.valueOf(integer);
                                    model.id = integer;
                                    mutualRoundModel.mutuals.add(model);
                                }
                                return mutualRoundModel;
                            }
                        });
                    }
                })
                .timeout(30, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<MutualRoundModel>() {

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
                    public void onNext(MutualRoundModel roundModel) {
                        onRoundReady(roundModel);
                    }
                });
    }

    @Override
    protected void onRoundReady(AbstractRoundModel roundModel) {
        MutualRoundModel mutualRoundModel = (MutualRoundModel) roundModel;
        mutual1TextView.setText(mutualRoundModel.targets.get(0).title);
        mutual2TextView.setText(mutualRoundModel.targets.get(1).title);
        Log.d("@@@@", mutualRoundModel.targets.get(0).id + " " + mutualRoundModel.targets.get(1).id);
        for (VariantModel mutual : mutualRoundModel.mutuals) {
            Log.d("@@@@", mutual.title);
        }
        boolean haveMutuals = mutualRoundModel.mutuals.size() > 1;
        variantYesButton.setTag(haveMutuals);
        variantNoButton.setTag(!haveMutuals);
    }
}
