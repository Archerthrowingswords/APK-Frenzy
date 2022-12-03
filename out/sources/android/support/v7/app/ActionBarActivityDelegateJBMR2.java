package android.support.v7.app;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class ActionBarActivityDelegateJBMR2 extends ActionBarActivityDelegateJB {
    /* JADX INFO: Access modifiers changed from: package-private */
    public ActionBarActivityDelegateJBMR2(ActionBarActivity activity) {
        super(activity);
    }

    @Override // android.support.v7.app.ActionBarActivityDelegateJB, android.support.v7.app.ActionBarActivityDelegateICS, android.support.v7.app.ActionBarActivityDelegate
    public ActionBar createSupportActionBar() {
        return new ActionBarImplJBMR2(this.mActivity, this.mActivity);
    }
}
