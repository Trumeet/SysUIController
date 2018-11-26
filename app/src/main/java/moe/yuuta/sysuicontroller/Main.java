package moe.yuuta.sysuicontroller;

import android.app.Application;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.perf.FirebasePerformance;

public class Main extends Application {
    public static final String GLOBAL_TAG = "UIC";

    @Override
    public void onCreate() {
        super.onCreate();
        FirebasePerformance.getInstance().setPerformanceCollectionEnabled(!BuildConfig.DEBUG);
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(!BuildConfig.DEBUG);
    }
}
