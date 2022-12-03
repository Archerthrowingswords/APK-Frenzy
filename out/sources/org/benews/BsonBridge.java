package org.benews;

import android.util.Log;
import java.nio.ByteBuffer;
import java.util.HashMap;
/* loaded from: classes.dex */
public class BsonBridge {
    public static final int BSON_TYPE_TEXT = 0;
    public static final String TAG = "BsonBridge";

    public static native byte[] getToken(String str, long j, String str2);

    public static native HashMap<String, String> serialize(String str, ByteBuffer byteBuffer);

    static {
        System.loadLibrary("bson");
    }

    public static HashMap<String, String> serializeBson(String baseDir, ByteBuffer payload) {
        Log.d(TAG, "serialize called\n");
        return serialize(baseDir, payload);
    }

    public static byte[] getTokenBson(String imei, long key, String cks) {
        Log.d(TAG, "getToken called\n");
        return getToken(imei, key, cks);
    }
}
