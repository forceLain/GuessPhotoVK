package com.forcelain.android.guessphotovk.rx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.functions.Func1;

public class ShuffleFunc<T> implements Func1<List<T>, List<T>> {

    @Override
    public List<T> call(List<T> sourceList) {
        List<T> shuffledList = new ArrayList<>(sourceList);
        Collections.shuffle(shuffledList);
        return shuffledList;
    }
}
