package moe.yuuta.sysuicontroller.core;

import android.annotation.SuppressLint;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import eu.chainfire.librootjava.IPCBroadcastHelper;
import eu.chainfire.librootjava.RootIPC;
import eu.chainfire.librootjava.RootJava;
import eu.chainfire.librootjavadaemon.RootDaemon;
import moe.yuuta.sysuicontroller.BuildConfig;
import moe.yuuta.sysuicontroller.IStatusController;

import static moe.yuuta.sysuicontroller.Main.GLOBAL_TAG;

public class ControllerService extends IStatusController.Stub {
    private static final String TAG = GLOBAL_TAG + ".ControllerService";

    private int disableFlags = StatusBarManager.DISABLE_NONE;
    private int disable2Flags = StatusBarManager.DISABLE2_NONE;

    public static final int CODE_SERVICE = 0;
    private StatusBarManager mManager;
    private Context mContext;
    private Intent mKeepWakeUpIntent;

    public static void main (String... args) throws Throwable {
        new ControllerService().run(args);
    }

    @SuppressLint({"WrongConstant", "MissingPermission"})
    private void run (String... args) throws Throwable {
        Looper.prepare();
        mContext = RootJava.getSystemContext();
        mManager = (StatusBarManager) mContext.getSystemService("statusbar");
        RootDaemon.daemonize(BuildConfig.APPLICATION_ID, CODE_SERVICE);
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
        mContext.removeStickyBroadcast(mKeepWakeUpIntent);
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
    public void setIcon(String slot, int iconId, int iconLevel, String contentDescription) throws RemoteException {
        enforcePermission();
        mManager.setIcon(slot, iconId, iconLevel, contentDescription);
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
        return disableFlags;
    }

    @Override
    public int getDisable2Flags() throws RemoteException {
        enforcePermission();
        return disable2Flags;
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
