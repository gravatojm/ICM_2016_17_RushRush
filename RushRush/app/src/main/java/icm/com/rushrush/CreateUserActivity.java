package icm.com.rushrush;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static android.content.ContentValues.TAG;

public class CreateUserActivity extends Activity {

    private EditText etName;
    private EditText etEmail;
    private EditText etPassword;
    private Button btnCreate;
    private Button btnCancel;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private User user;

    private String name;
    private String email;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        etName = (EditText) findViewById(R.id.etName);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btnCreate = (Button) findViewById(R.id.btnCreate);
        btnCancel = (Button) findViewById(R.id.btnCancel);

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void createAccount() {
        name = etName.getText().toString();
        email = etEmail.getText().toString();
        password = etEmail.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            etName.setText("");
                            etEmail.setText("");
                            etPassword.setText("");
                            Toast.makeText(CreateUserActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        } else {
                            // get users UID
                            String uid = task.getResult().getUser().getUid().toString();
                            user = new User(name, email);
                            Map<String, Object> userValues = user.toMap();
                            Map<String, Object> childUpdates = new HashMap<String, Object>();
                            childUpdates.put("/Users/" + uid, userValues);
                            mDatabase.updateChildren(childUpdates);

                            Toast.makeText(CreateUserActivity.this, "Success", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }

}
