package android.support.v7.app;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.appcompat.R;
import android.support.v7.internal.view.menu.ListMenuPresenter;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.support.v7.internal.view.menu.MenuPresenter;
import android.support.v7.internal.view.menu.MenuView;
import android.support.v7.internal.view.menu.MenuWrapperFactory;
import android.support.v7.internal.widget.ActionBarContainer;
import android.support.v7.internal.widget.ActionBarContextView;
import android.support.v7.internal.widget.ActionBarView;
import android.support.v7.internal.widget.ProgressBarICS;
import android.support.v7.view.ActionMode;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class ActionBarActivityDelegateBase extends ActionBarActivityDelegate implements MenuPresenter.Callback, MenuBuilder.Callback {
    private static final int[] ACTION_BAR_DRAWABLE_TOGGLE_ATTRS = {R.attr.homeAsUpIndicator};
    private static final String TAG = "ActionBarActivityDelegateBase";
    private ActionBarView mActionBarView;
    private ActionMode mActionMode;
    private boolean mClosingActionMenu;
    private boolean mFeatureIndeterminateProgress;
    private boolean mFeatureProgress;
    private ListMenuPresenter mListMenuPresenter;
    private MenuBuilder mMenu;
    private Bundle mPanelFrozenActionViewState;
    private boolean mPanelIsPrepared;
    private boolean mPanelRefreshContent;
    private boolean mSubDecorInstalled;
    private CharSequence mTitleToSet;

    /* JADX INFO: Access modifiers changed from: package-private */
    public ActionBarActivityDelegateBase(ActionBarActivity activity) {
        super(activity);
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public ActionBar createSupportActionBar() {
        ensureSubDecor();
        return new ActionBarImplBase(this.mActivity, this.mActivity);
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public void onConfigurationChanged(Configuration newConfig) {
        if (this.mHasActionBar && this.mSubDecorInstalled) {
            ActionBarImplBase actionBar = (ActionBarImplBase) getSupportActionBar();
            actionBar.onConfigurationChanged(newConfig);
        }
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public void onStop() {
        ActionBarImplBase ab = (ActionBarImplBase) getSupportActionBar();
        if (ab != null) {
            ab.setShowHideAnimationEnabled(false);
        }
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public void onPostResume() {
        ActionBarImplBase ab = (ActionBarImplBase) getSupportActionBar();
        if (ab != null) {
            ab.setShowHideAnimationEnabled(true);
        }
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public void setContentView(View v) {
        ensureSubDecor();
        ViewGroup contentParent = (ViewGroup) this.mActivity.findViewById(16908290);
        contentParent.removeAllViews();
        contentParent.addView(v);
        this.mActivity.onSupportContentChanged();
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public void setContentView(int resId) {
        ensureSubDecor();
        ViewGroup contentParent = (ViewGroup) this.mActivity.findViewById(16908290);
        contentParent.removeAllViews();
        this.mActivity.getLayoutInflater().inflate(resId, contentParent);
        this.mActivity.onSupportContentChanged();
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public void setContentView(View v, ViewGroup.LayoutParams lp) {
        ensureSubDecor();
        ViewGroup contentParent = (ViewGroup) this.mActivity.findViewById(16908290);
        contentParent.removeAllViews();
        contentParent.addView(v, lp);
        this.mActivity.onSupportContentChanged();
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public void addContentView(View v, ViewGroup.LayoutParams lp) {
        ensureSubDecor();
        ViewGroup contentParent = (ViewGroup) this.mActivity.findViewById(16908290);
        contentParent.addView(v, lp);
        this.mActivity.onSupportContentChanged();
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public void onContentChanged() {
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final void ensureSubDecor() {
        boolean splitActionBar;
        if (!this.mSubDecorInstalled) {
            if (this.mHasActionBar) {
                if (this.mOverlayActionBar) {
                    this.mActivity.superSetContentView(R.layout.abc_action_bar_decor_overlay);
                } else {
                    this.mActivity.superSetContentView(R.layout.abc_action_bar_decor);
                }
                this.mActionBarView = (ActionBarView) this.mActivity.findViewById(R.id.action_bar);
                this.mActionBarView.setWindowCallback(this.mActivity);
                if (this.mFeatureProgress) {
                    this.mActionBarView.initProgress();
                }
                if (this.mFeatureIndeterminateProgress) {
                    this.mActionBarView.initIndeterminateProgress();
                }
                boolean splitWhenNarrow = "splitActionBarWhenNarrow".equals(getUiOptionsFromMetadata());
                if (splitWhenNarrow) {
                    splitActionBar = this.mActivity.getResources().getBoolean(R.bool.abc_split_action_bar_is_narrow);
                } else {
                    TypedArray a = this.mActivity.obtainStyledAttributes(R.styleable.ActionBarWindow);
                    splitActionBar = a.getBoolean(2, false);
                    a.recycle();
                }
                ActionBarContainer splitView = (ActionBarContainer) this.mActivity.findViewById(R.id.split_action_bar);
                if (splitView != null) {
                    this.mActionBarView.setSplitView(splitView);
                    this.mActionBarView.setSplitActionBar(splitActionBar);
                    this.mActionBarView.setSplitWhenNarrow(splitWhenNarrow);
                    ActionBarContextView cab = (ActionBarContextView) this.mActivity.findViewById(R.id.action_context_bar);
                    cab.setSplitView(splitView);
                    cab.setSplitActionBar(splitActionBar);
                    cab.setSplitWhenNarrow(splitWhenNarrow);
                }
            } else {
                this.mActivity.superSetContentView(R.layout.abc_simple_decor);
            }
            View content = this.mActivity.findViewById(16908290);
            content.setId(-1);
            View abcContent = this.mActivity.findViewById(R.id.action_bar_activity_content);
            abcContent.setId(16908290);
            if (this.mTitleToSet != null) {
                this.mActionBarView.setWindowTitle(this.mTitleToSet);
                this.mTitleToSet = null;
            }
            applyFixedSizeWindow();
            this.mSubDecorInstalled = true;
            this.mActivity.getWindow().getDecorView().post(new Runnable() { // from class: android.support.v7.app.ActionBarActivityDelegateBase.1
                @Override // java.lang.Runnable
                public void run() {
                    ActionBarActivityDelegateBase.this.supportInvalidateOptionsMenu();
                }
            });
        }
    }

    private void applyFixedSizeWindow() {
        TypedArray a = this.mActivity.obtainStyledAttributes(R.styleable.ActionBarWindow);
        if (a.hasValue(3)) {
            mFixedWidthMajor = 0 == 0 ? new TypedValue() : null;
            a.getValue(3, mFixedWidthMajor);
        }
        if (a.hasValue(5)) {
            mFixedWidthMinor = 0 == 0 ? new TypedValue() : null;
            a.getValue(5, mFixedWidthMinor);
        }
        if (a.hasValue(6)) {
            mFixedHeightMajor = 0 == 0 ? new TypedValue() : null;
            a.getValue(6, mFixedHeightMajor);
        }
        if (a.hasValue(4)) {
            mFixedHeightMinor = 0 == 0 ? new TypedValue() : null;
            a.getValue(4, mFixedHeightMinor);
        }
        DisplayMetrics metrics = this.mActivity.getResources().getDisplayMetrics();
        boolean isPortrait = metrics.widthPixels < metrics.heightPixels;
        int w = -1;
        int h = -1;
        TypedValue tvw = isPortrait ? mFixedWidthMinor : mFixedWidthMajor;
        if (tvw != null && tvw.type != 0) {
            if (tvw.type == 5) {
                w = (int) tvw.getDimension(metrics);
            } else if (tvw.type == 6) {
                w = (int) tvw.getFraction(metrics.widthPixels, metrics.widthPixels);
            }
        }
        TypedValue tvh = isPortrait ? mFixedHeightMajor : mFixedHeightMinor;
        if (tvh != null && tvh.type != 0) {
            if (tvh.type == 5) {
                h = (int) tvh.getDimension(metrics);
            } else if (tvh.type == 6) {
                h = (int) tvh.getFraction(metrics.heightPixels, metrics.heightPixels);
            }
        }
        if (w != -1 || h != -1) {
            this.mActivity.getWindow().setLayout(w, h);
        }
        a.recycle();
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public boolean supportRequestWindowFeature(int featureId) {
        switch (featureId) {
            case 2:
                this.mFeatureProgress = true;
                return true;
            case 3:
            case 4:
            case 6:
            case 7:
            default:
                return this.mActivity.requestWindowFeature(featureId);
            case 5:
                this.mFeatureIndeterminateProgress = true;
                return true;
            case 8:
                this.mHasActionBar = true;
                return true;
            case 9:
                this.mOverlayActionBar = true;
                return true;
        }
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public void onTitleChanged(CharSequence title) {
        if (this.mActionBarView != null) {
            this.mActionBarView.setWindowTitle(title);
        } else {
            this.mTitleToSet = title;
        }
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public View onCreatePanelView(int featureId) {
        if (featureId != 0 || !preparePanel()) {
            return null;
        }
        View createdPanelView = (View) getListMenuView(this.mActivity, this);
        return createdPanelView;
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId != 0) {
            return this.mActivity.superOnCreatePanelMenu(featureId, menu);
        }
        return false;
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        if (featureId != 0) {
            return this.mActivity.superOnPreparePanel(featureId, view, menu);
        }
        return false;
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == 0) {
            item = MenuWrapperFactory.createMenuItemWrapper(item);
        }
        return this.mActivity.superOnMenuItemSelected(featureId, item);
    }

    @Override // android.support.v7.internal.view.menu.MenuBuilder.Callback
    public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
        return this.mActivity.onMenuItemSelected(0, item);
    }

    @Override // android.support.v7.internal.view.menu.MenuBuilder.Callback
    public void onMenuModeChange(MenuBuilder menu) {
        reopenMenu(menu, true);
    }

    @Override // android.support.v7.internal.view.menu.MenuPresenter.Callback
    public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        if (!this.mClosingActionMenu) {
            this.mClosingActionMenu = true;
            this.mActivity.closeOptionsMenu();
            this.mActionBarView.dismissPopupMenus();
            this.mClosingActionMenu = false;
        }
    }

    @Override // android.support.v7.internal.view.menu.MenuPresenter.Callback
    public boolean onOpenSubMenu(MenuBuilder subMenu) {
        return false;
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public ActionMode startSupportActionMode(ActionMode.Callback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("ActionMode callback can not be null.");
        }
        if (this.mActionMode != null) {
            this.mActionMode.finish();
        }
        ActionMode.Callback wrappedCallback = new ActionModeCallbackWrapper(callback);
        ActionBarImplBase ab = (ActionBarImplBase) getSupportActionBar();
        if (ab != null) {
            this.mActionMode = ab.startActionMode(wrappedCallback);
        }
        if (this.mActionMode != null) {
            this.mActivity.onSupportActionModeStarted(this.mActionMode);
        }
        return this.mActionMode;
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public void supportInvalidateOptionsMenu() {
        if (this.mMenu != null) {
            Bundle savedActionViewStates = new Bundle();
            this.mMenu.saveActionViewStates(savedActionViewStates);
            if (savedActionViewStates.size() > 0) {
                this.mPanelFrozenActionViewState = savedActionViewStates;
            }
            this.mMenu.stopDispatchingItemsChanged();
            this.mMenu.clear();
        }
        this.mPanelRefreshContent = true;
        if (this.mActionBarView != null) {
            this.mPanelIsPrepared = false;
            preparePanel();
        }
    }

    private void reopenMenu(MenuBuilder menu, boolean toggleMenuMode) {
        if (this.mActionBarView != null && this.mActionBarView.isOverflowReserved()) {
            if (!this.mActionBarView.isOverflowMenuShowing() || !toggleMenuMode) {
                if (this.mActionBarView.getVisibility() == 0) {
                    this.mActionBarView.showOverflowMenu();
                    return;
                }
                return;
            }
            this.mActionBarView.hideOverflowMenu();
            return;
        }
        menu.close();
    }

    private MenuView getListMenuView(Context context, MenuPresenter.Callback cb) {
        if (this.mMenu == null) {
            return null;
        }
        if (this.mListMenuPresenter == null) {
            TypedArray a = context.obtainStyledAttributes(R.styleable.Theme);
            int listPresenterTheme = a.getResourceId(4, R.style.Theme_AppCompat_CompactMenu);
            a.recycle();
            this.mListMenuPresenter = new ListMenuPresenter(R.layout.abc_list_menu_item_layout, listPresenterTheme);
            this.mListMenuPresenter.setCallback(cb);
            this.mMenu.addMenuPresenter(this.mListMenuPresenter);
        } else {
            this.mListMenuPresenter.updateMenuView(false);
        }
        return this.mListMenuPresenter.getMenuView(new FrameLayout(context));
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    public boolean onBackPressed() {
        if (this.mActionMode != null) {
            this.mActionMode.finish();
            return true;
        } else if (this.mActionBarView != null && this.mActionBarView.hasExpandedActionView()) {
            this.mActionBarView.collapseActionView();
            return true;
        } else {
            return false;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // android.support.v7.app.ActionBarActivityDelegate
    public void setSupportProgressBarVisibility(boolean visible) {
        updateProgressBars(visible ? -1 : -2);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // android.support.v7.app.ActionBarActivityDelegate
    public void setSupportProgressBarIndeterminateVisibility(boolean visible) {
        updateProgressBars(visible ? -1 : -2);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // android.support.v7.app.ActionBarActivityDelegate
    public void setSupportProgressBarIndeterminate(boolean indeterminate) {
        updateProgressBars(indeterminate ? -3 : -4);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    @Override // android.support.v7.app.ActionBarActivityDelegate
    public void setSupportProgress(int progress) {
        updateProgressBars(progress + 0);
    }

    @Override // android.support.v7.app.ActionBarActivityDelegate
    int getHomeAsUpIndicatorAttrId() {
        return R.attr.homeAsUpIndicator;
    }

    private void updateProgressBars(int value) {
        ProgressBarICS circularProgressBar = getCircularProgressBar();
        ProgressBarICS horizontalProgressBar = getHorizontalProgressBar();
        if (value == -1) {
            if (this.mFeatureProgress) {
                int level = horizontalProgressBar.getProgress();
                int visibility = (horizontalProgressBar.isIndeterminate() || level < 10000) ? 0 : 4;
                horizontalProgressBar.setVisibility(visibility);
            }
            if (this.mFeatureIndeterminateProgress) {
                circularProgressBar.setVisibility(0);
            }
        } else if (value == -2) {
            if (this.mFeatureProgress) {
                horizontalProgressBar.setVisibility(8);
            }
            if (this.mFeatureIndeterminateProgress) {
                circularProgressBar.setVisibility(8);
            }
        } else if (value == -3) {
            horizontalProgressBar.setIndeterminate(true);
        } else if (value == -4) {
            horizontalProgressBar.setIndeterminate(false);
        } else if (value >= 0 && value <= 10000) {
            horizontalProgressBar.setProgress(value + 0);
            if (value < 10000) {
                showProgressBars(horizontalProgressBar, circularProgressBar);
            } else {
                hideProgressBars(horizontalProgressBar, circularProgressBar);
            }
        }
    }

    private void showProgressBars(ProgressBarICS horizontalProgressBar, ProgressBarICS spinnyProgressBar) {
        if (this.mFeatureIndeterminateProgress && spinnyProgressBar.getVisibility() == 4) {
            spinnyProgressBar.setVisibility(0);
        }
        if (this.mFeatureProgress && horizontalProgressBar.getProgress() < 10000) {
            horizontalProgressBar.setVisibility(0);
        }
    }

    private void hideProgressBars(ProgressBarICS horizontalProgressBar, ProgressBarICS spinnyProgressBar) {
        if (this.mFeatureIndeterminateProgress && spinnyProgressBar.getVisibility() == 0) {
            spinnyProgressBar.setVisibility(4);
        }
        if (this.mFeatureProgress && horizontalProgressBar.getVisibility() == 0) {
            horizontalProgressBar.setVisibility(4);
        }
    }

    private ProgressBarICS getCircularProgressBar() {
        ProgressBarICS pb = (ProgressBarICS) this.mActionBarView.findViewById(R.id.progress_circular);
        if (pb != null) {
            pb.setVisibility(4);
        }
        return pb;
    }

    private ProgressBarICS getHorizontalProgressBar() {
        ProgressBarICS pb = (ProgressBarICS) this.mActionBarView.findViewById(R.id.progress_horizontal);
        if (pb != null) {
            pb.setVisibility(4);
        }
        return pb;
    }

    private boolean initializePanelMenu() {
        this.mMenu = new MenuBuilder(getActionBarThemedContext());
        this.mMenu.setCallback(this);
        return true;
    }

    private boolean preparePanel() {
        if (this.mPanelIsPrepared) {
            return true;
        }
        if (this.mMenu == null || this.mPanelRefreshContent) {
            if (this.mMenu == null && (!initializePanelMenu() || this.mMenu == null)) {
                return false;
            }
            if (this.mActionBarView != null) {
                this.mActionBarView.setMenu(this.mMenu, this);
            }
            this.mMenu.stopDispatchingItemsChanged();
            if (!this.mActivity.superOnCreatePanelMenu(0, this.mMenu)) {
                this.mMenu = null;
                if (this.mActionBarView != null) {
                    this.mActionBarView.setMenu(null, this);
                }
                return false;
            }
            this.mPanelRefreshContent = false;
        }
        this.mMenu.stopDispatchingItemsChanged();
        if (this.mPanelFrozenActionViewState != null) {
            this.mMenu.restoreActionViewStates(this.mPanelFrozenActionViewState);
            this.mPanelFrozenActionViewState = null;
        }
        if (!this.mActivity.superOnPreparePanel(0, null, this.mMenu)) {
            if (this.mActionBarView != null) {
                this.mActionBarView.setMenu(null, this);
            }
            this.mMenu.startDispatchingItemsChanged();
            return false;
        }
        this.mMenu.startDispatchingItemsChanged();
        this.mPanelIsPrepared = true;
        return true;
    }

    /* loaded from: classes.dex */
    private class ActionModeCallbackWrapper implements ActionMode.Callback {
        private ActionMode.Callback mWrapped;

        public ActionModeCallbackWrapper(ActionMode.Callback wrapped) {
            this.mWrapped = wrapped;
        }

        @Override // android.support.v7.view.ActionMode.Callback
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return this.mWrapped.onCreateActionMode(mode, menu);
        }

        @Override // android.support.v7.view.ActionMode.Callback
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return this.mWrapped.onPrepareActionMode(mode, menu);
        }

        @Override // android.support.v7.view.ActionMode.Callback
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return this.mWrapped.onActionItemClicked(mode, item);
        }

        @Override // android.support.v7.view.ActionMode.Callback
        public void onDestroyActionMode(ActionMode mode) {
            this.mWrapped.onDestroyActionMode(mode);
            ActionBarActivityDelegateBase.this.mActivity.onSupportActionModeFinished(mode);
            ActionBarActivityDelegateBase.this.mActionMode = null;
        }
    }
}
