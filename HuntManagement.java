package team5project.treasurehuntapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class HuntManagement extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunt_management);

        DataVault.updateTreasureHuntInfo();

        //logout button takes you to the login screen
        Button logoutButton = (Button) findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                DataVault.setUserInactive(DataVault.currentUser, DataVault.currentTeam);
                startActivity(new Intent(HuntManagement.this, LoginScreen.class));
                finish();
            }
        });

        //team tracker button link to team tracker page
        Button teamTrackerButton = (Button) findViewById(R.id.teamTrackerButton);
        teamTrackerButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                startActivity(new Intent(HuntManagement.this, TeamProgressTracker.class));
            }
        });

        //new treasure hunt button link to treasure hunt creation page
        Button newHuntButton = (Button) findViewById(R.id.newHuntButton);
        newHuntButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {

                Intent info = new Intent(HuntManagement.this, EditHunt.class);
                info.putExtra("Type", "New");

                startActivity(info);

            }
        });

    }

    @Override
    public void onBackPressed() {

    }

}
