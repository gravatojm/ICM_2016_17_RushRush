package icm.com.rushrush;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by jgravato on 10/12/2016.
 */

public class RushRush extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
