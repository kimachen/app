package com.brainydroid.daydreaming.ui.sequences;

import android.app.Activity;
import android.text.method.LinkMovementMethod;
import android.util.FloatMath;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brainydroid.daydreaming.R;
import com.brainydroid.daydreaming.background.Logger;
import com.brainydroid.daydreaming.db.StarRatingQuestionDescriptionDetails;
import com.brainydroid.daydreaming.db.StarRatingSubQuestion;
import com.brainydroid.daydreaming.sequence.StarRatingAnswer;
import com.brainydroid.daydreaming.ui.AlphaRatingBar;
import com.google.inject.Inject;

import java.util.ArrayList;

import roboguice.inject.InjectResource;

@SuppressWarnings("UnusedDeclaration")
public class StarRatingQuestionViewAdapter extends BaseQuestionViewAdapter
        implements IQuestionViewAdapter {

    private static String TAG = "StarRatingQuestionViewAdapter";

    @InjectResource(R.string.questionStarRating_please_rate) String
            textPleaseRate;
    @InjectResource(R.string.questionStarRating_star_ratings_untouched_multiple)
    String errorUntouchedMultiple;
    @InjectResource(R.string.questionStarRating_star_ratings_untouched_single)
    String errorUntouchedSingle;
    @InjectResource(R.string.questionStarRating_skipped) String textSkipped;

    @Inject ArrayList<View> subQuestionsViews;
    @Inject StarRatingAnswer answer;

    @Override
    protected ArrayList<View> inflateViews(Activity activity, RelativeLayout outerPageLayout,
                                           LinearLayout questionLayout) {
        Logger.d(TAG, "Inflating question views");

        ArrayList<StarRatingSubQuestion> subQuestions =
                ((StarRatingQuestionDescriptionDetails)question.getDetails())
                        .getSubQuestions();

        for (StarRatingSubQuestion subQuestion : subQuestions) {
            View view = inflateView(questionLayout, subQuestion);
            subQuestionsViews.add(view);
        }
        return subQuestionsViews;

    }

    private View inflateView(LinearLayout questionLayout, StarRatingSubQuestion subQuestion) {
        Logger.v(TAG, "Inflating view for subQuestion");

        View view = layoutInflater.inflate(R.layout.question_star_rating, questionLayout, false);
        final ArrayList<String> hints = subQuestion.getHints();
        final int hintsNumber = hints.size();

        TextView qText = (TextView)view.findViewById(R.id.question_star_rating_mainText);
        String initial_qText = subQuestion.getText();
        qText.setText(getExtendedQuestionText(initial_qText));
        qText.setMovementMethod(LinkMovementMethod.getInstance());

        TextView leftHintText = (TextView)view.findViewById(R.id.question_star_rating_leftHint);
        leftHintText.setText(hints.get(0));

        TextView rightHintText = (TextView)view.findViewById(R.id.question_star_rating_rightHint);
        rightHintText.setText(hints.get(hintsNumber - 1));

        final AlphaRatingBar ratingBar =
                (AlphaRatingBar)view.findViewById(R.id.question_star_rating_ratingBar);
        ratingBar.setProgressDrawable(view.getResources().getDrawable(R.drawable.question_slider_progress));
        ratingBar.setThumb(view.getResources().getDrawable(R.drawable.question_slider_thumb));

        final int numStars = subQuestion.getNumStars();
        final int effectiveNumStars;
        if (numStars != StarRatingSubQuestion.DEFAULT_NUM_STARS) {
            Logger.v(TAG, "Setting ratingBar numStars to {0}", numStars);
            ratingBar.setNumStars(numStars);
            effectiveNumStars = numStars;
        } else {
            effectiveNumStars = 5;
        }

        float stepSize = subQuestion.getStepSize();
        if (stepSize != StarRatingSubQuestion.DEFAULT_STEP_SIZE) {
            Logger.v(TAG, "Setting ratingBar stepSize to {}", stepSize);
            ratingBar.setStepSize(stepSize);
        }

        final float initialRating = subQuestion.getInitialRating();
        if (initialRating != StarRatingSubQuestion.DEFAULT_INITIAL_RATING) {
            Logger.v(TAG, "Setting ratingBar initial rating to {0}",
                    initialRating);
            ratingBar.setRating(initialRating);
        }

        final TextView selectedRating = (TextView)view.findViewById(R.id.question_star_rating_selectedRating);
        if (!subQuestion.getShowLiveIndication()) {
            selectedRating.setVisibility(View.GONE);
        }

        if (subQuestion.getAlreadyValid()) {
            Logger.v(TAG, "RatingBar is initially valid -> setting hint");
            float rating = ratingBar.getRating();
            int index = (int) FloatMath.floor((rating / (float)effectiveNumStars) * hintsNumber);
            if (index == hintsNumber) {
                // Have an open interval to the right
                index -= 1;
            }

            selectedRating.setText(hints.get(index));
        } else {
            Logger.v(TAG, "RatingBar is initially not valid -> setting transparent");
            ratingBar.setAlpha(0.5f);
        }

        final CheckBox naCheckBox =
                (CheckBox)view.findViewById(R.id.question_star_rating_naCheckBox);
        if (!subQuestion.getNotApplyAllowed()) {
            naCheckBox.setVisibility(View.GONE);
        }

        AlphaRatingBar.OnAlphaRatingBarChangeListener listener = new AlphaRatingBar.OnAlphaRatingBarChangeListener() {

            @Override
            public void onRatingChanged(AlphaRatingBar ratingBar,
                                        float rating, boolean fromUser) {
                if (!naCheckBox.isChecked()) {
                    Logger.v(TAG, "RatingBar rating changed -> changing text " +
                            "and transparency");
                    int index = (int) FloatMath.floor((rating / (float)effectiveNumStars) * hintsNumber);
                    if (index == hintsNumber) {
                        // Have an open interval to the right
                        index -= 1;
                    }

                    selectedRating.setText(hints.get(index));
                    ratingBar.setAlpha(1f);
                } else {
                    // Only reset the rating if the change came from the
                    // user, otherwise we might generate an infinite loop
                    if (fromUser) {
                        Logger.v(TAG, "RatingBar touched by user but skipping" +
                                " checkbox checked -> doing nothing");
                        ratingBar.setRating(initialRating);
                    }
                }
            }

        };

        ratingBar.setOnRatingBarChangeListener(listener);

        CheckBox.OnCheckedChangeListener naListener =
                new CheckBox.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ratingBar.setRating(initialRating);
                ratingBar.setAlpha(0.5f);
                if (b) {
                    Logger.d(TAG, "Skipping checkbox checked, " +
                            "disabling the rating bar");
                    selectedRating.setText(textSkipped);
                } else {
                    Logger.d(TAG, "Skipping checkbox unchecked, " +
                            "re-enabling the rating bar");
                    selectedRating.setText(textPleaseRate);
                }
            }

        };

        naCheckBox.setOnCheckedChangeListener(naListener);

        return view;
    }

    @Override
    public boolean validate() {
        Logger.i(TAG, "Validating answer");

        boolean isMultiple = subQuestionsViews.size() > 1;

        for (View subQuestionView : subQuestionsViews) {
            TextView selectedRating = (TextView)subQuestionView.findViewById(
                    R.id.question_star_rating_selectedRating);

            if (selectedRating.getText().equals(textPleaseRate)) {
                Logger.v(TAG, "Found an untouched star rating");
                Toast.makeText(context,
                        isMultiple ? errorUntouchedMultiple :
                                errorUntouchedSingle,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }

    @Override
    public void saveAnswer() {
        Logger.i(TAG, "Saving question answer");

        for (View subQuestionView : subQuestionsViews) {
            AlphaRatingBar ratingBar = (AlphaRatingBar)subQuestionView.findViewById(
                    R.id.question_star_rating_ratingBar);
            TextView textView = (TextView)subQuestionView.findViewById(
                    R.id.question_star_rating_mainText);
            String text = textView.getText().toString();
            CheckBox naCheckBox = (CheckBox)subQuestionView.findViewById(
                    R.id.question_star_rating_naCheckBox);
            float rating = naCheckBox.isChecked() ? -1 : ratingBar.getRating();
            answer.addAnswer(text, rating);
        }

        question.setAnswer(answer);
    }

}
