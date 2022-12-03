package android.support.v7.internal.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.View;
import android.widget.LinearLayout;
/* loaded from: classes.dex */
public class NativeActionModeAwareLayout extends LinearLayout {
    private OnActionModeForChildListener mActionModeForChildListener;

    /* loaded from: classes.dex */
    public interface OnActionModeForChildListener {
        ActionMode.Callback onActionModeForChild(ActionMode.Callback callback);
    }

    public NativeActionModeAwareLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setActionModeForChildListener(OnActionModeForChildListener listener) {
        this.mActionModeForChildListener = listener;
    }

    @Override // android.view.ViewGroup, android.view.ViewParent
    public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback) {
        if (this.mActionModeForChildListener != null) {
            callback = this.mActionModeForChildListener.onActionModeForChild(callback);
        }
        return super.startActionModeForChild(originalView, callback);
    }
}
