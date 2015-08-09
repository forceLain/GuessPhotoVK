package com.forcelain.android.guessphotovk.fragments;


import android.support.v4.app.Fragment;

import com.forcelain.android.guessphotovk.model.AbstractRoundModel;
import com.forcelain.android.guessphotovk.model.RoundModel;

public abstract class AbstractGameFragment extends Fragment {

    protected static final String TAG = "GameFragment";

    private static final long NEW_ROUND_DELAY_MS = 1000;

    protected void newRound() {
        onRoundPreparing();
        prepareRound();
    }

    protected abstract void onRoundPreparing();
    protected abstract void prepareRound();
    protected abstract void onRoundReady(final AbstractRoundModel roundModel);
}
