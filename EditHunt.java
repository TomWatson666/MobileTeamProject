package team5project.treasurehuntapp;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static team5project.treasurehuntapp.DataVault.dates;

/**
 * Created by joebr on 20/04/2017.
 */

public class EditHunt extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    public static final int LOADING_ANIMATION_TIME = 3000;
    public static final int FADE_OUT_TIME = 500;

    String type;
    String treasureHunt;
    EditText titleEditText, dateEditText;
    //DatePicker datePicker;
    ImageView gemHuntLogo;
    ProgressBar editHuntProgressBar;
    LinearLayout editHuntForm, buttonBar;
    static String dateBefore;
    int width = 0;
    int height = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_hunt);

        //create activity as a popup window
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;
        getWindow().setLayout((int) (width * 0.8), (int) (height * 0.5));

        type = getIntent().getStringExtra("Type");

        gemHuntLogo = (ImageView) findViewById(R.id.edit_hunt_gem_hunt_logo);
        editHuntProgressBar = (ProgressBar) findViewById(R.id.edit_hunt_progress_bar);
        editHuntForm = (LinearLayout) findViewById(R.id.edit_hunt_form);
        buttonBar = (LinearLayout) findViewById(R.id.team_tracker_team_popup_button_container);

        titleEditText = (EditText) findViewById(R.id.edit_treasure_hunt_title);
        dateEditText = (EditText) findViewById(R.id.edit_treasure_hunt_date);

        dateEditText.setFocusable(false);
        dateEditText.setFocusableInTouchMode(false);

        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment fragment = new DatePickerFragment();
                fragment.show(getSupportFragmentManager(),"date");
            }
        });

        //Create layout parameters for the dynamically made buttons
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, getResources().getDimensionPixelSize(R.dimen.button_height), 1);

        int margin = getResources().getDimensionPixelSize(R.dimen.button_margin);
        params.setMargins(margin, margin, margin, margin);

        if(type.equals("Edit")) {

            titleEditText.setFocusable(false);
            titleEditText.setFocusableInTouchMode(false);
            titleEditText.setClickable(false);

            treasureHunt = getIntent().getStringExtra("Title");

            titleEditText.setText(treasureHunt);
            dateEditText.setText(dates.get(DataVault.treasureHunts.indexOf(treasureHunt)));

            Button editLocationsButton = new Button(this);
            editLocationsButton.setLayoutParams(params);
            editLocationsButton.setText("Edit Locations");
            editLocationsButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            editLocationsButton.setTextColor(Color.WHITE);
            editLocationsButton.setBackground(getResources().getDrawable(R.drawable.button_blue));
            editLocationsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String newDate = dateEditText.getText().toString();

                    UpdateGenerateTreasureHunt hunt = new UpdateGenerateTreasureHunt();
                    hunt.execute(newDate);

                }
            });

            Button saveButton = new Button(this);
            saveButton.setLayoutParams(params);
            saveButton.setText("Save");
            saveButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            saveButton.setTextColor(Color.WHITE);
            saveButton.setBackground(getResources().getDrawable(R.drawable.button_blue));
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    validateEditInput();

                }
            });

            buttonBar.addView(editLocationsButton);
            buttonBar.addView(saveButton);

        } else {

            Button cancelButton = new Button(this);
            cancelButton.setLayoutParams(params);
            cancelButton.setText("Cancel");
            cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            cancelButton.setTextColor(Color.WHITE);
            cancelButton.setBackground(getResources().getDrawable(R.drawable.button_red));
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    startActivity(new Intent(EditHunt.this, HuntManagement.class));

                }
            });

            Button addButton = new Button(this);
            addButton.setLayoutParams(params);
            addButton.setText("Add");
            addButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            addButton.setTextColor(Color.WHITE);
            addButton.setBackground(getResources().getDrawable(R.drawable.button_blue));
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    validateAddInput();

                }
            });

            buttonBar.addView(cancelButton);
            buttonBar.addView(addButton);

        }

    }

    public void validateAddInput() {

        //a variable to ensure that the input from all the text boxes is valid
        boolean valid = true;
        View viewFocusedOn = null;

        String title = titleEditText.getText().toString();
        String date = dateEditText.getText().toString();

        //Make sure the text boxes are not empty
        if(TextUtils.isEmpty(date)) {

            Toast.makeText(EditHunt.this, "Please enter a date", Toast.LENGTH_LONG).show();

            valid = false;

        }

        if(TextUtils.isEmpty(title)) {

            Toast.makeText(EditHunt.this, "Please enter a Treasure Hunt Title", Toast.LENGTH_LONG).show();

            valid = false;

        }

        //Check to see if the date entered is equal to another date in the treasure hunt list
        for(int i = 0; i < dates.size(); i++) {

            String dateToCompare = dates.get(i);

            if(dateToCompare.equals(date)) {

                Toast.makeText(EditHunt.this, "Duplicate Date in Treasure Hunt list", Toast.LENGTH_LONG).show();

                valid = false;

            }

        }

        //Check to see that there are no other treasure hunts with the same name in the list
        for(int i = 0; i < DataVault.treasureHunts.size(); i++) {

            if(DataVault.treasureHunts.get(i).equals(title)) {

                Toast.makeText(EditHunt.this, "A Treasure already exists with that title", Toast.LENGTH_LONG).show();
                viewFocusedOn = titleEditText;

                valid = false;

            }

        }

        if(!valid) {

            if(viewFocusedOn != null) viewFocusedOn.requestFocus();

        } else {

            //To ensure it doesn't load locations from previous treasure hunt viewed
            DataVault.viewedTreasureHuntLocations.clear();

            UpdateGenerateTreasureHunt newTreasureHunt = new UpdateGenerateTreasureHunt();
            newTreasureHunt.execute(titleEditText.getText().toString(),
                    dateEditText.getText().toString());

        }

    }

    public void validateEditInput() {

        //Boolean to check that all inputs are valid
        boolean valid = true;

        String date = dateEditText.getText().toString();

        if(TextUtils.isEmpty(date)) {

            Toast.makeText(EditHunt.this, "Please enter a date", Toast.LENGTH_LONG).show();

            valid = false;
        }

        //Check to see that there are no other treasure hunts with the same date in the list
        for(int i = 0; i < dates.size(); i++) {

            String dateToCompare = dates.get(i);

            if(dateToCompare.equals(date) && !dateBefore.equals(date)) {

                Toast.makeText(EditHunt.this, "Duplicate Date in Treasure Hunt list", Toast.LENGTH_LONG).show();

                valid = false;

            }

        }

        if(valid) {

            dates.set(DataVault.treasureHunts.indexOf(treasureHunt), dateEditText.getText().toString());

            DataVault.updateTreasureHuntInfo(treasureHunt, dateEditText.getText().toString());

            startActivity(new Intent(EditHunt.this, HuntManagement.class));

        }

    }

    //A method to make the progress bar work between pages
    private void loadProgress() {

        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(LOADING_ANIMATION_TIME);
        fadeIn.setInterpolator(new AccelerateInterpolator());

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                gemHuntLogo.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        Resources res = getResources();
        Drawable progressBarDrawable = res.getDrawable(R.drawable.loading_circle);

        editHuntProgressBar.setProgressDrawable(progressBarDrawable);

        editHuntProgressBar.setMax(100);

        editHuntProgressBar.setSecondaryProgress(100);

        ObjectAnimator progressTransition = ObjectAnimator.ofInt(editHuntProgressBar, "progress",
                0, 100);
        progressTransition.setDuration(LOADING_ANIMATION_TIME);
        progressTransition.setInterpolator(new AccelerateDecelerateInterpolator());
        progressTransition.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                editHuntProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

                editHuntProgressBar.setProgress(100);
                fadeOut();

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        editHuntForm.setVisibility(View.GONE);
        progressTransition.start();
        gemHuntLogo.startAnimation(fadeIn);

    }

    //A method to fade out the progress bar when it has done loading
    private void fadeOut() {

        Animation fadeOut = new AlphaAnimation(1, 0);

        fadeOut.setDuration(FADE_OUT_TIME);
        fadeOut.setInterpolator(new AccelerateInterpolator());

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                gemHuntLogo.setVisibility(View.GONE);
                editHuntProgressBar.setVisibility(View.GONE);

                if(type.equals("Edit")) {

                    Intent nextPage = new Intent(EditHunt.this, ManagementMap.class);

                    nextPage.putExtra("Type", type);
                    nextPage.putExtra("Title", treasureHunt);
                    nextPage.putExtra("Previous Page", "EditHunt");
                    nextPage.putExtra("Correct Previous Page", "True");

                    startActivity(nextPage);

                } else {

                    Intent info = new Intent(EditHunt.this, ManagementMap.class);
                    info.putExtra("Type", type);
                    info.putExtra("Previous Page", "EditHunt");
                    info.putExtra("Correct Previous Page", "true");
                    info.putExtra("Title", titleEditText.getText().toString());
                    info.putExtra("Date", dateEditText.getText().toString());

                    startActivity(info);

                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        editHuntProgressBar.startAnimation(fadeOut);
        gemHuntLogo.startAnimation(fadeOut);

    }

    //Override the method to make sure that the date is returned in the correct format
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        StringBuilder date = new StringBuilder();

        month++;

        date.append(year + "-");
        date.append(month < 10 ? "0" + month + "-" : month + "-");
        date.append(dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth);

        dateEditText.setText(date.toString());

        editHuntForm.setVisibility(View.VISIBLE);

    }

    //An asynctask to update the treasure hunt
    public class UpdateGenerateTreasureHunt extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            if(type.equals("Edit")) {

                //Update the date of the treasure hunt accordingly
                String sqlQuery = "UPDATE `Treasure Hunt` SET Date = '" + params[0] + "' WHERE Treasure_Hunt_Title = '" + treasureHunt + "';";
                DatabaseConnection.executeQuery(sqlQuery);

                //Select location details for the treasure hunt so that it can be loaded in on the management map
                sqlQuery = "SELECT `Index`, Name, Latitude, Longitude, Clue FROM Location " +
                        "WHERE Treasure_Hunt_Title = '" + treasureHunt + "' " +
                        "ORDER BY `Index` ASC;";
                List<String> result = DatabaseConnection.executeQuery(sqlQuery);

                List<MapLocation> newLocations = new ArrayList<MapLocation>();

                if (!result.get(0).equals("nothing returned")) {
                    for (int i = 0; i < result.size(); i++) {

                        newLocations.add(new MapLocation(treasureHunt,
                                DatabaseConnection.parseResultSet(result, i, 0),
                                DatabaseConnection.parseResultSet(result, i, 1),
                                DatabaseConnection.parseResultSet(result, i, 2),
                                DatabaseConnection.parseResultSet(result, i, 3),
                                DatabaseConnection.parseResultSet(result, i, 4)));

                    }
                }

                DataVault.viewedTreasureHuntLocations = new ArrayList<MapLocation>(newLocations);

            } else {

                //Insert the treasure hunt that was created into the database, and DataVault
                String sqlQuery = "INSERT INTO `Treasure Hunt` (Treasure_Hunt_Title, Location_Count, Date) " +
                        "VALUES ('" + params[0] + "', '0', '" + params[1] + "');";
                DatabaseConnection.executeQuery(sqlQuery);

                DataVault.treasureHunts.add(params[0]);

            }
            return null;

        }

        @Override
        protected void onPreExecute() {

            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(EditHunt.this);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            getWindow().setLayout(width, height);
            editHuntForm.setVisibility(View.GONE);
            gemHuntLogo.setVisibility(View.VISIBLE);
            editHuntProgressBar.setVisibility(View.VISIBLE);
            editHuntProgressBar.setProgress(0);

            loadProgress();

        }

    }

    //This class allows the user of a Date Picker to pick the date, this ensure the correct format is given
    //for later comparisons
    public static class DatePickerFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(),
                    (DatePickerDialog.OnDateSetListener)
                            getActivity(), year, month, day);
        }
    }

    @Override
    public void onBackPressed() {

    }

}
