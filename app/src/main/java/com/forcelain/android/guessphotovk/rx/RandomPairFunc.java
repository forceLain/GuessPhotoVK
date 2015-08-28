package com.forcelain.android.guessphotovk.rx;

import android.support.v4.util.Pair;

import java.util.List;
import java.util.Random;

import rx.functions.Func1;

public class RandomPairFunc<T> implements Func1<List<T>, Pair<T, T>> {

    @Override
    public Pair<T, T> call(List<T> sourceList) {
        Random random = new Random();
        return Pair.create(sourceList.get(random.nextInt(sourceList.size())), sourceList.get(random.nextInt(sourceList.size())));
    }
}
