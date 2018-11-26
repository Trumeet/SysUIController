/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.app;

/**
 * Allows an app to control the status bar.
 *
 * @hide
 */
public class StatusBarManager {

    public static int DISABLE_EXPAND;
    public static int DISABLE_NOTIFICATION_ICONS;
    public static int DISABLE_NOTIFICATION_ALERTS;
    @Deprecated
    public static int DISABLE_NOTIFICATION_TICKER;
    public static int DISABLE_SYSTEM_INFO;
    public static int DISABLE_HOME;
    public static int DISABLE_RECENT;
    public static int DISABLE_BACK;
    public static int DISABLE_CLOCK;
    public static int DISABLE_SEARCH;

    @Deprecated
    public static int DISABLE_NAVIGATION;

    public static int DISABLE_NONE = 0x00000000;

    public static int DISABLE_MASK = DISABLE_EXPAND | DISABLE_NOTIFICATION_ICONS
            | DISABLE_NOTIFICATION_ALERTS | DISABLE_NOTIFICATION_TICKER
            | DISABLE_SYSTEM_INFO | DISABLE_RECENT | DISABLE_HOME | DISABLE_BACK | DISABLE_CLOCK
            | DISABLE_SEARCH;

    /**
     * Flag to disable quick settings.
     *
     * Setting this flag disables quick settings completely, but does not disable expanding the
     * notification shade.
     */
    public static int DISABLE2_QUICK_SETTINGS = 1;
    public static int DISABLE2_SYSTEM_ICONS = 1 << 1;
    public static int DISABLE2_NOTIFICATION_SHADE = 1 << 2;
    public static int DISABLE2_GLOBAL_ACTIONS = 1 << 3;
    public static int DISABLE2_ROTATE_SUGGESTIONS = 1 << 4;

    public static int DISABLE2_NONE = 0x00000000;

    public static int DISABLE2_MASK = DISABLE2_QUICK_SETTINGS | DISABLE2_SYSTEM_ICONS
            | DISABLE2_NOTIFICATION_SHADE | DISABLE2_GLOBAL_ACTIONS | DISABLE2_ROTATE_SUGGESTIONS;

    public static int NAVIGATION_HINT_BACK_ALT;
    public static int NAVIGATION_HINT_IME_SHOWN;

    public static int WINDOW_STATUS_BAR;
    public static int WINDOW_NAVIGATION_BAR;

    public static int WINDOW_STATE_SHOWING;
    public static int WINDOW_STATE_HIDING;
    public static int WINDOW_STATE_HIDDEN;

    public static int CAMERA_LAUNCH_SOURCE_WIGGLE;
    public static int CAMERA_LAUNCH_SOURCE_POWER_DOUBLE_TAP;
    public static int CAMERA_LAUNCH_SOURCE_LIFT_TRIGGER;

    /**
     * Disable some features in the status bar.  Pass the bitwise-or of the DISABLE_* flags.
     * To re-enable everything, pass {@link #DISABLE_NONE}.
     */
    public void disable(int what) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Disable additional status bar features. Pass the bitwise-or of the DISABLE2_* flags.
     * To re-enable everything, pass {@link #DISABLE_NONE}.
     *
     * Warning: Only pass DISABLE2_* flags into this function, do not use DISABLE_* flags.
     */
    public void disable2(int what) {
        throw new RuntimeException("Stub!");
    }

    /**
     * Expand the notifications panel.
     */
    public void expandNotificationsPanel() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Collapse the notifications and settings panels.
     */
    public void collapsePanels() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Expand the settings panel.
     */
    public void expandSettingsPanel() {
        expandSettingsPanel(null);
    }

    /**
     * Expand the settings panel and open a subPanel, pass null to just open the settings panel.
     */
    public void expandSettingsPanel(String subPanel) {
        throw new RuntimeException("Stub!");
    }

    public void setIcon(String slot, int iconId, int iconLevel, String contentDescription) {
        throw new RuntimeException("Stub!");
    }

    public void removeIcon(String slot) {
        throw new RuntimeException("Stub!");
    }

    public void setIconVisibility(String slot, boolean visible) {
        throw new RuntimeException("Stub!");
    }

    /** @hide */
    public static String windowStateToString(int state) {
        throw new RuntimeException("Stub!");
    }
}
