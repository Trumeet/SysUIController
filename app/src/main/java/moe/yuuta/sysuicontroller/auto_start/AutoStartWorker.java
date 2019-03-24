package moe.yuuta.sysuicontroller.auto_start;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import eu.chainfire.librootjava.RootIPCReceiver;
import eu.chainfire.librootjavadaemon.RootDaemon;
import eu.chainfire.libsuperuser.Shell;
import moe.yuuta.sysuicontroller.BuildConfig;
import moe.yuuta.sysuicontroller.IStatusController;
import moe.yuuta.sysuicontroller.core.ControllerService;

public class AutoStartWorker extends Worker {
    private static final String TAG = AutoStartWorker.class.getSimpleName();

    public AutoStartWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    private CountDownLatch countDownLatch;

    private RootIPCReceiver<IStatusController> mReceiver = new RootIPCReceiver<IStatusController>(null, ControllerService.CODE_SERVICE) {
        @Override
        public void onConnect(IStatusController ipc) {
            try {
                SharedPreferences preferences = getApplicationContext().getSharedPreferences("flags", Context.MODE_PRIVATE);
                ipc.disable(preferences.getInt("disable_flags", ipc.getDisableFlags()));
                ipc.disable2(preferences.getInt("disable2_flags", ipc.getDisable2Flags()));
                Log.i(TAG, "Settings restored");
            } catch (RemoteException e) {
                Log.e(TAG, "Unable to restore settings", e);
            }
            countDownLatch.countDown();
        }

        @Override
        public void onDisconnect(IStatusController ipc) {

        }
    };

    @NonNull
    @Override
    public Result doWork() {
        Log.i(TAG, "Start auto starting");
        countDownLatch = new CountDownLatch(1);
        mReceiver.setContext(getApplicationContext());
        List<String> commands = RootDaemon.getLaunchScript(getApplicationContext(),
                ControllerService.class,
                null,
                null,
                null, BuildConfig.APPLICATION_ID + ":daemon");
        if (BuildConfig.DEBUG) Log.d(TAG, commands.toString());
        Shell.Interactive shell = new Shell.Builder().useSU().open();
        shell.addCommand(commands);
        try {
            countDownLatch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "await()", e);
        }
        Log.i(TAG, "Done, releasing receiver");
        mReceiver.release();
        shell.kill(); // Kill the process, will make it idle and it won't kill the forked process
        shell.close();
        return Result.success();
    }
}
