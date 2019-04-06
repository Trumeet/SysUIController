package moe.yuuta.sysuicontroller;

import android.annotation.SuppressLint;
import android.app.StatusBarManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.AddTrace;
import com.google.firebase.perf.metrics.Trace;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import moe.shizuku.preference.Preference;
import moe.shizuku.preference.PreferenceCategory;
import moe.shizuku.preference.PreferenceFragment;
import moe.shizuku.preference.PreferenceGroup;
import moe.shizuku.preference.SwitchPreference;
import moe.yuuta.sysuicontroller.about.VersionDialogFragment;
import moe.yuuta.sysuicontroller.core.Controller;
import moe.yuuta.sysuicontroller.core.ControllerFactory;
import moe.yuuta.sysuicontroller.core.CoreUtils;
import moe.yuuta.sysuicontroller.core.DisableItem;
import moe.yuuta.sysuicontroller.core.IController;
import moe.yuuta.sysuicontroller.core.shizuku.ShizukuController;

import static moe.yuuta.sysuicontroller.Main.GLOBAL_TAG;

public class MainFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, IController.Callback {
    private static final String TAG = GLOBAL_TAG + ".MainFragment";

    private Preference mStatusPreference;
    private SparseArray<SwitchPreference> mDisableMap;
    private SparseArray<SwitchPreference> mDisable2Map;
    private List<PreferenceGroup> mGroupsShouldBeDisabledBeforeServer = new ArrayList<>(3);
    private Controller mController;
    // When it's true, the activity will be automatically recreated after the next successful status update.
    private volatile boolean mIsSwitchModeScheduled = false;

    private void serializeDisableFlags () {
        if (!mController.isServiceReady()) return;
        SharedPreferences.Editor editor = requireContext().getSharedPreferences("flags", Context.MODE_PRIVATE)
                .edit();
        try {
            int disableFlags = mController.getDisableFlags();
            editor.putInt("disable_flags", disableFlags);
        } catch (RemoteException e) {
            Log.e(TAG, "Receive disable flags", e);
        }
        try {
            int disable2Flags = mController.getDisable2Flags();
            editor.putInt("disable2_flags", disable2Flags);
        } catch (RemoteException e) {
            Log.e(TAG, "Receive disable2 flags", e);
        }
        editor.apply();
    }

    private void setNotAvailableBatch (@NonNull Set<String> showedKeys, @NonNull PreferenceGroup root) {
        for (int i = 0; i < root.getPreferenceCount(); i ++) {
            Preference preference = root.getPreference(i);
            if (BuildConfig.DEBUG) Log.d(TAG, "Pref " + preference.getKey() + "(" + preference.getClass().getSimpleName() + ")");
            if (preference instanceof PreferenceGroup) {
                setNotAvailableBatch(showedKeys, (PreferenceGroup) preference);
                continue;
            }
            if (!(preference instanceof SwitchPreference)) continue;
            if (showedKeys.contains(preference.getKey())) continue;
            Log.w(TAG, "Key not available for this device: " + preference.getKey());
            setNotAvailable(preference);
        }
    }

