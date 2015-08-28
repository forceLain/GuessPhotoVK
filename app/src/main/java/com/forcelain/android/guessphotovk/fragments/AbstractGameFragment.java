package com.forcelain.android.guessphotovk.fragments;


import android.support.v4.app.Fragment;

public abstract class AbstractGameFragment extends Fragment {

    protected static final String TAG = "GameFragment";

    protected static final long NEW_ROUND_DELAY_MS = 1000;
    protected static final long NEW_ROUND_TIMEOUT_SEC = 30;

    protected void newRound() {
        onRoundPreparing();
        makeRound();
    }

    protected abstract void onRoundPreparing();
    protected abstract void makeRound();
}
