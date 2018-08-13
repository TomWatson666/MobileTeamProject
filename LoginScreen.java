package team5project.treasurehuntapp;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;

@SuppressWarnings("deprecation")
public class LoginScreen extends AppCompatActivity {

    public static ActionBar.Tab loginTab, registerTab;
    public ActionBar actionBar;
    private Fragment loginFragment;
    private Fragment registerFragment;
    private static boolean permissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        loginFragment = new LoginFragment();
        registerFragment = new RegisterFragment();

        actionBar = getSupportActionBar();

        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        LayoutInflater inflater = getLayoutInflater();

        View newActionBar = inflater.inflate(R.layout.login_action_bar, null);

        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1B476A")));

        actionBar.setCustomView(newActionBar, new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        loginTab = actionBar.newTab().setText("Login");
        registerTab = actionBar.newTab().setText("Register");

        loginTab.setTabListener(new TabListener(loginFragment));
        registerTab.setTabListener(new TabListener(registerFragment));

        actionBar.addTab(loginTab);
        actionBar.addTab(registerTab);

        if(!permissionGranted) {
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(LoginScreen.this);
            mBuilder.setIcon(android.R.drawable.checkbox_on_background); //checkbox icon on dialogue box
            mBuilder.setTitle("Tracking Permission");
            mBuilder.setMessage(getResources().getString(R.string.tracking_permission_text));
            //Proceed if user accepts
            mBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    permissionGranted = true;
                    dialogInterface.dismiss();
                }
            });
            //Close app if they decline
            mBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });

            AlertDialog alertDialog = mBuilder.create();
            alertDialog.show();
        }

    }

    @Override
    public void onBackPressed() {

    }

}
