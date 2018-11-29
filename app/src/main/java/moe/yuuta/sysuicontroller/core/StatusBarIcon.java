package moe.yuuta.sysuicontroller.core;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class StatusBarIcon implements Parcelable {
    public static final String ICON_TYPE_RESOURCE = "RESOURCE";

    private String slot;
    private String iconType;
    private String pkg;
    private int id;
    private int level;
    private boolean visible;
    private int user;
    private String contentDescription;

    public StatusBarIcon() {
    }

    public StatusBarIcon(String slot, String iconType, String pkg, int id, int level, boolean visible, int user, String contentDescription) {
        this.slot = slot;
        this.iconType = iconType;
        this.pkg = pkg;
        this.id = id;
        this.level = level;
        this.visible = visible;
        this.user = user;
        this.contentDescription = contentDescription;
    }

    protected StatusBarIcon(Parcel in) {
        slot = in.readString();
        iconType = in.readString();
        pkg = in.readString();
        id = in.readInt();
        level = in.readInt();
        visible = in.readByte() != 0;
        user = in.readInt();
        contentDescription = in.readString();
    }

    public static final Creator<StatusBarIcon> CREATOR = new Creator<StatusBarIcon>() {
        @Override
        public StatusBarIcon createFromParcel(Parcel in) {
            return new StatusBarIcon(in);
        }

        @Override
        public StatusBarIcon[] newArray(int size) {
            return new StatusBarIcon[size];
        }
    };

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public String getIconType() {
        return iconType;
    }

    public void setIconType(String iconType) {
        this.iconType = iconType;
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public String getContentDescription() {
        return contentDescription;
    }

    public void setContentDescription(String contentDescription) {
        this.contentDescription = contentDescription;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(slot);
        dest.writeString(iconType);
        dest.writeString(pkg);
        dest.writeInt(id);
        dest.writeInt(level);
        dest.writeByte((byte) (visible ? 1 : 0));
        dest.writeInt(user);
        dest.writeString(contentDescription);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatusBarIcon icon = (StatusBarIcon) o;
        return id == icon.id &&
                level == icon.level &&
                visible == icon.visible &&
                user == icon.user &&
                Objects.equals(slot, icon.slot) &&
                Objects.equals(iconType, icon.iconType) &&
                Objects.equals(pkg, icon.pkg) &&
                Objects.equals(contentDescription, icon.contentDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slot, iconType, pkg, id, level, visible, user, contentDescription);
    }

    @Override
    public String toString() {
        return "StatusBarIcon{" +
                "slot='" + slot + '\'' +
                ", iconType='" + iconType + '\'' +
                ", pkg='" + pkg + '\'' +
                ", id=" + id +
                ", level=" + level +
                ", visible=" + visible +
                ", user=" + user +
                ", contentDescription='" + contentDescription + '\'' +
                '}';
    }
}
