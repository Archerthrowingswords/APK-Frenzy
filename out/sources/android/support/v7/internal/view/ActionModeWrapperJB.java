package android.support.v7.internal.view;

import android.content.Context;
import android.support.v7.internal.view.ActionModeWrapper;
import android.support.v7.view.ActionMode;
import android.view.ActionMode;
/* loaded from: classes.dex */
public class ActionModeWrapperJB extends ActionModeWrapper {
    public ActionModeWrapperJB(Context context, ActionMode frameworkActionMode) {
        super(context, frameworkActionMode);
    }

    @Override // android.support.v7.view.ActionMode
    public boolean getTitleOptionalHint() {
        return this.mWrappedObject.getTitleOptionalHint();
    }

    @Override // android.support.v7.view.ActionMode
    public void setTitleOptionalHint(boolean titleOptional) {
        this.mWrappedObject.setTitleOptionalHint(titleOptional);
    }

    @Override // android.support.v7.view.ActionMode
    public boolean isTitleOptional() {
        return this.mWrappedObject.isTitleOptional();
    }

    /* loaded from: classes.dex */
    public static class CallbackWrapper extends ActionModeWrapper.CallbackWrapper {
        public CallbackWrapper(Context context, ActionMode.Callback supportCallback) {
            super(context, supportCallback);
        }

        @Override // android.support.v7.internal.view.ActionModeWrapper.CallbackWrapper
        protected ActionModeWrapper createActionModeWrapper(Context context, android.view.ActionMode mode) {
            return new ActionModeWrapperJB(context, mode);
        }
    }
}
