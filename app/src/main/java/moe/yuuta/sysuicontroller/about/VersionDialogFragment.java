package moe.yuuta.sysuicontroller.about;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import moe.yuuta.sysuicontroller.BuildConfig;
import moe.yuuta.sysuicontroller.R;

public class VersionDialogFragment extends DialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        ((TextView) view.findViewById(R.id.text_version)).setText(getString(R.string.about_version, BuildConfig.VERSION_NAME));
        return view;
    }
}
