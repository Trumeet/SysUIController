package eu.chainfire.librootjava;

import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import androidx.annotation.NonNull;

/**
 * A helper to create custom broadcasts
 * Should be synced with {@link eu.chainfire.librootjava.RootIPC}.
 */
public class IPCBroadcastHelper {
    public static Intent buildStickyBroadcastIntent (@NonNull RootIPC rootIPC) throws NoSuchFieldException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // RootIPC.java part
        Field fIBinder = rootIPC.getClass().getDeclaredField("binder");
        Field fCode = rootIPC.getClass().getDeclaredField("code");
        Field fPackageName = rootIPC.getClass().getDeclaredField("packageName");
        fIBinder.setAccessible(true);
        fCode.setAccessible(true);
        fPackageName.setAccessible(true);
        Intent intent = new Intent();
        intent.setPackage((String) fPackageName.get(rootIPC));
        intent.setAction(RootIPCReceiver.BROADCAST_ACTION);

        Bundle bundle = new Bundle();
        bundle.putBinder(RootIPCReceiver.BROADCAST_BINDER, (IBinder) fIBinder.get(rootIPC));
        bundle.putInt(RootIPCReceiver.BROADCAST_CODE, fCode.getInt(rootIPC));
        intent.putExtra(RootIPCReceiver.BROADCAST_EXTRA, bundle);

        // Reflection.java part
        Method mGetFlagReceiverFromShell = Reflection.class.getDeclaredMethod("getFlagReceiverFromShell");
        mGetFlagReceiverFromShell.setAccessible(true);
        intent.setFlags((int) mGetFlagReceiverFromShell.invoke(null));

        return intent;
    }
}
