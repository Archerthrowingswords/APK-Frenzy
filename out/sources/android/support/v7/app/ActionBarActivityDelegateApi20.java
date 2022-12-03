package android.support.v7.app;

import android.support.v7.app.ActionBarActivityDelegateICS;
import android.view.Window;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class ActionBarActivityDelegateApi20 extends ActionBarActivityDelegateJBMR2 {
    /* JADX INFO: Access modifiers changed from: package-private */
    public ActionBarActivityDelegateApi20(ActionBarActivity activity) {
        super(activity);
    }

    @Override // android.support.v7.app.ActionBarActivityDelegateICS
    Window.Callback createWindowCallbackWrapper(Window.Callback cb) {
        return new WindowCallbackWrapperApi20(cb);
    }

    /* loaded from: classes.dex */
    class WindowCallbackWrapperApi20 extends ActionBarActivityDelegateICS.WindowCallbackWrapper {
        WindowCallbackWrapperApi20(Window.Callback wrapped) {
            super(wrapped);
        }
    }
}
