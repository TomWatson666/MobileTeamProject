package team5project.treasurehuntapp;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static team5project.treasurehuntapp.DatabaseConnection.executeQuery;

/**
 * Created by tomwa on 06/04/2017.
 */

public class DataVault {

    //Variable to prevent multiple iterations of infinite while loops being run unnecessarily
    public static boolean updateRunning = false;
    public static String currentUser;
    public static String currentTeam;
    public static String treasureHuntTitle = "";
    public static String locationCount = "";

    //A team's name, progress, latitude, longitude will all be at the same index with the way it has been retrieved from the database
    public static List<LatLng> teamLocations = new ArrayList<>();
    public static List<String> teamNames = new ArrayList<>();
    public static List<String> teamCodes = new ArrayList<>();
    public static List<String> teamProgress = new ArrayList<>();
    public static List<String> teamLatitudes = new ArrayList<>();
    public static List<String> teamLongitudes = new ArrayList<>();
    public static List<String> teamStartLocations = new ArrayList<>();
    public static List<MapLocation> locations = new ArrayList<>();

    public static List<String> usernames = new ArrayList<>();
    public static List<String> phoneAlerts = new ArrayList<>();
    public static List<String> emailAlerts = new ArrayList<>();

    //Relevant to Management version of Map only
    public static List<String> treasureHunts = new ArrayList<>();
    public static List<String> dates = new ArrayList<>();
    public static List<MapLocation> viewedTreasureHuntLocations = new ArrayList<>();

    //If admin goes to teamtracker on a day without a treasurehunt then animationSmoothnessMultiplier needs to be
    //initialised so that if they click on a team referred to from the database, it will not throw NullPointerException
    public static int animationSmoothnessMultiplier = 0;

    public static boolean updateLooper = false;


    /**
     *
     * List of Methods
     *
     * getPreferences()
     * Purpose: This will get the usernames, phone alerts, and email alerts so that the preferences are accurate
     * Uses: This will be called when the app starts
     *
     * updatePreferences(final String phoneAlert, final String emailAlert)
     * Purpose: Update preferences in the database when the user presses save on the alert dialog of team progress tracker
     * Uses: This will keep the database up to date with preferences of the students
     *
     * autoUpdateTeamInfo()
     * Purpose: This will keep the progress, latitude, and longitude of all teams up to date for the Admin Map/Team Tracker
     * Uses: This will be called when the app starts
     *
     * updateLocationInfo()
     * Purpose: This will load in all the info about the current treasure hunt's locations
     * Uses: This will be called from within updateTreasureHuntInfo() as it must run afterwards
     *
     * updateTeamData()
     * Purpose: This will load in the names of all teams ready for when the team tracker is loaded, so that all teams can be
     *          displayed in the ListView
     * Uses: This will be called when the app starts
     *
     * updateTreasureHuntInfo()
     * Purpose: This retrieves the current date to find out if a treasure hunt is active, and if so, it loads in it's location count.
     *          The location count allows the animation smoothness multiplier to be initialised, as secondary progress of the
     *          circular progress bar on team tracker has a max progress of 100
     * Uses: This will be called when the app starts
     *
     * autoUpdateCurrentLocation(final String latitude, final String longitude)
     * Purpose: This updates the location of a team for the admin map, so that the admin can track where each team is on the map
     * Uses: This will be called when the admin map is launched
     *
     * setUserActive(final String user, final String team)
     * Purpose: Set a user as active once they have logged in, and set them as the current student for
     *          a team if no other student in that team is active
     * Uses: This will be called when a student successfully logs in
     *
     * setUserInactive(final String user, final String team)
     * Purpose: Set a user as inactive once they have logged out, and set another student in that team
     *          as the current student to represent that teams location if there is another student in
     *          that team active at the time, otherwise current student is set to no-one to symbolise
     *          the team being inactive
     *
     * deleteTreasureHunt(final String treasureHuntTitle)
     * Purpose: Delete a treasure hunt from the database, this is done from the treasure hunt management page
     * Uses: This will be called when an admin presses the delete context menu item on the treasure hunt management page
     *
     * retrieveMarkerLocation(String latitude, String longitude)
     * Purpose: This gets the location information of the marker that is passed relevant to it's latitude and longitude
     * Uses: This is called when a marker is clicked on in the management map
     *
     * deleteMapLocation(final String treasureHuntTitle, final String index, final int locationCount)
     * Purpose: This deletes a map location for a particular treasure hunt being viewed on the management map, and shuffles all
     *          other locations for that treasure hunt down 1 index to compensate and leave no gaps
     *
     * editMapLocation(final MapLocation location)
     * Purpose: This edits information about a location plotted on the management map for the chosen treasure hunt
     *
     */

