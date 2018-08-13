package team5project.treasurehuntapp;

import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;

/**
 * Created by tomwa on 24/02/2017.
 */

public class TeamProgressTracker extends AppCompatActivity {

    private static boolean firstCreate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //This inflates the corresponding progress for the selected team
        TeamTrackerProgressFragment progressFragment = new TeamTrackerProgressFragment();

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if(!firstCreate) {
            ft.add(R.id.student_progress_tracker_container, progressFragment);
            firstCreate = true;
        } else
            ft.replace(R.id.student_progress_tracker_container, progressFragment);

        ft.commit();

        setContentView(R.layout.student_progress_tracker);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if(!DataVault.currentTeam.equals("Admin")) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.team_tracker_options_menu, menu);
        }

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //This brings up a student's preferences
        AlertDialog.Builder mBuilder = new AlertDialog.Builder((TeamProgressTracker.this));
        View mView = getLayoutInflater().inflate(R.layout.preferences, null);
        final Switch mEmailSwitch = (Switch) mView.findViewById(R.id.emailSwitch);
        final Switch mPhoneSwitch = (Switch) mView.findViewById(R.id.phoneSwitch);

        boolean phoneAlert = DataVault.phoneAlerts.get(DataVault.usernames.indexOf(DataVault.currentUser)).equals("Yes");
        boolean emailAlert = DataVault.emailAlerts.get(DataVault.usernames.indexOf(DataVault.currentUser)).equals("Yes");

        mEmailSwitch.setChecked(emailAlert);
        mPhoneSwitch.setChecked(phoneAlert);

        //make switches appear
        mBuilder.setView(mView);

        //set icon and title for alert dialog
        mBuilder.setIcon(android.R.drawable.ic_menu_info_details); //checkbox icon on dialogue box
        mBuilder.setTitle("Preferences");

        //dismiss alert dialog if user presses ok
        mBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                DataVault.updatePreferences(mPhoneSwitch.isChecked() ? "Yes" : "No", mEmailSwitch.isChecked() ? "Yes" : "No");

                DataVault.phoneAlerts.set(DataVault.usernames.indexOf(DataVault.currentUser), mPhoneSwitch.isChecked() ? "Yes" : "No");
                DataVault.emailAlerts.set(DataVault.usernames.indexOf(DataVault.currentUser), mEmailSwitch.isChecked() ? "Yes" : "No");

                dialogInterface.dismiss();

            }
        });

        AlertDialog alertDialog = mBuilder.create(); //create alert dialog
        alertDialog.show(); //show alert dialog

        return true;

    }

    @Override
    public void onBackPressed() {

    }

}
