// IStatusController.aidl
package moe.yuuta.sysuicontroller;

import moe.yuuta.sysuicontroller.core.DisableItem;
// Declare any non-default types here with import statements

interface IStatusController {
    // Same as StatusBarManager
    void disable (int flags);
    void disable2 (int flags);
    void expandNotificationsPanel ();
    void collapsePanels ();
    void expandSettingsPanel (in String subPanel);
    void setIcon(in String slot, int iconId, int iconLevel, in String contentDescription);
    void removeIcon(in String slot);
    void setIconVisibility(in String slot, boolean visible);

    // Private methods
    void exit ();
    int getDisableFlags ();
    int getDisable2Flags ();

    // Methods used to access hidden-api from server part
    List<DisableItem> getAvailableDisableItems ();
    int getDisableNoneFlag (boolean disable2);
}
