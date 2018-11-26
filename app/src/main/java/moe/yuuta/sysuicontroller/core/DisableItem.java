package moe.yuuta.sysuicontroller.core;

import android.os.Parcel;
import android.os.Parcelable;

public class DisableItem implements Parcelable {
    private int flag;
    private String key;
    private boolean disable2;

    public DisableItem() {
    }

    public DisableItem(int flag, String key, boolean disable2) {
        this.flag = flag;
        this.key = key;
        this.disable2 = disable2;
    }

    protected DisableItem(Parcel in) {
        flag = in.readInt();
        key = in.readString();
        disable2 = in.readByte() != 0;
    }

    public static final Creator<DisableItem> CREATOR = new Creator<DisableItem>() {
        @Override
        public DisableItem createFromParcel(Parcel in) {
            return new DisableItem(in);
        }

        @Override
        public DisableItem[] newArray(int size) {
            return new DisableItem[size];
        }
    };

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isDisable2() {
        return disable2;
    }

    public void setDisable2(boolean disable2) {
        this.disable2 = disable2;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(flag);
        dest.writeString(key);
        dest.writeByte((byte) (disable2 ? 1 : 0));
    }

    @Override
    public String toString() {
        return "DisableItem{" +
                "flag=" + flag +
                ", key='" + key + '\'' +
                ", disable2=" + disable2 +
                '}';
    }
}
