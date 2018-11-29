package moe.yuuta.sysuicontroller.dump;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import moe.yuuta.sysuicontroller.Main;

public class StatusBarServiceDumpDeserializer {
    private static final String TAG = Main.GLOBAL_TAG + ".Des";

    private int mDisable1 = -1;
    private int mDisable2 = -1;

    /**
     An example of input value:
     <code>
     mDisabled1=0xe0000
     mDisabled2=0x0
     mDisableRecords.size=2
     [0] userId=0 what1=0x000E0000 what2=0x00000000 pkg=null token=android.os.BinderProxy@3bd9a02
     [1] userId=0 what1=0x00000000 what2=0x00000000 pkg=null token=android.os.BinderProxy@c335f13
     mCurrentUserId=0
     mIcons=

     String -> StatusBarIcon(icon=Icon(typ=RESOURCE pkg=String id=0x7f070077) level=1 visible user=0 ) "String"

     String -> StatusBarIcon(icon=Icon(typ=RESOURCE pkg=String id=0x7f070078) level=1 visible user=0 ) "String"
     </code>
     */
    public void deserialize (@NonNull String input) {
        Pattern patternMDisabled = Pattern.compile(".*mDisabled\\d\\s*=\\s*(.*)", Pattern.MULTILINE);

        Matcher matcherDisabled = patternMDisabled.matcher(input);
        while (matcherDisabled.find()) {
            String group = matcherDisabled.group();
            if (group.contains("mDisabled1") && !group.contains("mDisabled2")) {
                String[] ar = group.trim().split("=");
                if (ar.length == 2) {
                    mDisable1 = Integer.parseInt(ar[1].replace("x", "0"), 16);
                }
            }
            if (group.contains("mDisabled2") && !group.contains("mDisabled1")) {
                String[] ar = group.trim().split("=");
                if (ar.length == 2) {
                    mDisable2 = Integer.parseInt(ar[1].replace("x", "0"), 16);
                }
            }
        }

        //
    }

    public int getDisable1 () { return mDisable1; }

    public int getDisable2 () { return mDisable2; }
}
