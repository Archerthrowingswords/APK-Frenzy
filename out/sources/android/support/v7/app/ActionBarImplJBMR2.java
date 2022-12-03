package android.support.v7.app;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
/* loaded from: classes.dex */
public class ActionBarImplJBMR2 extends ActionBarImplJB {
    public ActionBarImplJBMR2(Activity activity, ActionBar.Callback callback) {
        super(activity, callback);
    }

    @Override // android.support.v7.app.ActionBarImplJB, android.support.v7.app.ActionBarImplICS, android.support.v7.app.ActionBar
    public void setHomeAsUpIndicator(Drawable indicator) {
        this.mActionBar.setHomeAsUpIndicator(indicator);
    }

    @Override // android.support.v7.app.ActionBarImplJB, android.support.v7.app.ActionBarImplICS, android.support.v7.app.ActionBar
    public void setHomeAsUpIndicator(int resId) {
        this.mActionBar.setHomeAsUpIndicator(resId);
    }

    @Override // android.support.v7.app.ActionBar
    public void setHomeActionContentDescription(CharSequence description) {
        this.mActionBar.setHomeActionContentDescription(description);
    }

    @Override // android.support.v7.app.ActionBar
    public void setHomeActionContentDescription(int resId) {
        this.mActionBar.setHomeActionContentDescription(resId);
    }
}
