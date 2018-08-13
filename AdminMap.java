package team5project.treasurehuntapp;

/**
 * Created by rzarathore on 19/04/2017.
 */

import android.Manifest;
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
import android.widget.Button;
import android.widget.ImageView;
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

public class AdminMap extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        PlaceSelectionListener,
        GoogleMap.OnMarkerClickListener,
        LocationListener{

    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    private GoogleMap mMap;

    Bitmap studentLocationBitmap;

    //list of locations to add to the map. Used to test out the class. Will later be fed in
    List<MapLocation> locationsList = new ArrayList<>();
    List<LatLng> teamLatLngs = new ArrayList<>();

    //this is a list of markers that are locations
    List<Marker> locationsInHunt = new ArrayList<>();
    List<Marker> markers = new ArrayList<>();

    //treasure hunt title used for testing
    String viewedTreasureHuntTitle = "My Treasure Hunt";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_map);

        Button teamTrackerButton = (Button) findViewById(R.id.admin_map_team_tracker_button);
        Button logoutButton = (Button) findViewById(R.id.admin_map_logout_button);

        teamTrackerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminMap.this, TeamProgressTracker.class));
                finish();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DataVault.setUserInactive(DataVault.currentUser, DataVault.currentTeam);

                startActivity(new Intent(AdminMap.this, LoginScreen.class));
                finish();
            }
        });

        // Retrieve the PlaceAutocompleteFragment. - The Search Bar
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        autocompleteFragment.setOnPlaceSelectedListener(this);

        /**************************************************/
        /**********  TEST LOCATIONS AND TEAMS *************/
        /**************************************************/
        locationsList.add(new MapLocation(viewedTreasureHuntTitle, "1", "Claremont Tower", "54.980341", "-1.614106", "Go to computer science building" ));
        //gives "name not found" because latitude ends in "00". Need to fix, both here and in createtreasurehunt
        locationsList.add(new MapLocation(viewedTreasureHuntTitle, "5", "Students' Union", "54.979100", "-1.615008", "Go to the Union Building"));
        locationsList.add(new MapLocation(viewedTreasureHuntTitle, "7", "Business School", "54.974744", "-1.622528", "Go to the Business School" ));
        locationsList.add(new MapLocation(viewedTreasureHuntTitle, "9", "Agriculture School", "54.977944", "-1.617079", "Go to school of Agriculture"));

        teamLatLngs.add(new LatLng(54.97684,-1.614123));
        teamLatLngs.add(new LatLng(54.98001,-1.612345));
        teamLatLngs.add(new LatLng(54.97432,-1.615432));






        /*
           Test to check that the locations are being added to the locationsList list and are
           staying in the list when the activity is recreated. Also check that they are removed
           from the list when a location is deleted.
         */
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(AdminMap.this);
        mBuilder.setTitle("Number of items in locationsList:");
        mBuilder.setMessage(Integer.toString(locationsList.size()));
        mBuilder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = mBuilder.create();
        alertDialog.show();
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
        }

        //create a bitmap from the drawable student_location
        mMap.setOnMarkerClickListener(this); //make the onMarkerClick method apply to this map
        int px = getResources().getDimensionPixelSize(R.dimen.map_student_location_size);
        studentLocationBitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888); //the student location drawable bitmap
        Canvas canvas = new Canvas(studentLocationBitmap);
        Drawable shape = getResources().getDrawable(R.drawable.student_location);
        shape.setBounds(0, 0, studentLocationBitmap.getWidth(), studentLocationBitmap.getHeight());
        shape.draw(canvas);

        //place markers for team locations
        for (LatLng latLng : DataVault.teamLocations) {
            markers.add(mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(teamNameFinder(latLng))
                    .icon(BitmapDescriptorFactory.fromBitmap(studentLocationBitmap))));
        }

        //place markers for treasure hunt locations
        for (MapLocation mapLocation : DataVault.locations) {
            LatLng latLng = new LatLng(Double.parseDouble(mapLocation.getLatitude()),Double.parseDouble(mapLocation.getLongitude()));
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.gem_icon)))
                    .title(nameFinder(latLng)));

            //add marker to list of location markers
            locationsInHunt.add(marker);
        }


        /*
        When the user presses on the marker info window, they get displayed the clue for that location
         */
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(final Marker marker) {

                if(isMarkerALocation(marker)) {
                    // Dialog Box that asks user if they wish to delete the marker or edit the location details
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(AdminMap.this);
                    mBuilder.setIcon(android.R.drawable.ic_menu_info_details); //checkbox icon on dialogue box
                    mBuilder.setTitle("Clue");
                    mBuilder.setMessage(clueFinder(marker.getPosition()));
                    mBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog alertDialog = mBuilder.create();
                    alertDialog.show();
                }
            }
        });

    }

    @Override
    public void onLocationChanged(Location location) {
        //Move camera to your location
        LatLng currLatLng = new LatLng(location.getLatitude(), location.getLongitude()); //current location
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(currLatLng)); //moves camera to your location
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(15)); //zooms in

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
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
        Log.e("AdminMap", "onError: Status = " + status.toString());

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

        //Move camera to current location when the activity is launched
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));

    }
    @Override
    public void onConnectionSuspended(int i) {
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    //change markers the team has reached to green
    @Override
    public boolean onMarkerClick(Marker marker) {

        //if the marker clicked is not a location i.e. the marker clicked is a team marker
        if(!isMarkerALocation(marker)) {

            int index = 0;

            for(int i = 0; i < DataVault.teamNames.size(); i++) {
                if((Double.parseDouble(DataVault.teamLatitudes.get(i)) == marker.getPosition().latitude) &&
                        Double.parseDouble(DataVault.teamLongitudes.get(i)) == marker.getPosition().longitude) {
                    index = i;
                    break;
                }
            }

            //change all markers the team is completed to green
            changeMarkers(locationsInHunt, index);
        }
        return false; //makes sure the event is not consumed, the default functionality also still happens
    }

    //checks if a marker is in the locations list
    public boolean isMarkerALocation(Marker marker){
        for(Marker locationMarker : locationsInHunt){
            if(locationMarker.equals(marker)){
                return true;
            }
        }
        return false;
    }

    //colour all markers green that have been reached by the team
    public void changeMarkers(List<Marker> markers, int index) {

        boolean morePointsToColour = false;

        //The following calculations are made to ensure the correct points are changed
        boolean condition = (Integer.parseInt(DataVault.teamProgress.get(index)) +
                Integer.parseInt(DataVault.teamStartLocations.get(index)) - 1) >
                Integer.parseInt(DataVault.locationCount);

        int pointIfOverLocationCount = Integer.parseInt(DataVault.teamProgress.get(index)) +
                Integer.parseInt(DataVault.teamStartLocations.get(index))
                - 1
                - Integer.parseInt(DataVault.locationCount);

        int pointIfNotOverLocationCount = Integer.parseInt(DataVault.teamProgress.get(index)) +
                Integer.parseInt(DataVault.teamStartLocations.get(index))
                - 1;

        //the point the team has last reached
        int pointReached = condition ? pointIfOverLocationCount : pointIfNotOverLocationCount;

        //Reset all markers to normal gem before changing for appropriate team
        for(int i = 0; i < Integer.parseInt(DataVault.locationCount); i++) {

            markers.get(i).setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.gem_icon)));

        }

        //sets the lower bound of the for loop to the start location of the relevant team
        //sets the upper bound of the for loop to either location count or lower bound + progress
        for(int i = Integer.parseInt(DataVault.teamStartLocations.get(index)) - 1;
            i < (condition ? Integer.parseInt(DataVault.locationCount) : pointIfNotOverLocationCount); i++) {

            if((i == Integer.parseInt(DataVault.locationCount) - 1) && (pointReached < Integer.parseInt(DataVault.locationCount)))
                morePointsToColour = true;

            markers.get(i).setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerCollectedBitmapFromView(R.drawable.gem_collected)));

        }

        if(morePointsToColour)
            for(int i = 0; i < pointReached; i++) {
                markers.get(i).setIcon(BitmapDescriptorFactory.fromBitmap(getMarkerCollectedBitmapFromView(R.drawable.gem_collected)));

            }

    }

    //This gets the bitmap to set as the marker icon (normal gem)
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

    //This gets the bitmap to assign to the marker (collected gem)
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


    //find name of location at a certain marker
    public String nameFinder(LatLng currLatLng){
        //for all mapLocations in list of locations
        for(MapLocation mapLocation : DataVault.locations) {
            //if there is a location in the list that matches the co-ordinates of the current location
            if(Double.parseDouble(mapLocation.getLatitude())==currLatLng.latitude && mapLocation.getLongitude().equals(Double.toString(currLatLng.longitude))) {
                return mapLocation.getName();
            }
        }
        return "Name not found";
    }

    //find name of location at a certain marker
    public String clueFinder(LatLng currLatLng){
        //for all mapLocations in list of locations
        for(MapLocation mapLocation : DataVault.locations) {
            //if there is a location in the list that matches the co-ordinates of the current location
            if(mapLocation.getLatitude().equals(Double.toString(currLatLng.latitude)) && mapLocation.getLongitude().equals(Double.toString(currLatLng.longitude))) {
                return mapLocation.getClue();
            }
        }
        return "Clue not found";
    }

    //find name of location at a certain marker
    public String teamNameFinder(LatLng currLatLng) {
        for (int i = 0; i < DataVault.teamNames.size(); i++) {
            if ((Double.parseDouble(DataVault.teamLatitudes.get(i)) == currLatLng.latitude) &&
                    Double.parseDouble(DataVault.teamLongitudes.get(i)) == currLatLng.longitude) {
                return DataVault.teamNames.get(i);
            }
        }
        return "Team name not found";
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

                // Show an expanation to the user *asynchronously* -- don't block
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

    @Override
    public void onBackPressed() {

    }

    //An Asynctask to update student locations on a regular basis
    public class UpdateStudentLocations extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onProgressUpdate(Void... values) {

            //This progress update is a way of referring to the ui thread to update the position
            //of the student location markers every 5 seconds
            for(Marker marker : markers) {
                marker.remove();
            }

            markers.clear();

            //place markers for team locations
            for (LatLng latLng : DataVault.teamLocations) {
                markers.add(mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(teamNameFinder(latLng))
                        .icon(BitmapDescriptorFactory.fromBitmap(studentLocationBitmap))));
            }

        }

        @Override
        protected Void doInBackground(Void... params) {

            //The purpose of this thread is to automate the update of student location markers
            //every 5 seconds using progress update method
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            publishProgress();

            return null;
        }
    }

}