package moe.yuuta.sysuicontroller.core;

import android.os.Bundle;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface IController {
    /**
     * Start & init & request permissions. This won't be called if {@link #isServiceReady()} returns true
     */
    void startAsync();

    /**
     * Revert the settings only and not destroy the service
     */
    void stopSync();

    /**
     * Revert the settings only, it will be called only if #canStop() returns false.
     */
    void revert();

    boolean canStop();

    boolean isServiceReady();
    void disable(int flags) throws RemoteException;
    void disable2(int flags) throws RemoteException;
    int getDisableFlags() throws RemoteException;
    int getDisable2Flags() throws RemoteException;

    void restoreStatus(@Nullable Bundle savedInstanceState);
    void saveStatus(@NonNull Bundle savedInstanceState);

    /**
     * This will be called either is ready or not. Destroy the service only and not revert settings
     */
    void destroy();

    interface Callback {
        void update(@Nullable Throwable e);
    }
}
