package moe.yuuta.sysuicontroller.core.shizuku;

import android.app.Activity;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.android.internal.statusbar.IStatusBarService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import moe.shizuku.api.RemoteProcess;
import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.api.ShizukuClientHelper;
import moe.shizuku.api.ShizukuService;
import moe.yuuta.sysuicontroller.Main;
import moe.yuuta.sysuicontroller.R;
import moe.yuuta.sysuicontroller.core.CoreUtils;
import moe.yuuta.sysuicontroller.core.IController;

public class ShizukuController implements IController {
    private static final String TAG = "Controller_S";

    private static final int REQUEST_CODE_PERMISSION_V3 = 0x100;
    private static final int REQUEST_CODE_AUTHORIZATION_V3 = 0x300;

    private final Callback mStartCallback;
    private final Fragment mContext;

    private final IStatusBarService mService = new IStatusBarServiceShizuku();

    public ShizukuController(Callback mStartCallback, Fragment mContext) {
        this.mStartCallback = mStartCallback;
        this.mContext = mContext;
    }

    @Override
    public void startAsync() {
        if (!hasShizukuPermission()) {
            // if (mContext instanceof Activity && mContext.canStartActivityForResult()) {
                if (ShizukuClientHelper.isPreM()) {
                    Intent intent = ShizukuClientHelper.createPre23AuthorizationIntent(mContext.requireContext());
                    try {
                        mContext.startActivityForResult(intent, REQUEST_CODE_AUTHORIZATION_V3);
                    } catch (Throwable ignored) {
                    }
                } else {
                    mContext.requestPermissions(new String[]{ShizukuApiConstants.PERMISSION}, REQUEST_CODE_PERMISSION_V3);
                }
            // }
            return;
        }
        if (!enforceRoot()) {
            mStartCallback.update(null);
        }
    }

    private boolean enforceRoot() {
        if (!isUnderRoot()) {
            mStartCallback.update(new NonRootShizukuException(mContext.requireContext()));
            return true;
        }
        return false;
    }

    private boolean hasShizukuPermission() {
        if (ShizukuClientHelper.isPreM()) {
            return Main.isShizukuV3TokenValid();
        } else {
            return ActivityCompat.checkSelfPermission(mContext.requireContext(), ShizukuApiConstants.PERMISSION) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private boolean isUnderRoot() {
        try {
            // Check permission function doesn't function correctly
            return ShizukuService.getUid() == Process.ROOT_UID;
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to check whatever is under root", e);
            return false;
        }
    }

    @Override
    public boolean isServiceReady() {
        return hasShizukuPermission() && ShizukuService.pingBinder() && isUnderRoot();
    }

    @Override
    public void stopSync() {
        try {
            disable(StatusBarManager.DISABLE_NONE);
            disable2(StatusBarManager.DISABLE2_NONE);
        } catch (RemoteException ignored) {}
        mStartCallback.update(null);
    }

    @Override
    public void disable(int flags) throws RemoteException {
        mService.disable(flags, ShizukuService.getBinder(), "android");
    }

    @Override
    public void disable2(int flags) throws RemoteException {
        mService.disable2(flags, ShizukuService.getBinder(), "android");
    }

    @Override
    public int getDisableFlags() throws RemoteException {
        final String dumpRaw = dump();
        if (dumpRaw == null) return StatusBarManager.DISABLE_NONE;
        return CoreUtils.deserialize(dumpRaw).getDisable1();
    }

    @Nullable
    private String dump() throws RemoteException {
        RemoteProcess dumpProc = ShizukuService.newProcess(new String[]{
                        "/system/bin/dumpsys",
                        "statusbar"
                }, null, "/system/bin");
        final InputStream outStream = dumpProc.getInputStream();
        BufferedReader outReader = new BufferedReader(new InputStreamReader(outStream));
        try {
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = outReader.readLine()) != null) {
                builder.append(line);
                builder.append("\n");
            }
            final String output = builder.toString();
            int result;
            try {
                result = dumpProc.waitFor();
            } catch (InterruptedException ignored) {
                result = -100;
            }
            outReader.close();
            outStream.close();
            if (result != 0 || output.trim().equals("")) {
                Log.e(TAG, "Dump process exit with result " + result + ", has output: " + !output.trim().equals(""));
                return null;
            }
            return output;
        } catch (IOException e) {
            Log.e(TAG, "IOException during dump", e);
            return null;
        } finally {
            dumpProc.destroy();
        }
    }

    @Override
    public int getDisable2Flags() throws RemoteException {
        final String dumpRaw = dump();
        if (dumpRaw == null) return StatusBarManager.DISABLE2_NONE;
        return CoreUtils.deserialize(dumpRaw).getDisable2();
    }

    @Override
    public void restoreStatus(@Nullable Bundle savedInstanceState) {
    }

    @Override
    public void saveStatus(@NonNull Bundle savedInstanceState) {
    }

    @Override
    public void destroy() {}

    @Override
    public boolean canStop() {
        return false;
    }

    public static final class NonRootShizukuException extends Exception {
        private NonRootShizukuException(@NonNull Context context) {
            super(context.getString(R.string.shizuku_non_root));
        }
    }

    public boolean onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_V3: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (!enforceRoot()) {
                        mStartCallback.update(null);
                    }
                }
                return true;
            }
            default: {
                return false;
            }
        }
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // only called in API pre-23
            case REQUEST_CODE_AUTHORIZATION_V3: {
                if (resultCode == Activity.RESULT_OK) {
                    String token = ShizukuClientHelper.setPre23Token(data, mContext.requireContext());
                    if (ShizukuService.pingBinder()) {
                        try {
                            // each of your process need to call this
                            boolean valid = ShizukuService.setCurrentProcessTokenPre23(token);
                            Main.setV3TokenValid(valid);
                            if (!enforceRoot()) {
                                mStartCallback.update(null);
                            }
                        } catch (RemoteException e) {
                            mStartCallback.update(e);
                        }
                    }
                }
                return true;
            }
            default: {
                return false;
            }
        }
    }

    @Override
    public void revert() {
        try {
            disable(StatusBarManager.DISABLE_NONE);
            disable2(StatusBarManager.DISABLE2_NONE);
        } catch (RemoteException ignored) {}
    }
}
