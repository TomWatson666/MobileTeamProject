package team5project.treasurehuntapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class RegisterFragment extends Fragment {

    private View registerView;
    private View registerProgressBar;
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText phoneNumberEditText;
    private EditText emailAddressEditText;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private EditText teamNameEditText;
    private EditText codeEditText;
    private CheckedTextView emailCheckBox;
    private CheckedTextView phoneCheckBox;
    private Button registerButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.register_fragment, null);

        firstNameEditText = (EditText) rootView.findViewById(R.id.register_first_name_text);
        lastNameEditText = (EditText) rootView.findViewById(R.id.register_last_name_text);
        phoneNumberEditText = (EditText) rootView.findViewById(R.id.register_phone_number_text);
        emailAddressEditText = (EditText) rootView.findViewById(R.id.register_email_address_text);
        usernameEditText = (EditText) rootView.findViewById(R.id.register_username_text);
        passwordEditText = (EditText) rootView.findViewById(R.id.register_password_text);
        confirmPasswordEditText = (EditText) rootView.findViewById(R.id.register_confirm_password_text);
        teamNameEditText = (EditText) rootView.findViewById(R.id.register_team_name_text);
        codeEditText = (EditText) rootView.findViewById(R.id.register_code_text);
        emailCheckBox = (CheckedTextView) rootView.findViewById(R.id.register_email_check_box);
        phoneCheckBox = (CheckedTextView) rootView.findViewById(R.id.register_phone_check_box);

        //This allows you to check and uncheck the check boxes at the bottom of the registration form
        emailCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!emailCheckBox.isChecked()) {
                    emailCheckBox.setChecked(true);
                } else {
                    emailCheckBox.setChecked(false);
                }

            }
        });

        phoneCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!phoneCheckBox.isChecked()) {
                    phoneCheckBox.setChecked(true);
                } else {
                    phoneCheckBox.setChecked(false);
                }

            }
        });

        registerProgressBar = rootView.findViewById(R.id.register_progress_bar);
        registerButton = (Button) rootView.findViewById(R.id.register_button);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryRegister();
            }
        });

        registerView = rootView.findViewById(R.id.register_form);

        return rootView;

    }

    //This is a short animation progress bar that shows that your registration is being processed
    private void showProgress(final boolean show) {

        int longAnimTime = 2000;

        registerView.setVisibility(show ? View.GONE : View.VISIBLE);
        registerView.animate().setDuration(longAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                registerView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        registerProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        registerProgressBar.animate().setDuration(longAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                registerProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });

    }

    private void tryRegister() {

        firstNameEditText.setError(null);
        lastNameEditText.setError(null);
        usernameEditText.setError(null);
        passwordEditText.setError(null);
        confirmPasswordEditText.setError(null);
        teamNameEditText.setError(null);
        codeEditText.setError(null);

        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String phoneNumber = phoneNumberEditText.getText().toString();
        String emailAddress = emailAddressEditText.getText().toString();
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();
        String teamName = teamNameEditText.getText().toString();
        String code = codeEditText.getText().toString();
        String emailChecked = emailCheckBox.isChecked() ? "Yes" : "No";
        String phoneChecked = phoneCheckBox.isChecked() ? "Yes" : "No";

        //Boolean to check if input is valid
        boolean valid = true;
        View viewFocusedOn = null;

        //Check is the edit texts are empty
        if(TextUtils.isEmpty(code)) {
            codeEditText.setError("Please enter Code");
            viewFocusedOn = codeEditText;
            valid = false;
        }

        if(TextUtils.isEmpty(teamName)) {
            teamNameEditText.setError("Please enter Team Name");
            viewFocusedOn = teamNameEditText;
            valid = false;
        }

        if(TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordEditText.setError("Please enter Password");
            viewFocusedOn = confirmPasswordEditText;
            valid = false;
        }

        if(TextUtils.isEmpty(password)) {
            passwordEditText.setError("Please enter Password");
            viewFocusedOn = passwordEditText;
            valid = false;
        }

        if(TextUtils.isEmpty(username)) {
            usernameEditText.setError("Please enter Username");
            viewFocusedOn = usernameEditText;
            valid = false;
        }

        if(TextUtils.isEmpty(emailAddress)) {
            emailAddressEditText.setError("Please enter Email Address");
            viewFocusedOn = emailAddressEditText;
            valid = false;
        }

        if(TextUtils.isEmpty(phoneNumber)) {
            phoneNumberEditText.setError("Please enter Phone Number");
            viewFocusedOn = phoneNumberEditText;
            valid = false;
        }

        if(TextUtils.isEmpty(lastName)) {
            lastNameEditText.setError("Please enter Last Name");
            viewFocusedOn = lastNameEditText;
            valid = false;
        }

        if(TextUtils.isEmpty(firstName)) {
            firstNameEditText.setError("Please enter First Name");
            viewFocusedOn = firstNameEditText;
            valid = false;
        }

        //Check to see that all edit texts have the correct character formatting
        if(!code.matches("^[a-zA-Z0-9_ ]*$")) {
            codeEditText.setError("Alphanumeric characters only");
            codeEditText.setText("");
            viewFocusedOn = codeEditText;
            valid = false;
        }

        if(!teamName.matches("^[a-zA-Z0-9 ]*$")) {
            teamNameEditText.setError("Alphanumeric characters only");
            codeEditText.setText("");
            viewFocusedOn = teamNameEditText;
            valid = false;
        }

        if(!confirmPassword.matches("^[a-zA-Z0-9 ]*$")) {
            confirmPasswordEditText.setError("Alphanumeric characters only");
            confirmPasswordEditText.setText("");
            passwordEditText.setText("");
            viewFocusedOn = passwordEditText;
            valid = false;
        }

        if(!password.matches("^[a-zA-Z0-9 ]*$")) {
            passwordEditText.setError("Alphanumeric characters only");
            passwordEditText.setText("");
            confirmPasswordEditText.setText("");
            viewFocusedOn = passwordEditText;
            valid = false;
        }

        if(!username.matches("^[a-zA-Z0-9 ]*$")) {
            usernameEditText.setError("Alphanumeric characters only");
            passwordEditText.setText("");
            confirmPasswordEditText.setText("");
            viewFocusedOn = usernameEditText;
            valid = false;
        }

        if(!emailAddress.matches("^[a-zA-Z0-9@.]*$")) {
            emailAddressEditText.setError("Invalid Characters Entered");
            emailAddressEditText.setText("");
            viewFocusedOn = emailAddressEditText;
            valid = false;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {

            emailAddressEditText.setError("Invalid Characters Entered");
            emailAddressEditText.setText("");
            viewFocusedOn = emailAddressEditText;
            valid = false;

        }

        if(!lastName.matches("^[a-zA-Z0-9' ]*$")) {
            lastNameEditText.setError("Invalid characters used");

            viewFocusedOn = lastNameEditText;
            valid = false;
        }

        if(!firstName.matches("^[a-zA-Z0-9' ]*$")) {
            firstNameEditText.setError("Invalid characters used");

            viewFocusedOn = firstNameEditText;
            valid = false;
        }

        StringBuilder newName;
        String[] parts;

        //If there is an apostrophe, then it breaks the sql, these loops fix that, by doubling them up
        if((parts = lastName.split("'")).length > 1) {
            newName = new StringBuilder();
            for(int i = 0; i < parts.length; i++)
                newName.append(i == parts.length - 1 ? parts[i] : parts[i] + "''");

            lastName = newName.toString();
        }

        if((parts = firstName.split("'")).length > 1) {
            newName = new StringBuilder();
            for(int i = 0; i < parts.length; i++) {
                newName.append(i == parts.length - 1 ? parts[i] : parts[i] + "''");
            }

            firstName = newName.toString();
        }

        //This checks that the password and confirm password are the same
        if(!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("The passwords you entered aren't equal");
            passwordEditText.setError("The passwords you entered aren't equal");
            viewFocusedOn = passwordEditText;
            valid = false;
        }

        if (!valid) {
            viewFocusedOn.requestFocus();
        } else {

            RegistrationTask registrationTask = new RegistrationTask();
            registrationTask.execute(firstName, lastName, username, password, confirmPassword, teamName,
                    code, phoneNumber, emailAddress, phoneChecked, emailChecked);

        }

    }

    private void registrationSuccess() {

        //This resets all edit texts on successful registration
        firstNameEditText.setText("");
        lastNameEditText.setText("");
        phoneNumberEditText.setText("");
        emailAddressEditText.setText("");
        usernameEditText.setText("");
        passwordEditText.setText("");
        confirmPasswordEditText.setText("");
        teamNameEditText.setText("");
        codeEditText.setText("");

        LoginScreen.loginTab.select();

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

    //This is an async task to register a student
    public class RegistrationTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {

            showProgress(true);

        }

        @Override
        protected String doInBackground(String... params) {

            params[3] = md5(params[3]);
            params[4] = md5(params[4]);

            String sqlQuery = "SELECT Username FROM User WHERE Username = '" + params[2] + "';";
            List<String> result;
            String toReturn;

            result = DatabaseConnection.executeQuery(sqlQuery);

            if(!result.get(0).equals("nothing returned")) {
                toReturn = "Duplicate Username";
                return toReturn;
            }

            sqlQuery = "SELECT Team_Name, Code FROM Team WHERE Team_Name = '" + params[5] + "';";

            result = DatabaseConnection.executeQuery(sqlQuery);

            if(!result.get(0).equals("nothing returned")) {

                String dbCode = DatabaseConnection.parseResultSet(result, 0, 1);

                if(!params[6].equals(dbCode)) {
                    toReturn = "Incorrect Code";
                    return toReturn;
                } else {

                    sqlQuery = "INSERT INTO User (First_Name, Last_Name, Username, Password, " +
                            "Team_Name, Status, Longitude, Latitude, Phone_Number, Email_Address, " +
                            "Phone_Alert, Email_Alert) VALUES ('" + params[0] + "', '" + params[1] + "', '" +
                            params[2] + "', '" + params[3] + "', '" + params[5] + "', 'Inactive', " +
                            "'0', '0', '"  + params[7] + "', '" + params[8] + "', '" + params[9] + "', '" + params[10] + "');";
                    DatabaseConnection.executeQuery(sqlQuery);
                    toReturn = "Registration Successful";
                    return toReturn;

                }

            }

            toReturn = "Incorrect Team Name";
            return toReturn;

        }

        @Override
        protected void onPostExecute(String loginStatus) {

            showProgress(false);

            if(loginStatus.equals("Duplicate Username")) {
                Toast.makeText(getActivity(), "The username that you entered already exists", Toast.LENGTH_SHORT).show();
                usernameEditText.setText("");
                passwordEditText.setText("");
                confirmPasswordEditText.setText("");
                usernameEditText.requestFocus();
            } else if(loginStatus.equals("Incorrect Team Name")) {
                Toast.makeText(getActivity(), "The team name that you entered doesn't exist", Toast.LENGTH_SHORT).show();
                teamNameEditText.setText("");
                codeEditText.setText("");
                teamNameEditText.requestFocus();
            } else if(loginStatus.equals("Incorrect Code")) {
                Toast.makeText(getActivity(), "The code that you entered is incorrect", Toast.LENGTH_SHORT).show();
                codeEditText.setText("");
                codeEditText.requestFocus();
            } else {
                registrationSuccess();
            }

        }

    }

}
