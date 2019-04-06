package moe.yuuta.sysuicontroller.core;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import moe.shizuku.api.ShizukuClientHelper;
import moe.yuuta.sysuicontroller.core.root.RootController;
import moe.yuuta.sysuicontroller.core.shizuku.ShizukuController;

public final class ControllerFactory {
    public static final String ID_SHIZUKU = "shizuku";
    public static final String ID_ROOT = "root";

    public static final String[] ALL_CONTROLLERS = new String[] {
            ID_SHIZUKU,
            ID_ROOT
    };

    private static final String FALLBACK = ID_ROOT;

    public static void set(@NonNull Context context, @NonNull String id) {
        context.getSharedPreferences("mode", Context.MODE_PRIVATE)
                .edit()
                .putString("mode", id)
                .apply();
    }

    @NonNull
    public static String getUserChoice(@NonNull Fragment context) {
        List<String> available = getSupportedControllers(context.requireContext());
        String userChoice = context.requireContext().getSharedPreferences("mode", Context.MODE_PRIVATE)
                .getString("mode", FALLBACK);
        if (!available.contains(userChoice)) {
            userChoice = FALLBACK;
        }
        return userChoice;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public static Controller create(@NonNull Fragment context, @NonNull IController.Callback callback) {
        switch (getUserChoice(context)) {
            case ID_SHIZUKU:
                return new Controller(new ShizukuController(callback, context));
            case ID_ROOT:
                return new Controller(new RootController(callback, context));
            default:
                return new Controller(new RootController(callback, context));
        }
    }

    @NonNull
    public static List<String> getSupportedControllers(@NonNull Context context) {
        List<String> available = new ArrayList<>(2);
        if (ShizukuClientHelper.isManagerV3Installed(context)) {
            available.add(ID_SHIZUKU);
        }
        available.add(ID_ROOT);
        return available;
    }
}
