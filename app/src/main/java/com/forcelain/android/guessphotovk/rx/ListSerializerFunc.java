package com.forcelain.android.guessphotovk.rx;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;

public class ListSerializerFunc<T> implements Func1<List<T>, Observable<T>> {

    @Override
    public Observable<T> call(List<T> sourceList) {
        return Observable.from(sourceList);
    }
}
