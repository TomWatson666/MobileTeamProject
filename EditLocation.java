package team5project.treasurehuntapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by rzarathore on 19/04/2017.
 */

public class EditLocation extends AppCompatActivity {
    EditText Name, Clue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_location);

        //create activity as a popup window
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int)(width*0.8), (int)(height*0.6));

        Name = (EditText)findViewById(R.id.nameField);
        Clue = (EditText) findViewById(R.id.clueField);

        String previousName = nameFinder(new LatLng(ManagementMap.placeLatitude, ManagementMap.placeLongitude)); //get the name of this location
        Name.setText((previousName), TextView.BufferType.EDITABLE); //pre-fill the Name field with this location's name

        String previousClue = clueFinder(new LatLng(ManagementMap.placeLatitude, ManagementMap.placeLongitude));
        Clue.setText((previousClue), TextView.BufferType.EDITABLE);
    }


    //saves changes made to location and updates the locationsToAdd list and the DataVault LatLng list
    public void OnSave(View view){

        validateInput();

    }

    public void validateInput() {

        //Variable to check that all inputs are valid
        boolean valid = true;
        View viewFocusedOn = null;

        String name = Name.getText().toString();
        String clue = Clue.getText().toString();
        String latitude = Double.toString(ManagementMap.placeLatitude);
        String longitude = Double.toString(ManagementMap.placeLongitude);

        //Make sure that none of the input boxes are empty
        if(TextUtils.isEmpty(clue)) {
            Clue.setError("Please enter a clue");
            viewFocusedOn = Clue;
            valid = false;
        }

        if(TextUtils.isEmpty(name)) {
            Name.setError("Please enter a name");
            viewFocusedOn = Name;
            valid = false;
        }

        if(!clue.matches("^[a-zA-Z0-9' ]*$")) {
            Clue.setError("Invalid characters used");
            Clue.setText("");
            viewFocusedOn = Clue;
            valid = false;
        }

        if(!name.matches("^[a-zA-Z0-9' ]*$")) {
            Name.setError("Invalid characters used");
            Name.setText("");
            viewFocusedOn = Name;
            valid = false;
        }

        StringBuilder newField;
        String[] parts;

        //Apostrophes are allowed, but break sql, hence doubling them up fixes this
        if((parts = clue.split("'")).length > 1) {
            newField = new StringBuilder();
            for(int i = 0; i < parts.length; i++)
                newField.append(i == parts.length - 1 ? parts[i] : parts[i] + "''");

            clue = newField.toString();
        }

        if((parts = name.split("'")).length > 1) {
            newField = new StringBuilder();
            for(int i = 0; i < parts.length; i++) {
                newField.append(i == parts.length - 1 ? parts[i] : parts[i] + "''");
            }

            name = newField.toString();
        }

        if(!valid) {

            viewFocusedOn.requestFocus();

        } else {

            //find previous index to be entered as it cannot be edited
            String previousIndex = indexFinder(new LatLng(ManagementMap.placeLatitude, ManagementMap.placeLongitude));

            //update the details entered by the user in the Name, QR Code and Clue fields

            int position = -1;

            for(int i = 0; i < DataVault.viewedTreasureHuntLocations.size(); i++) {

                if(DataVault.viewedTreasureHuntLocations.get(i).getIndex().equals(previousIndex)) {
                    position = i;
                    break;
                }

            }

            //Set the name and the clue of the location to the changed version
            DataVault.viewedTreasureHuntLocations.get(position).setName(name);
            DataVault.viewedTreasureHuntLocations.get(position).setClue(clue);

            MapLocation location = new MapLocation(ManagementMap.viewedTreasureHuntTitle,
                    previousIndex, name, String.valueOf(ManagementMap.placeLatitude),
                    String.valueOf(ManagementMap.placeLongitude), clue);

            DataVault.editMapLocation(location);

            Intent intent = new Intent(EditLocation.this, ManagementMap.class);
            startActivity(intent);

        }

    }

    public void OnCancel () {
        finish();
    }

    /************************************************/
    /**** mapLocation properties finding methods ****/
    /************************************************/
    //finds name for the location in at this marker from the locationsToAdd list to pre-fill the "name" field
    public String nameFinder(LatLng currLatLng){

        for(MapLocation mapLocation : DataVault.viewedTreasureHuntLocations) {
            if(mapLocation.getLatitude().equals(Double.toString(currLatLng.latitude)) && mapLocation.getLongitude().equals(Double.toString(currLatLng.longitude))) {
                return mapLocation.getName();
            }
        }
        return "Name not found";
    }

    //finds index for the location in at this marker from the locationsToAdd list
    public String indexFinder(LatLng currLatLng){

        for(MapLocation mapLocation : DataVault.viewedTreasureHuntLocations) {
            if(mapLocation.getLatitude().equals(Double.toString(currLatLng.latitude)) && mapLocation.getLongitude().equals(Double.toString(currLatLng.longitude))) {
                return mapLocation.getIndex();
            }
        }
        return "Index not found";
    }

    //finds clue for the location in at this marker from the locationsToAdd list to pre-fill the "clue" field
    public String clueFinder(LatLng currLatLng){

        for(MapLocation mapLocation : DataVault.viewedTreasureHuntLocations) {
            if(mapLocation.getLatitude().equals(Double.toString(currLatLng.latitude)) && mapLocation.getLongitude().equals(Double.toString(currLatLng.longitude))) {
                return mapLocation.getClue();
            }
        }

        return "Clue not found";
    }

    @Override
    public void onBackPressed() {

    }

}
