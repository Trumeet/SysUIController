package moe.yuuta.sysuicontroller;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;

import androidx.work.Configuration;
import androidx.work.WorkManager;

public class Main extends Application {
    public static final String GLOBAL_TAG = "SysUIController";

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                unregisterActivityLifecycleCallbacks(this);
                FirebaseApp.initializeApp(activity);
                FirebaseAnalytics.getInstance(activity);
                WorkManager.initialize(activity, new Configuration.Builder().build());
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }
}
