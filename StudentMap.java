/**
 * Map Activity for the student section of the app.
 *
 * @author George Boulton
 * @version 2.3
 * @since 2017-04-04
 */

package team5project.treasurehuntapp;

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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

import static team5project.treasurehuntapp.DataVault.currentTeam;
import static team5project.treasurehuntapp.DataVault.locations;
import static team5project.treasurehuntapp.DataVault.retrieveCurrentHuntMarkerLocation;
import static team5project.treasurehuntapp.DataVault.teamNames;

/* UI Libraries */
/* Contains error codes for when device fails to connect to Google Play services */
/* GoogleApiClient Library - Allows access to all Google APIs */
/* Location Services Libraries */
/* Map Libraries */


public class StudentMap extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        PlaceSelectionListener,
        LocationListener {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private GoogleMap treasureMap;      // The map object
    GoogleApiClient googleApiClient;
    Location lastLocation;              // The last known location of the user
    Marker currentLocationMarker;       // The start point location marker - see onLocationChanged
    LocationRequest locationRequest;    // Object that determines accuracy and frequency of location updates
    Button clueButton;
    Button teamTrackerButton;
    Button logoutButton;
    ImageView seeker;
    List<MarkerOptions> points;
    Location mLastLocation;
    Location previousLocation;

    //These are to prevent memory leaks from infinite loops, these are changed to stop the loops at specific times
    boolean startUpdate = false;
    boolean updateHotAndCold = true;
    static boolean locationUpdatesStarted = false;
    int index = 0;
    int lastPoint = 0;
    static boolean updateStudentLooper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.student_map);

        if(getIntent().getStringExtra("Correct Previous Page") != null) {
            startUpdate = true;
        }

        updateStudentLooper = true;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        clueButton = (Button) findViewById(R.id.student_map_clue_button);
        teamTrackerButton = (Button) findViewById(R.id.student_map_team_tracker_button);
        logoutButton = (Button) findViewById(R.id.student_map_logout_button);

        seeker = (ImageView)findViewById(R.id.hotColdIcon);

        clueButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Dialog Box that shows user the clue
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(StudentMap.this);
                mBuilder.setIcon(android.R.drawable.ic_menu_info_details); //info icon on dialogue box
                mBuilder.setTitle("Clue");

                int index = teamNames.indexOf(currentTeam);

                //This finds the next point, so that the clue for the next point can be displayed to the student
                int nextPoint = lastPoint == Integer.parseInt(DataVault.locationCount) ?
                        1 : lastPoint < Integer.parseInt(DataVault.teamStartLocations.get(index)) ?
                        lastPoint : lastPoint + 1;

                LatLng latLng = new LatLng(Double.parseDouble(DataVault.locations.get(nextPoint - 1).getLatitude()),
                        Double.parseDouble(DataVault.locations.get(nextPoint - 1).getLongitude()));
                mBuilder.setMessage(clueFinder(latLng));
                mBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog alertDialog = mBuilder.create();
                alertDialog.show();

            }

        });

        // Button listener
        teamTrackerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                startActivity(new Intent(StudentMap.this, TeamProgressTracker.class));
                updateHotAndCold = false;
                finish();

            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Kill tracking thread here
                DataVault.setUserInactive(DataVault.currentUser, DataVault.currentTeam);
                startActivity(new Intent(StudentMap.this, LoginScreen.class));
                updateHotAndCold = false;
                finish();

            }
        });

    }

    /**
     * Alters the map as soon as it becomes available.
     * Reads in markers of points from database.
     * Initialises Google Play Services and configures the Google API client.
     *
     * @param googleMap - The GoogleMap object which will be altered
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        treasureMap = googleMap;

        // Load in Locations
        points = loadMarkers();

        treasureMap.clear();

        for(MarkerOptions marker : points) {
            treasureMap.addMarker(marker);
        }

        treasureMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                // Dialog Box that shows user the clue
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(StudentMap.this);
                mBuilder.setIcon(android.R.drawable.ic_menu_info_details); //info icon on dialogue box
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
        });

        // Initialise Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildClient();
                treasureMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildClient();
            treasureMap.setMyLocationEnabled(true);
        }

        //This updates the students/teams location in the database as the student uses the map
        if(!locationUpdatesStarted) {
            locationUpdatesStarted = true;

            new Thread(new Runnable() {

                @Override
                public void run() {

                    while (updateStudentLooper) {

                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if(updateStudentLooper) {

                            String latitude = lastLocation != null ? String.valueOf(lastLocation.getLatitude()) : "0";
                            String longitude = lastLocation != null ? String.valueOf(lastLocation.getLongitude()) : "0";

                            String sqlQuery = "UPDATE User SET Latitude = '" + latitude + "', Longitude = '" +
                                    longitude + "' WHERE Username = '" + DataVault.currentUser + "';";
                            DatabaseConnection.executeQuery(sqlQuery);

                            sqlQuery = "SELECT * FROM Team WHERE Student = '" + DataVault.currentUser + "';";

                            if (!DatabaseConnection.executeQuery(sqlQuery).equals("nothing returned")) {

                                sqlQuery = "UPDATE Team SET Latitude = '" + latitude + "', Longitude = '" +
                                        longitude + "' WHERE Team_Name = '" + DataVault.currentTeam + "';";
                                DatabaseConnection.executeQuery(sqlQuery);

                                DataVault.teamLocations.set(DataVault.teamNames.indexOf(DataVault.currentTeam),
                                        new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)));

                            }

                        }

                    }

                }

            }).start();

        }

    }



    /**
     * Loads in locations of treasure hunt into public list variable for use throughout class.
     * Markers do not appear until the student has visited the location.
     *
     * @return List of Locations
     */

    //This method loads in the marks to the map
    public List<MarkerOptions> loadMarkers() {

        List<MarkerOptions> markers = new ArrayList<>();

        int index = teamNames.indexOf(currentTeam);

        //This will be true if the start location + progress of the team exceeds the location count
        boolean morePointsToPlace = false;

        //This is the last point of the treasure hunt that the team have been to
        lastPoint = (Integer.parseInt(DataVault.teamProgress.get(index)) +
                Integer.parseInt(DataVault.teamStartLocations.get(index)) - 1) >
                Integer.parseInt(DataVault.locationCount) ?

                Integer.parseInt(DataVault.teamProgress.get(index)) +
                        Integer.parseInt(DataVault.teamStartLocations.get(index))
                        - Integer.parseInt(DataVault.locationCount) :

                Integer.parseInt(DataVault.teamProgress.get(index)) +
                        Integer.parseInt(DataVault.teamStartLocations.get(index))
                        - 1;

        if(Integer.parseInt(DataVault.teamProgress.get(index)) == 0)
            lastPoint = Integer.parseInt(DataVault.teamStartLocations.get(index));

        if(Integer.parseInt(DataVault.teamProgress.get(index)) != 0) {

            //This is the upper bound of the loop
            int upperBound = (Integer.parseInt(DataVault.teamProgress.get(index)) +
                    Integer.parseInt(DataVault.teamStartLocations.get(index)) - 1) >
                    Integer.parseInt(DataVault.locationCount) ?

                    Integer.parseInt(DataVault.locationCount)
                            + 1 :

                    Integer.parseInt(DataVault.teamProgress.get(index)) +
                            Integer.parseInt(DataVault.teamStartLocations.get(index));

            //These add the appropriate markers to the map
            for (int i = Integer.parseInt(DataVault.teamStartLocations.get(index)); i < upperBound; i++) {

                if ((i == Integer.parseInt(DataVault.locationCount)) && (lastPoint < Integer.parseInt(DataVault.locationCount)))
                    morePointsToPlace = true;

                markers.add(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromBitmap(getMarkerCollectedBitmapFromView(R.drawable.gem_collected)))
                        .position(new LatLng(Double.parseDouble(locations.get(i - 1).getLatitude()),
                                Double.parseDouble(locations.get(i - 1).getLongitude())))
                        .title(locations.get(i - 1).getName())
                        .snippet("Tap for Clue"));

            }

            if (morePointsToPlace) {
                for (int i = 1; i < lastPoint; i++) {

                    markers.add(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromBitmap(getMarkerCollectedBitmapFromView(R.drawable.gem_collected)))
                            .position(new LatLng(Double.parseDouble(locations.get(i - 1).getLatitude()),
                                    Double.parseDouble(locations.get(i - 1).getLongitude())))
                            .title(locations.get(i - 1).getName())
                            .snippet("Tap for Clue"));

                }
            }

        }

        return markers;

    }

    /**
     * Configures the Google API client.
     */
    protected synchronized void buildClient() {

        /*
            "synchronized" methods prevent simultaneous access to an object by multiple threads.
            This means that the object's variables cannot be read/written at the same time by
            multiple threads and thus prevents memory consistency errors.
         */

        googleApiClient = new GoogleApiClient.Builder(this) // Used to configure client
                .addConnectionCallbacks(this)               // provides callbacks when connection to Google Play Services changes
                .addOnConnectionFailedListener(this)        // Listens for any failed attempts to connect to Services
                .addApi(LocationServices.API)               // Adds Location Services API
                .build();                                   // Builds the client

        googleApiClient.connect();                          // Connects device to Services
    }

    @Override
    public void onConnected(Bundle bundle) {

        locationRequest = new LocationRequest();

        // Uses more power but more accurate tracking
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // 5 Seconds
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);

        // Get current location and centre camera on it
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        treasureMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) // If permissions have been granted to access location services
                == PackageManager.PERMISSION_GRANTED) {
            /*
                Begin regular location updates every 5 seconds (values above). The location will
                continue to update until the activity terminates.
             */
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    // Get the start location
    public void onLocationChanged(Location location) {

        lastLocation = location;
        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        }

        if(startUpdate) {
            CheckDistance distanceChecker = new CheckDistance();
            distanceChecker.execute();
            startUpdate = false;
        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (googleApiClient == null) {
                            buildClient();
                        }
                        treasureMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Show toast.
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    //This calculates the distance between the student and the next location, for the hot and cold marker
    public float distanceFromPoint(Location currentLocation) {

        int index = teamNames.indexOf(currentTeam);

        //This calculates the index of the next point that the team needs to go to
        int nextPoint = (Integer.parseInt(DataVault.teamProgress.get(index)) +
                Integer.parseInt(DataVault.teamStartLocations.get(index))) >
                Integer.parseInt(DataVault.locationCount) ?

                Integer.parseInt(DataVault.teamProgress.get(index)) +
                        Integer.parseInt(DataVault.teamStartLocations.get(index))
                        - Integer.parseInt(DataVault.locationCount) :

                Integer.parseInt(DataVault.teamProgress.get(index)) +
                        Integer.parseInt(DataVault.teamStartLocations.get(index));

        MapLocation nextLocation = retrieveCurrentHuntMarkerLocation(DataVault.locations.get(nextPoint - 1).getLatitude(),
                                    DataVault.locations.get(nextPoint - 1).getLongitude());

        //Make conversion between customer class MapLocation and google maps Location class

        // Variable changes depending on whereabouts in treasure hunt
        Location closeLocation = new Location("Close Location");

        closeLocation.setLatitude(Float.parseFloat(nextLocation.getLatitude()));
        closeLocation.setLongitude(Float.parseFloat(nextLocation.getLongitude()));

        // Shortest distance to a point taken and returned. Used for hotter and colder and change to scanner.
        float distance = currentLocation.distanceTo(closeLocation);

        return distance;

    }

    //This returns the bitmap for the collected gem icon
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
    public void onPlaceSelected(Place place) {

        treasureMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
        treasureMap.animateCamera(CameraUpdateFactory.zoomTo(18));

    }

    @Override
    public void onError(Status status) {

        Log.e("StudentMap", "onError: Status = " + status.toString());

        Toast.makeText(this, "Place selection failed: " + status.getStatusMessage(),
                Toast.LENGTH_SHORT).show();

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
        return "Name not found";
    }

    //An async task to keep the hot and cold marker accurate
    public class CheckDistance extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            while(updateHotAndCold) {

                previousLocation = lastLocation;

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                publishProgress();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

            //if(values == null) {
            seeker.setColorFilter(distanceFromPoint(previousLocation) > distanceFromPoint(lastLocation) ?
                    Color.rgb(181, 18, 43) : Color.rgb(63, 81, 181));

        }

    }

    @Override
    public void onBackPressed() {

    }

}
