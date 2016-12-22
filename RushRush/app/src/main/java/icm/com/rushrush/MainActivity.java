package icm.com.rushrush;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.support.v4.view.GravityCompat;
import android.support.design.widget.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import icm.com.rushrush.entities.Order;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private Button buttonCreateOrder;
    private Button buttonRunner;
    private Button btnSignOut;
    private DatabaseReference mDatabase;
    private ArrayList<String> activeOrdersKeyList;
    private ArrayList<String> pastOrdersKeyList;
    private ArrayList<String> draftOrdersKeyList;
    ArrayAdapter<String> adapterActiveOrders;
    ArrayAdapter<String> adapterPastOrders;
    ArrayAdapter<String> adapterDraftOrders;
    private ArrayList<Order> activeOrderList;
    private ArrayList<Order> pastOrderList;
    private ArrayList<Order> draftOrderList;
    ListView lvOrders;

    private int currentAdapter = 1;

    private static final int ZXING_CAMERA_PERMISSION = 1;
    private Class<?> mClss;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_final);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        final String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        activeOrdersKeyList = new ArrayList<>();
        pastOrdersKeyList = new ArrayList<>();
        draftOrdersKeyList = new ArrayList<>();
        activeOrderList = new ArrayList<>();
        pastOrderList = new ArrayList<>();
        draftOrderList = new ArrayList<>();

        lvOrders = (ListView) findViewById(R.id.lvOrders);
        lvOrders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String orderId = (String) lvOrders.getAdapter().getItem(position).toString();
                if(currentAdapter == 1) {
                    for(int i = 0; i < activeOrderList.size(); i++) {
                        if(orderId.equals(activeOrderList.get(i).getId())) {
                            callCreateOrder(activeOrderList.get(i));
                            break;
                        }
                    }
                }
                if(currentAdapter == 2) {
                    for(int i = 0; i < pastOrderList.size(); i++) {
                        if(orderId.equals(pastOrderList.get(i).getId())) {
                            callCreateOrder(pastOrderList.get(i));
                            break;
                        }
                    }
                }
                if(currentAdapter == 3) {
                    for(int i = 0; i < draftOrderList.size(); i++) {
                        if(orderId.equals(draftOrderList.get(i).getId())) {
                            callCreateOrder(draftOrderList.get(i));
                            break;
                        }
                    }
                }
            }
        });

        adapterActiveOrders = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, activeOrdersKeyList);
        adapterPastOrders = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pastOrdersKeyList);
        adapterDraftOrders = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, draftOrdersKeyList);

        // escutar modificações nas suas encomendas, na base de dados
        mDatabase.child("Users").child(userId).child("Orders").child("Active").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Order order = dataSnapshot.getValue(Order.class);
                String orderId = dataSnapshot.getKey();
                order.setId(orderId);
                activeOrderList.add(order);
                activeOrdersKeyList.add(orderId);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Order order = dataSnapshot.getValue(Order.class);
                String orderId = dataSnapshot.getKey();
                String runnerId = order.getRunnerId();
                if(order.getState().equals("PickingUp")) {
                    notifyUser(orderId, "PickingUp");
                    //Toast.makeText(MainActivity.this, "RunnerId " + runnerId, Toast.LENGTH_LONG).show();
                }
                if(order.getState().equals("DroppingOff")) {
                    notifyUser(orderId, "DroppingOff");
                }
                if(order.getState().equals("Done")) {
                    Map<String, Object> orderValues = order.toMap();
                    Map<String, Object> childUpdates = new HashMap<String, Object>();
                    childUpdates.put("/Users/" + userId + "/Orders/Past/" + orderId, orderValues);
                    mDatabase.updateChildren(childUpdates);
                    mDatabase.child("Users").child(userId).child("Orders").child("Active").child(orderId).removeValue();
                    for(int i = 0; i < activeOrderList.size(); i++) {
                        if(orderId.equals(activeOrderList.get(i).getId())) {
                            activeOrderList.remove(i);
                            break;
                        }
                    }
                    for(int i = 0; i < activeOrdersKeyList.size(); i++) {
                        if(orderId.equals(activeOrdersKeyList.get(i))) {
                            activeOrdersKeyList.remove(i);
                            break;
                        }
                    }
                    notifyUser(orderId, "Done");
                    // dataSnapshot.getRef().setValue(null); tambem funciona, supostamente
                }

                //Toast.makeText(MainActivity.this, "RunnerId " + runnerId, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.child("Users").child(userId).child("Orders").child("Draft").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String orderId = dataSnapshot.getKey();
                draftOrdersKeyList.add(orderId);
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
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.child("Users").child(userId).child("Orders").child("Past").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String orderId = dataSnapshot.getKey();
                pastOrdersKeyList.add(orderId);
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
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Starts with Active Orders
        setAdapter(adapterActiveOrders);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newDelivery = new Intent(MainActivity.this, CreateOrderActivity.class);
                startActivity(newDelivery);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    public void callCreateOrder(Order order) {
        Intent it = new Intent(this, CreateOrderActivity.class);
        it.putExtra("pickUpLocalByLocation", order.getPickUpLocalByLocation());
        it.putExtra("dropOffLocalByLocation", order.getDropOffLocalByLocation());
        it.putExtra("obs", order.getObs());
        it.putExtra("dropOffName", order.getDropOffName());
        startActivity(it);
    }

    private void setAdapter(ArrayAdapter adapter) {
        lvOrders.setAdapter(adapter);
    }

    // notificaçoes que avisam o utilizador de mudanças no estado das suas encomendas
    private void notifyUser(String orderId, String state) {
        String msg = "";
        switch (state) {
            case "PickingUp":
                msg = "Pick up process started.";
                break;
            case "DroppingOff":
                msg = "Drop off process started.";
                break;
            case "Done":
                msg = "Delivery completed";
                break;
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark_normal)
                .setContentTitle(state)
                .setContentText(msg + "\nOrder n. " + orderId);
        int notificationId = 001;
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(notificationId, mBuilder.build());
    }


    //

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    public void launchScalingScannerActivity(View v) {
        //launchActivity(ScannerActivity.class);
    }

    public void launchActivity(Class<?> clss) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            mClss = clss;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
        } else {
            Intent intent = new Intent(this, clss);
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ZXING_CAMERA_PERMISSION:
                /*
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(mClss != null) {
                        Intent intent = new Intent(this, mClss);
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(this, "Please grant camera permission to use the QR Scanner", Toast.LENGTH_SHORT).show();
                }
                */
                return;
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_active) {

            setAdapter(adapterActiveOrders);
            currentAdapter = 1;

        } else if (id == R.id.nav_past) {

            setAdapter(adapterPastOrders);
            currentAdapter = 2;

        } else if (id == R.id.nav_draft) {

            setAdapter(adapterDraftOrders);
            currentAdapter = 3;

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_profile) {
            Intent it = new Intent(this, MainRunner.class);
            startActivity(it);

        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent it = new Intent(this, LoginActivity.class);
            startActivity(it);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
