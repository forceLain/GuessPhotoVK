package com.forcelain.android.guessphotovk.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.forcelain.android.guessphotovk.R;
import com.forcelain.android.guessphotovk.fragments.AreFriendsGameFragment;
import com.forcelain.android.guessphotovk.fragments.FriendsPhotoGameFragment;
import com.forcelain.android.guessphotovk.fragments.GroupsPhotoGameFragment;
import com.forcelain.android.guessphotovk.fragments.MutualGameFragment;
import com.forcelain.android.guessphotovk.fragments.SongGameFragment;

import butterknife.ButterKnife;


public class GameActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "EXTRA_MODE";
    private static final int MODE_NONE = 0;
    public static final int MODE_FRIENDS = 1;
    public static final int MODE_GROUPS = 2;
    public static final int MODE_MUTUAL = 3;
    public static final int MODE_ARE_FRIENDS = 4;
    public static final int MODE_SONG = 5;

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
                replaceFragment(new FriendsPhotoGameFragment());
                break;
            case MODE_GROUPS:
                replaceFragment(new GroupsPhotoGameFragment());
                break;
            case MODE_MUTUAL:
                replaceFragment(new MutualGameFragment());
                break;
            case MODE_ARE_FRIENDS:
                replaceFragment(new AreFriendsGameFragment());
                break;
            case MODE_SONG:
                replaceFragment(new SongGameFragment());
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
