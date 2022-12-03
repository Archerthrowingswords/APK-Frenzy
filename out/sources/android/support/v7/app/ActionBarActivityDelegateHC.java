package android.support.v7.app;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class ActionBarActivityDelegateHC extends ActionBarActivityDelegateBase {
    /* JADX INFO: Access modifiers changed from: package-private */
    public ActionBarActivityDelegateHC(ActionBarActivity activity) {
        super(activity);
    }

    @Override // android.support.v7.app.ActionBarActivityDelegateBase, android.support.v7.app.ActionBarActivityDelegate
    public ActionBar createSupportActionBar() {
        ensureSubDecor();
        return new ActionBarImplHC(this.mActivity, this.mActivity);
    }
}