    public static void getPreferences() {

        new Thread(new Runnable() {

            @Override
            public void run() {

                String sqlQuery = "SELECT Username, Phone_Alert, Email_Alert FROM User;";
                List<String> result = DatabaseConnection.executeQuery(sqlQuery);

                List<String> newUsernames = new ArrayList<String>();
                List<String> newPhoneAlerts = new ArrayList<String>();
                List<String> newEmailAlerts = new ArrayList<String>();

                if(!result.get(0).equals("nothing returned")) {
                    for(int i = 0; i < result.size(); i++) {

                        newUsernames.add(DatabaseConnection.parseResultSet(result, i, 0));
                        newPhoneAlerts.add(DatabaseConnection.parseResultSet(result, i, 1));
                        newEmailAlerts.add(DatabaseConnection.parseResultSet(result, i, 2));

                    }
                }

                usernames = new ArrayList<String>(newUsernames);
                phoneAlerts = new ArrayList<String>(newPhoneAlerts);
                emailAlerts = new ArrayList<String>(newEmailAlerts);

            }

        }).start();

    }

    public static  void updatePreferences(final String phoneAlert, final String emailAlert) {

        new Thread(new Runnable() {

            @Override
            public void run() {

                String sqlQuery = "UPDATE User SET Phone_Alert = '" + phoneAlert + "', Email_Alert = '" + emailAlert + "' " +
                        "WHERE Username = '" + currentUser + "';";
                DatabaseConnection.executeQuery(sqlQuery);

            }

        }).start();

    }

    public static void autoUpdateTeamInfo() {

        new Thread(new Runnable() {

            @Override
            public void run() {

                updateRunning = true;

                while(updateLooper) {

                    String sqlQuery = "SELECT Team_Name, Code, Progress, Latitude, Longitude, Start_Location FROM Team";
                    List<String> result = executeQuery(sqlQuery);

                    List<String> newTeamNames = new ArrayList<String>(); //Added recently for testing
                    List<String> newTeamCodes = new ArrayList<String>();
                    List<String> newTeamProgress = new ArrayList<String>();
                    List<String> newTeamLatitudes = new ArrayList<String>();
                    List<String> newTeamLongitudes = new ArrayList<String>();
                    List<String> newTeamStartLocations = new ArrayList<String>();
                    List<LatLng> newTeamLocations = new ArrayList<>();

                    if(!result.get(0).equals("nothing returned")) {
                        for (int i = 0; i < result.size(); i++) {
                            if(!DatabaseConnection.parseResultSet(result, i, 0).equals("Admin")) {
                                newTeamNames.add(DatabaseConnection.parseResultSet(result, i, 0)); // Add recently for testing
                                newTeamCodes.add(DatabaseConnection.parseResultSet(result, i, 1));
                                newTeamProgress.add(DatabaseConnection.parseResultSet(result, i, 2));
                                newTeamLatitudes.add(DatabaseConnection.parseResultSet(result, i, 3));
                                newTeamLongitudes.add(DatabaseConnection.parseResultSet(result, i, 4));
                                newTeamStartLocations.add(DatabaseConnection.parseResultSet(result, i, 5));
                                newTeamLocations.add(
                                        new LatLng(Double.parseDouble(DatabaseConnection.parseResultSet(result, i, 3)),
                                        Double.parseDouble(DatabaseConnection.parseResultSet(result, i, 4))));
                            }
                        }
                    }

                    teamNames = new ArrayList<String>(newTeamNames);
                    teamCodes = new ArrayList<String>(newTeamCodes);
                    teamProgress = new ArrayList<String>(newTeamProgress);
                    teamLatitudes = new ArrayList<String>(newTeamLatitudes);
                    teamLongitudes = new ArrayList<String>(newTeamLongitudes);
                    teamStartLocations = new ArrayList<String>(newTeamStartLocations);
                    teamLocations = new ArrayList<LatLng>(newTeamLocations);

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }

                updateRunning = false;

            }

        }).start();

    }

    public static void editTeam(final String teamNameBefore, final String teamNameAfter, final String code, final String startLocation) {

        new Thread(new Runnable() {

            @Override
            public void run() {

                String sqlQuery = "UPDATE Team SET Team_Name = '" + teamNameAfter + "', Code = '" + code + "', Start_Location = '" + startLocation + "' " +
                        "WHERE Team_Name = '" + teamNameBefore + "';";
                executeQuery(sqlQuery);

            }

        }).start();

    }

