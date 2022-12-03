package org.benews;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
/* loaded from: classes.dex */
public class BootManager extends BroadcastReceiver {
    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, PullIntentService.class);
        context.startService(serviceIntent);
    }
}
