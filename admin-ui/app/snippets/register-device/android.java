package com.push.pushapplication;

import android.widget.Toast;

import com.feedhenry.sdk.FH;
import com.feedhenry.sdk.FHActCallback;
import com.feedhenry.sdk.FHResponse;
import android.app.Application;

public class PushApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Be sure run it after init()
        FH.pushRegister(new FHActCallback() {
            @Override
            public void success(FHResponse fhResponse) {
                // Do something cool
            }

            @Override
            public void fail(FHResponse fhResponse) {
                Toast.makeText(getApplicationContext(),
                        fhResponse.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
