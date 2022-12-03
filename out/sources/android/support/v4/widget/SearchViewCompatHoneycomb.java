package android.support.v4.widget;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.view.View;
import android.widget.SearchView;
/* loaded from: classes.dex */
class SearchViewCompatHoneycomb {

    /* loaded from: classes.dex */
    interface OnCloseListenerCompatBridge {
        boolean onClose();
    }

    /* loaded from: classes.dex */
    interface OnQueryTextListenerCompatBridge {
        boolean onQueryTextChange(String str);

        boolean onQueryTextSubmit(String str);
    }

    SearchViewCompatHoneycomb() {
    }

    public static View newSearchView(Context context) {
        return new SearchView(context);
    }

    public static void setSearchableInfo(View searchView, ComponentName searchableComponent) {
        SearchView sv = (SearchView) searchView;
        SearchManager searchManager = (SearchManager) sv.getContext().getSystemService("search");
        sv.setSearchableInfo(searchManager.getSearchableInfo(searchableComponent));
    }

    public static Object newOnQueryTextListener(final OnQueryTextListenerCompatBridge listener) {
        return new SearchView.OnQueryTextListener() { // from class: android.support.v4.widget.SearchViewCompatHoneycomb.1
            @Override // android.widget.SearchView.OnQueryTextListener
            public boolean onQueryTextSubmit(String query) {
                return OnQueryTextListenerCompatBridge.this.onQueryTextSubmit(query);
            }

            @Override // android.widget.SearchView.OnQueryTextListener
            public boolean onQueryTextChange(String newText) {
                return OnQueryTextListenerCompatBridge.this.onQueryTextChange(newText);
            }
        };
    }

    public static void setOnQueryTextListener(Object searchView, Object listener) {
        ((SearchView) searchView).setOnQueryTextListener((SearchView.OnQueryTextListener) listener);
    }

    public static Object newOnCloseListener(final OnCloseListenerCompatBridge listener) {
        return new SearchView.OnCloseListener() { // from class: android.support.v4.widget.SearchViewCompatHoneycomb.2
            @Override // android.widget.SearchView.OnCloseListener
            public boolean onClose() {
                return OnCloseListenerCompatBridge.this.onClose();
            }
        };
    }

    public static void setOnCloseListener(Object searchView, Object listener) {
        ((SearchView) searchView).setOnCloseListener((SearchView.OnCloseListener) listener);
    }

    public static CharSequence getQuery(View searchView) {
        return ((SearchView) searchView).getQuery();
    }

    public static void setQuery(View searchView, CharSequence query, boolean submit) {
        ((SearchView) searchView).setQuery(query, submit);
    }

    public static void setQueryHint(View searchView, CharSequence hint) {
        ((SearchView) searchView).setQueryHint(hint);
    }

    public static void setIconified(View searchView, boolean iconify) {
        ((SearchView) searchView).setIconified(iconify);
    }

    public static boolean isIconified(View searchView) {
        return ((SearchView) searchView).isIconified();
    }

    public static void setSubmitButtonEnabled(View searchView, boolean enabled) {
        ((SearchView) searchView).setSubmitButtonEnabled(enabled);
    }

    public static boolean isSubmitButtonEnabled(View searchView) {
        return ((SearchView) searchView).isSubmitButtonEnabled();
    }

    public static void setQueryRefinementEnabled(View searchView, boolean enable) {
        ((SearchView) searchView).setQueryRefinementEnabled(enable);
    }

    public static boolean isQueryRefinementEnabled(View searchView) {
        return ((SearchView) searchView).isQueryRefinementEnabled();
    }

    public static void setMaxWidth(View searchView, int maxpixels) {
        ((SearchView) searchView).setMaxWidth(maxpixels);
    }
}
