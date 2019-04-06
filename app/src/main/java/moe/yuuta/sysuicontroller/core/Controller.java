package moe.yuuta.sysuicontroller.core;

import android.os.Bundle;
import android.os.RemoteException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * User-friendly controller
 */
public final class Controller<T extends IController> implements IController {
    private final T mTargetController;

    public Controller(T mTargetController) {
        this.mTargetController = mTargetController;
    }

    @Override
    public void startAsync() {
        mTargetController.startAsync();
    }

    @Override
    public boolean isServiceReady() {
        return mTargetController.isServiceReady();
    }

    @Override
    public void disable(int flags) throws RemoteException {
        mTargetController.disable(flags);
    }

    @Override
    public void disable2(int flags) throws RemoteException {
        mTargetController.disable2(flags);
    }

    @Override
    public int getDisableFlags() throws RemoteException {
        return mTargetController.getDisableFlags();
    }

    @Override
    public int getDisable2Flags() throws RemoteException {
        return mTargetController.getDisable2Flags();
    }

    @Override
    public void restoreStatus(@Nullable Bundle savedInstanceState) {
        if (isServiceReady()) mTargetController.restoreStatus(savedInstanceState);
    }

    @Override
    public void saveStatus(@NonNull Bundle savedInstanceState) {
        if (isServiceReady()) mTargetController.saveStatus(savedInstanceState);
    }

    @Override
    public void destroy() {
        if (isServiceReady()) mTargetController.destroy();
    }

    @Override
    public void stopSync() {
        if (isServiceReady()) mTargetController.stopSync();
    }

    @Override
    public boolean canStop() {
        return mTargetController.canStop();
    }

    @Override
    public void revert() {
        mTargetController.revert();
    }

    public T controller() {
        return mTargetController;
    }
}
