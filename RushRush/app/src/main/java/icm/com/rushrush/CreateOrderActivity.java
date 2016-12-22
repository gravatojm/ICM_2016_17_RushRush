package icm.com.rushrush;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import icm.com.rushrush.entities.Order;

public class CreateOrderActivity extends Activity {

    private static final String API_KEY = "AIzaSyCnXBEi6u5zhFLtnlqvgTs7_rQPjeyf55o";

    private AutoCompleteTextView tvDropOffName;
    private AutoCompleteTextView tvDropOffAddress;
    private AutoCompleteTextView tvPickUpAddress;
    private AutoCompleteTextView tvObs;
    private CheckBox checkBoxEnvelope;
    private CheckBox checkBoxSmallBox;
    private CheckBox checkBoxUseCurrentLocation;
    private Button buttonSend;
    private DatabaseReference mDatabase;
    private FirebaseUser user;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private Order newOrder;
    private String userId;
    private String pickUpLocation;
    private String pickUpLatLng;
    private String dropOffLocation;
    private String dropOffLatLng;
    private String dropOffName;
    private String obs;
    private boolean isEnvelope = false;
    private boolean isSmallBox = false;
    private boolean isCurrentLocation = false;
    private String type;
    private LatLng latLngAux1, latLngAux2;
    private String myCurrentLocation;
    private double myCurrentLat, myCurrentLng;

