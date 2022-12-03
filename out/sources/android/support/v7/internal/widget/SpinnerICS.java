package android.support.v7.internal.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.appcompat.R;
import android.support.v7.internal.widget.AdapterViewICS;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class SpinnerICS extends AbsSpinnerICS implements DialogInterface.OnClickListener {
    private static final int MAX_ITEMS_MEASURED = 15;
    static final int MODE_DIALOG = 0;
    static final int MODE_DROPDOWN = 1;
    private static final int MODE_THEME = -1;
    private static final String TAG = "Spinner";
    int mDropDownWidth;
    private int mGravity;
    private SpinnerPopup mPopup;
    private DropDownAdapter mTempAdapter;
    private Rect mTempRect;

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public interface SpinnerPopup {
        void dismiss();

        CharSequence getHintText();

        boolean isShowing();

        void setAdapter(ListAdapter listAdapter);

        void setPromptText(CharSequence charSequence);

        void show();
    }

    SpinnerICS(Context context) {
        this(context, (AttributeSet) null);
    }

    SpinnerICS(Context context, int mode) {
        this(context, null, R.attr.spinnerStyle, mode);
    }

    SpinnerICS(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.spinnerStyle);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public SpinnerICS(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, -1);
    }

    SpinnerICS(Context context, AttributeSet attrs, int defStyle, int mode) {
        super(context, attrs, defStyle);
        this.mTempRect = new Rect();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Spinner, defStyle, 0);
        switch (mode == -1 ? a.getInt(7, 0) : mode) {
            case 0:
                this.mPopup = new DialogPopup();
                break;
            case 1:
                DropdownPopup popup = new DropdownPopup(context, attrs, defStyle);
                this.mDropDownWidth = a.getLayoutDimension(3, -2);
                popup.setBackgroundDrawable(a.getDrawable(2));
                int verticalOffset = a.getDimensionPixelOffset(5, 0);
                if (verticalOffset != 0) {
                    popup.setVerticalOffset(verticalOffset);
                }
                int horizontalOffset = a.getDimensionPixelOffset(4, 0);
                if (horizontalOffset != 0) {
                    popup.setHorizontalOffset(horizontalOffset);
                }
                this.mPopup = popup;
                break;
        }
        this.mGravity = a.getInt(0, 17);
        this.mPopup.setPromptText(a.getString(6));
        a.recycle();
        if (this.mTempAdapter != null) {
            this.mPopup.setAdapter(this.mTempAdapter);
            this.mTempAdapter = null;
        }
    }

    public void setGravity(int gravity) {
        if (this.mGravity != gravity) {
            if ((gravity & 7) == 0) {
                gravity |= 3;
            }
            this.mGravity = gravity;
            requestLayout();
        }
    }

    @Override // android.support.v7.internal.widget.AbsSpinnerICS, android.support.v7.internal.widget.AdapterViewICS
    public void setAdapter(SpinnerAdapter adapter) {
        super.setAdapter(adapter);
        if (this.mPopup != null) {
            this.mPopup.setAdapter(new DropDownAdapter(adapter));
        } else {
            this.mTempAdapter = new DropDownAdapter(adapter);
        }
    }

    @Override // android.view.View
    public int getBaseline() {
        int childBaseline;
        View child = null;
        if (getChildCount() > 0) {
            child = getChildAt(0);
        } else if (this.mAdapter != null && this.mAdapter.getCount() > 0) {
            child = makeAndAddView(0);
            this.mRecycler.put(0, child);
            removeAllViewsInLayout();
        }
        if (child == null || (childBaseline = child.getBaseline()) < 0) {
            return -1;
        }
        return child.getTop() + childBaseline;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.internal.widget.AdapterViewICS, android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mPopup != null && this.mPopup.isShowing()) {
            this.mPopup.dismiss();
        }
    }

    @Override // android.support.v7.internal.widget.AdapterViewICS
    public void setOnItemClickListener(AdapterViewICS.OnItemClickListener l) {
        throw new RuntimeException("setOnItemClickListener cannot be used with a spinner.");
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void setOnItemClickListenerInt(AdapterViewICS.OnItemClickListener l) {
        super.setOnItemClickListener(l);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.internal.widget.AbsSpinnerICS, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this.mPopup != null && View.MeasureSpec.getMode(widthMeasureSpec) == Integer.MIN_VALUE) {
            int measuredWidth = getMeasuredWidth();
            setMeasuredDimension(Math.min(Math.max(measuredWidth, measureContentWidth(getAdapter(), getBackground())), View.MeasureSpec.getSize(widthMeasureSpec)), getMeasuredHeight());
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.support.v7.internal.widget.AdapterViewICS, android.view.ViewGroup, android.view.View
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        this.mInLayout = true;
        layout(0, false);
        this.mInLayout = false;
    }

    @Override // android.support.v7.internal.widget.AbsSpinnerICS
    void layout(int delta, boolean animate) {
        int childrenLeft = this.mSpinnerPadding.left;
        int childrenWidth = ((getRight() - getLeft()) - this.mSpinnerPadding.left) - this.mSpinnerPadding.right;
        if (this.mDataChanged) {
            handleDataChanged();
        }
        if (this.mItemCount == 0) {
            resetList();
            return;
        }
        if (this.mNextSelectedPosition >= 0) {
            setSelectedPositionInt(this.mNextSelectedPosition);
        }
        recycleAllViews();
        removeAllViewsInLayout();
        this.mFirstPosition = this.mSelectedPosition;
        View sel = makeAndAddView(this.mSelectedPosition);
        int width = sel.getMeasuredWidth();
        int selectedOffset = childrenLeft;
        switch (this.mGravity & 7) {
            case 1:
                selectedOffset = ((childrenWidth / 2) + childrenLeft) - (width / 2);
                break;
            case 5:
                selectedOffset = (childrenLeft + childrenWidth) - width;
                break;
        }
        sel.offsetLeftAndRight(selectedOffset);
        this.mRecycler.clear();
        invalidate();
        checkSelectionChanged();
        this.mDataChanged = false;
        this.mNeedSync = false;
        setNextSelectedPositionInt(this.mSelectedPosition);
    }

    private View makeAndAddView(int position) {
        View child;
        if (!this.mDataChanged && (child = this.mRecycler.get(position)) != null) {
            setUpChild(child);
            return child;
        }
        View child2 = this.mAdapter.getView(position, null, this);
        setUpChild(child2);
        return child2;
    }

    private void setUpChild(View child) {
        ViewGroup.LayoutParams lp = child.getLayoutParams();
        if (lp == null) {
            lp = generateDefaultLayoutParams();
        }
        addViewInLayout(child, 0, lp);
        child.setSelected(hasFocus());
        int childHeightSpec = ViewGroup.getChildMeasureSpec(this.mHeightMeasureSpec, this.mSpinnerPadding.top + this.mSpinnerPadding.bottom, lp.height);
        int childWidthSpec = ViewGroup.getChildMeasureSpec(this.mWidthMeasureSpec, this.mSpinnerPadding.left + this.mSpinnerPadding.right, lp.width);
        child.measure(childWidthSpec, childHeightSpec);
        int childTop = this.mSpinnerPadding.top + ((((getMeasuredHeight() - this.mSpinnerPadding.bottom) - this.mSpinnerPadding.top) - child.getMeasuredHeight()) / 2);
        int childBottom = childTop + child.getMeasuredHeight();
        int width = child.getMeasuredWidth();
        int childRight = 0 + width;
        child.layout(0, childTop, childRight, childBottom);
    }

    @Override // android.view.View
    public boolean performClick() {
        boolean handled = super.performClick();
        if (!handled) {
            handled = true;
            if (!this.mPopup.isShowing()) {
                this.mPopup.show();
            }
        }
        return handled;
    }

    @Override // android.content.DialogInterface.OnClickListener
    public void onClick(DialogInterface dialog, int which) {
        setSelection(which);
        dialog.dismiss();
    }

    public void setPrompt(CharSequence prompt) {
        this.mPopup.setPromptText(prompt);
    }

    public void setPromptId(int promptId) {
        setPrompt(getContext().getText(promptId));
    }

    public CharSequence getPrompt() {
        return this.mPopup.getHintText();
    }

    int measureContentWidth(SpinnerAdapter adapter, Drawable background) {
        if (adapter == null) {
            return 0;
        }
        int width = 0;
        View itemView = null;
        int itemType = 0;
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, 0);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, 0);
        int start = Math.max(0, getSelectedItemPosition());
        int end = Math.min(adapter.getCount(), start + 15);
        int count = end - start;
        for (int i = Math.max(0, start - (15 - count)); i < end; i++) {
            int positionType = adapter.getItemViewType(i);
            if (positionType != itemType) {
                itemType = positionType;
                itemView = null;
            }
            itemView = adapter.getView(i, itemView, this);
            if (itemView.getLayoutParams() == null) {
                itemView.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
            }
            itemView.measure(widthMeasureSpec, heightMeasureSpec);
            width = Math.max(width, itemView.getMeasuredWidth());
        }
        if (background != null) {
            background.getPadding(this.mTempRect);
            return width + this.mTempRect.left + this.mTempRect.right;
        }
        return width;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public static class DropDownAdapter implements ListAdapter, SpinnerAdapter {
        private SpinnerAdapter mAdapter;
        private ListAdapter mListAdapter;

        public DropDownAdapter(SpinnerAdapter adapter) {
            this.mAdapter = adapter;
            if (adapter instanceof ListAdapter) {
                this.mListAdapter = (ListAdapter) adapter;
            }
        }

        @Override // android.widget.Adapter
        public int getCount() {
            if (this.mAdapter == null) {
                return 0;
            }
            return this.mAdapter.getCount();
        }

        @Override // android.widget.Adapter
        public Object getItem(int position) {
            if (this.mAdapter == null) {
                return null;
            }
            return this.mAdapter.getItem(position);
        }

        @Override // android.widget.Adapter
        public long getItemId(int position) {
            if (this.mAdapter == null) {
                return -1L;
            }
            return this.mAdapter.getItemId(position);
        }

        @Override // android.widget.Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            return getDropDownView(position, convertView, parent);
        }

        @Override // android.widget.SpinnerAdapter
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (this.mAdapter == null) {
                return null;
            }
            return this.mAdapter.getDropDownView(position, convertView, parent);
        }

        @Override // android.widget.Adapter
        public boolean hasStableIds() {
            return this.mAdapter != null && this.mAdapter.hasStableIds();
        }

        @Override // android.widget.Adapter
        public void registerDataSetObserver(DataSetObserver observer) {
            if (this.mAdapter != null) {
                this.mAdapter.registerDataSetObserver(observer);
            }
        }

        @Override // android.widget.Adapter
        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (this.mAdapter != null) {
                this.mAdapter.unregisterDataSetObserver(observer);
            }
        }

        @Override // android.widget.ListAdapter
        public boolean areAllItemsEnabled() {
            ListAdapter adapter = this.mListAdapter;
            if (adapter != null) {
                return adapter.areAllItemsEnabled();
            }
            return true;
        }

        @Override // android.widget.ListAdapter
        public boolean isEnabled(int position) {
            ListAdapter adapter = this.mListAdapter;
            if (adapter != null) {
                return adapter.isEnabled(position);
            }
            return true;
        }

        @Override // android.widget.Adapter
        public int getItemViewType(int position) {
            return 0;
        }

        @Override // android.widget.Adapter
        public int getViewTypeCount() {
            return 1;
        }

        @Override // android.widget.Adapter
        public boolean isEmpty() {
            return getCount() == 0;
        }
    }

    /* loaded from: classes.dex */
    private class DialogPopup implements SpinnerPopup, DialogInterface.OnClickListener {
        private ListAdapter mListAdapter;
        private AlertDialog mPopup;
        private CharSequence mPrompt;

        private DialogPopup() {
        }

        @Override // android.support.v7.internal.widget.SpinnerICS.SpinnerPopup
        public void dismiss() {
            this.mPopup.dismiss();
            this.mPopup = null;
        }

        @Override // android.support.v7.internal.widget.SpinnerICS.SpinnerPopup
        public boolean isShowing() {
            if (this.mPopup != null) {
                return this.mPopup.isShowing();
            }
            return false;
        }

        @Override // android.support.v7.internal.widget.SpinnerICS.SpinnerPopup
        public void setAdapter(ListAdapter adapter) {
            this.mListAdapter = adapter;
        }

        @Override // android.support.v7.internal.widget.SpinnerICS.SpinnerPopup
        public void setPromptText(CharSequence hintText) {
            this.mPrompt = hintText;
        }

        @Override // android.support.v7.internal.widget.SpinnerICS.SpinnerPopup
        public CharSequence getHintText() {
            return this.mPrompt;
        }

        @Override // android.support.v7.internal.widget.SpinnerICS.SpinnerPopup
        public void show() {
            AlertDialog.Builder builder = new AlertDialog.Builder(SpinnerICS.this.getContext());
            if (this.mPrompt != null) {
                builder.setTitle(this.mPrompt);
            }
            this.mPopup = builder.setSingleChoiceItems(this.mListAdapter, SpinnerICS.this.getSelectedItemPosition(), this).show();
        }

        @Override // android.content.DialogInterface.OnClickListener
        public void onClick(DialogInterface dialog, int which) {
            SpinnerICS.this.setSelection(which);
            if (SpinnerICS.this.mOnItemClickListener != null) {
                SpinnerICS.this.performItemClick(null, which, this.mListAdapter.getItemId(which));
            }
            dismiss();
        }
    }

    /* loaded from: classes.dex */
    private class DropdownPopup extends ListPopupWindow implements SpinnerPopup {
        private ListAdapter mAdapter;
        private CharSequence mHintText;

        public DropdownPopup(Context context, AttributeSet attrs, int defStyleRes) {
            super(context, attrs, defStyleRes);
            setAnchorView(SpinnerICS.this);
            setModal(true);
            setPromptPosition(0);
            AdapterView.OnItemClickListener listener = new AdapterViewICS.OnItemClickListenerWrapper(new AdapterViewICS.OnItemClickListener() { // from class: android.support.v7.internal.widget.SpinnerICS.DropdownPopup.1
                @Override // android.support.v7.internal.widget.AdapterViewICS.OnItemClickListener
                public void onItemClick(AdapterViewICS parent, View v, int position, long id) {
                    SpinnerICS.this.setSelection(position);
                    if (SpinnerICS.this.mOnItemClickListener != null) {
                        SpinnerICS.this.performItemClick(v, position, DropdownPopup.this.mAdapter.getItemId(position));
                    }
                    DropdownPopup.this.dismiss();
                }
            });
            setOnItemClickListener(listener);
        }

        @Override // android.support.v7.internal.widget.ListPopupWindow, android.support.v7.internal.widget.SpinnerICS.SpinnerPopup
        public void setAdapter(ListAdapter adapter) {
            super.setAdapter(adapter);
            this.mAdapter = adapter;
        }

        @Override // android.support.v7.internal.widget.SpinnerICS.SpinnerPopup
        public CharSequence getHintText() {
            return this.mHintText;
        }

        @Override // android.support.v7.internal.widget.SpinnerICS.SpinnerPopup
        public void setPromptText(CharSequence hintText) {
            this.mHintText = hintText;
        }

        @Override // android.support.v7.internal.widget.ListPopupWindow, android.support.v7.internal.widget.SpinnerICS.SpinnerPopup
        public void show() {
            int spinnerPaddingLeft = SpinnerICS.this.getPaddingLeft();
            if (SpinnerICS.this.mDropDownWidth == -2) {
                int spinnerWidth = SpinnerICS.this.getWidth();
                int spinnerPaddingRight = SpinnerICS.this.getPaddingRight();
                setContentWidth(Math.max(SpinnerICS.this.measureContentWidth((SpinnerAdapter) this.mAdapter, getBackground()), (spinnerWidth - spinnerPaddingLeft) - spinnerPaddingRight));
            } else if (SpinnerICS.this.mDropDownWidth == -1) {
                int spinnerWidth2 = SpinnerICS.this.getWidth();
                int spinnerPaddingRight2 = SpinnerICS.this.getPaddingRight();
                setContentWidth((spinnerWidth2 - spinnerPaddingLeft) - spinnerPaddingRight2);
            } else {
                setContentWidth(SpinnerICS.this.mDropDownWidth);
            }
            Drawable background = getBackground();
            int bgOffset = 0;
            if (background != null) {
                background.getPadding(SpinnerICS.this.mTempRect);
                bgOffset = -SpinnerICS.this.mTempRect.left;
            }
            setHorizontalOffset(bgOffset + spinnerPaddingLeft);
            setInputMethodMode(2);
            super.show();
            getListView().setChoiceMode(1);
            setSelection(SpinnerICS.this.getSelectedItemPosition());
        }
    }
}
