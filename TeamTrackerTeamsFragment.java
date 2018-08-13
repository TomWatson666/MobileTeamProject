package team5project.treasurehuntapp;

import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tomwa on 11/03/2017.
 */

public class TeamTrackerTeamsFragment extends ListFragment {

    private static final int BARCODE_READER_REQUEST_CODE = 1;

    private int currentItemSelected = 0;
    public ArrayAdapter<String> connectArrayToListView;
    private List<String> teamNames;
    private int index = 0;
    public List<String> teams;

    LinearLayout buttonBar;

    //Buttons to create for admin/student part of team tracker
    Button addTeamButton, selectButton, adminMapButton, managementButton, deleteButton, resetProgressButton;
    Button qrCodeButton, studentMapButton, logoutButton;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        teamNames = DataVault.teamNames;

        connectArrayToListView = new
                ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_activated_1,
                teamNames);

        setListAdapter(connectArrayToListView);

        if(DataVault.currentTeam.equals("Admin"))
            registerForContextMenu(getListView());

        if(savedInstanceState != null) {
            currentItemSelected = savedInstanceState.getInt("curChoice", 0);
        }

        //Dynamically create all buttons with layouts on the screen, so that they can change depending on student/admin login
        final FrameLayout list = (FrameLayout) getActivity().findViewById(R.id.team_tracker_teams_container);

        buttonBar = (LinearLayout) getActivity().findViewById(R.id.team_tracker_button_panel);

        if(DataVault.currentTeam.equals("Admin")) {

            final LinearLayout topPart = new LinearLayout(getActivity());
            topPart.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
            topPart.setOrientation(LinearLayout.HORIZONTAL);

            final LinearLayout bottomPart = new LinearLayout(getActivity());
            bottomPart.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
            bottomPart.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER_VERTICAL;

            int margin = getResources().getDimensionPixelSize(R.dimen.button_margin);
            params.setMargins(margin, margin, margin, margin);

            buttonBar.setLayoutParams(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ?
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 4) :
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 2));

            buttonBar.setOrientation(LinearLayout.VERTICAL);

            addTeamButton = buttonFactory("Add Team", "Red");
            addTeamButton.setLayoutParams(params);

            selectButton = buttonFactory("Select", "Red");
            adminMapButton = buttonFactory("Map", "Red");
            managementButton = buttonFactory("Management", "Red");
            logoutButton = buttonFactory("Logout", "Pale Red");
            deleteButton = buttonFactory("Delete", "Red");
            resetProgressButton = buttonFactory("Reset Progress", "Red");

            addTeamButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent info = new Intent(getActivity(), TeamTrackerTeamPopup.class);
                    info.putExtra("Function", "Add");

                    startActivity(info);

                }
            });

            selectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //This changes the buttons, and changes the select mode of the list view
                    if(getListView().getChoiceMode() != AbsListView.CHOICE_MODE_MULTIPLE){

                        getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

                        selectButton.setBackground(getResources().getDrawable(R.drawable.button_blue));
                        bottomPart.removeView(adminMapButton);
                        bottomPart.removeView(managementButton);
                        bottomPart.removeView(logoutButton);
                        bottomPart.addView(deleteButton);
                        bottomPart.addView(resetProgressButton);

                        buttonBar.removeView(topPart);
                        buttonBar.setLayoutParams(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ?
                                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 2) :
                                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
                        list.setLayoutParams(new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 0, 10));

                    } else {

                        //Change so user cannot select any list items
                        getListView().setChoiceMode(AbsListView.CHOICE_MODE_NONE);

                        selectButton.setBackground(getResources().getDrawable(R.drawable.button_red));
                        bottomPart.removeView(deleteButton);
                        bottomPart.removeView(resetProgressButton);
                        bottomPart.addView(adminMapButton);
                        bottomPart.addView(managementButton);
                        bottomPart.addView(logoutButton);

                        buttonBar.setLayoutParams(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ?
                                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 4) :
                                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 2));
                        list.setLayoutParams(new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 0, 8));

                        //To make sure that topPart is above bottomPart
                        buttonBar.removeView(bottomPart);
                        buttonBar.addView(topPart);
                        buttonBar.addView(bottomPart);

                        setListAdapter(connectArrayToListView);
                        connectArrayToListView.notifyDataSetChanged();

                    }

                }
            });

            adminMapButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    startActivity(new Intent(getActivity(), AdminMap.class));
                    getActivity().finish();

                }
            });

            managementButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(getActivity(), HuntManagement.class);
                    startActivity(intent);

                }
            });

            logoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    DataVault.setUserInactive(DataVault.currentUser, DataVault.currentTeam);
                    Intent intent = new Intent(getActivity(), LoginScreen.class);

                    startActivity(intent);

                }
            });

            //This deletes all selected teams
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    teams = new ArrayList<String>();

                    for(int i = getListView().getCount() - 1; i >= 0 ; i--) {

                        if(getListView().isItemChecked(i)) {

                            teams.add(getListView().getItemAtPosition(i).toString());

                        }

                    }

                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
                    mBuilder.setIcon(android.R.drawable.checkbox_on_background); //checkbox icon on dialogue box
                    mBuilder.setTitle("Delete Teams");
                    mBuilder.setMessage("Are you sure?");
                    //if the user selects yes
                    mBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            for(int j = 0; j < teams.size(); j++) {

                                String teamName = teams.get(j);

                                int index;

                                for (index = 0; index < teamNames.size(); index++) {

                                    if (teamName.equals(teamNames.get(index))) {

                                        break;

                                    }

                                }

                                DataVault.teamNames.remove(index);
                                DataVault.teamProgress.remove(index);
                                DataVault.teamLatitudes.remove(index);
                                DataVault.teamLongitudes.remove(index);
                                DataVault.teamCodes.remove(index);
                                DataVault.teamStartLocations.remove(index);

                                DataVault.teamNames.remove(teamName);
                                connectArrayToListView.notifyDataSetChanged();
                                setListAdapter(connectArrayToListView);

                                DataVault.deleteTeam(teamName);

                            }


                        }
                    });
                    //dismiss dialog box if user cancels
                    mBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    AlertDialog alertDialog = mBuilder.create();
                    alertDialog.show();

                }
            });

            //This resets the progress of all selected teams
            resetProgressButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    teams = new ArrayList<String>();

                    for(int i = getListView().getCount() - 1; i >= 0 ; i--) {

                        if(getListView().isItemChecked(i)) {

                            teams.add(getListView().getItemAtPosition(i).toString());

                        }

                    }

                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
                    mBuilder.setIcon(android.R.drawable.checkbox_on_background); //checkbox icon on dialogue box
                    mBuilder.setTitle("Reset Team Progress");
                    mBuilder.setMessage("Are you sure?");
                    //if the user selects yes
                    mBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int p) {

                            for(int j = 0; j < teams.size(); j++) {

                                String teamName = teams.get(j);

                                int index;

                                for (index = 0; index < teamNames.size(); index++) {

                                    if (teamName.equals(teamNames.get(index))) {

                                        break;

                                    }

                                }

                                System.out.println("Index is apparently: " + index);
                                System.out.println("Team Name is apparently: " + teamName);

                                DataVault.teamProgress.set(index, "0");

                                DataVault.resetTeamProgress(teamName);

                            }

                            TextView name = (TextView) getActivity().findViewById(R.id.team_name_text_view);
                            int teamIndex = DataVault.teamNames.indexOf(name.getText().toString());

                            showTeamProgress(teamIndex);

                            Toast.makeText(getActivity(), "Team Progress Reset", Toast.LENGTH_LONG).show();

                        }
                    });
                    //dismiss dialog box if user cancels
                    mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    AlertDialog alertDialog = mBuilder.create();
                    alertDialog.show();

                }
            });

            topPart.addView(addTeamButton);

            bottomPart.addView(selectButton);
            bottomPart.addView(adminMapButton);
            bottomPart.addView(managementButton);
            bottomPart.addView(logoutButton);

            buttonBar.addView(topPart);
            buttonBar.addView(bottomPart);

        } else {

            buttonBar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 2));
            list.setLayoutParams(new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 0, 10));

            qrCodeButton = buttonFactory("QR Scanner", "Red");
            studentMapButton = buttonFactory("Map", "Red");
            logoutButton = buttonFactory("Logout", "Pale Red");

            qrCodeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(getActivity(), BarcodeCaptureActivity.class);
                    startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);

                }
            });

            studentMapButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent info = new Intent(getActivity(), StudentMap.class);
                    info.putExtra("Correct Previous Page", "True");

                    startActivity(info);
                    getActivity().finish();

                }
            });

            logoutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(getActivity(), LoginScreen.class);

                    if(!DataVault.currentTeam.equals("Admin"))
                        DataVault.setUserInactive(DataVault.currentUser, DataVault.currentTeam);

                    startActivity(intent);
                    getActivity().finish();

                }
            });

            buttonBar.addView(qrCodeButton);
            buttonBar.addView(studentMapButton);
            buttonBar.addView(logoutButton);

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        //This checks the validity of the qr qode scanned
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    //barcode.displayValue

                    index = DataVault.teamNames.indexOf(DataVault.currentTeam);

                    //This calculates the index of the next point
                    int nextPoint = (Integer.parseInt(DataVault.teamProgress.get(index)) +
                            Integer.parseInt(DataVault.teamStartLocations.get(index))) >
                            Integer.parseInt(DataVault.locationCount) ?

                            Integer.parseInt(DataVault.teamProgress.get(index)) +
                                    Integer.parseInt(DataVault.teamStartLocations.get(index))
                                    - Integer.parseInt(DataVault.locationCount) :

                            Integer.parseInt(DataVault.teamProgress.get(index)) +
                                    Integer.parseInt(DataVault.teamStartLocations.get(index));

                    if(DataVault.locations.get(nextPoint - 1).getQrCode().equals(barcode.displayValue)) {

                        Toast.makeText(getActivity(),
                                "Correct Location Reached!\nProceed to the next location!",
                                Toast.LENGTH_LONG).show();

                        ProgressUpdater progressUpdater = new ProgressUpdater();
                        progressUpdater.execute(index);

                    } else {
                        Toast.makeText(getActivity(), "Wrong Location Scanned", Toast.LENGTH_LONG).show();
                    }

                } else Toast.makeText(getActivity(), "Error Scanning QR Code, Try Again", Toast.LENGTH_LONG).show(); //barcode has not yet been captured
            } else Toast.makeText(getActivity(), "Error Scanning QR Code, Try Again", Toast.LENGTH_LONG).show();
        } else super.onActivityResult(requestCode, resultCode, data);

    }

    //This creates buttons for the page
    public Button buttonFactory(String name, String color) {

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, getResources().getDimensionPixelSize(R.dimen.button_height), 1);
        params.gravity = Gravity.CENTER_VERTICAL;

        int margin = getResources().getDimensionPixelSize(R.dimen.button_margin);
        params.setMargins(margin, margin, margin, margin);

        Button button = new Button(getActivity());

        button.setLayoutParams(params);
        button.setText(name);
        button.setTextColor(Color.WHITE);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);

        if(color.equals("Pale Red"))
            button.setBackground(getResources().getDrawable(R.drawable.button_pale_red));
        else if(color.equals("Red"))
            button.setBackground(getResources().getDrawable(R.drawable.button_red));
        else
            button.setBackground(getResources().getDrawable(R.drawable.button_blue));

        return button;

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.team_tracker_context_menu, menu);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo teams = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Object itemClicked = getListView().getItemAtPosition(teams.position);
        String teamName = itemClicked.toString();

        switch(item.getItemId()) {

            case R.id.team_tracker_edit_option:
                onEditOptionSelected(teamName);
                return true;
            case R.id.team_tracker_delete_option:
                onDeleteOptionSelected(teamName);
                return true;
            default:
                return super.onContextItemSelected(item);

        }

    }

    private void onEditOptionSelected(String teamName) {

        Intent info = new Intent(getActivity(), TeamTrackerTeamPopup.class);
        info.putExtra("Team", teamName);
        info.putExtra("Function", "Edit");
        startActivity(info);

    }

    private void onDeleteOptionSelected(final String teamName) {

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        mBuilder.setIcon(android.R.drawable.checkbox_on_background); //checkbox icon on dialogue box
        mBuilder.setTitle("Delete Selected Teams");
        mBuilder.setMessage("Are you sure?");
        //if the user selects yes
        mBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                int index;

                for(index = 0; index < teamNames.size(); index++) {

                    if(teamName.equals(teamNames.get(index))) {

                        break;

                    }

                }

                DataVault.teamNames.remove(index);
                DataVault.teamProgress.remove(index);
                DataVault.teamLatitudes.remove(index);
                DataVault.teamLongitudes.remove(index);
                DataVault.teamCodes.remove(index);
                DataVault.teamStartLocations.remove(index);

                teamNames.remove(teamName);
                connectArrayToListView.notifyDataSetChanged();

                DataVault.deleteTeam(teamName);

            }
        });
        //dismiss dialog box if user cancels
        mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = mBuilder.create();
        alertDialog.show();




    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("curChoice", currentItemSelected);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        if(getListView().getChoiceMode() == AbsListView.CHOICE_MODE_NONE)
            showTeamProgress(position);

    }

    void showTeamProgress(int index) {

        //This is to inflate a new progress fragment per click of a list item, to keep it accurate
        currentItemSelected = index;

        getListView().setItemChecked(index, true);

        String teamName = getListView().getItemAtPosition(index).toString();

        TeamTrackerProgressFragment progressFragment = (TeamTrackerProgressFragment)
                getFragmentManager().findFragmentById(R.id.team_tracker_progress_fragment);

        if(progressFragment == null || progressFragment.getShownIndex() != index) {

            progressFragment = TeamTrackerProgressFragment.newInstance(index, teamName);

            FragmentTransaction ft =
                    getFragmentManager().beginTransaction();

            ft.replace(R.id.student_progress_tracker_container, progressFragment);

            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();

        }

    }

    //This updates the progress of the team
    public class ProgressUpdater extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... ints) {

            String sqlQuery = "UPDATE Team SET Progress = '" + (Integer.parseInt(DataVault.teamProgress.get(ints[0])) + 1) +
                    "' WHERE Team_Name = '" + DataVault.currentTeam + "';";
            DatabaseConnection.executeQuery(sqlQuery);

            DataVault.teamProgress.set(ints[0], String.valueOf(Integer.parseInt(DataVault.teamProgress.get(ints[0])) + 1));

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            TextView teamName = (TextView) getActivity().findViewById(R.id.team_name_text_view);
            int teamIndex = DataVault.teamNames.indexOf(teamName.getText().toString());

            showTeamProgress(teamIndex);

            if(DataVault.teamProgress.get(index).equals(DataVault.locationCount)) {

                startActivity(new Intent(getActivity(), CongratulationsPage.class));

            }

        }

    }

}
