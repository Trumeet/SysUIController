package moe.yuuta.sysuicontroller;

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
import android.widget.Toast;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import eu.chainfire.librootjava.RootIPCReceiver;
import eu.chainfire.librootjavadaemon.RootDaemon;
import eu.chainfire.libsuperuser.Shell;
import moe.shizuku.preference.Preference;
import moe.shizuku.preference.PreferenceCategory;
import moe.shizuku.preference.PreferenceFragment;
import moe.shizuku.preference.PreferenceGroup;
import moe.shizuku.preference.SwitchPreference;
import moe.yuuta.sysuicontroller.about.VersionDialogFragment;
import moe.yuuta.sysuicontroller.core.ControllerService;
import moe.yuuta.sysuicontroller.core.DisableItem;

import static moe.yuuta.sysuicontroller.Main.GLOBAL_TAG;

public class MainFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, Shell.OnCommandResultListener {
    private static final String TAG = GLOBAL_TAG + ".MainFragment";

    private static final String ARG_SERVICE = MainFragment.class.getName() + ".ARG_SERVICE";
    private static final int CODE_START = 1;

    private Shell.Interactive mShell;
    private volatile IStatusController mService;

    private Preference mStatusPreference;
    private SparseArray<SwitchPreference> mDisableMap;
    private SparseArray<SwitchPreference> mDisable2Map;
    private List<PreferenceGroup> mGroupsShouldBeDisabledBeforeServer = new ArrayList<>(3);

    private RootIPCReceiver<IStatusController> mReceiver = new RootIPCReceiver<IStatusController>(null, ControllerService.CODE_SERVICE) {
        @Override
        public void onConnect(IStatusController ipc) {
            Log.d(TAG, "Connected to remote service");
            mService = ipc;
            postStatusUpdate();
        }

        @Override
        public void onDisconnect(IStatusController ipc) {
            Log.d(TAG, "Disconnected to remote service");
            mService = ipc;
            postStatusUpdate();
        }
    };

    private void serializeDisableFlags () {
        if (obtainService() == null) return;
        SharedPreferences.Editor editor = requireContext().getSharedPreferences("flags", Context.MODE_PRIVATE)
                .edit();
        try {
            int disableFlags = mService.getDisableFlags();
            editor.putInt("disable_flags", disableFlags);
        } catch (RemoteException e) {
            Log.e(TAG, "Receive disable flags", e);
        }
        try {
            int disable2Flags = mService.getDisable2Flags();
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
            obtainService();
            boolean enable = mService != null;
            for (PreferenceGroup group : mGroupsShouldBeDisabledBeforeServer) {
                group.setEnabled(enable);
            }
            for (int i = 0; i < mDisableMap.size(); i ++) getPreferenceScreen().removePreference(mDisableMap.valueAt(i));
            for (int i = 0; i < mDisable2Map.size(); i ++) getPreferenceScreen().removePreference(mDisable2Map.valueAt(i));
            mDisable2Map.clear();
            mDisableMap.clear();
            if (enable) {
                try {
                    List<DisableItem> available = mService.getAvailableDisableItems();
                    Log.d(TAG, "Available disable items: " + available.toString());
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

                    // Disable not supported prebuilt preferences
                    Log.d(TAG, "Showed items: " + showedKeys.toString());
                    setNotAvailableBatch(showedKeys, getPreferenceScreen());
                } catch (Exception e) {
                    Log.e(TAG, "Read available disable items", e);
                }
                SharedPreferences preferences = requireContext().getSharedPreferences("flags", Context.MODE_PRIVATE);
                try {
                    mService.disable(preferences.getInt("disable_flags", mService.getDisableNoneFlag(false)));
                    updateUIDisabled(false, mService.getDisableFlags());
                } catch (RemoteException e) {
                    Log.e(TAG, "Receive disable flags", e);
                }

                try {
                    mService.disable2(preferences.getInt("disable2_flags", mService.getDisableNoneFlag(true)));
                    updateUIDisabled(true, mService.getDisableFlags());
                } catch (RemoteException e) {
                    Log.e(TAG, "Receive disable2 flags", e);
                }
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
        mReceiver.setContext(requireContext());
        mService = mReceiver.getIPC();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        runRestoreBinder(savedInstanceState);
    }

    private void runRestoreBinder (@Nullable Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            if (savedInstanceState == null) {
                Log.d(TAG, "savedInstanceState is null");
            } else {
                if (savedInstanceState.getBinder(ARG_SERVICE) == null)
                    Log.d(TAG, "savedInstanceState doesn't contain binder");
                else
                    Log.d(TAG, "Restoring");
            }
        }
        if (savedInstanceState != null && savedInstanceState.getBinder(ARG_SERVICE) != null) {
            // FIXME: 11/25/18 The restoried interface's binder not alive
            mService = IStatusController.Stub.asInterface(savedInstanceState.getBinder(ARG_SERVICE));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, 0, 0, com.google.android.gms.oss.licenses.R.string.oss_license_title);
        menu.add(0, 1, 0, R.string.about_view_in_play_store);
        menu.add(0, 2, 0, R.string.about_title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                startActivity(new Intent(getActivity(), OssLicensesMenuActivity.class));
                return true;
            case 1:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID))
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } catch (ActivityNotFoundException ignored) {}
                return true;
            case 2:
                new VersionDialogFragment()
                        .show(getChildFragmentManager(), "Version");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        mReceiver.release();
        // Shell will be closed immediately after executing.
        if (mShell != null) mShell.close();
        super.onDestroy();
    }

    @Override
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
        if (obtainService() == null) return false;
        Log.i(TAG, "runDisable: " + flag + " " + enable + " (" + name + ") " + disable2);
        try {
            int flags = disable2 ? mService.getDisable2Flags() : mService.getDisableFlags();
            if (enable) {
                flags |= flag;
            } else {
                flags ^= flag;
            }
            if (disable2) {
                mService.disable2(flags);
            } else {
                mService.disable(flags);
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
                boolean shouldStart = obtainService() == null;
                mStatusPreference.setEnabled(false);
                mStatusPreference.setSummary(shouldStart ? R.string.status_starting : R.string.status_stopping);
                if (shouldStart) {
                    mShell = new Shell.Builder()
                            .useSU()
                            .open(this); // Will start after shell opening
                } else {
                    try {
                        mService.exit();
                    } catch (RemoteException ignored) {
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCommandResult(int commandCode, int exitCode, List<String> output) {
        Log.i(TAG, "Result " + commandCode + ": " + exitCode);
        if (output != null) {
            Log.w(TAG, output.toString());
        }
        switch (commandCode) {
            case SHELL_RUNNING:
                if (exitCode != 0) {
                    Toast.makeText(getContext(), R.string.error_can_not_open_shell, Toast.LENGTH_LONG).show();
                    postStatusUpdate(); // Return status to not started
                    break;
                }
                mShell.addCommand(RootDaemon.getLaunchScript(requireContext(),
                        ControllerService.class,
                        null,
                        null,
                        null, BuildConfig.APPLICATION_ID + ":daemon"),
                        CODE_START, this);
                break;
            case CODE_START:
                // Kill process immediately
                mShell.kill();
                mShell.close();
                mShell = null;
                if (exitCode != 0) Toast.makeText(getContext(), R.string.error_can_not_start, Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (obtainService() != null)
            outState.putBinder(ARG_SERVICE, mService.asBinder());
    }

    private IStatusController obtainService () {
        if (!mReceiver.isConnected()) { // TODO: Add instance state support
            mService = null;
            return null;
        }
        mService = mReceiver.getIPC();
        return mService;
    }
}