    public static void addTeam(final String teamName, final String code, final String startLocation) {

        new Thread(new Runnable() {

            @Override
            public void run() {

                String sqlQuery = "INSERT INTO Team (Team_Name, Code, Progress, Longitude, Latitude, Student, Start_Location) VALUES ('" +
                        teamName + "', '" + code + "', 0, 0, 0, 'None', '" + startLocation + "');";
                executeQuery(sqlQuery);

            }

        }).start();

    }

    public static void deleteTeam(final String teamName) {

        new Thread(new Runnable() {

            @Override
            public void run() {

                String sqlQuery = "DELETE FROM User WHERE Team_Name = '" + teamName + "';";
                executeQuery(sqlQuery);

                sqlQuery = "DELETE FROM Team WHERE Team_Name = '" + teamName + "';";
                executeQuery(sqlQuery);

            }

        }).start();

    }

    public static void updateLocationInfo() {

        new Thread(new Runnable() {

            @Override
            public void run() {

                String sqlQuery = "SELECT `Index`, Name, Latitude, Longitude, Clue FROM Location " +
                        "WHERE Treasure_Hunt_Title = '" + treasureHuntTitle + "' " +
                        "ORDER BY `Index` ASC;";
                List<String> result = executeQuery(sqlQuery);

                List<MapLocation> newLocations = new ArrayList<MapLocation>();

                if (!result.get(0).equals("nothing returned")) {
                    for (int i = 0; i < result.size(); i++) {

                        newLocations.add(new MapLocation(treasureHuntTitle,
                                DatabaseConnection.parseResultSet(result, i, 0),
                                DatabaseConnection.parseResultSet(result, i, 1),
                                DatabaseConnection.parseResultSet(result, i, 2),
                                DatabaseConnection.parseResultSet(result, i, 3),
                                DatabaseConnection.parseResultSet(result, i, 4)));

                    }
                }

                locations = new ArrayList<MapLocation>(newLocations);

            }

        }).start();

    }

    public static void updateTreasureHunts() {

        new Thread(new Runnable() {

            @Override
            public void run() {

                String sqlQuery = "SELECT Treasure_Hunt_Title, Date FROM `Treasure Hunt`;";
                List<String> result = executeQuery(sqlQuery);

                List<String> newTreasureHunts = new ArrayList<String>();
                List<String> newDates = new ArrayList<String>();

                for(int i = 0; i < result.size(); i++) {

                    newTreasureHunts.add(DatabaseConnection.parseResultSet(result, i, 0));
                    newDates.add(DatabaseConnection.parseResultSet(result, i, 1));

                }

                treasureHunts = new ArrayList<String>(newTreasureHunts);
                dates = new ArrayList<String>(newDates);

            }

        }).start();

    }

    public static void updateTreasureHuntInfo(final String treasureHuntTitle, final String date) {

        new Thread(new Runnable() {

            @Override
            public void run() {

                String sqlQuery = "UPDATE `Treasure Hunt` SET Date = '" + date + "' WHERE Treasure_Hunt_Title = '" + treasureHuntTitle + "';";
                executeQuery(sqlQuery);

            }

        }).start();

    }

    public static void updateTreasureHuntInfo() {

        new Thread(new Runnable() {

            @Override
            public void run() {

                String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

                String sqlQuery = "SELECT Treasure_Hunt_Title FROM `Treasure Hunt` WHERE Date = '" +
                        currentDate + "';";
                List<String> result = executeQuery(sqlQuery);

                if (!result.get(0).equals("nothing returned")) {
                    treasureHuntTitle = DatabaseConnection.parseResultSet(result, 0, 0);
                }

                sqlQuery = "SELECT Location_Count FROM `Treasure Hunt` WHERE Treasure_Hunt_Title = '" +
                        treasureHuntTitle + "';";
                result = executeQuery(sqlQuery);

                if (!result.get(0).equals("nothing returned")) {
                    locationCount = DatabaseConnection.parseResultSet(result, 0, 0);
                    if(!locationCount.equals("0")) {
                        animationSmoothnessMultiplier = 100 / Integer.parseInt(locationCount);
                    } else {
                        animationSmoothnessMultiplier = 1;
                    }
                }

                updateLocationInfo();

                LoginFragment.treasureHuntDetailsRetrieved = true;

            }

        }).start();

    }

    public static void resetTeamProgress(final String teamName) {

        new Thread(new Runnable() {

            @Override
            public void run() {

                String sqlQuery = "UPDATE Team SET Progress = '0' WHERE Team_Name = '" + teamName + "';";
                executeQuery(sqlQuery);

            }

        }).start();

    }

