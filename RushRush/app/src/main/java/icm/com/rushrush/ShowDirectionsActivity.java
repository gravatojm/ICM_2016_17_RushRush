package icm.com.rushrush;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import icm.com.rushrush.tasks.DownloadDirectionsUrl;
import icm.com.rushrush.tasks.ParseDirections;

public class ShowDirectionsActivity extends FragmentActivity implements OnMapReadyCallback {

    private double myCurrentLat;
    private double myCurrentLng;
    private double pickUpLat;
    private double pickUpLng;
    private double dropOffLat;
    private double dropOffLng;

    private SupportMapFragment mapFragment;
    private LocationManager locationManager;
    private Location location;
    private String longitude = "";
    private String latitude = "";

    private List<PolylineOptions> options;
    private PolylineOptions polylineOptions;
    private String mode;
    private GoogleMap myMap;

    private boolean isNull = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_map);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent it = getIntent();
        mode = it.getStringExtra("mode");
        try {
            myCurrentLat = Double.parseDouble(it.getStringExtra("myCurrentLat"));
            myCurrentLng = Double.parseDouble(it.getStringExtra("myCurrentLng"));
            pickUpLat = Double.parseDouble(it.getStringExtra("pickUpLat"));
            pickUpLng = Double.parseDouble(it.getStringExtra("pickUpLng"));
            dropOffLat = Double.parseDouble(it.getStringExtra("dropOffLat"));
            dropOffLng = Double.parseDouble(it.getStringExtra("dropOffLng"));
        } catch (Exception e) {

        }

        options = new ArrayList<>();

        DownloadDirectionsUrl ddu = new DownloadDirectionsUrl();
        ParseDirections parseDirections = new ParseDirections();

        String directionsUrlPickUp, directionsUrlDropOff, urlData;
        List<List<HashMap<String, String>>> directions;

        // Modo do percurso:
        // 1 - completo,
        // 2 - posição atual - ponto pick up,
        // 3 - posição atual - ponto drop off

        // O modo completo deveria mostrar o percurso entre a posição atual e o ponto final
        // da entrega (drop off), passando pelo ponto de levantamento (pick up). No entanto, este
        // modo nunca é utilizado uma vez que não funciona corretamente

        switch (mode) {
            case "COMPLETE":
                Toast.makeText(this, "COMPLETE", Toast.LENGTH_LONG).show();
                directionsUrlPickUp = getDirectionsUrl(myCurrentLat, myCurrentLng, pickUpLat, pickUpLng);
                directionsUrlDropOff = getDirectionsUrl(pickUpLat, pickUpLng, dropOffLat, dropOffLng);
                List<String> urls = new ArrayList<>();
                urls.add(directionsUrlPickUp);
                urls.add(directionsUrlDropOff);
                urlData = null;
                directions = null;
                for(int i = 0; i < urls.size(); i++) {
                    try {
                        Toast.makeText(this, Integer.toString(i), Toast.LENGTH_LONG).show();
                        ddu = new DownloadDirectionsUrl();
                        parseDirections = new ParseDirections();
                        urlData = ddu.execute(urls.get(i)).get();
                        directions = parseDirections.execute(urlData).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    options.add(createPolyline(directions));
                    if(options.size() == 2 && myMap != null) {
                        addMarkers();
                    }
                }
                break;

            case "PICK_UP":
                Toast.makeText(this, "PICK_UP", Toast.LENGTH_LONG).show();
                directionsUrlPickUp = getDirectionsUrl(myCurrentLat, myCurrentLng, pickUpLat, pickUpLng);
                urlData = null;
                directions = null;
                try {
                    urlData = ddu.execute(directionsUrlPickUp).get();
                    directions = parseDirections.execute(urlData).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                polylineOptions = createPolyline(directions);
                break;

            case "DROP_OFF":
                Toast.makeText(this, "DROP_OFF", Toast.LENGTH_LONG).show();
                directionsUrlDropOff = getDirectionsUrl(myCurrentLat, myCurrentLng, dropOffLat, dropOffLng);
                urlData = null;
                directions = null;
                try {
                    urlData = ddu.execute(directionsUrlDropOff).get();
                    directions = parseDirections.execute(urlData).get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                polylineOptions = createPolyline(directions);
                break;

            default:
                break;
        }

    }

    public void addMarkers() {
        for(int i = 0; i < options.size(); i++) {
            myMap.addPolyline(options.get(i).color(i));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.myMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.addPolyline(polylineOptions.color(Color.RED));
        googleMap.setMyLocationEnabled(true);
        switch (mode) {
            case "PICK_UP":
                LatLng pickUpLoc = new LatLng(pickUpLat, pickUpLng);
                googleMap.addMarker(new MarkerOptions().position(new LatLng(myCurrentLat, myCurrentLng)).draggable(false).title("Me"));
                googleMap.addMarker(new MarkerOptions().position(pickUpLoc).draggable(false).title("PickUp"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickUpLoc, 15));
                break;
            case "DROP_OFF":
                LatLng dropOffLoc = new LatLng(dropOffLat, dropOffLng);
                googleMap.addMarker(new MarkerOptions().position(new LatLng(myCurrentLat, myCurrentLng)).draggable(false).title("Me"));
                googleMap.addMarker(new MarkerOptions().position(dropOffLoc).draggable(false).title("DropOff"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dropOffLoc, 15));
                break;
            default:
                break;
        }
    }

    /* -----------------------------------------------------------------------*/
    /* Gera um URL para obter os dados do percurso através da Google Maps API */
    /* ---------------------------------------------------------------------- */

    private String getDirectionsUrl(double myCurrentLat, double myCurrentLng, double dropOffLat, double dropOffLng) {
        String origin = "origin=" + myCurrentLat + "," + myCurrentLng;
        String destination = "destination=" + dropOffLat + "," + dropOffLng;
        String sensor = "sensor=false";
        String url = "https://maps.googleapis.com/maps/api/directions/json?"+ origin + "&" + destination + "&" + sensor;
        return url;
    }

    /* --------------------------------------------------*/
    /* Gera a linha do percurso que irá aparecer no mapa */
    /* ------------------------------------------------- */

    private PolylineOptions createPolyline(List<List<HashMap<String, String>>> directions) {
        ArrayList<PolylineOptions> listOptions = new ArrayList<>();
        ArrayList<LatLng> points;
        PolylineOptions options = new PolylineOptions();
        double lat, lng;

        for(int i = 0; i < directions.size(); i++) {
            points = new ArrayList<>();
            options = new PolylineOptions();
            List<HashMap<String, String>> path = directions.get(i);

            for(int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);
                lat = Double.parseDouble(point.get("lat"));
                lng = Double.parseDouble(point.get("lng"));
                LatLng pos = new LatLng(lat, lng);
                points.add(pos);
            }
            options.addAll(points);
            options.width(5);
        }
        return options;
    }

}