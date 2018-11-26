package moe.yuuta.sysuicontroller.auto_start;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import moe.yuuta.sysuicontroller.BuildConfig;

public class AutoStartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(AutoStartWorker.class.getSimpleName(), "Received " + intent.getAction());
        WorkManager.getInstance()
                .beginUniqueWork(BuildConfig.APPLICATION_ID + ":auto_start",
                        ExistingWorkPolicy.KEEP,
                        new OneTimeWorkRequest.Builder(AutoStartWorker.class).setConstraints(Constraints.NONE)
                                .build()).enqueue();
    }
}