    private void postStatusUpdate () {
        new Handler(Looper.getMainLooper()).post(() -> {
            Log.d(TAG, "Start update status");
            boolean enable = mController.isServiceReady();
            if (enable && !mController.canStop()) {
                mStatusPreference.setVisible(false);
            }
            for (PreferenceGroup group : mGroupsShouldBeDisabledBeforeServer) {
                group.setEnabled(enable);
            }
            Trace traceClear = FirebasePerformance.getInstance().newTrace("traceClear");
            traceClear.start();
            traceClear.putMetric("disable_map_size", mDisableMap.size());
            traceClear.putMetric("disable2_map_size", mDisable2Map.size());
            for (int i = 0; i < mDisableMap.size(); i ++) getPreferenceScreen().removePreference(mDisableMap.valueAt(i));
            for (int i = 0; i < mDisable2Map.size(); i ++) getPreferenceScreen().removePreference(mDisable2Map.valueAt(i));
            mDisable2Map.clear();
            mDisableMap.clear();
            traceClear.stop();
            if (enable) {
                List<DisableItem> available = CoreUtils.getAvailableDisableItems();
                Log.d(TAG, "Available disable items: " + available.toString());
                Trace traceDisplayAvailableItems = FirebasePerformance.getInstance().newTrace("traceDisplayAvailableItems");
                traceDisplayAvailableItems.start();
                traceDisplayAvailableItems.putMetric("available_item_size", available.size());
                Set<String /* Name (key) */> showedKeys = new HashSet<>(available.size());
                for (DisableItem item : available) {
                    showedKeys.add(item.getKey().toLowerCase());
                    SwitchPreference preference = (SwitchPreference) findPreference(item.getKey().toLowerCase());
                    if (preference == null) {
                        // Create a new one for special keys
                        preference = new SwitchPreference(requireContext(), null, moe.shizuku.preference.R.attr.switchPreferenceStyle,
                                R.style.Preference_SwitchPreference);
                        preference.setKey(item.getKey().toLowerCase());
                        preference.setTitle(item.getKey());
                        preference.setSummary(R.string.additional_key);
                        ((PreferenceCategory) findPreference("key_other")).addPreference(preference);
                    }
                    preference.setOnPreferenceChangeListener((p, newValue) -> runDisable(item.getFlag(), (Boolean) newValue, item.getKey(), item.isDisable2()));
                    if (item.isDisable2()) mDisable2Map.put(item.getFlag(), preference);
                    mDisableMap.put(item.getFlag(), preference);
                }
                traceDisplayAvailableItems.stop();

                // Disable not supported prebuilt preferences
                Trace traceSetNotAvailableBatch = FirebasePerformance.getInstance().newTrace("traceSetNotAvailableBatch");
                traceSetNotAvailableBatch.start();
                traceSetNotAvailableBatch.putMetric("showed_keys", showedKeys.size());
                Log.d(TAG, "Showed items: " + showedKeys.toString());
                setNotAvailableBatch(showedKeys, getPreferenceScreen());
                traceSetNotAvailableBatch.stop();
                SharedPreferences preferences = requireContext().getSharedPreferences("flags", Context.MODE_PRIVATE);
                Trace traceUpdateUIDisabled = FirebasePerformance.getInstance().newTrace("traceUpdateUIDisabled");
                traceUpdateUIDisabled.start();
                try {
                    mController.disable(preferences.getInt("disable_flags", StatusBarManager.DISABLE_NONE));
                    updateUIDisabled(false, mController.getDisableFlags());
                } catch (RemoteException e) {
                    Log.e(TAG, "Receive disable flags", e);
                }

                try {
                    mController.disable2(preferences.getInt("disable2_flags", StatusBarManager.DISABLE2_NONE));
                    updateUIDisabled(true, mController.getDisableFlags());
                } catch (RemoteException e) {
                    Log.e(TAG, "Receive disable2 flags", e);
                }
                traceUpdateUIDisabled.stop();
            }

            if (mStatusPreference != null) {
                mStatusPreference.setEnabled(true);
                mStatusPreference.setSummary(enable
                        ? R.string.status_started : R.string.main_service_status_summary);
            }
        });
    }

