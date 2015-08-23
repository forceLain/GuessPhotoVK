package com.forcelain.android.guessphotovk.fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.forcelain.android.guessphotovk.R;
import com.forcelain.android.guessphotovk.api.Api;
import com.forcelain.android.guessphotovk.api.UserEntity;
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

    @Bind(R.id.list) ListView commonFriendsList;
    @Bind(R.id.mutual_container) View mutualContainer;
    @Bind(R.id.variants_container) View variantsContainer;
    @Bind(R.id.text_variant_1) TextView mutual1TextView;
    @Bind(R.id.text_variant_2) TextView mutual2TextView;
    @Bind(R.id.variant_yes) Button variantYesButton;
    @Bind(R.id.variant_no) Button variantNoButton;
    private CommonFriendsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mutual_game, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        adapter = new CommonFriendsAdapter();
        commonFriendsList.setAdapter(adapter);
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
        commonFriendsList.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onRoundPreparing() {
        variantYesButton.setTextColor(Color.BLUE);
        variantNoButton.setTextColor(Color.BLUE);
        commonFriendsList.setVisibility(View.GONE);
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
                .take(2)
                .buffer(2)
                .flatMap(new Func1<List<UserEntity>, Observable<MutualRoundModel>>() {
                    @Override
                    public Observable<MutualRoundModel> call(List<UserEntity> userEntities) {
                        Observable<List<UserEntity>> randomGuysObs = Observable.just(userEntities);

                        Observable<List<UserEntity>> commonFriendsObs = new Api(VKAccessToken.currentToken().accessToken).getMutual(userEntities.get(0).id, userEntities.get(1).id)
                                .flatMap(new Func1<List<Integer>, Observable<List<UserEntity>>>() {
                                    @Override
                                    public Observable<List<UserEntity>> call(List<Integer> integers) {
                                        return new Api(VKAccessToken.currentToken().accessToken).getUsers(integers);
                                    }
                                });

                        return Observable.zip(commonFriendsObs, randomGuysObs, new Func2<List<UserEntity>, List<UserEntity>, MutualRoundModel>() {
                            @Override
                            public MutualRoundModel call(List<UserEntity> commonFriends, List<UserEntity> randomGuys) {
                                MutualRoundModel mutualRoundModel = new MutualRoundModel();
                                mutualRoundModel.targets = new ArrayList<>();
                                for (UserEntity userEntity : randomGuys) {
                                    VariantModel model = new VariantModel();
                                    model.title = userEntity.firstName + " " + userEntity.lastName;
                                    model.id = userEntity.id;
                                    mutualRoundModel.targets.add(model);
                                }
                                mutualRoundModel.mutuals = new ArrayList<>();
                                for (UserEntity userEntity : commonFriends) {
                                    VariantModel model = new VariantModel();
                                    model.title = userEntity.firstName + " " + userEntity.lastName;
                                    model.id = userEntity.id;
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

    protected void onRoundReady(MutualRoundModel mutualRoundModel) {
        mutual1TextView.setText(mutualRoundModel.targets.get(0).title);
        mutual2TextView.setText(mutualRoundModel.targets.get(1).title);
        boolean haveMutuals = mutualRoundModel.mutuals.size() > 1;
        variantYesButton.setTag(haveMutuals);
        variantNoButton.setTag(!haveMutuals);
        adapter.setUserEntities(mutualRoundModel.mutuals);
    }

    private class CommonFriendsAdapter extends BaseAdapter {

        List<VariantModel> userEntities;

        public void setUserEntities(List<VariantModel> userEntities) {
            this.userEntities = userEntities;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return userEntities == null ? 0 : userEntities.size();
        }

        @Override
        public VariantModel getItem(int position) {
            return userEntities.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = new TextView(parent.getContext());
            VariantModel user = getItem(position);
            textView.setText(user.title);
            return textView;
        }
    }
}
