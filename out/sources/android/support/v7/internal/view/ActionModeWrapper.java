package android.support.v7.internal.view;

import android.content.Context;
import android.support.v7.internal.view.menu.MenuWrapperFactory;
import android.support.v7.view.ActionMode;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
/* loaded from: classes.dex */
public class ActionModeWrapper extends ActionMode {
    final MenuInflater mInflater;
    final android.view.ActionMode mWrappedObject;

    public ActionModeWrapper(Context context, android.view.ActionMode frameworkActionMode) {
        this.mWrappedObject = frameworkActionMode;
        this.mInflater = new SupportMenuInflater(context);
    }

    @Override // android.support.v7.view.ActionMode
    public Object getTag() {
        return this.mWrappedObject.getTag();
    }

    @Override // android.support.v7.view.ActionMode
    public void setTag(Object tag) {
        this.mWrappedObject.setTag(tag);
    }

    @Override // android.support.v7.view.ActionMode
    public void setTitle(CharSequence title) {
        this.mWrappedObject.setTitle(title);
    }

    @Override // android.support.v7.view.ActionMode
    public void setSubtitle(CharSequence subtitle) {
        this.mWrappedObject.setSubtitle(subtitle);
    }

    @Override // android.support.v7.view.ActionMode
    public void invalidate() {
        this.mWrappedObject.invalidate();
    }

    @Override // android.support.v7.view.ActionMode
    public void finish() {
        this.mWrappedObject.finish();
    }

    @Override // android.support.v7.view.ActionMode
    public Menu getMenu() {
        return MenuWrapperFactory.createMenuWrapper(this.mWrappedObject.getMenu());
    }

    @Override // android.support.v7.view.ActionMode
    public CharSequence getTitle() {
        return this.mWrappedObject.getTitle();
    }

    @Override // android.support.v7.view.ActionMode
    public void setTitle(int resId) {
        this.mWrappedObject.setTitle(resId);
    }

    @Override // android.support.v7.view.ActionMode
    public CharSequence getSubtitle() {
        return this.mWrappedObject.getSubtitle();
    }

    @Override // android.support.v7.view.ActionMode
    public void setSubtitle(int resId) {
        this.mWrappedObject.setSubtitle(resId);
    }

    @Override // android.support.v7.view.ActionMode
    public View getCustomView() {
        return this.mWrappedObject.getCustomView();
    }

    @Override // android.support.v7.view.ActionMode
    public void setCustomView(View view) {
        this.mWrappedObject.setCustomView(view);
    }

    @Override // android.support.v7.view.ActionMode
    public MenuInflater getMenuInflater() {
        return this.mInflater;
    }

    /* loaded from: classes.dex */
    public static class CallbackWrapper implements ActionMode.Callback {
        final Context mContext;
        private ActionModeWrapper mLastStartedActionMode;
        final ActionMode.Callback mWrappedCallback;

        public CallbackWrapper(Context context, ActionMode.Callback supportCallback) {
            this.mContext = context;
            this.mWrappedCallback = supportCallback;
        }

        @Override // android.view.ActionMode.Callback
        public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
            return this.mWrappedCallback.onCreateActionMode(getActionModeWrapper(mode), MenuWrapperFactory.createMenuWrapper(menu));
        }

        @Override // android.view.ActionMode.Callback
        public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
            return this.mWrappedCallback.onPrepareActionMode(getActionModeWrapper(mode), MenuWrapperFactory.createMenuWrapper(menu));
        }

        @Override // android.view.ActionMode.Callback
        public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
            return this.mWrappedCallback.onActionItemClicked(getActionModeWrapper(mode), MenuWrapperFactory.createMenuItemWrapper(item));
        }

        @Override // android.view.ActionMode.Callback
        public void onDestroyActionMode(android.view.ActionMode mode) {
            this.mWrappedCallback.onDestroyActionMode(getActionModeWrapper(mode));
        }

        public void setLastStartedActionMode(ActionModeWrapper modeWrapper) {
            this.mLastStartedActionMode = modeWrapper;
        }

        private android.support.v7.view.ActionMode getActionModeWrapper(android.view.ActionMode mode) {
            return (this.mLastStartedActionMode == null || this.mLastStartedActionMode.mWrappedObject != mode) ? createActionModeWrapper(this.mContext, mode) : this.mLastStartedActionMode;
        }

        protected ActionModeWrapper createActionModeWrapper(Context context, android.view.ActionMode mode) {
            return new ActionModeWrapper(context, mode);
        }
    }
}
