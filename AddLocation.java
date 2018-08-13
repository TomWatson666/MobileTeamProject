package team5project.treasurehuntapp;

/**
 * Created by rzarathore on 09/04/2017.
 */

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AddLocation extends AppCompatActivity {

    public static final int LOADING_ANIMATION_TIME = 50;
    public static final int FADE_OUT_TIME = 500;

    EditText Name, Index, Clue;
    ProgressBar addLocationProgressBar;
    ImageView gemHuntLogo;
    TextView addLocationLoadingText;
    LinearLayout addLocationContainer;
    int numTasks = 0;
    int progress = 0;
    int width;
    int height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_location);

        //create activity as a popup window
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = dm.widthPixels;
        height = dm.heightPixels;
        getWindow().setLayout((int)(width*0.8), (int)(height*0.6));

        addLocationProgressBar = (ProgressBar) findViewById(R.id.add_location_progress_bar);
        gemHuntLogo = (ImageView) findViewById(R.id.add_location_gem_hunt_logo);
        addLocationLoadingText = (TextView) findViewById(R.id.add_location_update_text);
        addLocationContainer = (LinearLayout) findViewById(R.id.add_location_container);

        Index = (EditText) findViewById(R.id.indexField);
        Name = (EditText) findViewById(R.id.nameField);
        Clue = (EditText) findViewById(R.id.clueField);

    }

    public void validateInput() {

        //This will determine whether input is valid, and find which text box to focus on
        boolean valid = true;
        View viewFocusedOn = null;

        String name = Name.getText().toString();
        String index = Index.getText().toString();
        String clue = Clue.getText().toString();
        String latitude = Double.toString(ManagementMap.placeLatitude);
        String longitude = Double.toString(ManagementMap.placeLongitude);

        //Check that all edit texts are filled in
        if(TextUtils.isEmpty(clue)) {
            Clue.setError("Please enter a clue");
            viewFocusedOn = Clue;
            valid = false;
        }

        if(TextUtils.isEmpty(index)) {
            Index.setError("Please enter an index");
            viewFocusedOn = Index;
            valid = false;
        }

        if(TextUtils.isEmpty(name)) {
            Name.setError("Please enter a name");
            viewFocusedOn = Name;
            valid = false;
        }

        //Check to see that the index is in the correct range
        if(!TextUtils.isEmpty(index)) {
            if (Integer.parseInt(index) < 1 || Integer.parseInt(index) > DataVault.viewedTreasureHuntLocations.size() + 1) {
                Index.setError("Needs to be in range 1 - " + (DataVault.viewedTreasureHuntLocations.size() + 1));
                Index.setText("");
                viewFocusedOn = Index;
                valid = false;
            }
        }

        //Check to see that the name and clue have the correct characters only
        if(!clue.matches("^[a-zA-Z0-9' ]*$")) {
            Clue.setError("Invalid characters used");
            Clue.setText("");
            viewFocusedOn = Clue;
            valid = false;
        }

        if(!name.matches("^[a-zA-Z0-9' ]*$")) {
            Name.setError("Invalid characters used");
            Name.setText("");
            viewFocusedOn = Name;
            valid = false;
        }

        StringBuilder newField;
        String[] parts;

        //If there is an apostrophe, then it will break sql unless doubled up
        if((parts = clue.split("'")).length > 1) {
            newField = new StringBuilder();
            for(int i = 0; i < parts.length; i++)
                newField.append(i == parts.length - 1 ? parts[i] : parts[i] + "''");

            clue = newField.toString();
        }

        if((parts = name.split("'")).length > 1) {
            newField = new StringBuilder();
            for(int i = 0; i < parts.length; i++) {
                newField.append(i == parts.length - 1 ? parts[i] : parts[i] + "''");
            }

            name = newField.toString();
        }

        if(!valid) {

            viewFocusedOn.requestFocus();

        } else {

            //Add location
            MapLocation location = new MapLocation(ManagementMap.viewedTreasureHuntTitle,
                    index, name, latitude, longitude, clue);

            AddNewLocation addNewLocation = new AddNewLocation();
            addNewLocation.execute(location);

        }

    }

    public void OnAddLocation(View view){

        validateInput();

    }

    public void OnCancelPopUp(View view){
        finish();
    }

    @Override
    public void onBackPressed() {

    }

    //A method to make progress appear and animate from 0 to max value
    private void loadProgress(String task) {

        addLocationLoadingText.setText(task);

        int multiplier = 100 / numTasks;

        addLocationProgressBar.setMax(numTasks * multiplier);
        addLocationProgressBar.setSecondaryProgress(numTasks * multiplier);

        int progressBefore = (progress - 1) * multiplier;
        int progressAfter = progress * multiplier;

        //To avoid dividing by 0 exception
        float alphaBefore = progress == 1 ? 0 : (float) (progress - 1) / numTasks;
        float alphaAfter = (float) progress / numTasks;

        Animation fadeIn = new AlphaAnimation(alphaBefore, alphaAfter);
        fadeIn.setDuration(LOADING_ANIMATION_TIME);
        fadeIn.setInterpolator(new LinearInterpolator());

        ObjectAnimator progressTransition = ObjectAnimator.ofInt(addLocationProgressBar, "progress",
                progressBefore, progressAfter);
        progressTransition.setDuration(LOADING_ANIMATION_TIME);
        progressTransition.setInterpolator(new LinearInterpolator());

        progressTransition.start();
        gemHuntLogo.startAnimation(fadeIn);

    }

    //A method to make the progress bar fade out
    private void fadeOut() {

        addLocationLoadingText.setText("Done");

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
                addLocationProgressBar.setVisibility(View.GONE);
                addLocationLoadingText.setVisibility(View.GONE);

                Intent nextPage = new Intent(AddLocation.this, ManagementMap.class);
                nextPage.putExtra("Previous Page", "AddLocation");
                startActivity(nextPage);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        addLocationLoadingText.startAnimation(fadeOut);
        addLocationProgressBar.startAnimation(fadeOut);
        gemHuntLogo.startAnimation(fadeOut);

    }

    //Async task to Add a new location
    public class AddNewLocation extends AsyncTask<MapLocation, String, Void> {

        @Override
        protected void onPreExecute() {

            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(AddLocation.this);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            addLocationContainer.setVisibility(View.GONE);
            gemHuntLogo.setVisibility(View.VISIBLE);
            addLocationProgressBar.setVisibility(View.VISIBLE);
            addLocationLoadingText.setVisibility(View.VISIBLE);
            addLocationProgressBar.setProgress(0);
            getWindow().setLayout(width, height);

        }

        @Override
        protected void onPostExecute(Void aVoid) {

            fadeOut();

        }

        @Override
        protected void onProgressUpdate(String... values) {

            //This will give accurate feedback on current loading progress
            progress++;
            loadProgress(values[0]);

        }

        @Override
        protected Void doInBackground(MapLocation... locations) {

            String sqlQuery;

            if(!DataVault.viewedTreasureHuntLocations.isEmpty()) {
                for (int i = DataVault.viewedTreasureHuntLocations.size() - 1; i >= Integer.parseInt(locations[0].getIndex()) - 1; i--) {

                    numTasks = DataVault.viewedTreasureHuntLocations.size() - Integer.parseInt(locations[0].getIndex()) + 1;

                    //Update locations to account for new one being added (shift all up by 1)
                    sqlQuery = "UPDATE Location SET `Index` = '" + (Integer.parseInt(DataVault.viewedTreasureHuntLocations.get(i).getIndex()) + 1) +
                            "', QR_Code = '" + DataVault.viewedTreasureHuntLocations.get(0).getTreasureHuntTitle() + "|" +
                            + (Integer.parseInt(DataVault.viewedTreasureHuntLocations.get(i).getIndex()) + 1) +
                            "' WHERE Treasure_Hunt_Title = '" + ManagementMap.viewedTreasureHuntTitle +
                            "' AND `Index` = '" + DataVault.viewedTreasureHuntLocations.get(i).getIndex() + "';";
                    DatabaseConnection.executeQuery(sqlQuery);

                    publishProgress("Updating Treasure Hunt Details");

                }

                numTasks = 2;

                //Insert new location at the point given
                sqlQuery = "INSERT INTO Location (Treasure_Hunt_Title, `Index`, Name, Longitude, Latitude, QR_Code, Clue) " +
                        "VALUES ('" + DataVault.viewedTreasureHuntLocations.get(0).getTreasureHuntTitle() + "', '" +
                        locations[0].getIndex() + "', '" + locations[0].getName() + "', '" + locations[0].getLongitude() +
                        "', '" + locations[0].getLatitude() + "', '" + locations[0].getQrCode() + "', '" +
                        locations[0].getClue() + "');";
                DatabaseConnection.executeQuery(sqlQuery);

                publishProgress("Adding Location");

                DataVault.viewedTreasureHuntLocations.add(locations[0]);

                //Update the treasure hunt location count to account for the new location being added
                sqlQuery = "UPDATE `Treasure Hunt` SET Location_Count = '" + DataVault.viewedTreasureHuntLocations.size() +
                        "' WHERE Treasure_Hunt_Title = '" + ManagementMap.viewedTreasureHuntTitle + "';";
                DatabaseConnection.executeQuery(sqlQuery);

                publishProgress("Updating Treasure Hunt");

            } else {

                numTasks = 2;

                //Insert the location as it is the max index, it does not need to shuffle any other locations about
                sqlQuery = "INSERT INTO Location (Treasure_Hunt_Title, `Index`, Name, Longitude, Latitude, QR_Code, Clue) " +
                        "VALUES ('" + ManagementMap.viewedTreasureHuntTitle + "', '" +
                        locations[0].getIndex() + "', '" + locations[0].getName() + "', '" + locations[0].getLongitude() +
                        "', '" + locations[0].getLatitude() + "', '" + locations[0].getQrCode() + "', '" +
                        locations[0].getClue() + "');";
                DatabaseConnection.executeQuery(sqlQuery);

                publishProgress("Adding Location");

                DataVault.viewedTreasureHuntLocations.add(locations[0]);

                sqlQuery = "UPDATE `Treasure Hunt` SET Location_Count = '" + DataVault.viewedTreasureHuntLocations.size() +
                        "' WHERE Treasure_Hunt_Title = '" + ManagementMap.viewedTreasureHuntTitle + "';";
                DatabaseConnection.executeQuery(sqlQuery);

                publishProgress("Updating Treasure Hunt");

            }

            return null;

        }
    }

}
