package team5project.treasurehuntapp;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static team5project.treasurehuntapp.R.id.login;

public class LoginFragment extends Fragment {

    public static final int LOADING_ANIMATION_TIME = 3000;
    public static final int FADE_OUT_TIME = 500;

    private View loginView;
    private ProgressBar loginProgressBar;
    private ImageView gemHuntLogo;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private boolean isLoaded;
    private boolean loginError = true;
    public static boolean treasureHuntDetailsRetrieved = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.login_fragment, null);

        loginError = true;
        isLoaded = false;

        usernameEditText = (EditText) rootView.findViewById(R.id.login_username_text);

        passwordEditText = (EditText) rootView.findViewById(R.id.login_password_text);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == login || actionId == EditorInfo.IME_NULL) {
                    tryLogin();
                    return true;
                }
                return false;
            }
        });

        loginProgressBar = (ProgressBar) rootView.findViewById(R.id.login_progress_bar);
        gemHuntLogo = (ImageView) rootView.findViewById(R.id.gem_hunt_logo);
        loginButton = (Button) rootView.findViewById(R.id.login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryLogin();
            }
        });

        loginView = rootView.findViewById(R.id.login_form);

        return rootView;

    }

    private void showProgress() {

        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getActivity().getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(getActivity());
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

                if(!loginError)
                    fadeOut();
                else {
                    ((AppCompatActivity) getActivity()).getSupportActionBar().show();
                    gemHuntLogo.setVisibility(View.GONE);
                    loginProgressBar.setVisibility(View.GONE);
                    loginView.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        Resources res = getResources();
        Drawable progressBarDrawable = res.getDrawable(R.drawable.loading_circle);

        loginProgressBar.setProgressDrawable(progressBarDrawable);

        loginProgressBar.setMax(100);

        loginProgressBar.setSecondaryProgress(100);

        ObjectAnimator progressTransition = ObjectAnimator.ofInt(loginProgressBar, "progress",
                0, 100);
        progressTransition.setDuration(LOADING_ANIMATION_TIME);
        progressTransition.setInterpolator(new AccelerateDecelerateInterpolator());
        progressTransition.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                loginProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

                loginProgressBar.setProgress(100);

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        loginView.setVisibility(View.GONE);
        progressTransition.start();
        gemHuntLogo.startAnimation(fadeIn);

    }

    private void tryLogin() {

        //Reset errors to null
        usernameEditText.setError(null);
        passwordEditText.setError(null);

        //Set boolean to check if the user is eligible to login or not
        boolean valid = true;

        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        //Declare a view to set focus on either username or password if they are incorrect
        View viewFocusedOn = null;

        //Check to see that the user entered a username
        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Please enter a username");
            viewFocusedOn = usernameEditText;
            valid = false;
        }

        //Check to see that the user entered a password
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Please enter a password");
            viewFocusedOn = passwordEditText;
            valid = false;
        }

        if(!username.matches("^[a-zA-Z0-9 ]*$")) {
            usernameEditText.setError("Alphanumeric characters only");
            passwordEditText.setText("");
            viewFocusedOn = usernameEditText;
            valid = false;
        }

        if(!password.matches("^[a-zA-Z0-9 ]*$")) {
            passwordEditText.setError("Alphanumeric characters only");
            passwordEditText.setText("");
            viewFocusedOn = usernameEditText;
            valid = false;
        }

        if (!valid) {
            viewFocusedOn.requestFocus();
        } else {

            LoginTask loginTask = new LoginTask();
            loginTask.execute(username, password);

        }

    }

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
                loginProgressBar.setVisibility(View.GONE);

                Intent nextPage = null;

                if(DataVault.currentTeam.equals("Admin") && DataVault.treasureHuntTitle.equals("")) {
                    nextPage = new Intent(getActivity(), HuntManagement.class);
                }

                if(DataVault.currentTeam.equals("Admin") && !DataVault.treasureHuntTitle.equals("")) {
                    nextPage = new Intent(getActivity(), TeamProgressTracker.class);
                }

                if(!DataVault.currentTeam.equals("Admin") && DataVault.teamProgress.get(DataVault.teamNames.indexOf(DataVault.currentTeam))
                        .equals(DataVault.locationCount)) {
                    nextPage = new Intent(getActivity(), CongratulationsPage.class);
                }

                if(!DataVault.currentTeam.equals("Admin") && !DataVault.teamProgress.get(DataVault.teamNames.indexOf(DataVault.currentTeam))
                        .equals(DataVault.locationCount)) {
                    nextPage = new Intent(getActivity(), TeamProgressTracker.class);
                }

                startActivity(nextPage);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        loginProgressBar.startAnimation(fadeOut);
        gemHuntLogo.startAnimation(fadeOut);

    }

    //hash password algorithm
    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public class LoginTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected void onPreExecute() {

            DataVault.updateLooper = true;

            if(!DataVault.updateRunning) {
                DataVault.autoUpdateTeamInfo();
            }

            DataVault.updateTreasureHunts();
            DataVault.updateTreasureHuntInfo();

            DataVault.updateRunning = true;

            showProgress();

        }

        @Override
        protected String[] doInBackground(String... params) {

            params[1] = md5(params[1]);

            String sqlQuery = "SELECT Username, Password FROM User WHERE Username = '" + params[0] + "';";
            List<String> result;
            String[] toReturn = new String[3];

            result = DatabaseConnection.executeQuery(sqlQuery);

            if(!result.get(0).equals("nothing returned")) {

                if(params[1].equals(DatabaseConnection.parseResultSet(result, 0, 1))) {

                    sqlQuery = "SELECT Team_Name FROM User WHERE Username = '" + params[0] + "' AND Password = '" + params[1] + "';";

                    result = DatabaseConnection.executeQuery(sqlQuery);

                    String currentUserTeam = DatabaseConnection.parseResultSet(result, 0, 0);

                    while(!treasureHuntDetailsRetrieved);

                    if(DataVault.treasureHuntTitle.equals("") && !currentUserTeam.equals("Admin")) {
                        toReturn[0] = "No Treasure Hunts Active";
                        return toReturn;
                    }

                    toReturn[0] = "Correct Login";
                    toReturn[1] = params[0];
                    toReturn[2] = currentUserTeam;
                    return toReturn;

                }

            } else {

                toReturn[0] = "Incorrect Username";
                return toReturn;

            }

            toReturn[0] = "Incorrect Password";
            return toReturn;

        }

        @Override
        protected void onPostExecute(String[] loginStatus) {

            if(loginStatus[0].equals("Incorrect Username")) {
                Toast.makeText(getActivity(), "The username that you entered doesn't exist", Toast.LENGTH_SHORT).show();
                usernameEditText.setText("");
                passwordEditText.setText("");
                usernameEditText.requestFocus();
            } else if(loginStatus[0].equals("Incorrect Password")) {
                Toast.makeText(getActivity(), "The password that you entered is incorrect", Toast.LENGTH_SHORT).show();
                passwordEditText.setText("");
                passwordEditText.requestFocus();
            } else if(loginStatus[0].equals("No Treasure Hunts Active")) {
                Toast.makeText(getActivity(), "There are currently no treasure hunts active", Toast.LENGTH_SHORT).show();
                usernameEditText.setText("");
                passwordEditText.setText("");
                usernameEditText.requestFocus();
            } else {

                int teamIndex = -1;
                boolean badStartingPoint = false;

                for(int i = 0; i < DataVault.teamNames.size(); i++) {

                    if(loginStatus[2].equals(DataVault.teamNames.get(i))) {

                        teamIndex = i;
                        break;

                    }

                }

                if(!loginStatus[2].equals("Admin")) {

                    if(Integer.parseInt(DataVault.teamStartLocations.get(teamIndex)) > Integer.parseInt(DataVault.locationCount)) {

                        Toast.makeText(getActivity(), "Your starting location does not exist, refer to an admin", Toast.LENGTH_LONG).show();
                        badStartingPoint = true;

                    }

                }

                if(!badStartingPoint) {

                    usernameEditText.setText("");
                    passwordEditText.setText("");

                    DataVault.currentUser = loginStatus[1];
                    DataVault.currentTeam = loginStatus[2];

                    DataVault.getPreferences();
                    DataVault.setUserActive(loginStatus[1], loginStatus[2]);

                    loginError = false;

                }

            }

        }

    }

}
