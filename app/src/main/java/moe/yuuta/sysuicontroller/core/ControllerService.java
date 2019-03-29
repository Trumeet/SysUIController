package moe.yuuta.sysuicontroller.core;

import android.annotation.SuppressLint;
import android.app.ActivityThread;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import com.android.internal.statusbar.IStatusBarService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import eu.chainfire.librootjava.IPCBroadcastHelper;
import eu.chainfire.librootjava.RootIPC;
import eu.chainfire.librootjava.RootJava;
import eu.chainfire.librootjavadaemon.RootDaemon;
import eu.chainfire.libsuperuser.Shell;
import moe.yuuta.sysuicontroller.BuildConfig;
import moe.yuuta.sysuicontroller.IStatusController;
import moe.yuuta.sysuicontroller.dump.StatusBarServiceDumpDeserializer;

import static moe.yuuta.sysuicontroller.Main.GLOBAL_TAG;

public class ControllerService extends IStatusController.Stub {
    private static final String TAG = GLOBAL_TAG + ".ControllerService";

    private int disableFlags = StatusBarManager.DISABLE_NONE;
    private int disable2Flags = StatusBarManager.DISABLE2_NONE;

    public static final int CODE_SERVICE = 0;
    private StatusBarManager mManager;
    private Context mContext;
    private Intent mKeepWakeUpIntent;
    private IStatusBarService mService;

    public static void main (String... args) throws Throwable {
        try {
            new ControllerService().run(args);
        } catch (Throwable throwable) {
            Log.e(TAG, "FETAL EXCEPTION during init", throwable);
            System.exit(throwable.hashCode());
        }
    }

    @SuppressLint({"WrongConstant", "MissingPermission"})
    private void run (String... args) throws Throwable {
        Looper.prepare();
        Log.i(TAG, "Version: " + BuildConfig.VERSION_CODE);
        mContext = ActivityThread.systemMain().getSystemContext();
        mManager = (StatusBarManager) mContext.getSystemService("statusbar");
        @SuppressLint("PrivateApi") Method mGetService = StatusBarManager.class.getDeclaredMethod("getService");
        mGetService.setAccessible(true);
        mService = (IStatusBarService) mGetService.invoke(mManager);
        RootDaemon.daemonize(BuildConfig.APPLICATION_ID, CODE_SERVICE, false, null);
        RootJava.restoreOriginalLdLibraryPath();
        RootDaemon.register(BuildConfig.APPLICATION_ID, this, CODE_SERVICE);
        Log.i(TAG, "Started at " + new Date().toString());
        RootIPC rootIPC;
        try {
            rootIPC = new RootIPC(BuildConfig.APPLICATION_ID, this, CODE_SERVICE,
                    10 * 1000,
                    true);
        } catch (RootIPC.TimeoutException e) {
            Log.e(TAG, "Unable to establish a connection, exiting", e);
            throw e;
        }
        mKeepWakeUpIntent = IPCBroadcastHelper.buildStickyBroadcastIntent(rootIPC);
        mContext.sendStickyBroadcast(mKeepWakeUpIntent);
        RootDaemon.run();
        // Will be removed from exit() binder call, because this part may not be called.
        Log.i(TAG, "Stopped at " + new Date().toString());
    }

    private void enforcePermission () throws SecurityException {
        mContext.enforcePermission(BuildConfig.APPLICATION_ID + ".SERVICE",
                Binder.getCallingPid(), Binder.getCallingUid(), "Permission denial");
    }

    @SuppressLint("MissingPermission")
    @Override // Binder call
    public void exit () throws RemoteException {
        enforcePermission();
        try {
            if (mKeepWakeUpIntent != null) mContext.removeStickyBroadcast(mKeepWakeUpIntent);
        } catch (Throwable e) {
            Log.e(TAG, "Unable to remove sticky broadcast", e);
        }
        RootDaemon.exit();
    }

    @Override
    public void disable(int flags) throws RemoteException {
        enforcePermission();
        disableFlags = flags;
        mManager.disable(flags);
    }

    @Override
    public void disable2(int flags) throws RemoteException {
        enforcePermission();
        disable2Flags = flags;
        mManager.disable2(flags);
    }

    @Override
    public void expandNotificationsPanel() throws RemoteException {
        enforcePermission();
        mManager.expandNotificationsPanel();
    }

    @Override
    public void collapsePanels() throws RemoteException {
        enforcePermission();
        mManager.collapsePanels();
    }

    @Override
    public void expandSettingsPanel(String subPanel) throws RemoteException {
        enforcePermission();
        mManager.expandSettingsPanel(subPanel);
    }

    @Override
    public void setIcon(String slot, String packageName, int iconId, int iconLevel, String contentDescription) throws RemoteException {
        enforcePermission();
        mService.setIcon(slot, packageName, iconId, iconLevel, contentDescription);
    }

    @Override
    public void removeIcon(String slot) throws RemoteException {
        enforcePermission();
        mManager.removeIcon(slot);
    }

    @Override
    public void setIconVisibility(String slot, boolean visible) throws RemoteException {
        enforcePermission();
        mManager.setIconVisibility(slot, visible);
    }

    @Override
    public int getDisableFlags() throws RemoteException {
        enforcePermission();
        // Deserialize at first
        deserialize();
        return disableFlags;
    }

    @Override
    public int getDisable2Flags() throws RemoteException {
        enforcePermission();
        // Deserialize at first
        deserialize();
        return disable2Flags;
    }

    private void deserialize () {
        StatusBarServiceDumpDeserializer deserializer = new StatusBarServiceDumpDeserializer();
        try {
            StringBuilder builder = new StringBuilder();
            for (String res : Shell.SH.run(Collections.singletonList("dumpsys statusbar"))) {
                builder.append(res);
                builder.append("\n");
            }
            String result = builder.toString();
            if (BuildConfig.DEBUG) Log.d(TAG, "Result: " + result);
            deserializer.deserialize(result);
            if (deserializer.getDisable1() != -1) disableFlags = deserializer.getDisable1();
            if (deserializer.getDisable2() != -1) disable2Flags = deserializer.getDisable2();
        } catch (Exception e) {
            Log.e(TAG, "Error when deserialize status bar service", e);
        }
    }

    @Override
    public List<DisableItem> getAvailableDisableItems() throws RemoteException {
        Field[] fields = StatusBarManager.class.getDeclaredFields();
        if (fields.length <= 0) return Collections.emptyList();
        List<DisableItem> items = new ArrayList<>(fields.length);
        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) continue;
            if (!Modifier.isFinal(field.getModifiers())) continue;
            if (!Modifier.isPublic(field.getModifiers())) continue;
            if (field.getName().equals("DISABLE_MASK") || field.getName().equals("DISABLE2_MASK")
            || field.getName().equals("DISABLE_NONE") || field.getName().equals("DISABLE2_NONE")) continue;
            try {
                if (field.getName().startsWith("DISABLE_")) {
                    items.add(new DisableItem(field.getInt(null), field.getName(), false));
                    continue;
                }
                if (field.getName().startsWith("DISABLE2_")) {
                    items.add(new DisableItem(field.getInt(null), field.getName(), true));
                }
            } catch (IllegalAccessException e) {
                Log.e(TAG, "Unable to access value: " + field.getName(), e);
            }
        }
        return items;
    }

    @Override
    public int getDisableNoneFlag(boolean disable2) throws RemoteException {
        return disable2 ? StatusBarManager.DISABLE2_NONE : StatusBarManager.DISABLE_NONE;
    }
}
