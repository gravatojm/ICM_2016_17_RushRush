package icm.com.rushrush.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jgravato on 11/12/2016.
 */

@IgnoreExtraProperties
@JsonIgnoreProperties(ignoreUnknown=true)
public class Order {

    private String id;
    private String userId;
    private String userName;
    private String pickUpLocalByLocation;
    private String dropOffLocalByLocation;
    private String runnerId;
    private String pickUpLocation;
    private String pickUpLatLng;
    private String dropOffLatLng;
    private String dropOffLocation;
    private String dropOffName;
    private String state;
    private String obs;
    private String type;

    public Order() {

    }

    public Order(String userId, String pickUpLocation, String dropOffLocation, String dropOffName, String obs) {
        this.userId = userId;
        this.pickUpLocation = pickUpLocation;
        this.dropOffLocation = dropOffLocation;
        this.dropOffName = dropOffName;
        this.obs = obs;
    }

    public Order(String userId, String pickUpLocation, String dropOffLocation, String dropOffName,
                 String obs, String pickUpLatLng, String dropOffLatLng, String pickUpLocalByLocation,
                 String dropOffLocalByLocation, String type, String userName) {
        this.userId = userId;
        this.pickUpLocation = pickUpLocation;
        this.dropOffLocation = dropOffLocation;
        this.dropOffName = dropOffName;
        this.obs = obs;
        this.pickUpLatLng = pickUpLatLng;
        this.dropOffLatLng = dropOffLatLng;
        this.pickUpLocalByLocation = pickUpLocalByLocation;
        this.dropOffLocalByLocation = dropOffLocalByLocation;
        this.type = type;
        this.userName = userName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRunnerId() {
        return runnerId;
    }

    public void setRunnerId(String runnerId) {
        this.runnerId = runnerId;
    }

    public String getPickUpLocation() {
        return pickUpLocation;
    }

    public void setPickUpLocation(String pickUpLocation) {
        this.pickUpLocation = pickUpLocation;
    }

    public String getDropOffLocation() {
        return dropOffLocation;
    }

    public void setDropOffLocation(String dropOffLocation) {
        this.dropOffLocation = dropOffLocation;
    }

    public String getDropOffName() {
        return dropOffName;
    }

    public void setDropOffName(String dropOffName) {
        this.dropOffName = dropOffName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getObs() {
        return obs;
    }

    public void setObs(String obs) {
        this.obs = obs;
    }

    public String getPickUpLocalByLocation() {
        return pickUpLocalByLocation;
    }

    public void setPickUpLocalByLocation(String pickUpLocalByLocation) {
        this.pickUpLocalByLocation = pickUpLocalByLocation;
    }

    public String getDropOffLocalByLocation() {
        return dropOffLocalByLocation;
    }

    public void setDropOffLocalByLocation(String dropOffLocalByLocation) {
        this.dropOffLocalByLocation = dropOffLocalByLocation;
    }

    public String getPickUpLatLng() {
        return pickUpLatLng;
    }

    public void setPickUpLatLng(String pickUpLatLng) {
        this.pickUpLatLng = pickUpLatLng;
    }

    public String getDropOffLatLng() {
        return dropOffLatLng;
    }

    public void setDropOffLatLng(String dropOffLatLng) {
        this.dropOffLatLng = dropOffLatLng;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("runnerId", runnerId);
        result.put("pickUpLocation", pickUpLocation);
        result.put("dropOffLocation", dropOffLocation);
        result.put("state", state);
        result.put("obs", obs);
        //
        result.put("pickUpLatLng", pickUpLatLng);
        result.put("dropOffLatLng", dropOffLatLng);
        result.put("pickUpLocalByLocation", pickUpLocalByLocation);
        result.put("dropOffLocalByLocation", dropOffLocalByLocation);
        result.put("type", type);
        result.put("userName", userName);
        return result;
    }

    @Exclude
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("userId", userId);
            json.put("pickUpLocation", pickUpLocation);
            json.put("dropOffLocation", dropOffLocation);
            json.put("dropOffLatLng", dropOffLatLng);
            json.put("pickUpLatLng", pickUpLatLng);
            json.put("dropOffName", dropOffName);
            json.put("obs", obs);
            if(runnerId != null) {
                json.put("runnerId", runnerId);
            }
            json.put("userName", userName);
            json.put("type", type);
            json.put("pickUpLocalByLocation", pickUpLocalByLocation);
            json.put("dropOffLocalByLocation", dropOffLocalByLocation);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

}
