package moe.yuuta.sysuicontroller.core.shizuku;

import android.content.ComponentName;
import android.graphics.Rect;
import android.hardware.biometrics.IBiometricPromptReceiver;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import com.android.internal.statusbar.IStatusBar;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.internal.statusbar.StatusBarIcon;

import java.util.List;

import moe.shizuku.api.ShizukuApiConstants;
import moe.shizuku.api.ShizukuService;
import moe.shizuku.api.SystemServiceHelper;

class IStatusBarServiceShizuku implements IStatusBarService {
    private Parcel obtainData(String method) {
        Parcel data = Parcel.obtain();
        data.writeInterfaceToken(ShizukuApiConstants.BINDER_DESCRIPTOR);
        data.writeStrongBinder(SystemServiceHelper.getSystemService("statusbar"));
        data.writeInt(SystemServiceHelper.getTransactionCode(IStatusBarService.Stub.class.getName(),
                method));
        data.writeInterfaceToken(IStatusBarService.class.getName());
        return data;
    }

    private Parcel transact(Parcel data) throws RemoteException {
        Parcel reply = Parcel.obtain();

        try {
            ShizukuService.transactRemote(data, reply, 0);
            reply.readException();
            return reply;
        } finally {
            data.recycle();
        }
    }

    @Override
    public IBinder asBinder() {
        return null;
    }

    @Override
    public void expandNotificationsPanel() throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void collapsePanels() throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void togglePanel() throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void disable(int what, IBinder token, String pkg) throws RemoteException {
        Parcel data = obtainData("disable");
        data.writeInt(what);
        data.writeStrongBinder(token);
        data.writeString(pkg);
        transact(data).recycle();
    }

    @Override
    public void disableForUser(int what, IBinder token, String pkg, int userId) throws RemoteException {
        // TODO
    }

    @Override
    public void disable2(int what, IBinder token, String pkg) throws RemoteException {
        Parcel data = obtainData("disable2");
        data.writeInt(what);
        data.writeStrongBinder(token);
        data.writeString(pkg);
        transact(data).recycle();
    }

    @Override
    public void disable2ForUser(int what, IBinder token, String pkg, int userId) throws RemoteException {
        // TODO
    }

    @Override
    public void setIcon(String slot, String iconPackage, int iconId, int iconLevel, String contentDescription) throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setIconVisibility(String slot, boolean visible) throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeIcon(String slot) throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setImeWindowStatus(IBinder token, int vis, int backDisposition, boolean showImeSwitcher) throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void expandSettingsPanel(String subPanel) throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void registerStatusBar(IStatusBar callbacks, List<String> iconSlots, List<StatusBarIcon> iconList, int[] switches, List<IBinder> binders, Rect fullscreenStackBounds, Rect dockedStackBounds) throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onPanelRevealed(boolean clearNotificationEffects, int numItems) throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onPanelHidden() throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearNotificationEffects() throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onNotificationClick(String key, NotificationVisibility nv) throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onNotificationActionClick(String key, int actionIndex, NotificationVisibility nv) throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onNotificationError(String pkg, String tag, int id, int uid, int initialPid, String message, int userId) throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onClearAllNotifications(int userId) throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onNotificationClear(String pkg, String tag, int id, int userId, String key, int dismissalSurface, NotificationVisibility nv) throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onNotificationVisibilityChanged(NotificationVisibility[] newlyVisibleKeys, NotificationVisibility[] noLongerVisibleKeys) throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onNotificationExpansionChanged(String key, boolean userAction, boolean expanded) throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onNotificationDirectReplied(String key) throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onNotificationSmartRepliesAdded(String key, int replyCount) throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onNotificationSmartReplySent(String key, int replyIndex) throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onNotificationSettingsViewed(String key) throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSystemUiVisibility(int vis, int mask, String cause) throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onGlobalActionsShown() throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onGlobalActionsHidden() throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void shutdown() throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reboot(boolean safeMode) throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addTile(ComponentName tile) throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remTile(ComponentName tile) throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clickTile(ComponentName tile) throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void handleSystemKey(int key) throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void showPinningEnterExitToast(boolean entering) throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void showPinningEscapeToast() throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void showFingerprintDialog(Bundle bundle, IBiometricPromptReceiver receiver) throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onFingerprintAuthenticated() throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onFingerprintHelp(String message) throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onFingerprintError(String error) throws RemoteException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void hideFingerprintDialog() throws RemoteException {
        throw new UnsupportedOperationException();
    }
}
