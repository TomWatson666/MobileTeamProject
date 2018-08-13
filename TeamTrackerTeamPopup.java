package team5project.treasurehuntapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import static team5project.treasurehuntapp.DataVault.teamNames;

/**
 * Created by tomwa on 21/04/2017.
 */

public class TeamTrackerTeamPopup extends AppCompatActivity {

    EditText teamNameEditText, teamCodeEditText, teamStartLocationIndexEditText;
    Button saveButton, cancelButton, addButton;
    String team, function;
    int index = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.team_tracker_team_popup);

        Intent info = getIntent();

        function = info.getStringExtra("Function");
        if(function.equals("Edit"))
            team = info.getStringExtra("Team");

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int)(width*0.8), (int)(height*0.6));

        teamNameEditText = (EditText) findViewById(R.id.team_tracker_popup_team_name_text);
        teamCodeEditText = (EditText) findViewById(R.id.team_tracker_popup_team_code_text);
        teamStartLocationIndexEditText = (EditText) findViewById(R.id.team_tracker_popup_starting_location_index_text);

        LinearLayout buttonPanel = (LinearLayout) findViewById(R.id.team_tracker_team_popup_button_container);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        params.gravity = Gravity.CENTER_VERTICAL;

        int margin = getResources().getDimensionPixelSize(R.dimen.button_margin);
        params.setMargins(margin, margin, margin, margin);

        cancelButton = new Button(this);
        cancelButton.setLayoutParams(params);
        cancelButton.setText("Cancel");
        cancelButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        cancelButton.setTextColor(Color.WHITE);
        cancelButton.setBackground(getResources().getDrawable(R.drawable.button_red));

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TeamTrackerTeamPopup.this, TeamProgressTracker.class));
            }
        });

        if(function.equals("Edit")) {

            for(int i = 0; i < teamNames.size(); i++) {

                if(team.equals(teamNames.get(i))) {

                    index = i;
                    break;

                }

            }

            teamNameEditText.setText(team);
            teamCodeEditText.setText(DataVault.teamCodes.get(index));
            teamStartLocationIndexEditText.setText(DataVault.teamStartLocations.get(index));

            saveButton = new Button(this);
            saveButton.setLayoutParams(params);
            saveButton.setText("Save");
            saveButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            saveButton.setTextColor(Color.WHITE);
            saveButton.setBackground(getResources().getDrawable(R.drawable.button_blue));

            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    validateInput();

                }
            });

            buttonPanel.addView(saveButton);
            buttonPanel.addView(cancelButton);

        } else {

            addButton = new Button(this);
            addButton.setLayoutParams(params);
            addButton.setText("Add");
            addButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            addButton.setTextColor(Color.WHITE);
            addButton.setBackground(getResources().getDrawable(R.drawable.button_blue));

            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    validateInput();

                }
            });

            buttonPanel.addView(addButton);
            buttonPanel.addView(cancelButton);

        }

    }

    public void validateInput() {

        //Variable to check if the input provided by the user is valid
        boolean valid = true;
        View viewFocusedOn = null;

        String teamName = teamNameEditText.getText().toString();
        String teamCode = teamCodeEditText.getText().toString();
        String teamStartingLocationIndex = teamStartLocationIndexEditText.getText().toString();

        //Check that all text boxes aren't empty
        if(TextUtils.isEmpty(teamStartingLocationIndex)) {

            teamStartLocationIndexEditText.setError("Please enter a Starting Location");
            viewFocusedOn = teamStartLocationIndexEditText;

            valid = false;

        }

        if(TextUtils.isEmpty(teamCode)) {

            teamCodeEditText.setError("Please enter a Team Code");
            viewFocusedOn = teamCodeEditText;

            valid = false;

        }

        if(TextUtils.isEmpty(teamName)) {

            teamNameEditText.setError("Please enter a Team Name");
            viewFocusedOn = teamNameEditText;

            valid = false;

        }

        //Check that the team starting location is not less than 1
        if(!TextUtils.isEmpty(teamStartingLocationIndex)) {
            if (Integer.parseInt(teamStartingLocationIndex) < 1) {

                teamStartLocationIndexEditText.setError("Please enter an index that is not 0");
                teamStartLocationIndexEditText.setText("");
                viewFocusedOn = teamStartLocationIndexEditText;

                valid = false;

            }
        }

        //Check that the input for team name and code, are correct format
        if(!teamCode.matches("^[a-zA-Z0-9_ ]*$")) {

            teamCodeEditText.setError("Alphanumeric characters only");
            teamCodeEditText.setText("");
            viewFocusedOn = teamCodeEditText;

            valid = false;

        }

        if(!teamName.matches("^[a-zA-Z0-9 ]*$")) {

            teamNameEditText.setError("Alphanumeric characters only");
            teamNameEditText.setText("");
            viewFocusedOn = teamNameEditText;

            valid = false;

        }

        boolean isntSelf;

        if(function.equals("Edit")) {

            isntSelf = !teamName.equals(team);

        } else {

            isntSelf = true;

        }

        for(int i = 0; i < DataVault.teamNames.size(); i++) {

            if(DataVault.teamNames.get(i).equals(teamName) && isntSelf) {

                teamNameEditText.setError("The team you entered already exists");
                teamNameEditText.setText("");
                viewFocusedOn = teamNameEditText;

                valid = false;

            }

        }

        if(!valid) {

            viewFocusedOn.requestFocus();

        } else {

            if(function.equals("Edit")) {

                teamNames.set(index, teamName);
                DataVault.teamCodes.set(index, teamCode);
                DataVault.teamStartLocations.set(index, teamStartingLocationIndex);

                DataVault.editTeam(team, teamName, teamCode, teamStartingLocationIndex);

                startActivity(new Intent(TeamTrackerTeamPopup.this, TeamProgressTracker.class));

            } else {

                teamNames.add(teamName);
                DataVault.teamProgress.add("0");
                DataVault.teamLongitudes.add("0");
                DataVault.teamLatitudes.add("0");
                DataVault.teamCodes.add(teamCode);
                DataVault.teamStartLocations.add(teamStartingLocationIndex);

                DataVault.addTeam(teamName, teamCode, teamStartingLocationIndex);

                startActivity(new Intent(TeamTrackerTeamPopup.this, TeamProgressTracker.class));

            }

        }

    }

    @Override
    public void onBackPressed() {

    }

}
