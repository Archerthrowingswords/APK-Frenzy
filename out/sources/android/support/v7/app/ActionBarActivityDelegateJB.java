package android.support.v7.app;

import android.content.Context;
import android.support.v7.internal.view.ActionModeWrapper;
import android.support.v7.internal.view.ActionModeWrapperJB;
import android.support.v7.view.ActionMode;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class ActionBarActivityDelegateJB extends ActionBarActivityDelegateICS {
    /* JADX INFO: Access modifiers changed from: package-private */
    public ActionBarActivityDelegateJB(ActionBarActivity activity) {
        super(activity);
    }

    @Override // android.support.v7.app.ActionBarActivityDelegateICS, android.support.v7.app.ActionBarActivityDelegate
    public ActionBar createSupportActionBar() {
        return new ActionBarImplJB(this.mActivity, this.mActivity);
    }

    @Override // android.support.v7.app.ActionBarActivityDelegateICS
    ActionModeWrapper.CallbackWrapper createActionModeCallbackWrapper(Context context, ActionMode.Callback callback) {
        return new ActionModeWrapperJB.CallbackWrapper(context, callback);
    }

    @Override // android.support.v7.app.ActionBarActivityDelegateICS
    ActionModeWrapper createActionModeWrapper(Context context, android.view.ActionMode frameworkMode) {
        return new ActionModeWrapperJB(context, frameworkMode);
    }
}
