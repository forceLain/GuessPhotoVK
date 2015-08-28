package com.forcelain.android.guessphotovk.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.forcelain.android.guessphotovk.R;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        VKSdk.initialize(this);
    }

    @OnClick({R.id.button_friend_mode, R.id.button_groups_mode, R.id.button_mutual_mode,
            R.id.button_are_friends_mode, R.id.button_song_mode})
    void onGameModeClicked(View view){
        Intent intent = new Intent(this, GameActivity.class);
        switch (view.getId()){
            case R.id.button_friend_mode:
                intent.putExtra(GameActivity.EXTRA_MODE, GameActivity.MODE_FRIENDS);
                break;
            case R.id.button_groups_mode:
                intent.putExtra(GameActivity.EXTRA_MODE, GameActivity.MODE_GROUPS);
                break;
            case R.id.button_mutual_mode:
                intent.putExtra(GameActivity.EXTRA_MODE, GameActivity.MODE_MUTUAL);
                break;
            case R.id.button_are_friends_mode:
                intent.putExtra(GameActivity.EXTRA_MODE, GameActivity.MODE_ARE_FRIENDS);
                break;
            case R.id.button_song_mode:
                intent.putExtra(GameActivity.EXTRA_MODE, GameActivity.MODE_SONG);
                break;
        }
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkToken();
    }

    private void checkToken() {
        VKAccessToken accessToken = VKAccessToken.currentToken();
        if (accessToken == null || accessToken.isExpired()){
            askForLogin();
        }
    }

    private void askForLogin() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setMessage(R.string.need_to_login)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        login();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);
        builder.create().show();
    }

    void login() {
        VKSdk.login(this, "friends", "photos", "audio");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                res.save();
            }

            @Override
            public void onError(VKError error) {
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
