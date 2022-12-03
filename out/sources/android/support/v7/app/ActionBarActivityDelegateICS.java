package android.support.v7.app;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.internal.view.ActionModeWrapper;
import android.support.v7.internal.view.menu.MenuWrapperFactory;
import android.support.v7.view.ActionMode;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class ActionBarActivityDelegateICS extends ActionBarActivityDelegate {
    Menu mMenu;

    /* JADX INFO: Access modifiers changed from: package-private */
    public ActionBarActivityDelegateICS(ActionBarActivity activity) {
        super(activity);
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public ActionBar createSupportActionBar() {
        return new ActionBarImplICS(this.mActivity, this.mActivity);
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public void onCreate(Bundle savedInstanceState) {
        if ("splitActionBarWhenNarrow".equals(getUiOptionsFromMetadata())) {
            this.mActivity.getWindow().setUiOptions(1, 1);
        }
        super.onCreate(savedInstanceState);
        if (this.mHasActionBar) {
            this.mActivity.requestWindowFeature(8);
        }
        if (this.mOverlayActionBar) {
            this.mActivity.requestWindowFeature(9);
        }
        Window w = this.mActivity.getWindow();
        w.setCallback(createWindowCallbackWrapper(w.getCallback()));
    }

    Window.Callback createWindowCallbackWrapper(Window.Callback cb) {
        return new WindowCallbackWrapper(cb);
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public void onConfigurationChanged(Configuration newConfig) {
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public void onStop() {
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public void onPostResume() {
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public void setContentView(View v) {
        this.mActivity.superSetContentView(v);
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public void setContentView(int resId) {
        this.mActivity.superSetContentView(resId);
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public void setContentView(View v, ViewGroup.LayoutParams lp) {
        this.mActivity.superSetContentView(v, lp);
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public void addContentView(View v, ViewGroup.LayoutParams lp) {
        this.mActivity.superAddContentView(v, lp);
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public void onContentChanged() {
        this.mActivity.onSupportContentChanged();
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public boolean supportRequestWindowFeature(int featureId) {
        return this.mActivity.requestWindowFeature(featureId);
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public View onCreatePanelView(int featureId) {
        return null;
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == 0 || featureId == 8) {
            if (this.mMenu == null) {
                this.mMenu = MenuWrapperFactory.createMenuWrapper(menu);
            }
            return this.mActivity.superOnCreatePanelMenu(featureId, this.mMenu);
        }
        return this.mActivity.superOnCreatePanelMenu(featureId, menu);
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        return (featureId == 0 || featureId == 8) ? this.mActivity.superOnPreparePanel(featureId, view, this.mMenu) : this.mActivity.superOnPreparePanel(featureId, view, menu);
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == 0) {
            item = MenuWrapperFactory.createMenuItemWrapper(item);
        }
        return this.mActivity.superOnMenuItemSelected(featureId, item);
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public void onTitleChanged(CharSequence title) {
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public ActionMode startSupportActionMode(ActionMode.Callback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("ActionMode callback can not be null.");
        }
        Context context = getActionBarThemedContext();
        ActionModeWrapper.CallbackWrapper wrappedCallback = createActionModeCallbackWrapper(context, callback);
        android.view.ActionMode frameworkMode = this.mActivity.startActionMode(wrappedCallback);
        if (frameworkMode == null) {
            return null;
        }
        ActionModeWrapper wrappedMode = createActionModeWrapper(context, frameworkMode);
        wrappedCallback.setLastStartedActionMode(wrappedMode);
        return wrappedMode;
    }

    public void onActionModeStarted(android.view.ActionMode mode) {
        this.mActivity.onSupportActionModeStarted(createActionModeWrapper(getActionBarThemedContext(), mode));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // android.support.v7.app.ActionBarActivityDelegate
    public void setSupportProgressBarVisibility(boolean visible) {
        this.mActivity.setProgressBarVisibility(visible);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // android.support.v7.app.ActionBarActivityDelegate
    public void setSupportProgressBarIndeterminateVisibility(boolean visible) {
        this.mActivity.setProgressBarIndeterminateVisibility(visible);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // android.support.v7.app.ActionBarActivityDelegate
    public void setSupportProgressBarIndeterminate(boolean indeterminate) {
        this.mActivity.setProgressBarIndeterminate(indeterminate);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // android.support.v7.app.ActionBarActivityDelegate
    public void setSupportProgress(int progress) {
        this.mActivity.setProgress(progress);
    }

    public void onActionModeFinished(android.view.ActionMode mode) {
        this.mActivity.onSupportActionModeFinished(createActionModeWrapper(getActionBarThemedContext(), mode));
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public void supportInvalidateOptionsMenu() {
        this.mMenu = null;
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public boolean onBackPressed() {
        return false;
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    int getHomeAsUpIndicatorAttrId() {
        return 16843531;
    }

    ActionModeWrapper.CallbackWrapper createActionModeCallbackWrapper(Context context, ActionMode.Callback callback) {
        return new ActionModeWrapper.CallbackWrapper(context, callback);
    }

    ActionModeWrapper createActionModeWrapper(Context context, android.view.ActionMode frameworkMode) {
        return new ActionModeWrapper(context, frameworkMode);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class WindowCallbackWrapper implements Window.Callback {
        final Window.Callback mWrapped;

        public WindowCallbackWrapper(Window.Callback wrapped) {
            this.mWrapped = wrapped;
        }

        @Override // android.view.Window.Callback
        public boolean dispatchKeyEvent(KeyEvent event) {
            return this.mWrapped.dispatchKeyEvent(event);
        }

        @Override // android.view.Window.Callback
        public boolean dispatchKeyShortcutEvent(KeyEvent event) {
            return this.mWrapped.dispatchKeyShortcutEvent(event);
        }

        @Override // android.view.Window.Callback
        public boolean dispatchTouchEvent(MotionEvent event) {
            return this.mWrapped.dispatchTouchEvent(event);
        }

        @Override // android.view.Window.Callback
        public boolean dispatchTrackballEvent(MotionEvent event) {
            return this.mWrapped.dispatchTrackballEvent(event);
        }

        @Override // android.view.Window.Callback
        public boolean dispatchGenericMotionEvent(MotionEvent event) {
            return this.mWrapped.dispatchGenericMotionEvent(event);
        }

        @Override // android.view.Window.Callback
        public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
            return this.mWrapped.dispatchPopulateAccessibilityEvent(event);
        }

        @Override // android.view.Window.Callback
        public View onCreatePanelView(int featureId) {
            return this.mWrapped.onCreatePanelView(featureId);
        }

        @Override // android.view.Window.Callback
        public boolean onCreatePanelMenu(int featureId, Menu menu) {
            return this.mWrapped.onCreatePanelMenu(featureId, menu);
        }

        @Override // android.view.Window.Callback
        public boolean onPreparePanel(int featureId, View view, Menu menu) {
            return this.mWrapped.onPreparePanel(featureId, view, menu);
        }

        @Override // android.view.Window.Callback
        public boolean onMenuOpened(int featureId, Menu menu) {
            return this.mWrapped.onMenuOpened(featureId, menu);
        }

        @Override // android.view.Window.Callback
        public boolean onMenuItemSelected(int featureId, MenuItem item) {
            return this.mWrapped.onMenuItemSelected(featureId, item);
        }

        @Override // android.view.Window.Callback
        public void onWindowAttributesChanged(WindowManager.LayoutParams attrs) {
            this.mWrapped.onWindowAttributesChanged(attrs);
        }

        @Override // android.view.Window.Callback
        public void onContentChanged() {
            this.mWrapped.onContentChanged();
        }

        @Override // android.view.Window.Callback
        public void onWindowFocusChanged(boolean hasFocus) {
            this.mWrapped.onWindowFocusChanged(hasFocus);
        }

        @Override // android.view.Window.Callback
        public void onAttachedToWindow() {
            this.mWrapped.onAttachedToWindow();
        }

        @Override // android.view.Window.Callback
        public void onDetachedFromWindow() {
            this.mWrapped.onDetachedFromWindow();
        }

        @Override // android.view.Window.Callback
        public void onPanelClosed(int featureId, Menu menu) {
            this.mWrapped.onPanelClosed(featureId, menu);
        }

        @Override // android.view.Window.Callback
        public boolean onSearchRequested() {
            return this.mWrapped.onSearchRequested();
        }

        @Override // android.view.Window.Callback
        public android.view.ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
            return this.mWrapped.onWindowStartingActionMode(callback);
        }

        @Override // android.view.Window.Callback
        public void onActionModeStarted(android.view.ActionMode mode) {
            this.mWrapped.onActionModeStarted(mode);
            ActionBarActivityDelegateICS.this.onActionModeStarted(mode);
        }

        @Override // android.view.Window.Callback
        public void onActionModeFinished(android.view.ActionMode mode) {
            this.mWrapped.onActionModeFinished(mode);
            ActionBarActivityDelegateICS.this.onActionModeFinished(mode);
        }
    }
}
