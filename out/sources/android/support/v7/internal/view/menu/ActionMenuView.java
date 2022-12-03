package android.support.v7.internal.view.menu;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.widget.ExploreByTouchHelper;
import android.support.v7.appcompat.R;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.support.v7.internal.widget.ActivityChooserView;
import android.support.v7.internal.widget.LinearLayoutICS;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;
/* loaded from: classes.dex */
public class ActionMenuView extends LinearLayoutICS implements MenuBuilder.ItemInvoker, MenuView {
    static final int GENERATED_ITEM_PADDING = 4;
    static final int MIN_CELL_SIZE = 56;
    private static final String TAG = "ActionMenuView";
    private boolean mFormatItems;
    private int mFormatItemsWidth;
    private int mGeneratedItemPadding;
    private int mMaxItemHeight;
    private int mMeasuredExtraWidth;
    private MenuBuilder mMenu;
    private int mMinCellSize;
    private ActionMenuPresenter mPresenter;
    private boolean mReserveOverflow;

    /* loaded from: classes.dex */
    public interface ActionMenuChildView {
        boolean needsDividerAfter();

        boolean needsDividerBefore();
    }

    public ActionMenuView(Context context) {
        this(context, null);
    }

    public ActionMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBaselineAligned(false);
        float density = context.getResources().getDisplayMetrics().density;
        this.mMinCellSize = (int) (56.0f * density);
        this.mGeneratedItemPadding = (int) (4.0f * density);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ActionBar, R.attr.actionBarStyle, 0);
        this.mMaxItemHeight = a.getDimensionPixelSize(0, 0);
        a.recycle();
    }

    public void setPresenter(ActionMenuPresenter presenter) {
        this.mPresenter = presenter;
    }

    public boolean isExpandedFormat() {
        return this.mFormatItems;
    }

    @Override // android.view.View
    public void onConfigurationChanged(Configuration newConfig) {
        if (Build.VERSION.SDK_INT >= 8) {
            super.onConfigurationChanged(newConfig);
        }
        this.mPresenter.updateMenuView(false);
        if (this.mPresenter != null && this.mPresenter.isOverflowMenuShowing()) {
            this.mPresenter.hideOverflowMenu();
            this.mPresenter.showOverflowMenu();
        }
    }

    @Override // android.widget.LinearLayout, android.view.View
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        boolean wasFormatted = this.mFormatItems;
        this.mFormatItems = View.MeasureSpec.getMode(widthMeasureSpec) == 1073741824;
        if (wasFormatted != this.mFormatItems) {
            this.mFormatItemsWidth = 0;
        }
        int widthSize = View.MeasureSpec.getMode(widthMeasureSpec);
        if (this.mFormatItems && this.mMenu != null && widthSize != this.mFormatItemsWidth) {
            this.mFormatItemsWidth = widthSize;
            this.mMenu.onItemsChanged(true);
        }
        if (this.mFormatItems) {
            onMeasureExactFormat(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            lp.rightMargin = 0;
            lp.leftMargin = 0;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void onMeasureExactFormat(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
        int widthPadding = getPaddingLeft() + getPaddingRight();
        int heightPadding = getPaddingTop() + getPaddingBottom();
        int itemHeightSpec = heightMode == 1073741824 ? View.MeasureSpec.makeMeasureSpec(heightSize - heightPadding, 1073741824) : View.MeasureSpec.makeMeasureSpec(Math.min(this.mMaxItemHeight, heightSize - heightPadding), ExploreByTouchHelper.INVALID_ID);
        int widthSize2 = widthSize - widthPadding;
        int cellCount = widthSize2 / this.mMinCellSize;
        int cellSizeRemaining = widthSize2 % this.mMinCellSize;
        if (cellCount == 0) {
            setMeasuredDimension(widthSize2, 0);
            return;
        }
        int cellSize = this.mMinCellSize + (cellSizeRemaining / cellCount);
        int cellsRemaining = cellCount;
        int maxChildHeight = 0;
        int maxCellsUsed = 0;
        int expandableItemCount = 0;
        int visibleItemCount = 0;
        boolean hasOverflow = false;
        long smallestItemsAt = 0;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                boolean isGeneratedItem = child instanceof ActionMenuItemView;
                visibleItemCount++;
                if (isGeneratedItem) {
                    child.setPadding(this.mGeneratedItemPadding, 0, this.mGeneratedItemPadding, 0);
                }
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                lp.expanded = false;
                lp.extraPixels = 0;
                lp.cellsUsed = 0;
                lp.expandable = false;
                lp.leftMargin = 0;
                lp.rightMargin = 0;
                lp.preventEdgeOffset = isGeneratedItem && ((ActionMenuItemView) child).hasText();
                int cellsAvailable = lp.isOverflowButton ? 1 : cellsRemaining;
                int cellsUsed = measureChildForCells(child, cellSize, cellsAvailable, itemHeightSpec, heightPadding);
                maxCellsUsed = Math.max(maxCellsUsed, cellsUsed);
                if (lp.expandable) {
                    expandableItemCount++;
                }
                if (lp.isOverflowButton) {
                    hasOverflow = true;
                }
                cellsRemaining -= cellsUsed;
                maxChildHeight = Math.max(maxChildHeight, child.getMeasuredHeight());
                if (cellsUsed == 1) {
                    smallestItemsAt |= 1 << i;
                }
            }
        }
        boolean centerSingleExpandedItem = hasOverflow && visibleItemCount == 2;
        boolean needsExpansion = false;
        while (expandableItemCount > 0 && cellsRemaining > 0) {
            int minCells = ActivityChooserView.ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED;
            long minCellsAt = 0;
            int minCellsItemCount = 0;
            for (int i2 = 0; i2 < childCount; i2++) {
                LayoutParams lp2 = (LayoutParams) getChildAt(i2).getLayoutParams();
                if (lp2.expandable) {
                    if (lp2.cellsUsed < minCells) {
                        minCells = lp2.cellsUsed;
                        minCellsAt = 1 << i2;
                        minCellsItemCount = 1;
                    } else if (lp2.cellsUsed == minCells) {
                        minCellsAt |= 1 << i2;
                        minCellsItemCount++;
                    }
                }
            }
            smallestItemsAt |= minCellsAt;
            if (minCellsItemCount > cellsRemaining) {
                break;
            }
            int minCells2 = minCells + 1;
            for (int i3 = 0; i3 < childCount; i3++) {
                View child2 = getChildAt(i3);
                LayoutParams lp3 = (LayoutParams) child2.getLayoutParams();
                if (((1 << i3) & minCellsAt) == 0) {
                    if (lp3.cellsUsed == minCells2) {
                        smallestItemsAt |= 1 << i3;
                    }
                } else {
                    if (centerSingleExpandedItem && lp3.preventEdgeOffset && cellsRemaining == 1) {
                        child2.setPadding(this.mGeneratedItemPadding + cellSize, 0, this.mGeneratedItemPadding, 0);
                    }
                    lp3.cellsUsed++;
                    lp3.expanded = true;
                    cellsRemaining--;
                }
            }
            needsExpansion = true;
        }
        boolean singleItem = !hasOverflow && visibleItemCount == 1;
        if (cellsRemaining > 0 && smallestItemsAt != 0 && (cellsRemaining < visibleItemCount - 1 || singleItem || maxCellsUsed > 1)) {
            float expandCount = Long.bitCount(smallestItemsAt);
            if (!singleItem) {
                if ((1 & smallestItemsAt) != 0 && !((LayoutParams) getChildAt(0).getLayoutParams()).preventEdgeOffset) {
                    expandCount -= 0.5f;
                }
                if (((1 << (childCount - 1)) & smallestItemsAt) != 0 && !((LayoutParams) getChildAt(childCount - 1).getLayoutParams()).preventEdgeOffset) {
                    expandCount -= 0.5f;
                }
            }
            int extraPixels = expandCount > 0.0f ? (int) ((cellsRemaining * cellSize) / expandCount) : 0;
            for (int i4 = 0; i4 < childCount; i4++) {
                if (((1 << i4) & smallestItemsAt) != 0) {
                    View child3 = getChildAt(i4);
                    LayoutParams lp4 = (LayoutParams) child3.getLayoutParams();
                    if (child3 instanceof ActionMenuItemView) {
                        lp4.extraPixels = extraPixels;
                        lp4.expanded = true;
                        if (i4 == 0 && !lp4.preventEdgeOffset) {
                            lp4.leftMargin = (-extraPixels) / 2;
                        }
                        needsExpansion = true;
                    } else if (lp4.isOverflowButton) {
                        lp4.extraPixels = extraPixels;
                        lp4.expanded = true;
                        lp4.rightMargin = (-extraPixels) / 2;
                        needsExpansion = true;
                    } else {
                        if (i4 != 0) {
                            lp4.leftMargin = extraPixels / 2;
                        }
                        if (i4 != childCount - 1) {
                            lp4.rightMargin = extraPixels / 2;
                        }
                    }
                }
            }
            cellsRemaining = 0;
        }
        if (needsExpansion) {
            for (int i5 = 0; i5 < childCount; i5++) {
                View child4 = getChildAt(i5);
                LayoutParams lp5 = (LayoutParams) child4.getLayoutParams();
                if (lp5.expanded) {
                    int width = (lp5.cellsUsed * cellSize) + lp5.extraPixels;
                    child4.measure(View.MeasureSpec.makeMeasureSpec(width, 1073741824), itemHeightSpec);
                }
            }
        }
        if (heightMode != 1073741824) {
            heightSize = maxChildHeight;
        }
        setMeasuredDimension(widthSize2, heightSize);
        this.mMeasuredExtraWidth = cellsRemaining * cellSize;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static int measureChildForCells(View child, int cellSize, int cellsRemaining, int parentHeightMeasureSpec, int parentHeightPadding) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        int childHeightSize = View.MeasureSpec.getSize(parentHeightMeasureSpec) - parentHeightPadding;
        int childHeightMode = View.MeasureSpec.getMode(parentHeightMeasureSpec);
        int childHeightSpec = View.MeasureSpec.makeMeasureSpec(childHeightSize, childHeightMode);
        ActionMenuItemView itemView = child instanceof ActionMenuItemView ? (ActionMenuItemView) child : null;
        boolean hasText = itemView != null && itemView.hasText();
        int cellsUsed = 0;
        if (cellsRemaining > 0 && (!hasText || cellsRemaining >= 2)) {
            int childWidthSpec = View.MeasureSpec.makeMeasureSpec(cellSize * cellsRemaining, ExploreByTouchHelper.INVALID_ID);
            child.measure(childWidthSpec, childHeightSpec);
            int measuredWidth = child.getMeasuredWidth();
            cellsUsed = measuredWidth / cellSize;
            if (measuredWidth % cellSize != 0) {
                cellsUsed++;
            }
            if (hasText && cellsUsed < 2) {
                cellsUsed = 2;
            }
        }
        boolean expandable = !lp.isOverflowButton && hasText;
        lp.expandable = expandable;
        lp.cellsUsed = cellsUsed;
        int targetWidth = cellsUsed * cellSize;
        child.measure(View.MeasureSpec.makeMeasureSpec(targetWidth, 1073741824), childHeightSpec);
        return cellsUsed;
    }

    @Override // android.widget.LinearLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!this.mFormatItems) {
            super.onLayout(changed, left, top, right, bottom);
            return;
        }
        int childCount = getChildCount();
        int midVertical = (top + bottom) / 2;
        int dividerWidth = getSupportDividerWidth();
        int nonOverflowWidth = 0;
        int nonOverflowCount = 0;
        int widthRemaining = ((right - left) - getPaddingRight()) - getPaddingLeft();
        boolean hasOverflow = false;
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            if (v.getVisibility() != 8) {
                LayoutParams p = (LayoutParams) v.getLayoutParams();
                if (p.isOverflowButton) {
                    int overflowWidth = v.getMeasuredWidth();
                    if (hasSupportDividerBeforeChildAt(i)) {
                        overflowWidth += dividerWidth;
                    }
                    int height = v.getMeasuredHeight();
                    int r = (getWidth() - getPaddingRight()) - p.rightMargin;
                    int t = midVertical - (height / 2);
                    int b = t + height;
                    v.layout(r - overflowWidth, t, r, b);
                    widthRemaining -= overflowWidth;
                    hasOverflow = true;
                } else {
                    int size = v.getMeasuredWidth() + p.leftMargin + p.rightMargin;
                    nonOverflowWidth += size;
                    widthRemaining -= size;
                    if (hasSupportDividerBeforeChildAt(i)) {
                        nonOverflowWidth += dividerWidth;
                    }
                    nonOverflowCount++;
                }
            }
        }
        if (childCount == 1 && !hasOverflow) {
            View v2 = getChildAt(0);
            int width = v2.getMeasuredWidth();
            int height2 = v2.getMeasuredHeight();
            int midHorizontal = (right - left) / 2;
            int l = midHorizontal - (width / 2);
            int t2 = midVertical - (height2 / 2);
            v2.layout(l, t2, l + width, t2 + height2);
            return;
        }
        int spacerCount = nonOverflowCount - (hasOverflow ? 0 : 1);
        int spacerSize = Math.max(0, spacerCount > 0 ? widthRemaining / spacerCount : 0);
        int startLeft = getPaddingLeft();
        for (int i2 = 0; i2 < childCount; i2++) {
            View v3 = getChildAt(i2);
            LayoutParams lp = (LayoutParams) v3.getLayoutParams();
            if (v3.getVisibility() != 8 && !lp.isOverflowButton) {
                int startLeft2 = startLeft + lp.leftMargin;
                int width2 = v3.getMeasuredWidth();
                int height3 = v3.getMeasuredHeight();
                int t3 = midVertical - (height3 / 2);
                v3.layout(startLeft2, t3, startLeft2 + width2, t3 + height3);
                startLeft = startLeft2 + lp.rightMargin + width2 + spacerSize;
            }
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mPresenter.dismissPopupMenus();
    }

    public boolean isOverflowReserved() {
        return this.mReserveOverflow;
    }

    public void setOverflowReserved(boolean reserveOverflow) {
        this.mReserveOverflow = reserveOverflow;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.ViewGroup
    public LayoutParams generateDefaultLayoutParams() {
        LayoutParams params = new LayoutParams(-2, -2);
        params.gravity = 16;
        return params;
    }

    @Override // android.widget.LinearLayout, android.view.ViewGroup
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.ViewGroup
    public LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        if (p instanceof LayoutParams) {
            LayoutParams result = new LayoutParams((LayoutParams) p);
            if (result.gravity <= 0) {
                result.gravity = 16;
                return result;
            }
            return result;
        }
        return generateDefaultLayoutParams();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.widget.LinearLayout, android.view.ViewGroup
    public boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p != null && (p instanceof LayoutParams);
    }

    public LayoutParams generateOverflowButtonLayoutParams() {
        LayoutParams result = generateDefaultLayoutParams();
        result.isOverflowButton = true;
        return result;
    }

    @Override // android.support.v7.internal.view.menu.MenuBuilder.ItemInvoker
    public boolean invokeItem(MenuItemImpl item) {
        return this.mMenu.performItemAction(item, 0);
    }

    @Override // android.support.v7.internal.view.menu.MenuView
    public int getWindowAnimations() {
        return 0;
    }

    @Override // android.support.v7.internal.view.menu.MenuView
    public void initialize(MenuBuilder menu) {
        this.mMenu = menu;
    }

    @Override // android.support.v7.internal.widget.LinearLayoutICS
    protected boolean hasSupportDividerBeforeChildAt(int childIndex) {
        View childBefore = getChildAt(childIndex - 1);
        View child = getChildAt(childIndex);
        boolean result = false;
        if (childIndex < getChildCount() && (childBefore instanceof ActionMenuChildView)) {
            result = false | ((ActionMenuChildView) childBefore).needsDividerAfter();
        }
        if (childIndex > 0 && (child instanceof ActionMenuChildView)) {
            return result | ((ActionMenuChildView) child).needsDividerBefore();
        }
        return result;
    }

    @Override // android.view.View
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return false;
    }

    /* loaded from: classes.dex */
    public static class LayoutParams extends LinearLayout.LayoutParams {
        @ViewDebug.ExportedProperty
        public int cellsUsed;
        @ViewDebug.ExportedProperty
        public boolean expandable;
        public boolean expanded;
        @ViewDebug.ExportedProperty
        public int extraPixels;
        @ViewDebug.ExportedProperty
        public boolean isOverflowButton;
        @ViewDebug.ExportedProperty
        public boolean preventEdgeOffset;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(LayoutParams other) {
            super((LinearLayout.LayoutParams) other);
            this.isOverflowButton = other.isOverflowButton;
        }

        public LayoutParams(int width, int height) {
            super(width, height);
            this.isOverflowButton = false;
        }

        public LayoutParams(int width, int height, boolean isOverflowButton) {
            super(width, height);
            this.isOverflowButton = isOverflowButton;
        }
    }
}
