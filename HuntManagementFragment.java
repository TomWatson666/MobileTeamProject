package team5project.treasurehuntapp;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import static team5project.treasurehuntapp.DataVault.dates;

/**
 * Created by joebr on 19/04/2017.
 */

public class HuntManagementFragment extends ListFragment {

    public static final int LOADING_ANIMATION_TIME = 1000;
    public static final int FADE_OUT_TIME = 350;

    public ArrayAdapter<String> connectArrayToListView;
    private List<String> treasureHunts;
    static String currentTreasureHunt;
    String contextMenuItemPressed = "";
    ImageView gemHuntLogo;
    ProgressBar huntManagementProgressBar;
    RelativeLayout huntManagementForm;

    //creates the list elements and registers them for the context menu
    public void onActivityCreated(@Nullable Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        treasureHunts = DataVault.treasureHunts;

        gemHuntLogo = (ImageView) getActivity().findViewById(R.id.hunt_management_gem_hunt_logo);
        huntManagementProgressBar = (ProgressBar) getActivity().findViewById(R.id.hunt_management_progress_bar);
        huntManagementForm = (RelativeLayout) getActivity().findViewById(R.id.management_layout);

        if(getActivity().getIntent().getStringExtra("Title") != null) {
            if(!treasureHunts.contains(getActivity().getIntent().getStringExtra("Title"))) {
                treasureHunts.add(getActivity().getIntent().getStringExtra("Title"));
            }
        }

        connectArrayToListView = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_activated_1,
                treasureHunts);

        setListAdapter(connectArrayToListView);

        registerForContextMenu(getListView());

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.management_menu, menu);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //when treasure hunt is long pressed
        AdapterView.AdapterContextMenuInfo hunts = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Object itemClicked = getListView().getItemAtPosition(hunts.position);
        String treasureHunt = itemClicked.toString();
        currentTreasureHunt = treasureHunt;

        switch(item.getItemId()) {
            //three buttons in menu, edit, delete and QR codes
            case R.id.edit_option:
                onEditOptionSelected(treasureHunt);
                return true;
            case R.id.delete_option:
                onDeleteOptionSelected(treasureHunt);
                return true;
            case R.id.qr_option:
                onQRCodeOptionSelected(treasureHunt);
                return true;
            default:
                return super.onContextItemSelected(item);

        }

    }
    //when edit is selected in the menu, link to EditHunt
    private void onEditOptionSelected(String treasureHunt) {

        currentTreasureHunt = treasureHunt;

        EditHunt.dateBefore = dates.get(DataVault.treasureHunts.indexOf(currentTreasureHunt));

        Intent info = new Intent(getActivity(), EditHunt.class);
        info.putExtra("Title", currentTreasureHunt);
        info.putExtra("Type", "Edit");
        startActivity(info);

    }

    //when delete is selected in the menu, delete treasure hunt after confirmation
    private void onDeleteOptionSelected(final String treasureHunt) {

        new AlertDialog.Builder(getContext())
                .setTitle("Delete Treasure Hunt")
                .setMessage("Are you sure you want to delete this Treasure Hunt?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dates.remove(treasureHunts.indexOf(treasureHunt));
                        treasureHunts.remove(treasureHunt);
                        connectArrayToListView.notifyDataSetChanged();

                        DataVault.deleteTreasureHunt(treasureHunt);

                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();


    }

    //when qr code is selected, go to qr code list for treasure hunt
    private void onQRCodeOptionSelected(String treasureHunt) {

        contextMenuItemPressed = "QR";
        currentTreasureHunt = treasureHunt;

        GenerateTreasureHunt generateTreasureHunt = new GenerateTreasureHunt();
        generateTreasureHunt.execute();

    }


    //when a list item is clicked, will take the user straight to the map to view the treasure hunt selected
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        contextMenuItemPressed = "None";

        currentTreasureHunt = treasureHunts.get(position);

        GenerateTreasureHunt gen = new GenerateTreasureHunt();
        gen.execute();

    }

    //A method to animate the progress bar
    private void loadProgress() {

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

                fadeOut();

            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        Resources res = getResources();
        Drawable progressBarDrawable = res.getDrawable(R.drawable.loading_circle);

        huntManagementProgressBar.setProgressDrawable(progressBarDrawable);

        huntManagementProgressBar.setMax(100);

        huntManagementProgressBar.setSecondaryProgress(100);

        ObjectAnimator progressTransition = ObjectAnimator.ofInt(huntManagementProgressBar, "progress",
                0, 100);
        progressTransition.setDuration(LOADING_ANIMATION_TIME);
        progressTransition.setInterpolator(new AccelerateDecelerateInterpolator());
        progressTransition.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                huntManagementProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

                huntManagementProgressBar.setProgress(100);

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        huntManagementForm.setVisibility(View.GONE);
        progressTransition.start();
        gemHuntLogo.startAnimation(fadeIn);

    }

    //A method to fade out the progress bar when it has finished loading
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
                huntManagementProgressBar.setVisibility(View.GONE);

                Intent info = new Intent(getActivity(), ManagementMap.class);
                info.putExtra("Type", "Edit");
                info.putExtra("Title", currentTreasureHunt);
                info.putExtra("Correct Previous Page", "True");

                startActivity(info);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        huntManagementProgressBar.startAnimation(fadeOut);
        gemHuntLogo.startAnimation(fadeOut);

    }

    //async task for generating the points for the treasure hunt that needs to be loaded
    public class GenerateTreasureHunt extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            String sqlQuery = "SELECT `Index`, Name, Latitude, Longitude, Clue FROM Location " +
                    "WHERE Treasure_Hunt_Title = '" + currentTreasureHunt + "' " +
                    "ORDER BY `Index` ASC;";
            List<String> result = DatabaseConnection.executeQuery(sqlQuery);

            List<MapLocation> newLocations = new ArrayList<MapLocation>();

            if (!result.get(0).equals("nothing returned")) {
                for (int i = 0; i < result.size(); i++) {

                    newLocations.add(new MapLocation(currentTreasureHunt,
                            DatabaseConnection.parseResultSet(result, i, 0),
                            DatabaseConnection.parseResultSet(result, i, 1),
                            DatabaseConnection.parseResultSet(result, i, 2),
                            DatabaseConnection.parseResultSet(result, i, 3),
                            DatabaseConnection.parseResultSet(result, i, 4)));

                }
            }

            DataVault.viewedTreasureHuntLocations = new ArrayList<MapLocation>(newLocations);
            return null;

        }

        @Override
        protected void onPreExecute() {

            if(!contextMenuItemPressed.equals("QR")) {

                loadProgress();

            }

        }

        @Override
        protected void onPostExecute(Void aVoid) {

            if(contextMenuItemPressed.equals("QR")) {

                Intent info = new Intent(getActivity(), QRCodeDisplay.class);
                info.putExtra("Title", currentTreasureHunt);

                startActivity(info);

            }

        }

    }

}
