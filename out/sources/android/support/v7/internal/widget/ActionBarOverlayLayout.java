package android.support.v7.internal.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v7.app.ActionBar;
import android.support.v7.appcompat.R;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
/* loaded from: classes.dex */
public class ActionBarOverlayLayout extends FrameLayout {
    static final int[] mActionBarSizeAttr = {R.attr.actionBarSize};
    private ActionBar mActionBar;
    private View mActionBarBottom;
    private int mActionBarHeight;
    private View mActionBarTop;
    private ActionBarView mActionView;
    private ActionBarContainer mContainerView;
    private View mContent;
    private final Rect mZeroRect;

    public ActionBarOverlayLayout(Context context) {
        super(context);
        this.mZeroRect = new Rect(0, 0, 0, 0);
        init(context);
    }

    public ActionBarOverlayLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mZeroRect = new Rect(0, 0, 0, 0);
        init(context);
    }

    private void init(Context context) {
        TypedArray ta = getContext().getTheme().obtainStyledAttributes(mActionBarSizeAttr);
        this.mActionBarHeight = ta.getDimensionPixelSize(0, 0);
        ta.recycle();
    }

    public void setActionBar(ActionBar impl) {
        this.mActionBar = impl;
    }

    private boolean applyInsets(View view, Rect insets, boolean left, boolean top, boolean bottom, boolean right) {
        boolean changed = false;
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) view.getLayoutParams();
        if (left && lp.leftMargin != insets.left) {
            changed = true;
            lp.leftMargin = insets.left;
        }
        if (top && lp.topMargin != insets.top) {
            changed = true;
            lp.topMargin = insets.top;
        }
        if (right && lp.rightMargin != insets.right) {
            changed = true;
            lp.rightMargin = insets.right;
        }
        if (bottom && lp.bottomMargin != insets.bottom) {
            lp.bottomMargin = insets.bottom;
            return true;
        }
        return changed;
    }

    void pullChildren() {
        if (this.mContent == null) {
            this.mContent = findViewById(R.id.action_bar_activity_content);
            if (this.mContent == null) {
                this.mContent = findViewById(16908290);
            }
            this.mActionBarTop = findViewById(R.id.top_action_bar);
            this.mContainerView = (ActionBarContainer) findViewById(R.id.action_bar_container);
            this.mActionView = (ActionBarView) findViewById(R.id.action_bar);
            this.mActionBarBottom = findViewById(R.id.split_action_bar);
        }
    }
}