    private void updateUIDisabled(boolean disable2, int flags) {
        SparseArray<SwitchPreference> map =
                disable2 ? mDisable2Map : mDisableMap;
        for (int i = 0; i < map.size(); i ++) {
            int flag = map.keyAt(i);
            SwitchPreference preference = map.get(flag);
            if (preference == null) // Not supported
                continue;
            preference.setChecked((flags & flag) != 0);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mController = ControllerFactory.create(this, this);
        postStatusUpdate();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mController.restoreStatus(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        List<String> availableModes = ControllerFactory.getSupportedControllers(requireContext());
        if (!availableModes.contains(ControllerFactory.ID_ROOT)) menu.findItem(R.id.action_mode_root).setEnabled(false);
        if (!availableModes.contains(ControllerFactory.ID_SHIZUKU)) menu.findItem(R.id.action_mode_shizuku).setEnabled(false);
        switch (ControllerFactory.getUserChoice(this)) {
            case ControllerFactory.ID_ROOT:
                menu.findItem(R.id.action_mode_root).setChecked(true);
                break;
            case ControllerFactory.ID_SHIZUKU:
                menu.findItem(R.id.action_mode_shizuku).setChecked(true);
                break;
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_oss:
                startActivity(new Intent(getActivity(), OssLicensesMenuActivity.class));
                return true;
            case R.id.action_view:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Trumeet/SysUIController_Releases"))
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } catch (ActivityNotFoundException ignored) {}
                return true;
            case R.id.action_version:
                new VersionDialogFragment()
                        .show(getChildFragmentManager(), "Version");
                return true;
            case R.id.action_mode_shizuku:
                ControllerFactory.set(requireContext(), ControllerFactory.ID_SHIZUKU);
                mIsSwitchModeScheduled = true;
                mStatusPreference.performClick();
                return true;
            case R.id.action_mode_root:
                ControllerFactory.set(requireContext(), ControllerFactory.ID_ROOT);
                mIsSwitchModeScheduled = true;
                mStatusPreference.performClick();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        mController.destroy();
        super.onDestroy();
    }

    @Override
    @AddTrace(name = "MainFragment#onCreatePreferences")
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.main_settings, rootKey);
        mStatusPreference = findPreference("key_service_status");
        mStatusPreference.setOnPreferenceClickListener(this);
        mGroupsShouldBeDisabledBeforeServer.add((PreferenceCategory) findPreference("key_status_bar"));
        mGroupsShouldBeDisabledBeforeServer.add((PreferenceCategory) findPreference("key_notification"));
        mGroupsShouldBeDisabledBeforeServer.add((PreferenceCategory) findPreference("key_navigation_bar"));
        mGroupsShouldBeDisabledBeforeServer.add((PreferenceCategory) findPreference("key_quick_settings"));
        mGroupsShouldBeDisabledBeforeServer.add((PreferenceCategory) findPreference("key_other"));
        mDisable2Map = new SparseArray<>(5);
        mDisableMap = new SparseArray<>(7);
    }

    private boolean runDisable (int flag, boolean enable, String name, boolean disable2) {
        if (!mController.isServiceReady()) return false;
        Log.i(TAG, "runDisable: " + flag + " " + enable + " (" + name + ") " + disable2);
        try {
            int flags = disable2 ? mController.getDisable2Flags() : mController.getDisableFlags();
            if (enable) {
                flags |= flag;
            } else {
                flags ^= flag;
            }
            if (disable2) {
                mController.disable2(flags);
            } else {
                mController.disable(flags);
            }
            serializeDisableFlags();
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "disable " + name, e);
            return false;
        }
    }

    private void setNotAvailable(@NonNull Preference preference) {
        preference.setEnabled(false);
        preference.setSummary(R.string.not_available);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case "key_service_status": {
                if (mController.canStop()) {
                    boolean shouldStart = !mController.isServiceReady();
                    Log.d(TAG, "sS: " + shouldStart);
                    mStatusPreference.setEnabled(false);
                    mStatusPreference.setSummary(shouldStart ? R.string.status_starting : R.string.status_stopping);
                    if (shouldStart) {
                        mController.startAsync();
                    } else {
                        mController.stopSync();
                    }
                } else if (mIsSwitchModeScheduled) {
                    // Revert only if the user is switching mode and the current mode doesn't support stopping.
                    mController.revert();
                    update(null);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mController.saveStatus(outState);
    }

    @Override
    public void update(@Nullable Throwable e) {
        postStatusUpdate();
        if (e != null) {
            new AlertDialog.Builder(requireContext())
                    .setMessage(e.getClass().getName() + ": " + e.getMessage())
                    .setPositiveButton(android.R.string.ok, null)
                    .setCancelable(false)
                    .show();
        } else {
            if (mIsSwitchModeScheduled) {
                requireActivity().recreate();
                mIsSwitchModeScheduled = false;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        if (mController != null && mController.controller() instanceof ShizukuController) {
            if (((ShizukuController) mController.controller()).onActivityResult(requestCode, resultCode, data))
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        if (mController != null && mController.controller() instanceof ShizukuController) {
            if (((ShizukuController) mController.controller()).onRequestPermissionsResult(requestCode, permissions, grantResults))
                return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}