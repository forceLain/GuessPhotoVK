package com.forcelain.android.guessphotovk.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

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

    @OnClick(R.id.button_friend_mode)
      void startFriendsMode(){
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_MODE, GameActivity.MODE_FRIENDS);
        startActivity(intent);
    }

    @OnClick(R.id.button_groups_mode)
    void startGroupsMode(){
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_MODE, GameActivity.MODE_GROUPS);
        startActivity(intent);
    }

    @OnClick(R.id.button_mutual_mode)
    void startMutualMode(){
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_MODE, GameActivity.MODE_MUTUAL);
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
        VKSdk.login(this, "friends", "photos");
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
