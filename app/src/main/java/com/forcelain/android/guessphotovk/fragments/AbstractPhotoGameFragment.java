package com.forcelain.android.guessphotovk.fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.forcelain.android.guessphotovk.R;
import com.forcelain.android.guessphotovk.model.PhotoRoundModel;
import com.forcelain.android.guessphotovk.model.VariantModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public abstract class AbstractPhotoGameFragment extends AbstractGameFragment {

    @Bind(R.id.image_photo) ImageView photoView;
    @Bind({R.id.buttons_1_2, R.id.buttons_3_4}) List<View> buttonBars;
    @Bind({R.id.button_var1, R.id.button_var2, R.id.button_var3, R.id.button_var4}) List<Button> variantsButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photo_game, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @OnClick({R.id.button_var1, R.id.button_var2, R.id.button_var3, R.id.button_var4})
    void onVariantButtonClicked(Button view){
        view.setTextColor(Color.RED);
        for (Button button : variantsButton) {
            Boolean correct = (Boolean) button.getTag();
            button.setClickable(false);
            if (correct){
                button.setTextColor(Color.GREEN);
            }
        }
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                newRound();
            }
        }, NEW_ROUND_DELAY_MS);
    }

    @OnClick(R.id.button_go)
    void onButtonGoClicked(){
        newRound();
    }

    @Override
    protected void onRoundPreparing(){
        photoView.setImageDrawable(null);
        for (View buttonBar : buttonBars) {
            buttonBar.setVisibility(View.INVISIBLE);
        }
    }

    protected void onRoundReady(final PhotoRoundModel photoRoundModel) {

        Picasso.with(getActivity()).load(photoRoundModel.correctAnswer.photoSrc).into(photoView, new Callback() {
            @Override
            public void onSuccess() {
                for (int i = 0; i < variantsButton.size(); i++) {
                    Button button = variantsButton.get(i);
                    VariantModel variantModel = photoRoundModel.versions.get(i);
                    button.setClickable(true);
                    button.setText(variantModel.title);
                    button.setTextColor(Color.BLUE);
                    button.setTag(variantModel.id == photoRoundModel.correctAnswer.id);
                }

                for (View buttonBar : buttonBars) {
                    buttonBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError() {

            }
        });
    }
}
