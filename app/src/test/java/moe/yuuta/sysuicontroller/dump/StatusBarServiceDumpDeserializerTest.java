package moe.yuuta.sysuicontroller.dump;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StatusBarServiceDumpDeserializerTest {
    private StatusBarServiceDumpDeserializer mDeserializer;

    private static final String TEST_INPUT = "  mDisabled1=0xe0000\n" +
            "  mDisabled2=0x0\n" +
            "  mDisableRecords.size=3\n" +
            "    [0] userId=0 what1=0x00000000 what2=0x00000000 pkg=null token=android.os.BinderProxy@544056c\n" +
            "    [1] userId=0 what1=0x000E0000 what2=0x00000000 pkg=null token=android.os.BinderProxy@8013c9\n" +
            "    [2] userId=0 what1=0x00000000 what2=0x00000000 pkg=null token=android.os.Binder@305dace\n" +
            "  mCurrentUserId=0\n" +
            "  mIcons=\n" +
            "\n" +
            "deo-1 -> StatusBarIcon(icon=Icon(typ=RESOURCE pkg=moe.yuuta.sysuicontroller id=0x7f070077) level=1 visible user=0 ) \"deo icon\"\n" +
            "\n" +
            "deo-2 -> StatusBarIcon(icon=Icon(typ=RESOURCE pkg=moe.yuuta.sysuicontroller id=0x7f070078) level=1 visible user=0 ) \"deo icon2\"";

    @Before
    public void setUp() throws Exception {
        mDeserializer = new StatusBarServiceDumpDeserializer();
        mDeserializer.deserialize(TEST_INPUT);
    }

    @Test
    public void testGetDisable1() {
        assertEquals(0xe0000, mDeserializer.getDisable1());
    }

    @Test
    public void testGetDisable2() {
        assertEquals(0x0, mDeserializer.getDisable2());
    }
}