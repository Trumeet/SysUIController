package moe.yuuta.sysuicontroller.core;

import android.app.StatusBarManager;
import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import moe.yuuta.sysuicontroller.BuildConfig;
import moe.yuuta.sysuicontroller.dump.StatusBarServiceDumpDeserializer;

public class CoreUtils {
    private static final String TAG = "CU";
    public static List<DisableItem> getAvailableDisableItems() {
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

    @NonNull
    public static StatusBarServiceDumpDeserializer deserialize (@NonNull final String result) {
        StatusBarServiceDumpDeserializer deserializer = new StatusBarServiceDumpDeserializer();
        try {
            if (BuildConfig.DEBUG) Log.d(TAG, "Result: " + result);
            deserializer.deserialize(result);
        } catch (Exception e) {
            Log.e(TAG, "Error when deserialize status bar service", e);
        }
        return deserializer;
    }
}
