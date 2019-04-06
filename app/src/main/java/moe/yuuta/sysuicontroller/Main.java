package moe.yuuta.sysuicontroller;

import android.app.Application;
import android.content.Context;
import android.os.Build;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.perf.FirebasePerformance;

import moe.shizuku.api.ShizukuClientHelper;
import moe.shizuku.api.ShizukuService;

public class Main extends Application {
    public static final String GLOBAL_TAG = "UIC";

    private static boolean v3Failed;
    private static boolean v3TokenValid;

    // From the sample of Shizuku
    public static boolean isShizukuV3Failed() {
        return v3Failed;
    }

    public static boolean isShizukuV3TokenValid() {
        return v3TokenValid;
    }

    public static void setV3TokenValid(boolean v3TokenValid) {
        Main.v3TokenValid = v3TokenValid;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebasePerformance.getInstance().setPerformanceCollectionEnabled(!BuildConfig.DEBUG);
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(!BuildConfig.DEBUG);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ShizukuClientHelper.setBinderReceivedListener(() -> {
            if (ShizukuService.getBinder() == null) {
                v3Failed = true;
                return;
            } else {
                try {
                    // test the binder first
                    ShizukuService.pingBinder();

                    if (Build.VERSION.SDK_INT < 23) {
                        String token = ShizukuClientHelper.loadPre23Token(base);
                        v3TokenValid = ShizukuService.setCurrentProcessTokenPre23(token);
                    }
                } catch (Throwable tr) {
                    return;
                }
            }
        });
    }
}
