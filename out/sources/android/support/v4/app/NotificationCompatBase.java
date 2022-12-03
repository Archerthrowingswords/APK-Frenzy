package android.support.v4.app;

import android.app.PendingIntent;
import android.os.Bundle;
import android.support.v4.app.RemoteInputCompatBase;
/* loaded from: classes.dex */
class NotificationCompatBase {

    /* loaded from: classes.dex */
    public static abstract class Action {

        /* loaded from: classes.dex */
        public interface Factory {
            Action build(int i, CharSequence charSequence, PendingIntent pendingIntent, Bundle bundle, RemoteInputCompatBase.RemoteInput[] remoteInputArr);

            Action[] newArray(int i);
        }

        /* JADX INFO: Access modifiers changed from: protected */
        public abstract PendingIntent getActionIntent();

        /* JADX INFO: Access modifiers changed from: protected */
        public abstract Bundle getExtras();

        /* JADX INFO: Access modifiers changed from: protected */
        public abstract int getIcon();

        /* JADX INFO: Access modifiers changed from: protected */
        public abstract RemoteInputCompatBase.RemoteInput[] getRemoteInputs();

        /* JADX INFO: Access modifiers changed from: protected */
        public abstract CharSequence getTitle();
    }

    NotificationCompatBase() {
    }
}