    private String pickUpLocalByLocation;
    private String dropOffLocalByLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_order);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        tvDropOffName = (AutoCompleteTextView) findViewById(R.id.tvOrderData);
        tvDropOffAddress = (AutoCompleteTextView) findViewById(R.id.tvDropOffAddress);
        tvPickUpAddress = (AutoCompleteTextView) findViewById(R.id.tvPickUpAddress);
        tvObs = (AutoCompleteTextView) findViewById(R.id.tvObs);
        checkBoxEnvelope = (CheckBox) findViewById(R.id.checkBoxEnvelope);
        checkBoxSmallBox = (CheckBox) findViewById(R.id.checkBoxSmallBox);
        checkBoxUseCurrentLocation = (CheckBox) findViewById(R.id.checkBoxCurrentLoc);
        buttonSend = (Button) findViewById(R.id.buttonSend);

        // exemplos de endereços possiveis
        // evitar erros na obtençao das coordenadas de um endereço e da posição atual
        String[] data = new String[] {"Rua Associação Humanitária dos Bombeiros Voluntários de Aveiro, 3810-902 Aveiro", "Av. 25 de Abril, 3830-044 Ílhavo"};
        ArrayAdapter<?> addressAdapter = new ArrayAdapter<Object>(this, android.R.layout.simple_dropdown_item_1line, data);
        tvPickUpAddress.setAdapter(addressAdapter);
        tvDropOffAddress.setAdapter(addressAdapter);


        // caso da activity ser utilizada para completar um pedido de entrega em rascunho (draft)
        Intent it = getIntent();
        try {
            if (it.hasExtra("pickUpLocalByLocation")) {
                tvPickUpAddress.setText(it.getStringExtra("pickUpLocalByLocation"));
            }
            if (it.hasExtra("dropOffLocalByLocation")) {
                tvDropOffAddress.setText(it.getStringExtra("dropOffLocalByLocation"));
            }
            if (it.hasExtra("obs")) {
                tvObs.setText(it.getStringExtra("obs"));
            }
            if (it.hasExtra("dropOffName")) {
                tvDropOffName.setText(it.getStringExtra("dropOffName"));
            }
        } catch (Exception ex) {

        }


        checkBoxUseCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkBoxUseCurrentLocation.isChecked()) {
                    stopCurrentLocation();
                    isCurrentLocation = false;
                }
                if(checkBoxUseCurrentLocation.isChecked()) {
                    getCurrentLocation();
                    isCurrentLocation = true;
                }
            }
        });

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {

                    // obter uid
                    userId = user.getUid();

                    // obter localização atual
                    pickUpLocation = tvPickUpAddress.getText().toString();

                    // obter coordenadas da localização atual
                    latLngAux1 = getLocationFromAddress(pickUpLocation);
                    pickUpLatLng = latLngAux1.latitude + "," + latLngAux1.longitude;

                    // obter localização final
                    dropOffLocation = tvDropOffAddress.getText().toString();

                    // obter coordenadas da localização final
                    latLngAux2 = getLocationFromAddress(dropOffLocation);
                    dropOffLatLng = latLngAux2.latitude + "," + latLngAux2.longitude;

                    // nome da pessoa a quem entregar a encomenda
                    dropOffName = tvDropOffName.getText().toString();

                    // observaçoes para aquele pedido
                    obs = tvObs.getText().toString();

                    // tipo do objeto a entregar
                    // envelope ou pequena caixa
                    if(checkBoxEnvelope.isChecked()) {
                        isEnvelope = true;
                        type = "Envelope";
                    }
                    else if (checkBoxSmallBox.isChecked()) {
                        isSmallBox = true;
                        type = "SmallBox";
                    }

                    // obter o endereço dos locais pelas coordenadas dos mesmos
                    pickUpLocalByLocation = getLocationFromLatLng(latLngAux1);
                    dropOffLocalByLocation = getLocationFromLatLng(latLngAux2);

                    // obter o email da conta do cliente e utilizar como nome
                    String userMail = user.getEmail();

                    newOrder = new Order(userId, pickUpLocation, dropOffLocation, dropOffName, obs, pickUpLatLng, dropOffLatLng, pickUpLocalByLocation, dropOffLocalByLocation, type, userMail);

                    // criar um "child" em Orders e obter a chave gerada
                    // chave gerada representa o identificador do pedido
                    String orderId = mDatabase.child("/Orders").push().getKey();
                    newOrder.setState("Waiting");
                    newOrder.setId(orderId);

                    // introdução dos dados do novo pedido na base de dados
                    Map<String, Object> orderValues = newOrder.toMap();
                    Map<String, Object> childUpdates = new HashMap<String, Object>();

                    // introduzir no cliente
                    childUpdates.put("/Users/" + userId + "/Orders/Active/" + orderId, orderValues);

                    //introduzir na parte geral da base de dados
                    childUpdates.put("/Orders/" + orderId, orderValues);

                    mDatabase.updateChildren(childUpdates);

                    finish();
                } else {
                    Toast.makeText(CreateOrderActivity.this, "No logged user", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void saveAsDraft() {
        // get user id
        userId = user.getUid();

        // get current location
        pickUpLocation = tvPickUpAddress.getText().toString();

        latLngAux1 = getLocationFromAddress(pickUpLocation);
        pickUpLatLng = latLngAux1.latitude + "," + latLngAux1.longitude;

        // get drop off location
        dropOffLocation = tvDropOffAddress.getText().toString();

        latLngAux2 = getLocationFromAddress(dropOffLocation);
        dropOffLatLng = latLngAux2.latitude + "," + latLngAux2.longitude;

        // get receiver name
        dropOffName = tvDropOffName.getText().toString();

        // get observations
        obs = tvObs.getText().toString();

        // get order type
        if(checkBoxEnvelope.isChecked()) {
            isEnvelope = true;
            type = "Envelope";
        }
        else if (checkBoxSmallBox.isChecked()) {
            isSmallBox = true;
            type = "SmallBox";
        }

        // get location' names
        pickUpLocalByLocation = getLocationFromLatLng(latLngAux1);
        dropOffLocalByLocation = getLocationFromLatLng(latLngAux2);

        //newOrder = new Order(userId, pickUpLocation, dropOffLocation, dropOffName, obs);
        String userMail = user.getEmail();
        newOrder = new Order(userId, pickUpLocation, dropOffLocation, dropOffName, obs, pickUpLatLng, dropOffLatLng, pickUpLocalByLocation, dropOffLocalByLocation, type, userMail);

        String orderId = mDatabase.child("/Orders").push().getKey();
        newOrder.setState("Waiting");
        newOrder.setId(orderId);

        Map<String, Object> orderValues = newOrder.toMap();
        Map<String, Object> childUpdates = new HashMap<String, Object>();
        childUpdates.put("/Users/" + userId + "/Orders/Draft/" + orderId, orderValues);
        childUpdates.put("/Orders/" + orderId, orderValues);

        mDatabase.updateChildren(childUpdates);

        Toast toast = Toast.makeText(this, "Saved as Draft", Toast.LENGTH_LONG);
        toast.show();
        finish();
    }

    @Override
    public void onBackPressed() {
        DialogInterface.OnClickListener dialogInterface = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_NEGATIVE:
                        finish();
                        break;
                    case DialogInterface.BUTTON_POSITIVE:
                        saveAsDraft();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(CreateOrderActivity.this);
        builder.setMessage("Save as Draft?").setPositiveButton("Yes", dialogInterface).setNegativeButton("No", dialogInterface);
        builder.show();

    }

    private void getCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(this.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                myCurrentLat = location.getLatitude();
                myCurrentLng = location.getLongitude();
                tvPickUpAddress.setText(getLocationFromLatLng(new LatLng(myCurrentLat, myCurrentLng)));
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
    }

    private void stopCurrentLocation() {
        locationManager = null;
        locationListener = null;
    }

    private LatLng getLocationFromAddress(String addressStr) {
        Geocoder geocoder = new Geocoder(this);
        List<Address> listAddress;
        Address address;
        LatLng result = new LatLng(0, 0);

        try {
            listAddress = geocoder.getFromLocationName(addressStr, 3);
            if(listAddress == null) {
                return result;
            }
            address = listAddress.get(0);
            result = new LatLng(address.getLatitude(), address.getLongitude());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private String getLocationFromLatLng(LatLng location) {
        Geocoder geocoder = new Geocoder(this);
        List<Address> listAddress;
        String result = "";

        try {
            listAddress = geocoder.getFromLocation(location.latitude, location.longitude, 3);
            if(listAddress == null) {
                return result;
            }
            result += listAddress.get(0).getAddressLine(0) + ", ";
            result += listAddress.get(0).getPostalCode() + " - ";
            result += listAddress.get(0).getLocality();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
