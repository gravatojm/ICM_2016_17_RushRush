package icm.com.rushrush;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import icm.com.rushrush.tasks.DownloadDirectionsUrl;
import icm.com.rushrush.tasks.ParseDirections;

public class DropOffActivity extends AppCompatActivity {

    private TextView tvTitle;
    private TextView tvOrderData;
    private Button mapBtn;
    private Button gpsBtn;
    private Button qrBtn;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private JSONObject order;
    private String orderId;
    private String userName;
    private String userId;
    private String type;
    private String obs;
    private String dropOffLocationName;
    private String dropOffLat;
    private String dropOffLng;
    private double myCurrentLat;
    private double myCurrentLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drop_off);

        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvOrderData = (TextView) findViewById(R.id.tvOrderData);
        mapBtn = (Button) findViewById(R.id.mapBtn);
        gpsBtn = (Button) findViewById(R.id.gpsBtn);
        qrBtn = (Button) findViewById(R.id.qrBtn);
        qrBtn.setClickable(false);

        locationManager = (LocationManager) getSystemService(this.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                myCurrentLat = location.getLatitude();
                myCurrentLng = location.getLongitude();
                setUpLocationListener();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);

        Intent it = getIntent();
        orderId = it.getStringExtra("orderId");
        try {
            order = new JSONObject(it.getStringExtra("order"));
            userName = order.getString("userName");
            userId = order.getString("userId");
            type = order.getString("type");
            obs = order.getString("obs");
            dropOffLocationName = order.getString("dropOffLocalByLocation");
            String[] dropOffCoord = order.get("dropOffLatLng").toString().split(",");
            dropOffLat = dropOffCoord[0];
            dropOffLng = dropOffCoord[1];
        } catch (JSONException e) {
            e.printStackTrace();
        }

        tvTitle.setText("\nDrop of\n");
        tvTitle.setTextSize(24);
        tvTitle.setTypeface(null, Typeface.BOLD_ITALIC);

        // colocar dados da entrega no ecrã
        String text = "Address: \t" + dropOffLocationName +
                "\n\nClient ID: \t" + userName +
                "\n\nType: \t     " + type +
                "\n\nOrder ID: \t" + orderId;
        tvOrderData.setText(text);

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callShowDirectionsActivity();
            }
        });

        gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callNavigation();
            }
        });

        qrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callScannerActivity();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        qrBtn.setClickable(true);
    }

    /* ---------------------------- */
    /* Criar um mapa com o percurso */
    /* ---------------------------- */

    public void callShowDirectionsActivity() {
        Intent it = new Intent(this, ShowDirectionsActivity.class);
        it.putExtra("myCurrentLat", myCurrentLat);
        it.putExtra("myCurrentLng", myCurrentLng);
        it.putExtra("pickUpLat", "1");
        it.putExtra("pickUpLng", "1");
        it.putExtra("dropOffLat", dropOffLat);
        it.putExtra("dropOffLng", dropOffLng);
        it.putExtra("mode", "DROP_OFF");
        startActivity(it);
    }

    private void setUpLocationListener() {

    }

    private boolean calculateDistance(double lat2, double lng2) {

        final int R = 6371; // Radius of the earth

        double lat1 = Double.parseDouble(dropOffLat);
        double lng1 = Double.parseDouble(dropOffLng);

        Double lat = Math.toRadians(lat2 - lat1);
        Double lng = Math.toRadians(lng2 - lng1);
        Double a = Math.sin(lat / 2) * Math.sin(lng / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lat / 2) * Math.sin(lng / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        distance = Math.pow(distance, 2);

        if(Math.sqrt(distance) < 50) {
            return true;
        }
        else {
            return false;
        }
    }

    /* ------------------------------- */
    /* Iniciar um serviço de navegação */
    /* ------------------------------- */

    private void callNavigation() {
        String uri = "http://maps.google.com/maps?f=d&hl=en&saddr="+myCurrentLat+","+myCurrentLng+"&daddr="+dropOffLat+","+dropOffLng;
        Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(Intent.createChooser(it, "Select Navigation App"));
    }

    /* -------------------------------------------------------- */
    /* Iniciar um o scanner de QR code para finalizar a entrega */
    /* -------------------------------------------------------- */

    private void callScannerActivity() {
        Intent it = new Intent(this, ScannerActivity.class);
        it.putExtra("orderId", orderId);
        it.putExtra("clientId", userId);
        startActivity(it);
        finish();
    }

}
