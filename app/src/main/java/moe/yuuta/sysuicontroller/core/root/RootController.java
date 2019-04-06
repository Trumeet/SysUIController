package moe.yuuta.sysuicontroller.core.root;

import android.app.StatusBarManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

import eu.chainfire.librootjava.RootIPCReceiver;
import eu.chainfire.librootjavadaemon.RootDaemon;
import eu.chainfire.libsuperuser.Shell;
import moe.yuuta.sysuicontroller.BuildConfig;
import moe.yuuta.sysuicontroller.IStatusController;
import moe.yuuta.sysuicontroller.core.IController;

public class RootController implements IController, Shell.OnCommandResultListener {
    private static final String TAG = "Controller_R";

    private static final int CODE_START = 1;

    private Shell.Interactive mShell;
    private volatile IStatusController mService;

    private static final String ARG_SERVICE = RootController.class.getName() + ".ARG_SERVICE";

    private final Callback mStartCallback;
    private final Fragment mContext;

    public RootController(Callback mStartCallback, Fragment mContext) {
        this.mStartCallback = mStartCallback;
        this.mContext = mContext;
        mReceiver.setContext(mContext.requireContext());
        mService = mReceiver.getIPC();
    }

    @Override
    public void startAsync() {
        mShell = new Shell.Builder()
                .useSU()
                .open(this); // Will start after shell opening
    }

    @Override
    public boolean isServiceReady() {
        return mService != null && mReceiver.isConnected() && !mReceiver.isDisconnectScheduled();
    }

    @Override
    public void disable(int flags) throws RemoteException {
        mService.disable(flags);
    }

    @Override
    public void disable2(int flags) throws RemoteException {
        mService.disable2(flags);
    }

    @Override
    public int getDisableFlags() throws RemoteException {
        return mService.getDisableFlags();
    }

    @Override
    public int getDisable2Flags() throws RemoteException {
        return mService.getDisable2Flags();
    }


    private RootIPCReceiver<IStatusController> mReceiver = new RootIPCReceiver<IStatusController>(null, ControllerService.CODE_SERVICE) {
        @Override
        public void onConnect(IStatusController ipc) {
            Log.d(TAG, "Connected to remote service");
            mService = ipc;
            mStartCallback.update(null);
        }

        @Override
        public void onDisconnect(IStatusController ipc) {
            Log.d(TAG, "Disconnected to remote service");
            mService = ipc;
            mStartCallback.update(null);
        }
    };

    @Override
    public void onCommandResult(int commandCode, int exitCode, List<String> output) {
        Log.i(TAG, "Result " + commandCode + ": " + exitCode);
        if (output != null) {
            Log.w(TAG, output.toString());
        }
        switch (commandCode) {
            case SHELL_RUNNING:
                if (exitCode != 0) {
                    mStartCallback.update(new UnexpectedExitCodeException(exitCode));
                    break;
                }
                mShell.addCommand(RootDaemon.getLaunchScript(mContext.requireContext(),
                        ControllerService.class,
                        null,
                        null,
                        null, BuildConfig.APPLICATION_ID + ":daemon"),
                        CODE_START, this);
                break;
            case CODE_START:
                // Kill process immediately
                mShell.kill();
                mShell.close();
                mShell = null;
                if (exitCode != 0) {
                    mStartCallback.update(new UnexpectedExitCodeException(exitCode));
                }
                break;
        }
    }

    private void runRestoreBinder (@Nullable Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            if (savedInstanceState == null) {
                Log.d(TAG, "savedInstanceState is null");
            } else {
                if (savedInstanceState.getBinder(ARG_SERVICE) == null)
                    Log.d(TAG, "savedInstanceState doesn't contain binder");
                else
                    Log.d(TAG, "Restoring");
            }
        }
        if (savedInstanceState != null && savedInstanceState.getBinder(ARG_SERVICE) != null) {
            // FIXME: 11/25/18 The restoried interface's binder not alive
            mService = IStatusController.Stub.asInterface(savedInstanceState.getBinder(ARG_SERVICE));
        }
    }

    @Override
    public void restoreStatus(@Nullable Bundle savedInstanceState) {
        runRestoreBinder(savedInstanceState);
    }

    @Override
    public void saveStatus(@NonNull Bundle savedInstanceState) {
        savedInstanceState.putBinder(ARG_SERVICE, mService.asBinder());
    }

    @Override
    public void destroy() {
        mReceiver.release();
        // Shell will be closed immediately after executing.
        if (mShell != null) mShell.close();
    }

    @Override
    public void stopSync() {
        try {
            // When the service stops, SystemUI auto revert settings.
            mReceiver.disconnect();
            mService.exit();
        } catch (Throwable e) {
            Log.e(TAG, "exit()", e);
        }
        mStartCallback.update(null);
    }

    @Override
    public boolean canStop() {
        return true;
    }

    @Override
    public void revert() {
        try {
            mService.disable(StatusBarManager.DISABLE_NONE);
            mService.disable2(StatusBarManager.DISABLE2_NONE);
        } catch (RemoteException ignored) {}
    }

    public static class UnexpectedExitCodeException extends Exception {
        private final int abnormalExitCode;

        public UnexpectedExitCodeException(int abnormalExitCode) {
            super("Abnormal exit code " + abnormalExitCode);
            this.abnormalExitCode = abnormalExitCode;
        }

        public int getAbnormalExitCode() {
            return abnormalExitCode;
        }
    }
}