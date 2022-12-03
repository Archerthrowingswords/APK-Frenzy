package android.support.v7.app;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.appcompat.R;
import android.support.v7.internal.view.ActionBarPolicy;
import android.support.v7.internal.view.SupportMenuInflater;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.support.v7.internal.view.menu.SubMenuBuilder;
import android.support.v7.internal.widget.ActionBarContainer;
import android.support.v7.internal.widget.ActionBarContextView;
import android.support.v7.internal.widget.ActionBarOverlayLayout;
import android.support.v7.internal.widget.ActionBarView;
import android.support.v7.internal.widget.ScrollingTabContainerView;
import android.support.v7.view.ActionMode;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.SpinnerAdapter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class ActionBarImplBase extends ActionBar {
    private static final int CONTEXT_DISPLAY_NORMAL = 0;
    private static final int CONTEXT_DISPLAY_SPLIT = 1;
    private static final int INVALID_POSITION = -1;
    ActionModeImpl mActionMode;
    private ActionBarView mActionView;
    private ActionBarActivity mActivity;
    private ActionBar.Callback mCallback;
    private ActionBarContainer mContainerView;
    private View mContentView;
    private Context mContext;
    private int mContextDisplayMode;
    private ActionBarContextView mContextView;
    ActionMode mDeferredDestroyActionMode;
    ActionMode.Callback mDeferredModeDestroyCallback;
    private Dialog mDialog;
    private boolean mDisplayHomeAsUpSet;
    private boolean mHasEmbeddedTabs;
    private boolean mHiddenByApp;
    private boolean mHiddenBySystem;
    private boolean mLastMenuVisibility;
    private ActionBarOverlayLayout mOverlayLayout;
    private TabImpl mSelectedTab;
    private boolean mShowHideAnimationEnabled;
    private boolean mShowingForMode;
    private ActionBarContainer mSplitView;
    private ScrollingTabContainerView mTabScrollView;
    Runnable mTabSelector;
    private Context mThemedContext;
    private ViewGroup mTopVisibilityView;
    private ArrayList<TabImpl> mTabs = new ArrayList<>();
    private int mSavedTabPosition = -1;
    private ArrayList<ActionBar.OnMenuVisibilityListener> mMenuVisibilityListeners = new ArrayList<>();
    final Handler mHandler = new Handler();
    private int mCurWindowVisibility = 0;
    private boolean mNowShowing = true;

    public ActionBarImplBase(ActionBarActivity activity, ActionBar.Callback callback) {
        this.mActivity = activity;
        this.mContext = activity;
        this.mCallback = callback;
        init(this.mActivity);
    }

    private void init(ActionBarActivity activity) {
        boolean z = false;
        this.mOverlayLayout = (ActionBarOverlayLayout) activity.findViewById(R.id.action_bar_overlay_layout);
        if (this.mOverlayLayout != null) {
            this.mOverlayLayout.setActionBar(this);
        }
        this.mActionView = (ActionBarView) activity.findViewById(R.id.action_bar);
        this.mContextView = (ActionBarContextView) activity.findViewById(R.id.action_context_bar);
        this.mContainerView = (ActionBarContainer) activity.findViewById(R.id.action_bar_container);
        this.mTopVisibilityView = (ViewGroup) activity.findViewById(R.id.top_action_bar);
        if (this.mTopVisibilityView == null) {
            this.mTopVisibilityView = this.mContainerView;
        }
        this.mSplitView = (ActionBarContainer) activity.findViewById(R.id.split_action_bar);
        if (this.mActionView == null || this.mContextView == null || this.mContainerView == null) {
            throw new IllegalStateException(getClass().getSimpleName() + " can only be used with a compatible window decor layout");
        }
        this.mActionView.setContextView(this.mContextView);
        this.mContextDisplayMode = this.mActionView.isSplitActionBar() ? 1 : 0;
        int current = this.mActionView.getDisplayOptions();
        boolean homeAsUp = (current & 4) != 0;
        if (homeAsUp) {
            this.mDisplayHomeAsUpSet = true;
        }
        ActionBarPolicy abp = ActionBarPolicy.get(this.mContext);
        if (abp.enableHomeButtonByDefault() || homeAsUp) {
            z = true;
        }
        setHomeButtonEnabled(z);
        setHasEmbeddedTabs(abp.hasEmbeddedTabs());
        setTitle(this.mActivity.getTitle());
    }

    public void onConfigurationChanged(Configuration newConfig) {
        setHasEmbeddedTabs(ActionBarPolicy.get(this.mContext).hasEmbeddedTabs());
    }

    private void setHasEmbeddedTabs(boolean hasEmbeddedTabs) {
        boolean z = true;
        this.mHasEmbeddedTabs = hasEmbeddedTabs;
        if (!this.mHasEmbeddedTabs) {
            this.mActionView.setEmbeddedTabView(null);
            this.mContainerView.setTabContainer(this.mTabScrollView);
        } else {
            this.mContainerView.setTabContainer(null);
            this.mActionView.setEmbeddedTabView(this.mTabScrollView);
        }
        boolean isInTabMode = getNavigationMode() == 2;
        if (this.mTabScrollView != null) {
            if (isInTabMode) {
                this.mTabScrollView.setVisibility(0);
            } else {
                this.mTabScrollView.setVisibility(8);
            }
        }
        ActionBarView actionBarView = this.mActionView;
        if (this.mHasEmbeddedTabs || !isInTabMode) {
            z = false;
        }
        actionBarView.setCollapsable(z);
    }

    public boolean hasNonEmbeddedTabs() {
        return !this.mHasEmbeddedTabs && getNavigationMode() == 2;
    }

    @Override // android.support.v7.app.ActionBar
    public void setCustomView(View view) {
        this.mActionView.setCustomNavigationView(view);
    }

    @Override // android.support.v7.app.ActionBar
    public void setCustomView(View view, ActionBar.LayoutParams layoutParams) {
        view.setLayoutParams(layoutParams);
        this.mActionView.setCustomNavigationView(view);
    }

    @Override // android.support.v7.app.ActionBar
    public void setCustomView(int resId) {
        setCustomView(LayoutInflater.from(getThemedContext()).inflate(resId, (ViewGroup) this.mActionView, false));
    }

    @Override // android.support.v7.app.ActionBar
    public void setIcon(int resId) {
        this.mActionView.setIcon(resId);
    }

    @Override // android.support.v7.app.ActionBar
    public void setIcon(Drawable icon) {
        this.mActionView.setIcon(icon);
    }

    @Override // android.support.v7.app.ActionBar
    public void setLogo(int resId) {
        this.mActionView.setLogo(resId);
    }

    @Override // android.support.v7.app.ActionBar
    public void setLogo(Drawable logo) {
        this.mActionView.setLogo(logo);
    }

    @Override // android.support.v7.app.ActionBar
    public void setListNavigationCallbacks(SpinnerAdapter adapter, ActionBar.OnNavigationListener callback) {
        this.mActionView.setDropdownAdapter(adapter);
        this.mActionView.setCallback(callback);
    }

    @Override // android.support.v7.app.ActionBar
    public void setSelectedNavigationItem(int position) {
        switch (this.mActionView.getNavigationMode()) {
            case 1:
                this.mActionView.setDropdownSelectedPosition(position);
                return;
            case 2:
                selectTab(this.mTabs.get(position));
                return;
            default:
                throw new IllegalStateException("setSelectedNavigationIndex not valid for current navigation mode");
        }
    }

    @Override // android.support.v7.app.ActionBar
    public int getSelectedNavigationIndex() {
        switch (this.mActionView.getNavigationMode()) {
            case 1:
                return this.mActionView.getDropdownSelectedPosition();
            case 2:
                if (this.mSelectedTab != null) {
                    return this.mSelectedTab.getPosition();
                }
                return -1;
            default:
                return -1;
        }
    }

    @Override // android.support.v7.app.ActionBar
    public int getNavigationItemCount() {
        switch (this.mActionView.getNavigationMode()) {
            case 1:
                SpinnerAdapter adapter = this.mActionView.getDropdownAdapter();
                if (adapter != null) {
                    return adapter.getCount();
                }
                return 0;
            case 2:
                return this.mTabs.size();
            default:
                return 0;
        }
    }

    @Override // android.support.v7.app.ActionBar
    public void setTitle(CharSequence title) {
        this.mActionView.setTitle(title);
    }

    @Override // android.support.v7.app.ActionBar
    public void setTitle(int resId) {
        setTitle(this.mContext.getString(resId));
    }

    @Override // android.support.v7.app.ActionBar
    public void setSubtitle(CharSequence subtitle) {
        this.mActionView.setSubtitle(subtitle);
    }

    @Override // android.support.v7.app.ActionBar
    public void setSubtitle(int resId) {
        setSubtitle(this.mContext.getString(resId));
    }

    @Override // android.support.v7.app.ActionBar
    public void setDisplayOptions(int options) {
        if ((options & 4) != 0) {
            this.mDisplayHomeAsUpSet = true;
        }
        this.mActionView.setDisplayOptions(options);
    }

    @Override // android.support.v7.app.ActionBar
    public void setDisplayOptions(int options, int mask) {
        int current = this.mActionView.getDisplayOptions();
        if ((mask & 4) != 0) {
            this.mDisplayHomeAsUpSet = true;
        }
        this.mActionView.setDisplayOptions((options & mask) | ((mask ^ (-1)) & current));
    }

    @Override // android.support.v7.app.ActionBar
    public void setDisplayUseLogoEnabled(boolean useLogo) {
        setDisplayOptions(useLogo ? 1 : 0, 1);
    }

    @Override // android.support.v7.app.ActionBar
    public void setDisplayShowHomeEnabled(boolean showHome) {
        setDisplayOptions(showHome ? 2 : 0, 2);
    }

    @Override // android.support.v7.app.ActionBar
    public void setDisplayHomeAsUpEnabled(boolean showHomeAsUp) {
        setDisplayOptions(showHomeAsUp ? 4 : 0, 4);
    }

    @Override // android.support.v7.app.ActionBar
    public void setDisplayShowTitleEnabled(boolean showTitle) {
        setDisplayOptions(showTitle ? 8 : 0, 8);
    }

    @Override // android.support.v7.app.ActionBar
    public void setDisplayShowCustomEnabled(boolean showCustom) {
        setDisplayOptions(showCustom ? 16 : 0, 16);
    }

    @Override // android.support.v7.app.ActionBar
    public void setHomeButtonEnabled(boolean enable) {
        this.mActionView.setHomeButtonEnabled(enable);
    }

    @Override // android.support.v7.app.ActionBar
    public void setBackgroundDrawable(Drawable d) {
        this.mContainerView.setPrimaryBackground(d);
    }

    @Override // android.support.v7.app.ActionBar
    public void setStackedBackgroundDrawable(Drawable d) {
        this.mContainerView.setStackedBackground(d);
    }

    @Override // android.support.v7.app.ActionBar
    public void setSplitBackgroundDrawable(Drawable d) {
        this.mContainerView.setSplitBackground(d);
    }

    @Override // android.support.v7.app.ActionBar
    public View getCustomView() {
        return this.mActionView.getCustomNavigationView();
    }

    @Override // android.support.v7.app.ActionBar
    public CharSequence getTitle() {
        return this.mActionView.getTitle();
    }

    @Override // android.support.v7.app.ActionBar
    public CharSequence getSubtitle() {
        return this.mActionView.getSubtitle();
    }

    @Override // android.support.v7.app.ActionBar
    public int getNavigationMode() {
        return this.mActionView.getNavigationMode();
    }

    @Override // android.support.v7.app.ActionBar
    public void setNavigationMode(int mode) {
        boolean z = false;
        int oldMode = this.mActionView.getNavigationMode();
        switch (oldMode) {
            case 2:
                this.mSavedTabPosition = getSelectedNavigationIndex();
                selectTab(null);
                this.mTabScrollView.setVisibility(8);
                break;
        }
        this.mActionView.setNavigationMode(mode);
        switch (mode) {
            case 2:
                ensureTabsExist();
                this.mTabScrollView.setVisibility(0);
                if (this.mSavedTabPosition != -1) {
                    setSelectedNavigationItem(this.mSavedTabPosition);
                    this.mSavedTabPosition = -1;
                    break;
                }
                break;
        }
        ActionBarView actionBarView = this.mActionView;
        if (mode == 2 && !this.mHasEmbeddedTabs) {
            z = true;
        }
        actionBarView.setCollapsable(z);
    }

    @Override // android.support.v7.app.ActionBar
    public int getDisplayOptions() {
        return this.mActionView.getDisplayOptions();
    }

    @Override // android.support.v7.app.ActionBar
    public ActionBar.Tab newTab() {
        return new TabImpl();
    }

    @Override // android.support.v7.app.ActionBar
    public void addTab(ActionBar.Tab tab) {
        addTab(tab, this.mTabs.isEmpty());
    }

    @Override // android.support.v7.app.ActionBar
    public void addTab(ActionBar.Tab tab, boolean setSelected) {
        ensureTabsExist();
        this.mTabScrollView.addTab(tab, setSelected);
        configureTab(tab, this.mTabs.size());
        if (setSelected) {
            selectTab(tab);
        }
    }

    @Override // android.support.v7.app.ActionBar
    public void addTab(ActionBar.Tab tab, int position) {
        addTab(tab, position, this.mTabs.isEmpty());
    }

    @Override // android.support.v7.app.ActionBar
    public void addTab(ActionBar.Tab tab, int position, boolean setSelected) {
        ensureTabsExist();
        this.mTabScrollView.addTab(tab, position, setSelected);
        configureTab(tab, position);
        if (setSelected) {
            selectTab(tab);
        }
    }

    @Override // android.support.v7.app.ActionBar
    public void removeTab(ActionBar.Tab tab) {
        removeTabAt(tab.getPosition());
    }

    @Override // android.support.v7.app.ActionBar
    public void removeTabAt(int position) {
        if (this.mTabScrollView != null) {
            int selectedTabPosition = this.mSelectedTab != null ? this.mSelectedTab.getPosition() : this.mSavedTabPosition;
            this.mTabScrollView.removeTabAt(position);
            TabImpl removedTab = this.mTabs.remove(position);
            if (removedTab != null) {
                removedTab.setPosition(-1);
            }
            int newTabCount = this.mTabs.size();
            for (int i = position; i < newTabCount; i++) {
                this.mTabs.get(i).setPosition(i);
            }
            if (selectedTabPosition == position) {
                selectTab(this.mTabs.isEmpty() ? null : this.mTabs.get(Math.max(0, position - 1)));
            }
        }
    }

    @Override // android.support.v7.app.ActionBar
    public void removeAllTabs() {
        cleanupTabs();
    }

    @Override // android.support.v7.app.ActionBar
    public void selectTab(ActionBar.Tab tab) {
        if (getNavigationMode() != 2) {
            this.mSavedTabPosition = tab != null ? tab.getPosition() : -1;
            return;
        }
        FragmentTransaction trans = this.mActivity.getSupportFragmentManager().beginTransaction().disallowAddToBackStack();
        if (this.mSelectedTab == tab) {
            if (this.mSelectedTab != null) {
                this.mSelectedTab.getCallback().onTabReselected(this.mSelectedTab, trans);
                this.mTabScrollView.animateToTab(tab.getPosition());
            }
        } else {
            this.mTabScrollView.setTabSelected(tab != null ? tab.getPosition() : -1);
            if (this.mSelectedTab != null) {
                this.mSelectedTab.getCallback().onTabUnselected(this.mSelectedTab, trans);
            }
            this.mSelectedTab = (TabImpl) tab;
            if (this.mSelectedTab != null) {
                this.mSelectedTab.getCallback().onTabSelected(this.mSelectedTab, trans);
            }
        }
        if (!trans.isEmpty()) {
            trans.commit();
        }
    }

    @Override // android.support.v7.app.ActionBar
    public ActionBar.Tab getSelectedTab() {
        return this.mSelectedTab;
    }

    @Override // android.support.v7.app.ActionBar
    public ActionBar.Tab getTabAt(int index) {
        return this.mTabs.get(index);
    }

    @Override // android.support.v7.app.ActionBar
    public int getTabCount() {
        return this.mTabs.size();
    }

    @Override // android.support.v7.app.ActionBar
    public Context getThemedContext() {
        if (this.mThemedContext == null) {
            TypedValue outValue = new TypedValue();
            Resources.Theme currentTheme = this.mContext.getTheme();
            currentTheme.resolveAttribute(R.attr.actionBarWidgetTheme, outValue, true);
            int targetThemeRes = outValue.resourceId;
            if (targetThemeRes != 0) {
                this.mThemedContext = new ContextThemeWrapper(this.mContext, targetThemeRes);
            } else {
                this.mThemedContext = this.mContext;
            }
        }
        return this.mThemedContext;
    }

    @Override // android.support.v7.app.ActionBar
    public void setHomeAsUpIndicator(Drawable indicator) {
        this.mActionView.setHomeAsUpIndicator(indicator);
    }

    @Override // android.support.v7.app.ActionBar
    public void setHomeAsUpIndicator(int resId) {
        this.mActionView.setHomeAsUpIndicator(resId);
    }

    @Override // android.support.v7.app.ActionBar
    public int getHeight() {
        return this.mContainerView.getHeight();
    }

    @Override // android.support.v7.app.ActionBar
    public void show() {
        if (this.mHiddenByApp) {
            this.mHiddenByApp = false;
            updateVisibility(false);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void showForActionMode() {
        if (!this.mShowingForMode) {
            this.mShowingForMode = true;
            updateVisibility(false);
        }
    }

    @Override // android.support.v7.app.ActionBar
    public void hide() {
        if (!this.mHiddenByApp) {
            this.mHiddenByApp = true;
            updateVisibility(false);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void hideForActionMode() {
        if (this.mShowingForMode) {
            this.mShowingForMode = false;
            updateVisibility(false);
        }
    }

    @Override // android.support.v7.app.ActionBar
    public boolean isShowing() {
        return this.mNowShowing;
    }

    @Override // android.support.v7.app.ActionBar
    public void addOnMenuVisibilityListener(ActionBar.OnMenuVisibilityListener listener) {
        this.mMenuVisibilityListeners.add(listener);
    }

    @Override // android.support.v7.app.ActionBar
    public void removeOnMenuVisibilityListener(ActionBar.OnMenuVisibilityListener listener) {
        this.mMenuVisibilityListeners.remove(listener);
    }

    public ActionMode startActionMode(ActionMode.Callback callback) {
        if (this.mActionMode != null) {
            this.mActionMode.finish();
        }
        this.mContextView.killMode();
        ActionModeImpl mode = new ActionModeImpl(callback);
        if (mode.dispatchOnCreate()) {
            mode.invalidate();
            this.mContextView.initForMode(mode);
            animateToMode(true);
            if (this.mSplitView != null && this.mContextDisplayMode == 1 && this.mSplitView.getVisibility() != 0) {
                this.mSplitView.setVisibility(0);
            }
            this.mContextView.sendAccessibilityEvent(32);
            this.mActionMode = mode;
            return mode;
        }
        return null;
    }

    void animateToMode(boolean toActionMode) {
        if (toActionMode) {
            showForActionMode();
        } else {
            hideForActionMode();
        }
        this.mActionView.animateToVisibility(toActionMode ? 4 : 0);
        this.mContextView.animateToVisibility(toActionMode ? 0 : 8);
        if (this.mTabScrollView != null && !this.mActionView.hasEmbeddedTabs() && this.mActionView.isCollapsed()) {
            this.mTabScrollView.setVisibility(toActionMode ? 8 : 0);
        }
    }

    /* loaded from: classes.dex */
    public class TabImpl extends ActionBar.Tab {
        private ActionBar.TabListener mCallback;
        private CharSequence mContentDesc;
        private View mCustomView;
        private Drawable mIcon;
        private int mPosition = -1;
        private Object mTag;
        private CharSequence mText;

        public TabImpl() {
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public Object getTag() {
            return this.mTag;
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public ActionBar.Tab setTag(Object tag) {
            this.mTag = tag;
            return this;
        }

        public ActionBar.TabListener getCallback() {
            return this.mCallback;
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public ActionBar.Tab setTabListener(ActionBar.TabListener callback) {
            this.mCallback = callback;
            return this;
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public View getCustomView() {
            return this.mCustomView;
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public ActionBar.Tab setCustomView(View view) {
            this.mCustomView = view;
            if (this.mPosition >= 0) {
                ActionBarImplBase.this.mTabScrollView.updateTab(this.mPosition);
            }
            return this;
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public ActionBar.Tab setCustomView(int layoutResId) {
            return setCustomView(LayoutInflater.from(ActionBarImplBase.this.getThemedContext()).inflate(layoutResId, (ViewGroup) null));
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public Drawable getIcon() {
            return this.mIcon;
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public int getPosition() {
            return this.mPosition;
        }

        public void setPosition(int position) {
            this.mPosition = position;
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public CharSequence getText() {
            return this.mText;
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public ActionBar.Tab setIcon(Drawable icon) {
            this.mIcon = icon;
            if (this.mPosition >= 0) {
                ActionBarImplBase.this.mTabScrollView.updateTab(this.mPosition);
            }
            return this;
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public ActionBar.Tab setIcon(int resId) {
            return setIcon(ActionBarImplBase.this.mContext.getResources().getDrawable(resId));
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public ActionBar.Tab setText(CharSequence text) {
            this.mText = text;
            if (this.mPosition >= 0) {
                ActionBarImplBase.this.mTabScrollView.updateTab(this.mPosition);
            }
            return this;
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public ActionBar.Tab setText(int resId) {
            return setText(ActionBarImplBase.this.mContext.getResources().getText(resId));
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public void select() {
            ActionBarImplBase.this.selectTab(this);
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public ActionBar.Tab setContentDescription(int resId) {
            return setContentDescription(ActionBarImplBase.this.mContext.getResources().getText(resId));
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public ActionBar.Tab setContentDescription(CharSequence contentDesc) {
            this.mContentDesc = contentDesc;
            if (this.mPosition >= 0) {
                ActionBarImplBase.this.mTabScrollView.updateTab(this.mPosition);
            }
            return this;
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public CharSequence getContentDescription() {
            return this.mContentDesc;
        }
    }

    /* loaded from: classes.dex */
    class ActionModeImpl extends ActionMode implements MenuBuilder.Callback {
        private ActionMode.Callback mCallback;
        private WeakReference<View> mCustomView;
        private MenuBuilder mMenu;

        public ActionModeImpl(ActionMode.Callback callback) {
            this.mCallback = callback;
            this.mMenu = new MenuBuilder(ActionBarImplBase.this.getThemedContext()).setDefaultShowAsAction(1);
            this.mMenu.setCallback(this);
        }

        @Override // android.support.v7.view.ActionMode
        public MenuInflater getMenuInflater() {
            return new SupportMenuInflater(ActionBarImplBase.this.getThemedContext());
        }

        @Override // android.support.v7.view.ActionMode
        public Menu getMenu() {
            return this.mMenu;
        }

        @Override // android.support.v7.view.ActionMode
        public void finish() {
            if (ActionBarImplBase.this.mActionMode == this) {
                if (!ActionBarImplBase.checkShowingFlags(ActionBarImplBase.this.mHiddenByApp, ActionBarImplBase.this.mHiddenBySystem, false)) {
                    ActionBarImplBase.this.mDeferredDestroyActionMode = this;
                    ActionBarImplBase.this.mDeferredModeDestroyCallback = this.mCallback;
                } else {
                    this.mCallback.onDestroyActionMode(this);
                }
                this.mCallback = null;
                ActionBarImplBase.this.animateToMode(false);
                ActionBarImplBase.this.mContextView.closeMode();
                ActionBarImplBase.this.mActionView.sendAccessibilityEvent(32);
                ActionBarImplBase.this.mActionMode = null;
            }
        }

        @Override // android.support.v7.view.ActionMode
        public void invalidate() {
            this.mMenu.stopDispatchingItemsChanged();
            try {
                this.mCallback.onPrepareActionMode(this, this.mMenu);
            } finally {
                this.mMenu.startDispatchingItemsChanged();
            }
        }

        public boolean dispatchOnCreate() {
            this.mMenu.stopDispatchingItemsChanged();
            try {
                return this.mCallback.onCreateActionMode(this, this.mMenu);
            } finally {
                this.mMenu.startDispatchingItemsChanged();
            }
        }

        @Override // android.support.v7.view.ActionMode
        public void setCustomView(View view) {
            ActionBarImplBase.this.mContextView.setCustomView(view);
            this.mCustomView = new WeakReference<>(view);
        }

        @Override // android.support.v7.view.ActionMode
        public void setSubtitle(CharSequence subtitle) {
            ActionBarImplBase.this.mContextView.setSubtitle(subtitle);
        }

        @Override // android.support.v7.view.ActionMode
        public void setTitle(CharSequence title) {
            ActionBarImplBase.this.mContextView.setTitle(title);
        }

        @Override // android.support.v7.view.ActionMode
        public void setTitle(int resId) {
            setTitle(ActionBarImplBase.this.mContext.getResources().getString(resId));
        }

        @Override // android.support.v7.view.ActionMode
        public void setSubtitle(int resId) {
            setSubtitle(ActionBarImplBase.this.mContext.getResources().getString(resId));
        }

        @Override // android.support.v7.view.ActionMode
        public CharSequence getTitle() {
            return ActionBarImplBase.this.mContextView.getTitle();
        }

        @Override // android.support.v7.view.ActionMode
        public CharSequence getSubtitle() {
            return ActionBarImplBase.this.mContextView.getSubtitle();
        }

        @Override // android.support.v7.view.ActionMode
        public void setTitleOptionalHint(boolean titleOptional) {
            super.setTitleOptionalHint(titleOptional);
            ActionBarImplBase.this.mContextView.setTitleOptional(titleOptional);
        }

        @Override // android.support.v7.view.ActionMode
        public boolean isTitleOptional() {
            return ActionBarImplBase.this.mContextView.isTitleOptional();
        }

        @Override // android.support.v7.view.ActionMode
        public View getCustomView() {
            if (this.mCustomView != null) {
                return this.mCustomView.get();
            }
            return null;
        }

        @Override // android.support.v7.internal.view.menu.MenuBuilder.Callback
        public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
            if (this.mCallback != null) {
                return this.mCallback.onActionItemClicked(this, item);
            }
            return false;
        }

        @Override // android.support.v7.internal.view.menu.MenuBuilder.Callback
        public void onMenuModeChange(MenuBuilder menu) {
            if (this.mCallback != null) {
                invalidate();
                ActionBarImplBase.this.mContextView.showOverflowMenu();
            }
        }

        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        }

        public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
            if (this.mCallback == null) {
                return false;
            }
            if (!subMenu.hasVisibleItems()) {
            }
            return true;
        }

        public void onCloseSubMenu(SubMenuBuilder menu) {
        }

        public void onMenuModeChange(Menu menu) {
            if (this.mCallback != null) {
                invalidate();
                ActionBarImplBase.this.mContextView.showOverflowMenu();
            }
        }
    }

    private void ensureTabsExist() {
        if (this.mTabScrollView == null) {
            ScrollingTabContainerView tabScroller = new ScrollingTabContainerView(this.mContext);
            if (this.mHasEmbeddedTabs) {
                tabScroller.setVisibility(0);
                this.mActionView.setEmbeddedTabView(tabScroller);
            } else {
                if (getNavigationMode() == 2) {
                    tabScroller.setVisibility(0);
                } else {
                    tabScroller.setVisibility(8);
                }
                this.mContainerView.setTabContainer(tabScroller);
            }
            this.mTabScrollView = tabScroller;
        }
    }

    private void configureTab(ActionBar.Tab tab, int position) {
        TabImpl tabi = (TabImpl) tab;
        ActionBar.TabListener callback = tabi.getCallback();
        if (callback == null) {
            throw new IllegalStateException("Action Bar Tab must have a Callback");
        }
        tabi.setPosition(position);
        this.mTabs.add(position, tabi);
        int count = this.mTabs.size();
        for (int i = position + 1; i < count; i++) {
            this.mTabs.get(i).setPosition(i);
        }
    }

    private void cleanupTabs() {
        if (this.mSelectedTab != null) {
            selectTab(null);
        }
        this.mTabs.clear();
        if (this.mTabScrollView != null) {
            this.mTabScrollView.removeAllTabs();
        }
        this.mSavedTabPosition = -1;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean checkShowingFlags(boolean hiddenByApp, boolean hiddenBySystem, boolean showingForMode) {
        if (showingForMode) {
            return true;
        }
        return (hiddenByApp || hiddenBySystem) ? false : true;
    }

    private void updateVisibility(boolean fromSystem) {
        boolean shown = checkShowingFlags(this.mHiddenByApp, this.mHiddenBySystem, this.mShowingForMode);
        if (shown) {
            if (!this.mNowShowing) {
                this.mNowShowing = true;
                doShow(fromSystem);
            }
        } else if (this.mNowShowing) {
            this.mNowShowing = false;
            doHide(fromSystem);
        }
    }

    public void setShowHideAnimationEnabled(boolean enabled) {
        this.mShowHideAnimationEnabled = enabled;
        if (!enabled) {
            this.mTopVisibilityView.clearAnimation();
            if (this.mSplitView != null) {
                this.mSplitView.clearAnimation();
            }
        }
    }

    public void doShow(boolean fromSystem) {
        this.mTopVisibilityView.clearAnimation();
        if (this.mTopVisibilityView.getVisibility() != 0) {
            boolean animate = isShowHideAnimationEnabled() || fromSystem;
            if (animate) {
                Animation anim = AnimationUtils.loadAnimation(this.mContext, R.anim.abc_slide_in_top);
                this.mTopVisibilityView.startAnimation(anim);
            }
            this.mTopVisibilityView.setVisibility(0);
            if (this.mSplitView != null && this.mSplitView.getVisibility() != 0) {
                if (animate) {
                    Animation anim2 = AnimationUtils.loadAnimation(this.mContext, R.anim.abc_slide_in_bottom);
                    this.mSplitView.startAnimation(anim2);
                }
                this.mSplitView.setVisibility(0);
            }
        }
    }

    public void doHide(boolean fromSystem) {
        this.mTopVisibilityView.clearAnimation();
        if (this.mTopVisibilityView.getVisibility() != 8) {
            boolean animate = isShowHideAnimationEnabled() || fromSystem;
            if (animate) {
                Animation anim = AnimationUtils.loadAnimation(this.mContext, R.anim.abc_slide_out_top);
                this.mTopVisibilityView.startAnimation(anim);
            }
            this.mTopVisibilityView.setVisibility(8);
            if (this.mSplitView != null && this.mSplitView.getVisibility() != 8) {
                if (animate) {
                    Animation anim2 = AnimationUtils.loadAnimation(this.mContext, R.anim.abc_slide_out_bottom);
                    this.mSplitView.startAnimation(anim2);
                }
                this.mSplitView.setVisibility(8);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isShowHideAnimationEnabled() {
        return this.mShowHideAnimationEnabled;
    }
}
