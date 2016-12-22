package icm.com.rushrush;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
//import com.google.android.gms.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;

import icm.com.rushrush.entities.Order;

public class MainRunner extends AppCompatActivity {

    private LocationManager locationManager;
    private LocationListener locationListener;

    private double myCurrentLat;
    private double myCurrentLng;

    private TextView tvDropOffName;
    private TextView tvDropOffAddress;
    private Button buttonAccept;
    private Button btnLogout;
    private Switch switchLfw;
    private Firebase firebase;

    private boolean isOnline = false;

    private String orderId;
    private Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_runner);

        tvDropOffName = (TextView) findViewById(R.id.tvOrderData);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                myCurrentLat = location.getLatitude();
                myCurrentLng = location.getLongitude();
                if(isOnline) {
                    // aviso para evitar que se inicie o serviço sem ter a localização atual
                    tvDropOffAddress.setText("My location ready!");
                }
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
            Toast.makeText(this, "Nao tem permissoes", Toast.LENGTH_LONG).show();
            return;
        }
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //Toast.makeText(this, "Using GPS", Toast.LENGTH_LONG).show();;
        } else {
            //Toast.makeText(this, "Using NETWORK", Toast.LENGTH_LONG).show();
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);

        switchLfw = (Switch) findViewById(R.id.switchLfr);
        switchLfw.setChecked(false);
        tvDropOffAddress = (TextView) findViewById(R.id.tvDropOffAddress);
        buttonAccept = (Button) findViewById(R.id.buttonAccept);
        buttonAccept.setVisibility(View.INVISIBLE);
        buttonAccept.setClickable(false);
        btnLogout = (Button) findViewById(R.id.btnLogout);

        switchLfw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    isOnline = true;
                    buttonAccept.setClickable(true);
                } else {
                    buttonAccept.setClickable(false);
                    isOnline = false;
                }
            }
        });

        firebase = new Firebase("https://icm2016rushrush.firebaseio.com/Orders");

        buttonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebase.child(orderId).child("runnerId").setValue("12345");
                callPickUpActivity(orderId, order);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        // escuta modificaçoes na base de dados
        // espera que seja criada uma nova entrega
        firebase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                orderId = dataSnapshot.getKey().toString();
                order = dataSnapshot.getValue(Order.class);
                    tvDropOffName.setText("New delivery!");
                    tvDropOffName.setTextSize(24);
                    buttonAccept.setVisibility(View.VISIBLE);
                    //buttonAccept.setClickable(true);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseAuth.getInstance().signOut();
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent it = new Intent(this, LoginActivity.class);
        startActivity(it);
        finish();
    }

    private void callPickUpActivity(String orderId, Order order) {
        String myCurrentLatStr = String.valueOf(myCurrentLat);
        String myCurrentLngStr = String.valueOf(myCurrentLng);
        Toast.makeText(this, "CurLoc - " + myCurrentLat + "," + myCurrentLng, Toast.LENGTH_LONG).show();
        Intent it = new Intent(this, PickUpActivity.class);
        it.putExtra("orderId", orderId);
        it.putExtra("order", order.toJson().toString());
        it.putExtra("myCurrentLat", myCurrentLatStr);
        it.putExtra("myCurrentLng", myCurrentLngStr);
        startActivity(it);
    }

}
