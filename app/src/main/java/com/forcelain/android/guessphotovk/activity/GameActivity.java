package com.forcelain.android.guessphotovk.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.forcelain.android.guessphotovk.R;
import com.forcelain.android.guessphotovk.fragments.FriendsGameFragment;
import com.forcelain.android.guessphotovk.fragments.GroupsGameFragment;
import com.forcelain.android.guessphotovk.fragments.MutualGameFragment;

import butterknife.ButterKnife;


public class GameActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "EXTRA_MODE";
    private static final int MODE_NONE = 0;
    public static final int MODE_FRIENDS = 1;
    public static final int MODE_GROUPS = 2;
    public static final int MODE_MUTUAL = 3;

    private int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        ButterKnife.bind(this);
        mode = getIntent().getIntExtra(EXTRA_MODE, MODE_NONE);

        if (savedInstanceState == null){
            onFirstCreate();
        }
    }

    private void onFirstCreate() {
        switch (mode){
            case MODE_FRIENDS:
                replaceFragment(new FriendsGameFragment());
                break;
            case MODE_GROUPS:
                replaceFragment(new GroupsGameFragment());
                break;
            case MODE_MUTUAL:
                replaceFragment(new MutualGameFragment());
                break;
            default:
                finish();
                break;
        }
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.game_fragment, fragment)
                .commit();
    }
}
