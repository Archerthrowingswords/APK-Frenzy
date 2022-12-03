package android.support.v7.app;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.NavUtils;
import android.support.v7.appcompat.R;
import android.support.v7.internal.view.SupportMenuInflater;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public abstract class ActionBarActivityDelegate {
    static final String METADATA_UI_OPTIONS = "android.support.UI_OPTIONS";
    private static final String TAG = "ActionBarActivityDelegate";
    static final String UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW = "splitActionBarWhenNarrow";
    private ActionBar mActionBar;
    final ActionBarActivity mActivity;
    private boolean mEnableDefaultActionBarUp;
    boolean mHasActionBar;
    private MenuInflater mMenuInflater;
    boolean mOverlayActionBar;

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract void addContentView(View view, ViewGroup.LayoutParams layoutParams);

    abstract ActionBar createSupportActionBar();

    abstract int getHomeAsUpIndicatorAttrId();

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract boolean onBackPressed();

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract void onConfigurationChanged(Configuration configuration);

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract void onContentChanged();

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract boolean onCreatePanelMenu(int i, Menu menu);

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract View onCreatePanelView(int i);

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract boolean onMenuItemSelected(int i, MenuItem menuItem);

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract void onPostResume();

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract boolean onPreparePanel(int i, View view, Menu menu);

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract void onStop();

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract void onTitleChanged(CharSequence charSequence);

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract void setContentView(int i);

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract void setContentView(View view);

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract void setContentView(View view, ViewGroup.LayoutParams layoutParams);

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract void setSupportProgress(int i);

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract void setSupportProgressBarIndeterminate(boolean z);

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract void setSupportProgressBarIndeterminateVisibility(boolean z);

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract void setSupportProgressBarVisibility(boolean z);

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract ActionMode startSupportActionMode(ActionMode.Callback callback);

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract void supportInvalidateOptionsMenu();

    /* JADX INFO: Access modifiers changed from: package-private */
    public abstract boolean supportRequestWindowFeature(int i);

    /* JADX INFO: Access modifiers changed from: package-private */
    public static ActionBarActivityDelegate createDelegate(ActionBarActivity activity) {
        if (Build.VERSION.SDK_INT >= 20) {
            return new ActionBarActivityDelegateApi20(activity);
        }
        if (Build.VERSION.SDK_INT >= 18) {
            return new ActionBarActivityDelegateJBMR2(activity);
        }
        if (Build.VERSION.SDK_INT >= 16) {
            return new ActionBarActivityDelegateJB(activity);
        }
        if (Build.VERSION.SDK_INT >= 14) {
            return new ActionBarActivityDelegateICS(activity);
        }
        if (Build.VERSION.SDK_INT >= 11) {
            return new ActionBarActivityDelegateHC(activity);
        }
        return new ActionBarActivityDelegateBase(activity);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public ActionBarActivityDelegate(ActionBarActivity activity) {
        this.mActivity = activity;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final ActionBar getSupportActionBar() {
        if (this.mHasActionBar || this.mOverlayActionBar) {
            if (this.mActionBar == null) {
                this.mActionBar = createSupportActionBar();
                if (this.mEnableDefaultActionBarUp) {
                    this.mActionBar.setDisplayHomeAsUpEnabled(true);
                }
            }
        } else {
            this.mActionBar = null;
        }
        return this.mActionBar;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public MenuInflater getMenuInflater() {
        if (this.mMenuInflater == null) {
            this.mMenuInflater = new SupportMenuInflater(getActionBarThemedContext());
        }
        return this.mMenuInflater;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onCreate(Bundle savedInstanceState) {
        TypedArray a = this.mActivity.obtainStyledAttributes(R.styleable.ActionBarWindow);
        if (!a.hasValue(0)) {
            a.recycle();
            throw new IllegalStateException("You need to use a Theme.AppCompat theme (or descendant) with this activity.");
        }
        this.mHasActionBar = a.getBoolean(0, false);
        this.mOverlayActionBar = a.getBoolean(1, false);
        a.recycle();
        if (NavUtils.getParentActivityName(this.mActivity) != null) {
            if (this.mActionBar == null) {
                this.mEnableDefaultActionBarUp = true;
            } else {
                this.mActionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean onPrepareOptionsPanel(View view, Menu menu) {
        return Build.VERSION.SDK_INT < 16 ? this.mActivity.onPrepareOptionsMenu(menu) : this.mActivity.superOnPrepareOptionsPanel(view, menu);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public final ActionBarDrawerToggle.Delegate getDrawerToggleDelegate() {
        return new ActionBarDrawableToggleImpl();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final String getUiOptionsFromMetadata() {
        try {
            PackageManager pm = this.mActivity.getPackageManager();
            ActivityInfo info = pm.getActivityInfo(this.mActivity.getComponentName(), 128);
            if (info.metaData == null) {
                return null;
            }
            String uiOptions = info.metaData.getString(METADATA_UI_OPTIONS);
            return uiOptions;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "getUiOptionsFromMetadata: Activity '" + this.mActivity.getClass().getSimpleName() + "' not in manifest");
            return null;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public final Context getActionBarThemedContext() {
        Context context = this.mActivity;
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            Context context2 = ab.getThemedContext();
            return context2;
        }
        return context;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class ActionBarDrawableToggleImpl implements ActionBarDrawerToggle.Delegate {
        private ActionBarDrawableToggleImpl() {
        }

        @Override // android.support.v4.app.ActionBarDrawerToggle.Delegate
        public Drawable getThemeUpIndicator() {
            TypedArray a = ActionBarActivityDelegate.this.mActivity.obtainStyledAttributes(new int[]{ActionBarActivityDelegate.this.getHomeAsUpIndicatorAttrId()});
            Drawable result = a.getDrawable(0);
            a.recycle();
            return result;
        }

        @Override // android.support.v4.app.ActionBarDrawerToggle.Delegate
        public void setActionBarUpIndicator(Drawable upDrawable, int contentDescRes) {
            ActionBar ab = ActionBarActivityDelegate.this.getSupportActionBar();
            if (ab != null) {
                ab.setHomeAsUpIndicator(upDrawable);
                ab.setHomeActionContentDescription(contentDescRes);
            }
        }

        @Override // android.support.v4.app.ActionBarDrawerToggle.Delegate
        public void setActionBarDescription(int contentDescRes) {
            ActionBar ab = ActionBarActivityDelegate.this.getSupportActionBar();
            if (ab != null) {
                ab.setHomeActionContentDescription(contentDescRes);
            }
        }
    }
}
