package icm.com.rushrush.tasks;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jgravato on 13/12/2016.
 */

public class ParseDirections extends AsyncTask<String, Integer, List<List<HashMap<String,String>>>> {

    @Override
    protected List<List<HashMap<String, String>>> doInBackground(String... params) {
        JSONObject json;
        List<List<HashMap<String,String>>> directions = null;

        try {
            json = new JSONObject(params[0]);
            directions = parse(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return directions;
    }

    private List<List<HashMap<String, String>>> parse(JSONObject json) {
        List<List<HashMap<String, String>>> directions = new ArrayList<>();
        List<HashMap<String, String>> path;
        HashMap<String, String> coordinates;
        JSONArray routes = null;
        JSONArray legs = null;
        JSONArray steps = null;
        String polyline = "";
        List<LatLng> listCoord;

        try {
            routes = json.getJSONArray("routes");

            for(int i = 0; i < routes.length(); i++) {
                legs = ((JSONObject) routes.get(i)).getJSONArray("legs");
                path = new ArrayList<>();

                for(int j = 0; j < legs.length(); j++) {
                    steps = ((JSONObject) legs.get(j)).getJSONArray("steps");

                    for(int k = 0; k < steps.length(); k++) {
                        polyline = (String) ((JSONObject)((JSONObject) steps.get(k)).get("polyline")).get("points");
                        listCoord = decodePoly(polyline);

                        for(int m = 0; m < listCoord.size(); m++) {
                            coordinates = new HashMap<>();
                            coordinates.put("lat", Double.toString(((LatLng) listCoord.get(m)).latitude));
                            coordinates.put("lng", Double.toString(((LatLng) listCoord.get(m)).longitude));
                            path.add(coordinates);
                        }
                    }
                    directions.add(path);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return directions;

    }

    /**
     * Method to decode polyline points
     * Courtesy : http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     * */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }


}
