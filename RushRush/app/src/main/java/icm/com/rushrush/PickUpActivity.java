package icm.com.rushrush;

import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

public class PickUpActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    private String myCurrentLat;
    private String myCurrentLng;
    private String pickUpLat;
    private String pickUpLng;
    private String dropOffLat;
    private String dropOffLng;
    private String pickUpLocation;
    private String orderId;
    private String userName;
    private String userId;
    private String type;
    private JSONObject order;

    private TextView tvOrderData;
    private TextView tvTitle;

    private Button mapBtn1;
    private Button gpsBtn;
    private Button confirmBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_up);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        tvOrderData = (TextView) findViewById(R.id.tvOrderData);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        mapBtn1 = (Button) findViewById(R.id.mapBtn1);
        gpsBtn = (Button) findViewById(R.id.gpsBtn);
        confirmBtn = (Button) findViewById(R.id.confirmBtn);

        Intent it = getIntent();
        try {
            orderId = it.getStringExtra("orderId");
        } catch (Exception ex) {

        }
        try {
            order = new JSONObject(it.getStringExtra("order"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            String[] pickUpCoord = order.get("pickUpLatLng").toString().split(",");
            String[] dropOffCoord = order.get("dropOffLatLng").toString().split(",");
            pickUpLat = pickUpCoord[0];
            pickUpLng = pickUpCoord[1];
            dropOffLat = dropOffCoord[0];
            dropOffLng = dropOffCoord[1];
            myCurrentLat = it.getStringExtra("myCurrentLat");
            myCurrentLng = it.getStringExtra("myCurrentLng");
            userName = order.getString("userName");
            type = order.getString("type");
            userId = order.getString("userId");
            pickUpLocation = order.getString("pickUpLocalByLocation");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        tvTitle.setText("\nPick up\n");
        tvTitle.setTextSize(24);
        tvTitle.setTypeface(null, Typeface.BOLD_ITALIC);

        String text = "Address: \t" + pickUpLocation +
                "\n\nClient ID: \t" + userName +
                "\n\nType: \t     " + type +
                "\n\nOrder ID: \t" + orderId;
        tvOrderData.setText(text);

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeOrderStatus();
                callDropOffActivity2();
                finish();
            }
        });

        mapBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callDropOffActivity();
            }
        });

        gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callNavigation();
            }
        });

    }

    public void callDropOffActivity() {
        Intent it = new Intent(this, ShowDirectionsActivity.class);
        it.putExtra("myCurrentLat", myCurrentLat);
        it.putExtra("myCurrentLng", myCurrentLng);
        it.putExtra("pickUpLat", pickUpLat);
        it.putExtra("pickUpLng", pickUpLng);
        it.putExtra("dropOffLat", dropOffLat);
        it.putExtra("dropOffLng", dropOffLng);
        it.putExtra("mode", "PICK_UP");
        startActivity(it);
    }

    public void callDropOffActivity2() {
        Intent it = new Intent(this, DropOffActivity.class);
        it.putExtra("orderId", orderId);
        it.putExtra("order", order.toString());
        startActivity(it);
        finish();
    }

    public void callNavigation() {
        String uri = "http://maps.google.com/maps?f=d&hl=en&saddr="+myCurrentLat+","+myCurrentLng+"&daddr="+pickUpLat+","+pickUpLng;
        Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(Intent.createChooser(it, "Select Navigation App"));
    }

    public void changeOrderStatus() {
        mDatabase.child("Users").child(userId).child("Orders").child("Active").child(orderId).child("state").setValue("DroppingOff");
    }
}
