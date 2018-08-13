package team5project.treasurehuntapp;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import static team5project.treasurehuntapp.DataVault.locations;
import static team5project.treasurehuntapp.DataVault.viewedTreasureHuntLocations;

public class ManagementMap extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMarkerClickListener,
        PlaceSelectionListener,
        LocationListener {

    public static final int LOADING_ANIMATION_TIME = 50;
    public static final int FADE_OUT_TIME = 500;

    ImageView gemHuntLogo;
    ProgressBar managementMapProgressBar;
    RelativeLayout managementMapForm;
    TextView progressUpdateText;
    int numTasks = 0;
    int progress = 0;
    Marker currentMarker;

    static String type = "";
    static String viewedTreasureHuntTitle = "";
    static String date = "";
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    private GoogleMap mMap;
    String index = "";

    //temporary holders for co-ordinates to carry over to addLocation and editLocation
    public static double placeLatitude;
    public static double placeLongitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.management_map);

        checkLocationPermission();

        gemHuntLogo = (ImageView) findViewById(R.id.management_map_gem_hunt_logo);
        managementMapProgressBar = (ProgressBar) findViewById(R.id.management_map_progress_bar);
        managementMapForm = (RelativeLayout) findViewById(R.id.management_map_form);
        progressUpdateText = (TextView) findViewById(R.id.management_map_update_text);

        for(int i = 0; i < 20; i++)
            System.out.println(getIntent().getStringExtra("Correct Previous Page"));

        if(getIntent().getStringExtra("Correct Previous Page") != null) {
            type = getIntent().getStringExtra("Type");
            viewedTreasureHuntTitle = getIntent().getStringExtra("Title");
            if(type.equals("Edit"))
                date = DataVault.dates.get(DataVault.treasureHunts.indexOf(viewedTreasureHuntTitle));
            else
                date = getIntent().getStringExtra("Date");
        }

        // Retrieve the PlaceAutocompleteFragment. - The Search Bar
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.management_map);
        mapFragment.getMapAsync(this);
        autocompleteFragment.setOnPlaceSelectedListener(this);

    }



    /** Finish button :
     *  On pressing the finish button, a dialog box appears asking the user if they are sure.
     *  If they select yes, all the locations in the locationsToAdd list are added to the database
     *  and then the user is returned to the admin main page
     * **/
    public void OnFinish(View view){

        if(locations.size() != 0)
            if(viewedTreasureHuntTitle.equals(DataVault.locations.get(0).getTreasureHuntTitle()))
                DataVault.updateTreasureHuntInfo();

        startActivity(new Intent(ManagementMap.this, HuntManagement.class));

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        for(int i = 0; i < 100; i++) System.out.println("I'm ready!");
        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        //place markers from previously destroyed instance
        for (MapLocation mapLocation : DataVault.viewedTreasureHuntLocations) {
            mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.gem_icon)))
                    .position(new LatLng(Double.parseDouble(mapLocation.getLatitude()),Double.parseDouble(mapLocation.getLongitude())))
                    .title(mapLocation.getName())
                    .snippet("Click to edit location details"));
        }

        /*
        When the user presses on the marker info window, they get a dialog box asking if they want edit the location or delete it

        If they select delete, the location is removed from the locationsToAdd list and the marker is removed from the map

        If they select delete, the editLocation popup window opens and they can edit the details of that location
         */
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(final Marker marker) {
                placeLatitude=marker.getPosition().latitude; //temporary latitude holder holds locations of marker that had its infowindow clicked
                placeLongitude=marker.getPosition().longitude; //temporary longitude holder holds locations of marker that had its infowindow clicked

                // Dialog Box that asks user if they wish to delete the marker or edit the location details
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(ManagementMap.this);
                mBuilder.setIcon(android.R.drawable.checkbox_on_background); //checkbox icon on dialogue box
                mBuilder.setTitle("Edit Location");
                mBuilder.setMessage("Do you wish to edit or delete the location");
                mBuilder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(ManagementMap.this, EditLocation.class);
                        startActivity(intent);
                    }
                });

                //if the user presses "Delete"
                mBuilder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //remove element from markerLatLng list so that it isn't placed the next time the activity is recreated
                        String index = DataVault.retrieveMarkerLocation(String.valueOf(marker.getPosition().latitude),
                                String.valueOf(marker.getPosition().longitude)).getIndex();


                        marker.remove(); //remove the marker from this activity

                        DeleteMarker deleteMarker = new DeleteMarker();
                        deleteMarker.execute();

                    }
                });
                mBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });


                AlertDialog alertDialog = mBuilder.create();
                alertDialog.show();
            }
        });

        //Will place a new marker and will open the AddLocation class popup window where the details are to be filled in
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                placeLatitude=latLng.latitude; //temporary latitude holder holds locations of marker that had its infowindow clicked
                placeLongitude=latLng.longitude; //temporary longitude holder holds locations of marker that had its infowindow clicked
                Intent intent = new Intent(ManagementMap.this, AddLocation.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onLocationChanged(Location location) {
        //Move camera to your location
        LatLng currLatLng = new LatLng(location.getLatitude(), location.getLongitude()); //current location
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currLatLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    //open the AddLocation activity when the info window is clicked
    @Override
    public void onInfoWindowClick(Marker marker) {
        startActivity(new Intent(ManagementMap.this, AddLocation.class));
    }

    /** PlaceSelectionListener Methods:  **\
     *   These methods let the user search for a location in the search bar and then the camera
     *   moves to that location and zooms in.
     * */
    // When the user selects a location from the autocomplete drop down menu
    @Override
    public void onPlaceSelected(Place place) {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
    }
    //Callback invoked when place selection search encounters an error.
    @Override
    public void onError(Status status) {
        Log.e("ManagementMap", "onError: Status = " + status.toString());

        Toast.makeText(this, "Place selection failed: " + status.getStatusMessage(),
                Toast.LENGTH_SHORT).show();
    }


    //builder method used to initialise Google Play Services
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }
    @Override
    public void onConnectionSuspended(int i) {
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    /**********************/
    /**Permission methods**/
    /**********************/

    //check for permission
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    //get permission
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission was granted.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            //You can add here other case statements according to your requirement.
        }
    }

    /**
     * All following code is for loading between Management Map and Hunt Management Page
     */

    private void loadProgress(String task) {

        progressUpdateText.setText(task);

        int multiplier = 100 / numTasks;

        managementMapProgressBar.setMax(numTasks * multiplier);
        managementMapProgressBar.setSecondaryProgress(numTasks * multiplier);

        int progressBefore = (progress - 1) * multiplier;
        int progressAfter = progress * multiplier;

        //To avoid dividing by 0 exception
        float alphaBefore = progress == 1 ? 0 : (float) (progress - 1) / numTasks;
        float alphaAfter = (float) progress / numTasks;

        Animation fadeIn = new AlphaAnimation(alphaBefore, alphaAfter);
        fadeIn.setDuration(LOADING_ANIMATION_TIME);
        fadeIn.setInterpolator(new LinearInterpolator());

        ObjectAnimator progressTransition = ObjectAnimator.ofInt(managementMapProgressBar, "progress",
                progressBefore, progressAfter);
        progressTransition.setDuration(LOADING_ANIMATION_TIME);
        progressTransition.setInterpolator(new LinearInterpolator());

        progressTransition.start();
        gemHuntLogo.startAnimation(fadeIn);

    }

    private void fadeOut() {

        progressUpdateText.setText("Done");

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
                managementMapProgressBar.setVisibility(View.GONE);

                Intent nextPage = new Intent(ManagementMap.this, HuntManagement.class);
                startActivity(nextPage);
                finish();

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        progressUpdateText.startAnimation(fadeOut);
        managementMapProgressBar.startAnimation(fadeOut);
        gemHuntLogo.startAnimation(fadeOut);

    }

    private Bitmap getMarkerBitmapFromView(@DrawableRes int resId) {

        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker, null);
        ImageView markerImageView = (ImageView) customMarkerView.findViewById(R.id.location_icon);
        markerImageView.setImageResource(resId);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }

    private Bitmap getMarkerCollectedBitmapFromView(@DrawableRes int resId) {

        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_collected, null);
        ImageView markerImageView = (ImageView) customMarkerView.findViewById(R.id.location_icon);
        markerImageView.setImageResource(resId);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        currentMarker = marker;
        return false;
    }


    public class DeleteMarker extends AsyncTask<Void, String, Void> {

        @Override
        protected void onPreExecute() {

            managementMapForm.setVisibility(View.GONE);
            gemHuntLogo.setVisibility(View.VISIBLE);
            managementMapProgressBar.setVisibility(View.VISIBLE);
            progressUpdateText.setVisibility(View.VISIBLE);
            managementMapProgressBar.setProgress(0);
            progress = 0;

        }

        @Override
        protected Void doInBackground(Void... voids) {

            for(MapLocation mapLocation : DataVault.viewedTreasureHuntLocations) {
                if(mapLocation.getLatitude().equals(Double.toString(placeLatitude)) &&
                        mapLocation.getLongitude().equals(Double.toString(placeLongitude))) {
                    index = mapLocation.getIndex();
                }
            }

            numTasks = DataVault.viewedTreasureHuntLocations.size() - Integer.parseInt(index) + 2;

            String sqlQuery = "DELETE FROM Location Where Treasure_Hunt_Title = '" + viewedTreasureHuntTitle + "' AND `Index` = '" +
                    index + "';";
            DatabaseConnection.executeQuery(sqlQuery);
            publishProgress("Deleting Location");

            for(int i = Integer.parseInt(index) + 1; i < DataVault.viewedTreasureHuntLocations.size() + 1; i++) {

                sqlQuery = "UPDATE Location SET `Index` = '" + (i - 1) + "', QR_Code = '" +
                        viewedTreasureHuntTitle + "|" +
                        (i - 1) +
                        "' WHERE Treasure_Hunt_Title = '" + viewedTreasureHuntTitle
                        + "' AND `Index` = '" + i + "';";
                DatabaseConnection.executeQuery(sqlQuery);

                publishProgress("Changing locations");

            }

            sqlQuery = "UPDATE `Treasure Hunt` SET Location_Count = '" + (DataVault.viewedTreasureHuntLocations.size() - 1) +
                    "' WHERE Treasure_Hunt_Title = '" + viewedTreasureHuntTitle + "';";
            DatabaseConnection.executeQuery(sqlQuery);

            publishProgress("Updating Treasure Hunt");

            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {

            int position = -1;

            for(int i = 0; i < DataVault.viewedTreasureHuntLocations.size(); i++) {

                if(DataVault.viewedTreasureHuntLocations.get(i).getIndex().equals(index)) {
                    position = i;
                    break;
                }

            }

            DataVault.viewedTreasureHuntLocations.remove(position);

            managementMapForm.setVisibility(View.VISIBLE);
            gemHuntLogo.setVisibility(View.GONE);
            managementMapProgressBar.setVisibility(View.GONE);
            progressUpdateText.setVisibility(View.GONE);

        }

        @Override
        protected void onProgressUpdate(String... values) {

            progress++;
            loadProgress(values[0]);

        }
    }

    @Override
    public void onBackPressed() {

    }

}

