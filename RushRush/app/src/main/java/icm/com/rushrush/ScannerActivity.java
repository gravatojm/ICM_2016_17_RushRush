package icm.com.rushrush;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerActivity extends BaseScannerActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;
    private DatabaseReference mDatabase;

    private String orderId;
    private String clientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        setupToolbar();
        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.content_frame);
        mScannerView = new ZXingScannerView(this);
        contentFrame.addView(mScannerView);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        Intent it = getIntent();
        orderId = it.getStringExtra("orderId");
        clientId = it.getStringExtra("clientId");
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void handleResult(Result rawResult) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScannerView.resumeCameraPreview(ScannerActivity.this);
            }
        }, 2000);

        //TODO: change orderId to order.getId()

        //compare rawResult with orderId
        if(rawResult.getText().equals(orderId)){
            Toast.makeText(this, "Valid QRcode. Delivery completed.", Toast.LENGTH_LONG).show();
            finishOrder();
            finish();
        } else{
            Toast.makeText(this, "Wrong QRcode, Try Again", Toast.LENGTH_LONG).show();
        }

    }

    public void finishOrder(){

        mDatabase.child("Users").child(clientId).child("Orders").child("Active").child(orderId).child("state").setValue("Done");

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishOrder();
        finish();
    }
}
