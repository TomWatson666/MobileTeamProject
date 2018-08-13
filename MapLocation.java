package team5project.treasurehuntapp;

/**
 * Created by tomwa on 17/04/2017.
 */

public class MapLocation implements Cloneable {

    private String treasureHuntTitle;
    private String index;
    private String name;
    private String latitude;
    private String longitude;
    private String qrCode;
    private String clue;

    public MapLocation(String treasureHuntTitle, String index, String name, String latitude, String longitude, String clue) {
        this.treasureHuntTitle = treasureHuntTitle;
        this.index = index;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.qrCode = treasureHuntTitle + "|" + index;
        this.clue = clue;
    }

    //This ensure it can be cloned, as it is used in lists that need to be hard copied, for comparison purposes
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String getTreasureHuntTitle() {
        return treasureHuntTitle;
    }

    public void setTreasureHuntTitle(String treasureHuntID) {
        this.treasureHuntTitle = treasureHuntID;
    }

    public void remakeQr() {

        this.qrCode = treasureHuntTitle + "|" + index;

    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getClue() {
        return clue;
    }

    public void setClue(String clue) {
        this.clue = clue;
    }
}
