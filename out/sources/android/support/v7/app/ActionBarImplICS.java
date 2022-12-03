package android.support.v7.app;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SpinnerAdapter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
/* loaded from: classes.dex */
class ActionBarImplICS extends ActionBar {
    final android.app.ActionBar mActionBar;
    FragmentTransaction mActiveTransaction;
    final Activity mActivity;
    private ArrayList<WeakReference<OnMenuVisibilityListenerWrapper>> mAddedMenuVisWrappers;
    final ActionBar.Callback mCallback;
    private ImageView mHomeActionView;

    public ActionBarImplICS(Activity activity, ActionBar.Callback callback) {
        this(activity, callback, true);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ActionBarImplICS(Activity activity, ActionBar.Callback callback, boolean checkHomeAsUpOption) {
        this.mAddedMenuVisWrappers = new ArrayList<>();
        this.mActivity = activity;
        this.mCallback = callback;
        this.mActionBar = activity.getActionBar();
        if (checkHomeAsUpOption && (getDisplayOptions() & 4) != 0) {
            setHomeButtonEnabled(true);
        }
    }

    private OnMenuVisibilityListenerWrapper findAndRemoveMenuVisWrapper(ActionBar.OnMenuVisibilityListener compatListener) {
        int i = 0;
        while (i < this.mAddedMenuVisWrappers.size()) {
            OnMenuVisibilityListenerWrapper wrapper = this.mAddedMenuVisWrappers.get(i).get();
            if (wrapper == null) {
                this.mAddedMenuVisWrappers.remove(i);
                i--;
            } else if (wrapper.mWrappedListener == compatListener) {
                this.mAddedMenuVisWrappers.remove(i);
                return wrapper;
            }
            i++;
        }
        return null;
    }

    @Override // android.support.v7.app.ActionBar
    public void setCustomView(View view) {
        this.mActionBar.setCustomView(view);
    }

    @Override // android.support.v7.app.ActionBar
    public void setCustomView(View view, ActionBar.LayoutParams layoutParams) {
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(layoutParams);
        lp.gravity = layoutParams.gravity;
        this.mActionBar.setCustomView(view, lp);
    }

    @Override // android.support.v7.app.ActionBar
    public void setCustomView(int resId) {
        this.mActionBar.setCustomView(resId);
    }

    @Override // android.support.v7.app.ActionBar
    public void setIcon(int resId) {
        this.mActionBar.setIcon(resId);
    }

    @Override // android.support.v7.app.ActionBar
    public void setIcon(Drawable icon) {
        this.mActionBar.setIcon(icon);
    }

    @Override // android.support.v7.app.ActionBar
    public void setLogo(int resId) {
        this.mActionBar.setLogo(resId);
    }

    @Override // android.support.v7.app.ActionBar
    public void setLogo(Drawable logo) {
        this.mActionBar.setLogo(logo);
    }

    @Override // android.support.v7.app.ActionBar
    public void setListNavigationCallbacks(SpinnerAdapter adapter, ActionBar.OnNavigationListener callback) {
        this.mActionBar.setListNavigationCallbacks(adapter, callback != null ? new OnNavigationListenerWrapper(callback) : null);
    }

    @Override // android.support.v7.app.ActionBar
    public void setSelectedNavigationItem(int position) {
        this.mActionBar.setSelectedNavigationItem(position);
    }

    @Override // android.support.v7.app.ActionBar
    public int getSelectedNavigationIndex() {
        return this.mActionBar.getSelectedNavigationIndex();
    }

    @Override // android.support.v7.app.ActionBar
    public int getNavigationItemCount() {
        return this.mActionBar.getNavigationItemCount();
    }

    @Override // android.support.v7.app.ActionBar
    public void setTitle(CharSequence title) {
        this.mActionBar.setTitle(title);
    }

    @Override // android.support.v7.app.ActionBar
    public void setTitle(int resId) {
        this.mActionBar.setTitle(resId);
    }

    @Override // android.support.v7.app.ActionBar
    public void setSubtitle(CharSequence subtitle) {
        this.mActionBar.setSubtitle(subtitle);
    }

    @Override // android.support.v7.app.ActionBar
    public void setSubtitle(int resId) {
        this.mActionBar.setSubtitle(resId);
    }

    @Override // android.support.v7.app.ActionBar
    public void setDisplayOptions(int options) {
        this.mActionBar.setDisplayOptions(options);
    }

    @Override // android.support.v7.app.ActionBar
    public void setDisplayOptions(int options, int mask) {
        this.mActionBar.setDisplayOptions(options, mask);
    }

    @Override // android.support.v7.app.ActionBar
    public void setDisplayUseLogoEnabled(boolean useLogo) {
        this.mActionBar.setDisplayUseLogoEnabled(useLogo);
    }

    @Override // android.support.v7.app.ActionBar
    public void setDisplayShowHomeEnabled(boolean showHome) {
        this.mActionBar.setDisplayShowHomeEnabled(showHome);
    }

    @Override // android.support.v7.app.ActionBar
    public void setDisplayHomeAsUpEnabled(boolean showHomeAsUp) {
        this.mActionBar.setDisplayHomeAsUpEnabled(showHomeAsUp);
    }

    @Override // android.support.v7.app.ActionBar
    public void setDisplayShowTitleEnabled(boolean showTitle) {
        this.mActionBar.setDisplayShowTitleEnabled(showTitle);
    }

    @Override // android.support.v7.app.ActionBar
    public void setDisplayShowCustomEnabled(boolean showCustom) {
        this.mActionBar.setDisplayShowCustomEnabled(showCustom);
    }

    @Override // android.support.v7.app.ActionBar
    public void setBackgroundDrawable(Drawable d) {
        this.mActionBar.setBackgroundDrawable(d);
    }

    @Override // android.support.v7.app.ActionBar
    public void setStackedBackgroundDrawable(Drawable d) {
        this.mActionBar.setStackedBackgroundDrawable(d);
    }

    @Override // android.support.v7.app.ActionBar
    public void setSplitBackgroundDrawable(Drawable d) {
        this.mActionBar.setSplitBackgroundDrawable(d);
    }

    @Override // android.support.v7.app.ActionBar
    public View getCustomView() {
        return this.mActionBar.getCustomView();
    }

    @Override // android.support.v7.app.ActionBar
    public CharSequence getTitle() {
        return this.mActionBar.getTitle();
    }

    @Override // android.support.v7.app.ActionBar
    public CharSequence getSubtitle() {
        return this.mActionBar.getSubtitle();
    }

    @Override // android.support.v7.app.ActionBar
    public int getNavigationMode() {
        return this.mActionBar.getNavigationMode();
    }

    @Override // android.support.v7.app.ActionBar
    public void setNavigationMode(int mode) {
        this.mActionBar.setNavigationMode(mode);
    }

    @Override // android.support.v7.app.ActionBar
    public int getDisplayOptions() {
        return this.mActionBar.getDisplayOptions();
    }

    @Override // android.support.v7.app.ActionBar
    public ActionBar.Tab newTab() {
        ActionBar.Tab realTab = this.mActionBar.newTab();
        TabWrapper result = new TabWrapper(realTab);
        realTab.setTag(result);
        return result;
    }

    @Override // android.support.v7.app.ActionBar
    public void addTab(ActionBar.Tab tab) {
        this.mActionBar.addTab(((TabWrapper) tab).mWrappedTab);
    }

    @Override // android.support.v7.app.ActionBar
    public void addTab(ActionBar.Tab tab, boolean setSelected) {
        this.mActionBar.addTab(((TabWrapper) tab).mWrappedTab, setSelected);
    }

    @Override // android.support.v7.app.ActionBar
    public void addTab(ActionBar.Tab tab, int position) {
        this.mActionBar.addTab(((TabWrapper) tab).mWrappedTab, position);
    }

    @Override // android.support.v7.app.ActionBar
    public void addTab(ActionBar.Tab tab, int position, boolean setSelected) {
        this.mActionBar.addTab(((TabWrapper) tab).mWrappedTab, position, setSelected);
    }

    @Override // android.support.v7.app.ActionBar
    public void removeTab(ActionBar.Tab tab) {
        this.mActionBar.removeTab(((TabWrapper) tab).mWrappedTab);
    }

    @Override // android.support.v7.app.ActionBar
    public void removeTabAt(int position) {
        this.mActionBar.removeTabAt(position);
    }

    @Override // android.support.v7.app.ActionBar
    public void removeAllTabs() {
        this.mActionBar.removeAllTabs();
    }

    @Override // android.support.v7.app.ActionBar
    public void selectTab(ActionBar.Tab tab) {
        this.mActionBar.selectTab(((TabWrapper) tab).mWrappedTab);
    }

    @Override // android.support.v7.app.ActionBar
    public ActionBar.Tab getSelectedTab() {
        return (ActionBar.Tab) this.mActionBar.getSelectedTab().getTag();
    }

    @Override // android.support.v7.app.ActionBar
    public ActionBar.Tab getTabAt(int index) {
        return (ActionBar.Tab) this.mActionBar.getTabAt(index).getTag();
    }

    @Override // android.support.v7.app.ActionBar
    public int getTabCount() {
        return this.mActionBar.getTabCount();
    }

    @Override // android.support.v7.app.ActionBar
    public Context getThemedContext() {
        return this.mActionBar.getThemedContext();
    }

    @Override // android.support.v7.app.ActionBar
    public void setHomeAsUpIndicator(Drawable indicator) {
        ImageView homeActionView = getHomeActionView();
        if (homeActionView != null) {
            if (indicator == null) {
                indicator = getThemeDefaultUpIndicator();
            }
            homeActionView.setImageDrawable(indicator);
        }
    }

    @Override // android.support.v7.app.ActionBar
    public void setHomeAsUpIndicator(int resId) {
        ImageView homeActionView = getHomeActionView();
        if (homeActionView != null) {
            if (resId != 0) {
                homeActionView.setImageResource(resId);
            } else {
                homeActionView.setImageDrawable(getThemeDefaultUpIndicator());
            }
        }
    }

    @Override // android.support.v7.app.ActionBar
    public int getHeight() {
        return this.mActionBar.getHeight();
    }

    @Override // android.support.v7.app.ActionBar
    public void show() {
        this.mActionBar.show();
    }

    @Override // android.support.v7.app.ActionBar
    public void hide() {
        this.mActionBar.hide();
    }

    @Override // android.support.v7.app.ActionBar
    public boolean isShowing() {
        return this.mActionBar.isShowing();
    }

    @Override // android.support.v7.app.ActionBar
    public void addOnMenuVisibilityListener(ActionBar.OnMenuVisibilityListener listener) {
        if (listener != null) {
            OnMenuVisibilityListenerWrapper w = new OnMenuVisibilityListenerWrapper(listener);
            this.mAddedMenuVisWrappers.add(new WeakReference<>(w));
            this.mActionBar.addOnMenuVisibilityListener(w);
        }
    }

    @Override // android.support.v7.app.ActionBar
    public void removeOnMenuVisibilityListener(ActionBar.OnMenuVisibilityListener listener) {
        OnMenuVisibilityListenerWrapper l = findAndRemoveMenuVisWrapper(listener);
        this.mActionBar.removeOnMenuVisibilityListener(l);
    }

    @Override // android.support.v7.app.ActionBar
    public void setHomeButtonEnabled(boolean enabled) {
        this.mActionBar.setHomeButtonEnabled(enabled);
    }

    FragmentTransaction getActiveTransaction() {
        if (this.mActiveTransaction == null) {
            this.mActiveTransaction = this.mCallback.getSupportFragmentManager().beginTransaction().disallowAddToBackStack();
        }
        return this.mActiveTransaction;
    }

    void commitActiveTransaction() {
        if (this.mActiveTransaction != null && !this.mActiveTransaction.isEmpty()) {
            this.mActiveTransaction.commit();
        }
        this.mActiveTransaction = null;
    }

    ImageView getHomeActionView() {
        if (this.mHomeActionView == null) {
            View home = this.mActivity.findViewById(16908332);
            if (home == null) {
                return null;
            }
            ViewGroup parent = (ViewGroup) home.getParent();
            int childCount = parent.getChildCount();
            if (childCount != 2) {
                return null;
            }
            View first = parent.getChildAt(0);
            View second = parent.getChildAt(1);
            View up = first.getId() == 16908332 ? second : first;
            if (up instanceof ImageView) {
                this.mHomeActionView = (ImageView) up;
            }
        }
        return this.mHomeActionView;
    }

    Drawable getThemeDefaultUpIndicator() {
        TypedArray a = this.mActivity.obtainStyledAttributes(new int[]{16843531});
        Drawable result = a.getDrawable(0);
        a.recycle();
        return result;
    }

    /* loaded from: classes.dex */
    static class OnNavigationListenerWrapper implements ActionBar.OnNavigationListener {
        private final ActionBar.OnNavigationListener mWrappedListener;

        public OnNavigationListenerWrapper(ActionBar.OnNavigationListener l) {
            this.mWrappedListener = l;
        }

        @Override // android.app.ActionBar.OnNavigationListener
        public boolean onNavigationItemSelected(int itemPosition, long itemId) {
            return this.mWrappedListener.onNavigationItemSelected(itemPosition, itemId);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class OnMenuVisibilityListenerWrapper implements ActionBar.OnMenuVisibilityListener {
        final ActionBar.OnMenuVisibilityListener mWrappedListener;

        public OnMenuVisibilityListenerWrapper(ActionBar.OnMenuVisibilityListener l) {
            this.mWrappedListener = l;
        }

        @Override // android.app.ActionBar.OnMenuVisibilityListener
        public void onMenuVisibilityChanged(boolean isVisible) {
            this.mWrappedListener.onMenuVisibilityChanged(isVisible);
        }
    }

    /* loaded from: classes.dex */
    class TabWrapper extends ActionBar.Tab implements ActionBar.TabListener {
        private CharSequence mContentDescription;
        private ActionBar.TabListener mTabListener;
        private Object mTag;
        final ActionBar.Tab mWrappedTab;

        public TabWrapper(ActionBar.Tab tab) {
            this.mWrappedTab = tab;
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public int getPosition() {
            return this.mWrappedTab.getPosition();
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public Drawable getIcon() {
            return this.mWrappedTab.getIcon();
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public CharSequence getText() {
            return this.mWrappedTab.getText();
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public ActionBar.Tab setIcon(Drawable icon) {
            this.mWrappedTab.setIcon(icon);
            return this;
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public ActionBar.Tab setIcon(int resId) {
            this.mWrappedTab.setIcon(resId);
            return this;
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public ActionBar.Tab setText(CharSequence text) {
            this.mWrappedTab.setText(text);
            return this;
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public ActionBar.Tab setText(int resId) {
            this.mWrappedTab.setText(resId);
            return this;
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public ActionBar.Tab setCustomView(View view) {
            this.mWrappedTab.setCustomView(view);
            return this;
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public ActionBar.Tab setCustomView(int layoutResId) {
            this.mWrappedTab.setCustomView(layoutResId);
            return this;
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public View getCustomView() {
            return this.mWrappedTab.getCustomView();
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public ActionBar.Tab setTag(Object obj) {
            this.mTag = obj;
            return this;
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public Object getTag() {
            return this.mTag;
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public ActionBar.Tab setTabListener(ActionBar.TabListener listener) {
            this.mTabListener = listener;
            this.mWrappedTab.setTabListener(listener != null ? this : null);
            return this;
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public void select() {
            this.mWrappedTab.select();
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public ActionBar.Tab setContentDescription(int resId) {
            this.mContentDescription = ActionBarImplICS.this.mActivity.getText(resId);
            return this;
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public ActionBar.Tab setContentDescription(CharSequence contentDesc) {
            this.mContentDescription = contentDesc;
            return this;
        }

        @Override // android.support.v7.app.ActionBar.Tab
        public CharSequence getContentDescription() {
            return this.mContentDescription;
        }

        @Override // android.app.ActionBar.TabListener
        public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
            this.mTabListener.onTabSelected(this, ft != null ? ActionBarImplICS.this.getActiveTransaction() : null);
            ActionBarImplICS.this.commitActiveTransaction();
        }

        @Override // android.app.ActionBar.TabListener
        public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
            this.mTabListener.onTabUnselected(this, ft != null ? ActionBarImplICS.this.getActiveTransaction() : null);
        }

        @Override // android.app.ActionBar.TabListener
        public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
            this.mTabListener.onTabReselected(this, ft != null ? ActionBarImplICS.this.getActiveTransaction() : null);
            ActionBarImplICS.this.commitActiveTransaction();
        }
    }
}