    public static void setUserActive(final String user, final String team) {

        new Thread(new Runnable() {

            @Override
            public void run() {

                String sqlQuery = "UPDATE User SET Status = 'Active' WHERE Username = '" + user + "';";
                executeQuery(sqlQuery);

                if(!team.equals("Admin")) {

                    sqlQuery = "SELECT * FROM Team WHERE Team_Name = '" + team + "' AND Student = 'None';";
                    if (!executeQuery(sqlQuery).get(0).equals("nothing returned")) {

                        sqlQuery = "UPDATE Team SET Student = '" + user + "' WHERE Team.Team_Name = '" +
                                team + "';";
                        executeQuery(sqlQuery);

                    }

                }

            }

        }).start();

    }

    public static void clearDataOnLogout() {

        //Reset latitudes and longitudes to 0 for that user
        String sqlQuery = "UPDATE User SET Latitude = '0', Longitude = '0' WHERE Username = '" +
                DataVault.currentUser + "';";
        executeQuery(sqlQuery);

        //Stop infinite while loops from running in the background
        updateLooper = false;

        //Reset the current user and current team of the app
        DataVault.currentUser = "";
        DataVault.currentTeam = "";

    }

    public static void setUserInactive(final String user, final String team) {

        new Thread(new Runnable() {

            @Override
            public void run() {

                String sqlQuery = "UPDATE User SET Status = 'Inactive' WHERE Username = '" + user + "';";
                executeQuery(sqlQuery);

                if(!team.equals("Admin")) {

                    StudentMap.updateStudentLooper = false;
                    StudentMap.locationUpdatesStarted = false;

                    sqlQuery = "SELECT Username FROM User WHERE Status = 'Active' AND Team_Name = '" + team + "';";
                    List<String> result = executeQuery(sqlQuery);

                    if (result.get(0).equals("nothing returned")) {
                        sqlQuery = "UPDATE Team SET Student = 'None', Latitude = '0'," +
                                " Longitude = '0' WHERE Team_Name = '" + team + "';";
                        executeQuery(sqlQuery);
                    } else {
                        sqlQuery = "UPDATE Team SET Student = '" + DatabaseConnection.parseResultSet(result, 0, 0) + "' " +
                                "WHERE Team_Name = '" + team + "';";
                        executeQuery(sqlQuery);

                        String newStudent = DatabaseConnection.parseResultSet(result, 0, 0);

                        sqlQuery = "SELECT Latitude, Longitude FROM User WHERE Username = '" + newStudent + "';";
                        result = executeQuery(sqlQuery);

                        sqlQuery = "UPDATE Team SET Latitude = '" + DatabaseConnection.parseResultSet(result, 0, 0) +
                                "', Longitude = '" + DatabaseConnection.parseResultSet(result, 0, 1) + "' WHERE Username = '" +
                        newStudent + "';";
                        executeQuery(sqlQuery);
                    }

                }

                clearDataOnLogout();

            }

        }).start();

    }

    public static void deleteTreasureHunt(final String treasureHuntTitle) {

        new Thread(new Runnable() {

            @Override
            public void run() {

                String sqlQuery = "DELETE FROM Location WHERE Treasure_Hunt_Title = '" + treasureHuntTitle + "';";
                executeQuery(sqlQuery);

                sqlQuery = "DELETE FROM `Treasure Hunt` WHERE Treasure_Hunt_Title = '" + treasureHuntTitle + "';";
                executeQuery(sqlQuery);

            }

        }).start();

    }

    public static MapLocation retrieveMarkerLocation(String latitude, String longitude) {

        for(MapLocation location : viewedTreasureHuntLocations) {
            if(location.getLatitude().equals(latitude) && location.getLongitude().equals(longitude))
                return location;
        }

        return null;

    }

    public static MapLocation retrieveCurrentHuntMarkerLocation(String latitude, String longitude) {

        for(MapLocation location : locations) {
            if(location.getLatitude().equals(latitude) && location.getLongitude().equals(longitude))
                return location;
        }

        return null;

    }

    public static void addMapLocation(final String treasureHuntTitle, final String index, final String startLocation) {

        new Thread(new Runnable() {

            @Override
            public void run() {



            }

        }).start();

    }

    public static void editMapLocation(final MapLocation location) {

        new Thread(new Runnable() {

            @Override
            public void run() {

                String sqlQuery = "UPDATE Location SET Name = '" + location.getName() + "', " +
                        "Clue = '" + location.getClue() + "' " +
                        "WHERE Treasure_Hunt_Title = '" + location.getTreasureHuntTitle() + "' " +
                        "AND `Index` = '" + location.getIndex() + "';";

                executeQuery(sqlQuery);

            }

        }).start();

    }

}
