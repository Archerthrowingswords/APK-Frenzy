package android.support.v7.app;

import android.support.v7.app.ActionBar;
import android.support.v7.appcompat.R;
import android.support.v7.internal.widget.NativeActionModeAwareLayout;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
/* loaded from: classes.dex */
class ActionBarImplHC extends ActionBarImplBase implements NativeActionModeAwareLayout.OnActionModeForChildListener {
    private ActionMode mCurActionMode;
    final NativeActionModeAwareLayout mNativeActionModeAwareLayout;

    public ActionBarImplHC(ActionBarActivity activity, ActionBar.Callback callback) {
        super(activity, callback);
        this.mNativeActionModeAwareLayout = (NativeActionModeAwareLayout) activity.findViewById(R.id.action_bar_root);
        if (this.mNativeActionModeAwareLayout != null) {
            this.mNativeActionModeAwareLayout.setActionModeForChildListener(this);
        }
    }

    @Override // android.support.v7.internal.widget.NativeActionModeAwareLayout.OnActionModeForChildListener
    public ActionMode.Callback onActionModeForChild(ActionMode.Callback callback) {
        return new CallbackWrapper(callback);
    }

    @Override // android.support.v7.app.ActionBarImplBase, android.support.v7.app.ActionBar
    public void show() {
        super.show();
        if (this.mCurActionMode != null) {
            this.mCurActionMode.finish();
        }
    }

    @Override // android.support.v7.app.ActionBarImplBase, android.support.v7.app.ActionBar
    public void hide() {
        super.hide();
        if (this.mCurActionMode != null) {
            this.mCurActionMode.finish();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // android.support.v7.app.ActionBarImplBase
    public boolean isShowHideAnimationEnabled() {
        return this.mCurActionMode == null && super.isShowHideAnimationEnabled();
    }

    /* loaded from: classes.dex */
    private class CallbackWrapper implements ActionMode.Callback {
        private final ActionMode.Callback mWrappedCallback;

        CallbackWrapper(ActionMode.Callback callback) {
            this.mWrappedCallback = callback;
        }

        @Override // android.view.ActionMode.Callback
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            boolean wrappedResult = this.mWrappedCallback.onCreateActionMode(mode, menu);
            if (wrappedResult) {
                ActionBarImplHC.this.mCurActionMode = mode;
                ActionBarImplHC.this.showForActionMode();
            }
            return wrappedResult;
        }

        @Override // android.view.ActionMode.Callback
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return this.mWrappedCallback.onPrepareActionMode(mode, menu);
        }

        @Override // android.view.ActionMode.Callback
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return this.mWrappedCallback.onActionItemClicked(mode, item);
        }

        @Override // android.view.ActionMode.Callback
        public void onDestroyActionMode(ActionMode mode) {
            this.mWrappedCallback.onDestroyActionMode(mode);
            ActionBarImplHC.this.hideForActionMode();
            ActionBarImplHC.this.mCurActionMode = null;
        }
    }
}
