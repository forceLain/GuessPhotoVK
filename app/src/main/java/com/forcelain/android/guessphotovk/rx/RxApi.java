package com.forcelain.android.guessphotovk.rx;

import com.forcelain.android.guessphotovk.api.Api;
import com.forcelain.android.guessphotovk.api.GroupEntity;
import com.forcelain.android.guessphotovk.api.PhotoEntity;
import com.forcelain.android.guessphotovk.api.SongEntity;
import com.forcelain.android.guessphotovk.api.UserEntity;

import java.util.List;

import rx.Observable;
import rx.Subscriber;

public class RxApi {

    private final Api api;

    public RxApi(Api api) {
        this.api = api;
    }

    public Observable<List<UserEntity>> getUsers(final Iterable<Integer> ids){
        return Observable.create(new Observable.OnSubscribe<List<UserEntity>>() {
            @Override
            public void call(Subscriber<? super List<UserEntity>> subscriber) {
                try {
                    subscriber.onNext(api.getUsers(ids));
                } catch (Exception e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    public Observable<List<Integer>> getCommonFriends(final int sourceId, final int targetId){
        return Observable.create(new Observable.OnSubscribe<List<Integer>>() {
            @Override
            public void call(Subscriber<? super List<Integer>> subscriber) {
                try {
                    subscriber.onNext(api.getCommonFriends(sourceId, targetId));
                } catch (Exception e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    public Observable<List<GroupEntity>> getAllGroups(){
        return Observable.create(new Observable.OnSubscribe<List<GroupEntity>>() {
            @Override
            public void call(Subscriber<? super List<GroupEntity>> subscriber) {
                try {
                    subscriber.onNext(api.getGroups());
                } catch (Exception e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    public Observable<List<UserEntity>> getFriends(final Integer userId){
        return Observable.create(new Observable.OnSubscribe<List<UserEntity>>() {
            @Override
            public void call(Subscriber<? super List<UserEntity>> subscriber) {
                try {
                    subscriber.onNext(api.getFriends(userId));
                } catch (Exception e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    public Observable<List<SongEntity>> getSongs(final int userId){
        return Observable.create(new Observable.OnSubscribe<List<SongEntity>>() {
            @Override
            public void call(Subscriber<? super List<SongEntity>> subscriber) {
                try {
                    subscriber.onNext(api.getSongs(userId));
                } catch (Exception e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    public Observable<List<PhotoEntity>> getPhotos(final int userId){
        return Observable.create(new Observable.OnSubscribe<List<PhotoEntity>>() {
            @Override
            public void call(Subscriber<? super List<PhotoEntity>> subscriber) {
                try {
                    subscriber.onNext(api.getPhotos(userId));
                } catch (Exception e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }
}
