package com.android.internal.statusbar;

public interface IStatusBarService {
    void setIcon(String slot, String packageName, int iconId, int iconLevel, String contentDescription);
}
